public void automationUpdateLeadRequest(RequestDto requestDto) {
    try {
        String requestId = requestDto.getId().toString();
        log.info("automationUpdateLeadRequest() - submitting initial screen test for request ({})", requestDto.getId());

        boolean releasedFlag = initialRequest(requestId, requestDto);

        if (releasedFlag) {
            updateRequest(requestDto.getId(), SUBMITTED);
            return;
        }

        List<RequestUpdateDto> requestUpdates = requestDto.getUpdates();
        String latestRequestStatus = CollectionUtils.isEmpty(requestUpdates) ? null : requestUpdates.get(0).getStatus();
        String status = checkOrderStatus(requestId);

        // Track the start time when the lead enters Lead Screening
        long leadScreeningStartTime = System.currentTimeMillis();

        // Retry until checkOrderStatus is in either closed status list or released status list.
        while (!closedStatus.contains(status) && !releasedStatus.contains(status)) {
            log.info("automationUpdateLeadRequest() - checking for status update on request ({})", requestDto.getId());

            if (onHoldStatus.contains(status) || errorStatus.contains(status) || status == null) {
                if (!LEAD_SCREENING.equals(latestRequestStatus) && onHoldStatus.contains(status)) {
                    updateRequest(requestDto.getId(), LEAD_SCREENING, status);
                }

                // Check if lead has been in Lead Screening for 10 minutes
                if (LEAD_SCREENING.equals(latestRequestStatus)) {
                    long currentTime = System.currentTimeMillis();
                    long timeInLeadScreening = (currentTime - leadScreeningStartTime) / 60000; // in minutes
                    if (timeInLeadScreening >= 10) {
                        log.info("Lead has been in Lead Screening for more than 10 minutes, releasing it automatically.");
                        updateRequest(requestDto.getId(), SUBMITTED);
                        return;
                    }
                }

                Thread.sleep(300000L); // Sleep for 5 minutes
                requestDto = RequestRestClient.getRequestById(getIntraServiceToken(), Long.valueOf(requestId));

                if (requestDto == null || !LEAD_SCREENING.equalsIgnoreCase(requestDto.getStatus())) {
                    return;
                }

                status = checkOrderStatus(requestId);
            }

            if (serviceDownStatus.contains(status)) {
                Thread.sleep(3600000L); // Sleep for 1 hour
                requestDto = RequestRestClient.getRequestById(getIntraServiceToken(), Long.valueOf(requestId));

                if (requestDto == null || !LEAD_SCREENING.equalsIgnoreCase(requestDto.getStatus())) {
                    return;
                }

                boolean flag = initialRequest(requestId, requestDto);
                if (flag) {
                    updateRequest(requestDto.getId(), SUBMITTED);
                    return;
                }

                status = checkOrderStatus(requestId);
            }

            latestRequestStatus = CollectionUtils.isEmpty(requestDto.getUpdates()) ? null : requestDto.getUpdates().get(0).getStatus();
        }

        if (closedStatus.contains(status)) {
            updateRequest(requestDto.getId(), CLOSED);
        }

        if (releasedStatus.contains(status)) {
            updateRequest(requestDto.getId(), SUBMITTED);
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.error("automationUpdateLeadRequest thread sleep exception " + e.getMessage());
        errorReportForException(e, "services");
    } catch (Exception e) {
        log.error("automationUpdateLeadRequest{}: Exiting due to Exception: " + e.getMessage());
        errorReportForException(e, "services");
    }
}
