
public boolean hasValidFlags(int flags, String catRecNo, String dealerId, String customerId) {
    // Check single conditions first
    if (isAuthFlagValid(flags, catRecNo)) return true;

    // Define flag-condition mappings
    Map<Integer, Supplier<Boolean>> flagCheckers = Map.of(
        SUPERADMIN_FLAG, this::isSuperAdmin,
        SERVER_FLAG, this::isServer,
        NOAUTH_FLAG, this::isAnonymous,
        CONSULTANT_FLAG, this::isConsultant,
        DEALERADMIN_FLAG, () -> isDealerAdmin(dealerId),
        DEALERLEADMGMT_ONLY_FLAG, () -> isDealerLeadManagement(dealerId),
        DEALERRENTALMGMT_ONLY_FLAG, () -> isDealerRentalManagement(dealerId),
        DEALERCATALOGMGMT_ONLY_FLAG, () -> isDealerCatalogManagement(dealerId),
        DEALERCUSTOMERMGMT_ONLY_FLAG, () -> isDealerCustomerManagement(dealerId),
        DEALERSERVICEMGMT_ONLY_FLAG, () -> isDealerServiceManagement(dealerId),
        DEALERCALLOFFMGMT_ONLY_FLAG, () -> isDealerCallOffManagement(dealerId),
        DEALEREXTENDMGMT_ONLY_FLAG, () -> isDealerExtendManagement(dealerId),
        DEALERTRANSFERMGMT_ONLY_FLAG, () -> isDealerTransferManagement(dealerId),
        DEALERJOBSITEMGMT_ONLY_FLAG, () -> isDealerJobsiteManagement(dealerId),
        DEALERQUOTEMGMT_ONLY_FLAG, () -> isDealerQuoteManagement(dealerId),
        DEALERREPORTS_ONLY_FLAG, () -> isDealerReports(dealerId),
        DEALERSETTINGS_ONLY_FLAG, () -> isDealerSettings(dealerId),
        CUSTOMERUSER_ONLY_FLAG, () -> isCustomerUser(dealerId, customerId),
        CUSTOMERADMIN_ONLY_FLAG, () -> isCustomerAdmin(dealerId, customerId),
        REPORTS_FLAG, this::isReports,
        HELPDESK_FLAG, this::isHelpDesk
    );

    // Iterate over the map and check conditions
    return flagCheckers.entrySet().stream()
        .anyMatch(entry -> (flags & entry.getKey()) > 0 && entry.getValue().get());
}

private boolean isAuthFlagValid(int flags, String catRecNo) {
    return flags == AUTH_FLAG && !catRecNo.isEmpty();
}
