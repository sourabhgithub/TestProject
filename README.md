Note that this:
                } else if (!(abstractUser.hasPermissionFlags(dealerId, null, DEALERCUSTOMERMGMT_FLAG) || abstractUser.hasPermissionFlags(dealerId, null, CONSULTANT_FLAG) || abstractUser.hasPermissionFlags(dealerId,customerId,CUSTOMERADMIN_ONLY_FLAG))) {
Should be equivalent to this:
                } else if (!(abstractUser.hasPermissionFlags(dealerId,customerId,DEALERCUSTOMERMGMT_FLAG|CONSULTANT_FLAG|CUSTOMERADMIN_ONLY_FLAG))) {
as well.  The checks for the superadmin-level roles ignore dealerId and customerId, the checks for dealer-level roles ignore customerId, and the checks for customer-level roles use all arguments.
