private boolean checkPermission(List<Map<String, Object>> invitationRoles, Long userId) {
    UserDto userDto = InfrastructureRestClient.getUserByIdFromCacheFull(userId);
    AbstractUser abstractUser = new com.cat.rental.shared.AsyncUser(userDto);

    for (Map<String, Object> role : invitationRoles) {
        String roleName = getString(role, "roleName");
        Long dealerId = getLong(role, "dealerId");
        Long customerId = getLong(role, "customerId");
        Long dealerLocationId = getLong(role, "dealerlocationId");

        if (roleName == null) {
            return false;
        }

        if (NON_DEALER_RELATED_ROLES.contains(roleName)) {
            if (!isValidNonDealerRole(abstractUser, dealerId, customerId)) {
                return false;
            }
        } else if (DEALER_ADMIN.equals(roleName)) {
            if (!isValidDealerAdmin(abstractUser, dealerId, customerId)) {
                return false;
            }
        } else if (roleName.startsWith(DEALER) && !DEALER_ADMIN.equals(roleName)) {
            if (!isValidDealerRole(abstractUser, dealerId, customerId)) {
                return false;
            }
        } else if (roleName.startsWith(CUSTOMER)) {
            if (!isValidCustomerRole(abstractUser, dealerId, customerId, dealerLocationId)) {
                return false;
            }
        }
    }
    return true;
}

private String getString(Map<String, Object> map, String key) {
    return map.get(key) != null ? map.get(key).toString() : null;
}

private Long getLong(Map<String, Object> map, String key) {
    return map.get(key) != null ? Long.parseLong(map.get(key).toString()) : null;
}

private boolean isValidNonDealerRole(AbstractUser user, Long dealerId, Long customerId) {
    return (user.isSuperAdmin() || user.isConsultant()) &&
           (dealerId == null || dealerId.equals(0L)) &&
           (customerId == null || customerId.equals(0L));
}

private boolean isValidDealerAdmin(AbstractUser user, Long dealerId, Long customerId) {
    return (user.isSuperAdmin() || user.isConsultant()) &&
           DealerCache.getDealerCache().containsKey(dealerId) &&
           (customerId == null || customerId.equals(0L));
}

private boolean isValidDealerRole(AbstractUser user, Long dealerId, Long customerId) {
    return dealerId != null &&
           (user.hasPermissionFlags(dealerId, null, DEALERADMIN_FLAG) || user.hasPermissionFlags(dealerId, null, CONSULTANT_FLAG)) &&
           (customerId == null || customerId.equals(0L));
}

private boolean isValidCustomerRole(AbstractUser user, Long dealerId, Long customerId, Long dealerLocationId) {
    if (dealerId == null || customerId == null || dealerLocationId == null) {
        return false;
    }
    if (!(user.hasPermissionFlags(dealerId, null, DEALERCUSTOMERMGMT_FLAG) || 
          user.hasPermissionFlags(dealerId, null, CONSULTANT_FLAG) || 
          user.hasPermissionFlags(dealerId, customerId, CUSTOMERADMIN_ONLY_FLAG))) {
        return false;
    }
    return DealerCache.getDealerCache().containsKey(dealerId) &&
           CustomerCache.getCustomerCache().containsKey(customerId) &&
           DealerLocationCache.getDealerLocationCache().containsKey(dealerLocationId) &&
           DealerLocationCache.getDealerLocationCache().get(dealerLocationId).getDealerId().equals(dealerId) &&
           CustomerCache.getCustomerCache().get(customerId).getDealerId().equals(dealerId);
}
