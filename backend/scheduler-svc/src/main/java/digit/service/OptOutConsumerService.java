package digit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import digit.config.Configuration;
import digit.kafka.Producer;
import digit.repository.ReScheduleRequestRepository;
import digit.util.CaseUtil;
import digit.web.models.*;
import digit.web.models.cases.CaseCriteria;
import digit.web.models.cases.SearchCaseRequest;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class OptOutConsumerService {

    private final Producer producer;

    private final ReScheduleRequestRepository repository;

    private final Configuration configuration;

    private final ObjectMapper mapper;

    private final HearingService hearingService;

    private final RescheduleRequestOptOutService optOutService;

    private final CaseUtil caseUtil;

    @Autowired
    public OptOutConsumerService(Producer producer, ReScheduleRequestRepository repository, Configuration configuration, ObjectMapper mapper, HearingService hearingService, RescheduleRequestOptOutService optOutService, CaseUtil caseUtil) {
        this.producer = producer;
        this.repository = repository;
        this.configuration = configuration;
        this.mapper = mapper;
        this.hearingService = hearingService;
        this.optOutService = optOutService;
        this.caseUtil = caseUtil;
    }


    public void checkAndScheduleHearingForOptOut(HashMap<String, Object> record) {
        try {
            log.info("operation = checkAndScheduleHearingForOptOut, result = IN_PROGRESS, record = {}", record);
            OptOutRequest optOutRequest = mapper.convertValue(record, OptOutRequest.class);
            RequestInfo requestInfo = optOutRequest.getRequestInfo();

            List<OptOut> optOuts = optOutRequest.getOptOuts();

//            optOuts.forEach((optOut -> {
//
//                List<LocalDate> optoutDates = optOut.getOptoutDates();
//
//                //todo: check size here
//
//                Collections.sort(optoutDates);
//
//                // get the list and cancelled the hearings
//                List<ScheduleHearing> hearingList = hearingService.search(HearingSearchRequest
//                        .builder().requestInfo(requestInfo)
//                        .criteria(ScheduleHearingSearchCriteria.builder()
//                                .rescheduleId(optOut.getRescheduleRequestId())
////                                .status(Collections.singletonList(Status.BLOCKED.toString()))
//                                .build()).build(), null, null);
////                hearingList.forEach(hearing -> hearing.setStatus(Status.CANCELLED.toString()));
//
//                //release judge calendar for opt out dates
//                hearingService.update(ScheduleHearingRequest.builder()
//                        .requestInfo(requestInfo)
//                        .hearing(hearingList).build());
//
//
//                //TODO: get list of litigants
//                SearchCaseRequest searchCaseRequest = SearchCaseRequest.builder().RequestInfo(requestInfo).tenantId(configuration.getEgovStateTenantId()).criteria(Collections.singletonList(CaseCriteria.builder().caseId(optOut.getCaseId()).build())).build();
//                JsonNode representatives = caseUtil.getRepresentatives(searchCaseRequest);
//
//                //TODO: get opt out of litigants
//
//                List<OptOut> existingOptOut = optOutService.search(OptOutSearchRequest.builder().requestInfo(RequestInfo.builder().build()).criteria(
//                        OptOutSearchCriteria.builder()
//                                .rescheduleRequestId(optOut.getRescheduleRequestId()).build()
//                ).build(), null, null);
//
//
//                //  updated available days in db
//                String rescheduleRequestId = optOut.getRescheduleRequestId();
//
//                List<ReScheduleHearing> reScheduleRequest = repository.getReScheduleRequest(ReScheduleHearingReqSearchCriteria.builder()
//                        .rescheduledRequestId(Collections.singletonList(rescheduleRequestId)).build(), null, null);
//
//
//                List<Long> suggestedDates = reScheduleRequest.get(0).getSuggestedDates();
//                List<Long> availableDates = reScheduleRequest.get(0).getAvailableDates();
//                Set<Long> suggestedDatesSet = existingOptOut.isEmpty() ? new HashSet<>(availableDates) : new HashSet<>(suggestedDates);
//
//                optoutDates.forEach(suggestedDatesSet::remove);
//
//
//                reScheduleRequest.get(0).setAvailableDates(new ArrayList<>(suggestedDatesSet));
//                //if this is last one then update the status to review
//                if (representatives.size() - existingOptOut.size() == 1)
////                    reScheduleRequest.get(0).setStatus(Status.REVIEW);
//
//
//                producer.push(configuration.getUpdateRescheduleRequestTopic(), reScheduleRequest);
//
//            }));
            log.info("operation = checkAndScheduleHearingForOptOut, result = SUCCESS");

        } catch (Exception e) {
            log.error("KAFKA_PROCESS_ERROR:", e);
            log.info("operation = checkAndScheduleHearingForOptOut, result = FAILURE, message = {}", e.getMessage());

        }
    }
}
