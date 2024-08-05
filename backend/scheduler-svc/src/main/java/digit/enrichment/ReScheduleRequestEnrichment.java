package digit.enrichment;


import digit.config.Configuration;
import digit.models.coremodels.AuditDetails;
import digit.util.IdgenUtil;
import digit.web.models.ReScheduleHearing;
import digit.web.models.ReScheduleHearingRequest;
import digit.web.models.Workflow;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

@Component
@Slf4j
public class ReScheduleRequestEnrichment {

    @Autowired
    private IdgenUtil idgenUtil;

    @Autowired
    private Configuration configuration;

    public void enrichRescheduleRequest(ReScheduleHearingRequest reScheduleHearingsRequest) {
        List<ReScheduleHearing> reScheduleHearing = reScheduleHearingsRequest.getReScheduleHearing();
        RequestInfo requestInfo = reScheduleHearingsRequest.getRequestInfo();
        log.info("starting update method for reschedule hearing enrichment");
        log.info("generating IDs for reschedule hearing enrichment using IdGenService");
//        List<String> idList = idgenUtil.getIdList(requestInfo,
//                reScheduleHearing.get(0).getTenantId(),
//                configuration.getRescheduleHearingIdFormat(), null, reScheduleHearing.size());

        AuditDetails auditDetails = getAuditDetailsReScheduleHearing(requestInfo);

//        int index = 0;
        for (ReScheduleHearing element : reScheduleHearing) {
//            element.setRescheduledRequestId(idList.get(index++));
            element.setRowVersion(1);
            element.setAuditDetails(auditDetails);
        }
    }

    private AuditDetails getAuditDetailsReScheduleHearing(RequestInfo requestInfo) {

        return AuditDetails.builder()
                .createdBy(requestInfo.getUserInfo().getUuid())
                .createdTime(System.currentTimeMillis())
                .lastModifiedBy(requestInfo.getUserInfo().getUuid())
                .lastModifiedTime(System.currentTimeMillis())
                .build();

    }

    public void enrichRequestOnUpdate(ReScheduleHearingRequest reScheduleHearingsRequest, List<ReScheduleHearing> existingReScheduleHearingsReq) {
        HashMap<String, Workflow> map = new HashMap<>();

        HashMap<String, LocalDate> availableAfterMap = new HashMap<>();

        HashMap<String, LocalDate> scheduleDateMap = new HashMap<>();


//        reScheduleHearingsRequest.getReScheduleHearing().forEach((element) ->
//        {
//            map.put(element.getRescheduledRequestId(), element.getWorkflow());
//            availableAfterMap.put(element.getRescheduledRequestId(), element.getAvailableAfter());
//
//            scheduleDateMap.put(element.getRescheduledRequestId(), element.getScheduleDate());
//        });
        String auditingUser = reScheduleHearingsRequest.getRequestInfo().getUserInfo().getUuid();
//        existingReScheduleHearingsReq.forEach((updateHearing) -> {
//
//            Workflow workflowNeedToUpdate = map.get(updateHearing.getRescheduledRequestId());
//            updateHearing.setWorkflow(workflowNeedToUpdate);
//            updateHearing.getAuditDetails().setLastModifiedBy(auditingUser);
//            updateHearing.getAuditDetails().setLastModifiedTime(System.currentTimeMillis());
//            updateHearing.setRowVersion(updateHearing.getRowVersion() + 1);
//            updateHearing.setAvailableAfter(availableAfterMap.get(updateHearing.getRescheduledRequestId()));
//            updateHearing.setScheduleDate(scheduleDateMap.get(updateHearing.getRescheduledRequestId()));
//
//        });

        reScheduleHearingsRequest.setReScheduleHearing(existingReScheduleHearingsReq);


    }
}
