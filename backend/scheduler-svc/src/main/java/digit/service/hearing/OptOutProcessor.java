package digit.service.hearing;

import com.fasterxml.jackson.databind.ObjectMapper;
import digit.config.Configuration;
import digit.kafka.producer.Producer;
import digit.repository.ReScheduleRequestRepository;
import digit.service.HearingService;
import digit.service.RescheduleRequestOptOutService;
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
public class OptOutProcessor {

    private final Producer producer;
    private final ReScheduleRequestRepository repository;
    private final Configuration configuration;
    private final ObjectMapper mapper;
    private final HearingService hearingService;
    private final RescheduleRequestOptOutService optOutService;



    @Autowired
    public OptOutProcessor(Producer producer, ReScheduleRequestRepository repository, Configuration configuration, ObjectMapper mapper, HearingService hearingService, RescheduleRequestOptOutService optOutService) {
        this.producer = producer;
        this.repository = repository;
        this.configuration = configuration;
        this.mapper = mapper;
        this.hearingService = hearingService;
        this.optOutService = optOutService;

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
            Set<Long> suggestedDatesSet = optOuts.isEmpty() ? new HashSet<>(suggestedDates) : new HashSet<>(availableDates);

            optoutDates.forEach(suggestedDatesSet::remove);

            // todo: audit details part
            reScheduleHearing.setAvailableDates(new ArrayList<>(suggestedDatesSet));
            if (totalOptOutCanBeMade - optOutAlreadyMade == 1 || totalOptOutCanBeMade - optOutAlreadyMade == 0) { // second condition is for lag if this data is already persisted into the db,it should be second only

                // this is last opt out, need to close the request. open the pending task for judge

                //unblock the calendar for judge (suggested days -available days)
                ReScheduleHearingRequest request = ReScheduleHearingRequest.builder().requestInfo(requestInfo).reScheduleHearing(Collections.singletonList(reScheduleHearing)).build();
                unblockJudgeCalendarForSuggestedDays(request);

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

    public void unblockJudgeCalendarForSuggestedDays(ReScheduleHearingRequest request) {
        try {
            log.info("operation = unblockJudgeCalendarForSuggestedDays, result = IN_PROGRESS, request = {}", request.getReScheduleHearing());
            ReScheduleHearing reScheduleHearing = request.getReScheduleHearing().get(0);
            RequestInfo requestInfo = request.getRequestInfo();
            List<Long> suggestedDays = reScheduleHearing.getSuggestedDates();
            List<Long> availableDays = reScheduleHearing.getAvailableDates();
            Set<Long> suggestedDaysSet = new HashSet<>(suggestedDays);
            availableDays.forEach(suggestedDaysSet::remove);
            List<ScheduleHearing> scheduleHearings = hearingService.search(HearingSearchRequest.builder()
                    .criteria(ScheduleHearingSearchCriteria.builder()
                            .rescheduleId(reScheduleHearing.getRescheduledRequestId())
                            .tenantId(reScheduleHearing.getTenantId())
                            .build())
                    .build(), null, null);

            List<ScheduleHearing> newHearings = new ArrayList<>();
            for (ScheduleHearing scheduleHearing : scheduleHearings) {
                Long hearingDate = scheduleHearing.getHearingDate();
                if(suggestedDaysSet.contains(hearingDate)){
                    scheduleHearing.setStatus(INACTIVE);
                    newHearings.add(scheduleHearing);
                }
            }
            hearingService.update(ScheduleHearingRequest.builder()
                    .requestInfo(requestInfo)
                    .hearing(newHearings)
                    .build());
            log.info("operation = unblockJudgeCalendarForSuggestedDays, result = SUCCESS");
        } catch (Exception e) {
            log.error("Error unblocking calendar: {}", e.getMessage());
            log.info("operation = unblockJudgeCalendarForSuggestedDays, result = FAILURE, message = {}", e.getMessage());
        }
    }
}
