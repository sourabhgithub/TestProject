public List<UserRoleDto> insertUserRoles(String userId, Long dealerId, List<UserRoleDto> insertRoleDtos, boolean isNotificationRequired) {
    User user = getUserFromDB(userId);
    if (Objects.isNull(user)) {
        throw new BadRequestException(CommonUtils.getTranslation(INVALID_USER_IDENTIFIER_ERROR, cu.getLocales(null), CommonUtils.getStringMap(USER_ID, userId)));
    }
    
    Lock lock = stripedLocks.get(userId);
    lock.lock();
    log.info("Entering insertUserRole(user: {}, roles:{})", userId, insertRoleDtos);
    try {
        List<UserRole> insertRoles = new ArrayList<>();
        List<UserRole> customerUserRoles = new ArrayList<>();
        Set<Long> customersWithNoUserRole = new HashSet<>();
        Set<Long> customersWithNewFirstRole = new HashSet<>();
        Set<Long> checkedCustomerId = new HashSet<>();

        List<UserRole> currentRoles = user.getUserRoles();
        Set<String> dealerCustomerRoleStringSetForCurrent = currentRoles.stream().filter(UserRole::isActive).map(x->
            x.getRoleName() + (Objects.nonNull(x.getDealer()) ? x.getDealer().getId() : null) + 
            (Objects.nonNull(x.getCustomer()) ? x.getCustomer().getId() : null)
        ).collect(Collectors.toSet());
        
        Set<String> dealerCustomerRoleStringSetForInsert = new HashSet<>();
        boolean hasNonAnonymousCustomerUserRoleForInsert = false;
        List<UserRole> customerRoles = new ArrayList<>();

        for (UserRoleDto roleDto : insertRoleDtos) {
            // Fetch the dealerId from each roleDto instead of using the global dealerId
            Long roleDealerId = roleDto.getDealerId();
            Dealer dealer = null;
            if (Objects.nonNull(roleDealerId) && !roleDealerId.equals(0L)) {
                dealer = dealerService.getDealerFromDB(roleDealerId);
            }

            // Build the dealerCustomerRoleValue based on roleDealerId instead of a global dealerId
            String dealerCustomerRoleValue = roleDto.getRoleName() + "-" + roleDealerId + "-" + roleDto.getCustomerId();
            String customerRoleValue = "CustomerUser" + "-" + roleDealerId + "-" + roleDto.getCustomerId();
            String customerAdminRoleValue = "CustomerAdminUser" + "-" + roleDealerId + "-" + roleDto.getCustomerId();

            // Remove duplicated roles, existing roles
            if (dealerCustomerRoleValue.equals(customerAdminRoleValue) && dealerCustomerRoleStringSetForInsert.contains(customerRoleValue)) {
                dealerCustomerRoleStringSetForInsert.remove(customerRoleValue);
            }
            if (dealerCustomerRoleValue.equals(customerRoleValue) && dealerCustomerRoleStringSetForInsert.contains(customerAdminRoleValue)) {
                continue;
            }
            if (dealerCustomerRoleStringSetForInsert.contains(dealerCustomerRoleValue) || dealerCustomerRoleStringSetForCurrent.contains(dealerCustomerRoleValue)) {
                continue;
            }

            dealerCustomerRoleStringSetForInsert.add(dealerCustomerRoleValue);

            Long customerId = roleDto.getCustomerId();
            hasNonAnonymousCustomerUserRoleForInsert = hasNonAnonymousCustomerUserRoleForInsert || !(CUSTOMER_USER.equals(roleDto.getRoleName()) && Objects.isNull(customerId));

            UserRole role = Mask.mapAndMask(roleDto, UserRole.class, UserRoleDto.MIN);
            role.setCreatedDate(ZonedDateTime.now());
            role.setModifiedDate(ZonedDateTime.now());
            role.setUser(user);
            role.setId(null);

            checkRestrictedJobsiteIds(roleDto, roleDto.getCustomerId());

            // Set dealer and customer for the role
            if (Objects.nonNull(roleDealerId)) {
                role.setDealer(dealer);  // Assign dealer for each role based on roleDto dealerId
                if (Objects.nonNull(customerId)) {
                    Customer customer = getCustomerFromDB(customerId);
                    if (!checkedCustomerId.contains(customerId) && userRoleRepository.getActiveUserRoleCountByCustomerId(customerId) == 0) {
                        checkedCustomerId.add(customerId);
                        customersWithNoUserRole.add(customerId);
                    }
                    if (!Boolean.TRUE.equals(customer.getActive())) {
                        throw new AccessDeniedException(CommonUtils.getTranslation(NO_PERMISSION_MSG, cu.getLocales(null)));
                    }
                    if (!roleDealerId.equals(customer.getDealer().getId())) {
                        throw new BadRequestException(CommonUtils.getTranslation("svc-user-badrequest-customer-not-associated-with-dealer", cu.getLocales(null)));
                    }
                    role.setCustomer(customer);
                    customerUserRoles.add(role);
                }
            } else {
                role.setDealer(null);
                role.setCustomer(null);
            }
            insertRoles.add(role);
            if (role.getRoleName().startsWith(CUSTOMER)) {
                customerRoles.add(role);
            }
        }

        try {
            // Save roles and update restricted flag
            insertRoles = roleRepository.saveAll(insertRoles);
            insertRoles.forEach(r -> {
                Long customerId = r.getCustomer() == null ? null : r.getCustomer().getId();
                if (CUSTOMER_ADMIN.equalsIgnoreCase(r.getRoleName())) {
                    addNotificationSubscriptionForCustomerAdmin(r.getDealer().getId(), customerId, user);
                }
                if (customersWithNoUserRole.contains(customerId)) {
                    customersWithNewFirstRole.add(customerId);
                }
            });

            // Send notification and trigger datapull
            if (!customerUserRoles.isEmpty() && isNotificationRequired) {
                asyncExecutor.execute(() -> sendDCNAssociationEmail(user, null));
                customerUserRoles.forEach(r -> {
                    Long customerId = r.getCustomer().getId();
                    if (customersWithNewFirstRole.contains(customerId)) {
                        DataPullRestClient.pullByDealerAndCustomer(getIntraServiceToken(), r.getDealer().getId(), customerId);
                    }
                });
                customerRoles.forEach(x -> {
                    Long dealerIdParam = x.getDealer().getId();
                    Long customerIdParam = x.getCustomer().getId();
                    AsyncUser asyncUser = new AsyncUser(cu);
                    asyncExecutor.execute(() -> sendCustomerRoleNotification(dealerIdParam, customerIdParam, x, user, asyncUser, true));
                });
            }
            log.info("Leaving insertUserRoles()");
            return insertRoles.stream().map(r -> r.toDto(UserRoleDto.MIN)).collect(Collectors.toList());
        } catch (Exception e) {
            throw new BadRequestException(CommonUtils.getTranslation("svc-user-badrequest-insert-user-roles-failed", cu.getLocales(null), CommonUtils.getStringMap(MESSAGE, e.getMessage())));
        }
    } finally {
        lock.unlock();
    }
}
