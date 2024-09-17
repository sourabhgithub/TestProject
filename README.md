public List<UserRoleDto> insertUserRoles(String userId, List<UserRoleDto> insertRoleDtos, boolean isNotificationRequired) {
    Set<String> customerWithNoUserRole = new HashSet<>();
    Set<Long> customerWithNewFirstRole = new HashSet<>();

    List<UserRole> currentRoles = user.getUserRoles();
    Set<String> dealerCustomerRoleStringSetForInsert = new HashSet<>();
    boolean hasNonAnonymousCustomerUserRoleForInsert = false;
    List<UserRoleDto> customerRoles = new ArrayList<>();

    for (UserRoleDto roleDto : insertRoleDtos) {
        Long dealerId = roleDto.getDealerId(); // Use dealerId from each roleDto
        Dealer dealer = dealerService.getDealerFromDB(dealerId); // Get dealer per role

        // Proceed only if the dealer exists
        if (dealer != null) {
            String dealerCustomerRoleValue = roleDto.getRoleName() + "_" + dealerId + "_" + roleDto.getCustomerId();
            String customerUserValue = "CustomerUser_" + dealerId + "_" + roleDto.getCustomerId();
            String customerAdminValue = "CustomerAdminUser_" + dealerId + "_" + roleDto.getCustomerId();

            // Remove duplicated roles or existing roles
            if (!dealerCustomerRoleStringSetForInsert.contains(dealerCustomerRoleValue)) {
                dealerCustomerRoleStringSetForInsert.add(dealerCustomerRoleValue);

                // Validate that the role doesn't contain restricted roles
                if (!hasNonAnonymousCustomerUserRoleForInsert || !CUSTOMER_USER.equals(roleDto.getRoleName()) || Objects.isNull(roleDto.getCustomerId())) {
                    Long customerId = roleDto.getCustomerId();

                    // Insert the role for the corresponding dealer and customer
                    UserRole newRole = Mask.mapAndMask(roleDto, UserRole.class, userId, true);
                    userRoleRepo.save(newRole);

                    if (customerUserValue.equals(roleDto.getRoleName())) {
                        hasNonAnonymousCustomerUserRoleForInsert = true;
                    }
                }
            }
        }
    }

    return customerRoles;
}
