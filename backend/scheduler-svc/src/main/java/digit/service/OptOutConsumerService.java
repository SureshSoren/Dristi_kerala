package digit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import digit.config.Configuration;
import digit.kafka.Producer;
import digit.repository.ReScheduleRequestRepository;
import digit.util.CaseUtil;
import digit.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static digit.config.ServiceConstants.ACTIVE;
import static digit.config.ServiceConstants.INACTIVE;

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

            OptOut optOut = optOutRequest.getOptOut();
            List<Long> optoutDates = optOut.getOptoutDates();

            String rescheduleRequestId = optOut.getRescheduleRequestId();

            OptOutSearchRequest searchRequest = OptOutSearchRequest.builder()
                    .requestInfo(requestInfo)
                    .criteria(OptOutSearchCriteria.builder()
                            .rescheduleRequestId(rescheduleRequestId)
                            .build()).build();

            List<OptOut> optOuts = optOutService.search(searchRequest, null, null);
            int optOutAlreadyMade = optOuts.size();
            List<ReScheduleHearing> reScheduleRequest = repository.getReScheduleRequest(ReScheduleHearingReqSearchCriteria.builder()
                    .rescheduledRequestId(Collections.singletonList(rescheduleRequestId)).build(), null, null);

            ReScheduleHearing reScheduleHearing = reScheduleRequest.get(0);
            int totalOptOutCanBeMade = reScheduleHearing.getLitigants().size() + reScheduleHearing.getRepresentatives().size();

            List<Long> suggestedDates = reScheduleHearing.getSuggestedDates();
            List<Long> availableDates = reScheduleHearing.getAvailableDates();
            Set<Long> suggestedDatesSet = optOuts.isEmpty() ? new HashSet<>(availableDates) : new HashSet<>(suggestedDates);

            optoutDates.forEach(suggestedDatesSet::remove);

            // todo: audit details part
            reScheduleHearing.setAvailableDates(new ArrayList<>(suggestedDatesSet));
            if (totalOptOutCanBeMade - optOutAlreadyMade == 1 || totalOptOutCanBeMade - optOutAlreadyMade == 0) { // second condition is for lag if this data is already persisted into the db,it should be second only

                // this is last opt out, need to close the request. open the pending task for judge

                //unblock the calendar for judge (suggested days -available days)
                reScheduleHearing.setStatus(INACTIVE);

            } else {
                //update the request and reduce available dates
                reScheduleHearing.setStatus(ACTIVE);
            }

            producer.push(configuration.getUpdateRescheduleRequestTopic(), reScheduleRequest);

            log.info("operation = checkAndScheduleHearingForOptOut, result = SUCCESS");

        } catch (Exception e) {
            log.error("KAFKA_PROCESS_ERROR:", e);
            log.info("operation = checkAndScheduleHearingForOptOut, result = FAILURE, message = {}", e.getMessage());

        }


    }

}
