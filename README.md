import java.util.Map;
import java.util.function.Supplier;
import java.util.function.BiFunction;

public boolean hasPermissionFlags(Long dealerId, Long customerId, long flags) {
    // Check the simplest condition first and return early
    if (flags == AUTH_FLAG && !catRecNo.isEmpty()) {
        return true;
    }

    // Map of flag checks using lambdas
    Map<Long, Supplier<Boolean>> simpleFlagCheckers = Map.of(
        SUPERADMIN_FLAG, this::isSuperAdmin,
        SERVER_FLAG, this::isServer,
        NOAUTH_FLAG, this::isAnonymous,
        CONSULTANT_FLAG, this::isConsultant,
        REPORTS_FLAG, this::isReports,
        HELPDESK_FLAG, this::isHelpDesk
    );

    // Iterate over the simple map and check conditions
    if (simpleFlagCheckers.entrySet().stream()
        .anyMatch(entry -> (flags & entry.getKey()) > 0 && entry.getValue().get())) {
        return true;
    }

    // Map of flag checks that require additional parameters (dealerId or customerId)
    Map<Long, BiFunction<Long, Long, Boolean>> dealerCustomerFlagCheckers = Map.of(
        DEALERADMIN_FLAG, (d, c) -> isDealerAdmin(d),
        DEALERLEADMGMT_ONLY_FLAG, (d, c) -> isDealerLeadManagement(d),
        DEALERRENTALMGMT_ONLY_FLAG, (d, c) -> isDealerRentalManagement(d),
        DEALERCATALOGMGMT_ONLY_FLAG, (d, c) -> isDealerCatalogManagement(d),
        DEALERCUSTOMERMGMT_ONLY_FLAG, (d, c) -> isDealerCustomerManagement(d),
        DEALERSERVICEMGMT_ONLY_FLAG, (d, c) -> isDealerServiceManagement(d),
        DEALERCALLOFFMGMT_ONLY_FLAG, (d, c) -> isDealerCallOffManagement(d),
        DEALEREXTENDMGMT_ONLY_FLAG, (d, c) -> isDealerExtendManagement(d),
        DEALERTRANSFERMGMT_ONLY_FLAG, (d, c) -> isDealerTransferManagement(d),
        DEALERJOBSITEMGMT_ONLY_FLAG, (d, c) -> isDealerJobsiteManagement(d),
        DEALERQUOTEMGMT_ONLY_FLAG, (d, c) -> isDealerQuoteManagement(d),
        DEALERREPORTS_ONLY_FLAG, (d, c) -> isDealerReports(d),
        DEALERSETTINGS_ONLY_FLAG, (d, c) -> isDealerSettings(d),
        CUSTOMERUSER_ONLY_FLAG, this::isCustomerUser,
        CUSTOMERADMIN_ONLY_FLAG, this::isCustomerAdmin
    );

    // Iterate over the dealer/customer map and check conditions
    return dealerCustomerFlagCheckers.entrySet().stream()
        .anyMatch(entry -> (flags & entry.getKey()) > 0 && entry.getValue().apply(dealerId, customerId));
}
