package digit.enrichment;


import digit.models.coremodels.AuditDetails;
import digit.web.models.OptOutRequest;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.stereotype.Component;

@Component
public class RescheduleRequestOptOutEnrichment {
    public void enrichCreateRequest(OptOutRequest request) {

        AuditDetails auditDetails = getAuditDetailsScheduleHearing(request.getRequestInfo());

        request.getOptOut().setAuditDetails(auditDetails);
        request.getOptOut().setRowVersion(1);

    }

//    public void enrichUpdateRequest(OptOutRequest request) {
//
//        RequestInfo requestInfo = request.getRequestInfo();
//        request.getOptOuts().forEach((application) -> {
//
//            Long currentTime = System.currentTimeMillis();
//            application.getAuditDetails().setLastModifiedTime(currentTime);
//            application.getAuditDetails().setLastModifiedBy(requestInfo.getUserInfo().getUuid());
//            application.setRowVersion(application.getRowVersion() + 1);
//
//        });
//    }


    private AuditDetails getAuditDetailsScheduleHearing(RequestInfo requestInfo) {

        return AuditDetails.builder()
                .createdBy(requestInfo.getUserInfo().getUuid())
                .createdTime(System.currentTimeMillis())
                .lastModifiedBy(requestInfo.getUserInfo().getUuid())
                .lastModifiedTime(System.currentTimeMillis())
                .build();

    }
}
