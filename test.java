private boolean checkPermission(List<Map<String, Object>> invitationRoles, Long userId) {
    UserDto userDto = InfrastructureRestClient.getUserByIdFromCacheFull(userId);
    AbstractUser abstractUser = new com.cat.rental.shared.AsyncUser(userDto);

    for (Map<String, Object> role : invitationRoles) {
        String roleName = getStringValue(role, "roleName");
        Long dealerId = getLongValue(role, "dealerId");
        Long customerId = getLongValue(role, "customerId");
        Long dealerLocationId = getLongValue(role, "dealerlocationId");

        if (roleName == null) {
            return false;
        }

        if (NON_DEALER_RELATED_ROLES.contains(roleName)) {
            if (!isValidNonDealerRole(abstractUser, dealerId, customerId)) {
                return false;
            }
        } else if (DEALER_ADMIN.equals(roleName)) {
            if (!isValidDealerAdminRole(abstractUser, dealerId, customerId)) {
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

private String getStringValue(Map<String, Object> map, String key) {
    Object value = map.get(key);
    return value != null ? value.toString() : null;
}

private Long getLongValue(Map<String, Object> map, String key) {
    Object value = map.get(key);
    return value != null ? Long.parseLong(value.toString()) : null;
}

private boolean isValidNonDealerRole(AbstractUser user, Long dealerId, Long customerId) {
    return (user.isSuperAdmin() || user.isConsultant()) &&
            (dealerId == null || dealerId.equals(0L)) &&
            (customerId == null || customerId.equals(0L));
}

private boolean isValidDealerAdminRole(AbstractUser user, Long dealerId, Long customerId) {
    return (user.isSuperAdmin() || user.isConsultant()) &&
            DealerCache.getDealerCache().get(dealerId) != null &&
            (customerId == null || customerId.equals(0L));
}

private boolean isValidDealerRole(AbstractUser user, Long dealerId, Long customerId) {
    return dealerId != null &&
            user.hasPermissionFlags(dealerId, null, DEALERADMIN_FLAG | CONSULTANT_FLAG) &&
            (customerId == null || customerId.equals(0L));
}

private boolean isValidCustomerRole(AbstractUser user, Long dealerId, Long customerId, Long dealerLocationId) {
    if (dealerId == null || customerId == null || dealerLocationId == null) {
        return false;
    }

    if (!user.hasPermissionFlags(dealerId, null, DEALERCUSTOMERMGMT_FLAG | CUSTOMERADMIN_ONLY_FLAG | CONSULTANT_FLAG)) {
        return false;
    }

    if (DealerCache.getDealerCache().get(dealerId) == null ||
        CustomerCache.getCustomerCache().get(customerId) == null ||
        !CustomerCache.getCustomerCache().get(customerId).getDealerId().equals(dealerId)) {
        return false;
    }

    return DealerLocationCache.getDealerLocationCache().get(dealerLocationId) != null &&
           DealerLocationCache.getDealerLocationCache().get(dealerLocationId).getDealerId().equals(dealerId);
}
