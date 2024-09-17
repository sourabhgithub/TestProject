public List<UserRoleDto> insertUserRoles(String userId, List<UserRoleDto> insertRoleDtos, boolean isNotificationRequired) {
    Set<String> customerWithNoUserRole = new HashSet<>();
    Set<Long> customerWithNewFirstRole = new HashSet<>();
    
    List<UserRole> currentRoles = user.getUserRoles();
    Set<String> dealerCustomerRoleStringSetForInsert = new HashSet<>();
    boolean hasNonAnonymousCustomerUserRoleForInsert = false;
    List<UserRoleDto> customerRoles = new ArrayList<>();
    
    for (UserRoleDto roleDto : insertRoleDtos) {
        Long dealerId = roleDto.getDealerId(); // Get dealerId from each roleDto
        Dealer dealer = null;
        
        // Fetch dealer for each roleDto individually
        if (Objects.nonNull(dealerId) && !dealerId.equals(0L)) {
            dealer = dealerService.getDealerFromDB(dealerId); // Get the dealer for this role
        }

        if (dealer == null) {
            dealerId = null; // or handle the null dealer case here as per your requirement
        }

        String dealerCustomerRoleValue = roleDto.getRoleName() + "_" + dealerId + "_" + roleDto.getCustomerId();
        String customerUserValue = "CustomerUser_" + dealerId + "_" + roleDto.getCustomerId();
        String customerAdminValue = "CustomerAdminUser_" + dealerId + "_" + roleDto.getCustomerId();

        // Remove duplicated roles, existing roles
        if (!dealerCustomerRoleStringSetForInsert.contains(dealerCustomerRoleValue)) {
            dealerCustomerRoleStringSetForInsert.add(dealerCustomerRoleValue);

            // Validate insert roles, non-anonymous customer role
            if (!hasNonAnonymousCustomerUserRoleForInsert || 
                (!CUSTOMER_USER.equals(roleDto.getRoleName()) && Objects.nonNull(roleDto.getCustomerId()))) {
                
                Long customerId = roleDto.getCustomerId();

                // Insert role for the corresponding dealer and customer
                UserRole newRole = Mask.mapAndMask(roleDto, UserRole.class, userId, true);
                userRoleRepo.save(newRole);

                if (CUSTOMER_USER.equals(roleDto.getRoleName())) {
                    hasNonAnonymousCustomerUserRoleForInsert = true;
                }
            }
        }
    }

    return customerRoles;
}
