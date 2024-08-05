package digit.validator;


import com.fasterxml.jackson.databind.JsonNode;
import digit.config.Configuration;
import digit.repository.ReScheduleRequestRepository;
import digit.service.HearingService;
import digit.util.CaseUtil;
import digit.web.models.*;
import digit.web.models.cases.CaseCriteria;
import digit.web.models.cases.SearchCaseRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class ReScheduleRequestValidator {

    private final HearingService hearingService;

    private final ReScheduleRequestRepository repository;

    private final Configuration config;

    private final CaseUtil caseUtil;


    @Autowired
    public ReScheduleRequestValidator(HearingService hearingService, ReScheduleRequestRepository repository, Configuration config, CaseUtil caseUtil) {
        this.hearingService = hearingService;
        this.repository = repository;
        this.config = config;

        this.caseUtil = caseUtil;
    }


    public void validateRescheduleRequest(ReScheduleHearingRequest reScheduleHearingsRequest) {
        log.info("operation = validateRescheduleRequest, result = IN_PROGRESS");

        List<ReScheduleHearing> rescheduleRequests = reScheduleHearingsRequest.getReScheduleHearing();
        RequestInfo requestInfo = reScheduleHearingsRequest.getRequestInfo();
        // required field validation
        rescheduleRequests.forEach((element) -> {
            if (ObjectUtils.isEmpty(element.getTenantId())) {
                throw new CustomException("DK_RR_TENANT_VAL_ERR", "tenantId is necessary to process the hearing reschedule");
            }
            if (ObjectUtils.isEmpty(element.getHearingBookingId())) {
                throw new CustomException("DK_RR_HEARING_ID_VAL_ERR", "Hearing ID is necessary to process the hearing reschedule");
            }

            if (ObjectUtils.isEmpty(element.getRequesterId())) {
                throw new CustomException("DK_RR_REQ_ID_VAL_ERR", "Requester ID is necessary to process the hearing reschedule");
            }

            if (ObjectUtils.isEmpty(element.getReason())) {
                throw new CustomException("DK_RR_REASON_VAL_ERR", "Reason is necessary to process the hearing reschedule");
            }

            if (ObjectUtils.isEmpty(element.getJudgeId())) {
                throw new CustomException("DK_RR_JUDGE_VAL_ERR", "Judge ID is necessary to process the hearing reschedule");
            }

            if (ObjectUtils.isEmpty(element.getCaseId())) {
                throw new CustomException("DK_RR_CASE_VAL_ERR", "Case ID is necessary to process the hearing reschedule");
            }
        });

        //VALIDATING HEARINGS FOR WHICH RESCHEDULE REQUEST IS RAISED

        List<String> ids = rescheduleRequests.stream().map(ReScheduleHearing::getHearingBookingId).toList();
        List<ScheduleHearing> hearingsToReschedule = hearingService.search(HearingSearchRequest.builder().requestInfo(reScheduleHearingsRequest.getRequestInfo()).criteria(ScheduleHearingSearchCriteria.builder().hearingIds(ids).build()).build(), null, null);

        if (hearingsToReschedule.size() != ids.size()) {
            throw new CustomException("DK_RR_INVALID_REQ_ERR", "Hearing does not exist in the database");
        }

        Long rescheduleRequestDueDate = config.getRescheduleRequestDueDate();
//        hearingsToReschedule.forEach((hearing) -> {
//            if (!(LocalDate.now().isEqual(hearing.getDate().minusDays(rescheduleRequestDueDate)) || LocalDate.now().isBefore(hearing.getDate().minusDays(rescheduleRequestDueDate)))) {
//                throw new CustomException("DK_RR_DUE_PASS_ERR", "last date to raise reschedule request is pass");
//            }
//        });


        //checking is there any open reschedule request in db, if there are any then restricting user to create new one
        rescheduleRequests.forEach(element -> {
            List<ReScheduleHearing> reScheduleRequest = repository.getReScheduleRequest(
                    ReScheduleHearingReqSearchCriteria.builder()
                            .hearingBookingId(element.getHearingBookingId())
                            .tenantId(element.getTenantId())
                            .build(),
                    null,
                    null
            );

            // checking only the latest request
//            if (element.getWorkflow().getAction().equals("APPLY")
//                    && !reScheduleRequest.isEmpty()
//                    && !(reScheduleRequest.get(0).getStatus().equals(Status.HEARING_SCHEDULE)
//                    || reScheduleRequest.get(0).getStatus().equals(Status.CANCELLED)
//                    || reScheduleRequest.get(0).getStatus().equals(Status.REJECTED))) {
//                throw new CustomException("DK_RR_DUPLICATE_ERR", "A reschedule request has already been initiated for Hearing: " + element.getHearingBookingId());
//            }

        });


        // validation through case service
        rescheduleRequests.forEach((element) -> {

            //Case Integration
            SearchCaseRequest searchCaseRequest = SearchCaseRequest.builder().RequestInfo(requestInfo).tenantId(config.getEgovStateTenantId()).criteria(Collections.singletonList(CaseCriteria.builder().caseId(element.getCaseId()).build())).build();

            JsonNode representatives = caseUtil.getRepresentatives(searchCaseRequest);
            Set<String> representativeIds = caseUtil.getIdsFromJsonNodeArray(representatives);
            if (!representativeIds.contains(element.getRequesterId())) {
                throw new CustomException("DK_RR_INVALID_REQ_ERR", "Invalid requesterId.");
            }
        });

        log.info("operation = validateRescheduleRequest, result = SUCCESS");

    }

    public List<ReScheduleHearing> validateExistingApplication(ReScheduleHearingRequest reScheduleHearingsRequest) {
        log.info("operation = validateExistingApplication, result = IN_PROGRESS");


        List<ReScheduleHearing> reScheduleHearing = reScheduleHearingsRequest.getReScheduleHearing();
        List<String> ids = new ArrayList<>();
        reScheduleHearingsRequest.getReScheduleHearing().forEach((element) -> {

//            if (element.getWorkflow().getAction().equals("APPROVE")) {
//                if (ObjectUtils.isEmpty(element.getAvailableAfter())) {
//                    throw new CustomException("DK_SH_APP_ERR", "Available after cannot be null");
//                }
//                if (element.getAvailableAfter().isBefore(LocalDate.now())) {
//                    throw new CustomException("DK_SH_APP_ERR", "available after cannot be past date");
//                }
//            }

            ids.add(element.getRescheduledRequestId());

        });

        List<ReScheduleHearing> existingReScheduleRequests = repository.getReScheduleRequest(ReScheduleHearingReqSearchCriteria.builder().rescheduledRequestId(ids).build(), null, null);
        if (existingReScheduleRequests.size() != ids.size()) {
            //TODO: proper error msg
            throw new CustomException("DK_RR_INVALID_ID_ERR", "Reschedule request does not exist in the database");
        }

        log.info("operation = validateExistingApplication, result = SUCCESS");

        return existingReScheduleRequests;

    }

    public void validateBulkRescheduleRequest(BulkReScheduleHearingRequest request) {
        log.info("operation = validateBulkRescheduleRequest, result = IN_PROGRESS");
        BulkReschedulingOfHearings bulkRescheduling = request.getBulkRescheduling();


        LocalDateTime currentDateTime = LocalDateTime.now();

        if (ObjectUtils.isEmpty(bulkRescheduling.getJudgeId())) {
            throw new CustomException("DK_SH_APP_ERR", "judge id must not be null");
        }
//        Long startTime = bulkRescheduling.getStartTime();
//        if (startTime.isBefore(currentDateTime)) {
//            throw new CustomException("DK_SH_APP_ERR", "Can not reschedule for past date hearings");
//        }
//
//        Long endTime = bulkRescheduling.getEndTime();
//        if (endTime.isBefore(startTime)) {
//            throw new CustomException("DK_SH_APP_ERR", "end time is before start time");
//        }

        Long scheduleAfter = bulkRescheduling.getScheduleAfter();
//        if (scheduleAfter.isBefore(LocalDate.now())) {
//            throw new CustomException("DK_SH_APP_ERR", "can not reschedule for past dates");
//        }
        log.info("operation = validateBulkRescheduleRequest, result = SUCCESS");

    }
}
