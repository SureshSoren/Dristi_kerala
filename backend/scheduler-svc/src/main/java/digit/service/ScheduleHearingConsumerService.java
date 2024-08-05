package digit.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import digit.config.Configuration;
import digit.kafka.Producer;
import digit.util.CaseUtil;
import digit.web.models.*;
import digit.web.models.cases.CaseCriteria;
import digit.web.models.cases.SearchCaseRequest;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class ScheduleHearingConsumerService {

    private final Configuration configuration;

    private final ObjectMapper mapper;

    private final CalendarService calendarService;

    private final Producer producer;

    private final HearingService hearingService;

    private final CaseUtil caseUtil;

    public ScheduleHearingConsumerService(Configuration configuration, ObjectMapper mapper, CalendarService calendarService, Producer producer, HearingService hearingService, CaseUtil caseUtil) {
        this.configuration = configuration;
        this.mapper = mapper;
        this.calendarService = calendarService;
        this.producer = producer;
        this.hearingService = hearingService;
        this.caseUtil = caseUtil;
    }


    // blocked judged calendar by creating temp hearings
    public void updateRequestForBlockCalendar(HashMap<String, Object> record) {

        try {
            log.info("operation = updateRequestForBlockCalendar, result = IN_PROGRESS, record = {}", record);

            ReScheduleHearingRequest hearingUpdateRequest = mapper.convertValue(record, ReScheduleHearingRequest.class);
            RequestInfo requestInfo = hearingUpdateRequest.getRequestInfo();

            List<ReScheduleHearing> hearingDetails = hearingUpdateRequest.getReScheduleHearing();
            String tenantId = hearingDetails.get(0).getTenantId();

            for (ReScheduleHearing hearingDetail : hearingDetails) {

                SearchCaseRequest searchCaseRequest = SearchCaseRequest.builder().RequestInfo(requestInfo).tenantId("kl").criteria(Collections.singletonList(CaseCriteria.builder().caseId(hearingDetail.getCaseId()).build())).build();
                JsonNode representatives = caseUtil.getRepresentatives(searchCaseRequest);
                Set<String> representativeIds = caseUtil.getIdsFromJsonNodeArray(representatives);
                int noOfAttendees = representativeIds.size();
                Integer numberOfSuggestedDays = Math.toIntExact(configuration.getOptOutLimit() * noOfAttendees + 1);

                List<AvailabilityDTO> availability = calendarService.getJudgeAvailability(JudgeAvailabilitySearchRequest
                        .builder()
                        .requestInfo(requestInfo)
                        .criteria(JudgeAvailabilitySearchCriteria.builder()
                                .judgeId(hearingDetail.getJudgeId())
                                .fromDate(hearingDetail.getAvailableAfter())
                                .courtId("0001")  //TODO: need to configure somewhere
                                .numberOfSuggestedDays(numberOfSuggestedDays) //TODO: later we change this to no of attendees
                                .tenantId(tenantId)
                                .build()).build());

                // update here all the suggestedDay in reschedule hearing day

                List<Long> suggestedDays = availability.stream().map(
                                (suggestedDate) -> Long.valueOf(suggestedDate.getDate()))
                        .toList();
                hearingDetail.setSuggestedDates(suggestedDays);
                hearingDetail.setRowVersion(hearingDetail.getRowVersion() + 1);


                List<ScheduleHearing> hearings = hearingService.search(HearingSearchRequest.builder()
                        .requestInfo(requestInfo)
                        .criteria(ScheduleHearingSearchCriteria.builder()
                                .hearingIds(Collections.singletonList(hearingDetail.getHearingBookingId()))
                                .build()).build(), null, null);
                ScheduleHearing hearing = hearings.get(0);
//                hearings.get(0).setStatus(Status.RE_SCHEDULED.toString());


                //reschedule hearing to unblock the calendar
                hearingService.update(ScheduleHearingRequest.builder()
                        .requestInfo(requestInfo).hearing(hearings).build());


                List<ScheduleHearing> udpateHearingList = new ArrayList<>();

                for (AvailabilityDTO availabilityDTO : availability) {
                    //TODO: update logic to assign start time and end time

                    ScheduleHearing scheduleHearing = new ScheduleHearing(hearing);

                    scheduleHearing.setStartTime(hearing.getStartTime());
                    scheduleHearing.setEndTime(hearing.getEndTime());
//                    scheduleHearing.setStatus(Status.BLOCKED.toString());
                    udpateHearingList.add(scheduleHearing);
                    scheduleHearing.setRescheduleRequestId(hearingDetail.getRescheduledRequestId());

                }
                hearingService.schedule(ScheduleHearingRequest.builder()
                        .requestInfo(requestInfo).hearing(udpateHearingList).build());

            }
            log.info("operation = updateRequestForBlockCalendar, result = SUCCESS");

            producer.push(configuration.getUpdateRescheduleRequestTopic(), hearingDetails);
        } catch (Exception e) {
            log.error("KAFKA_PROCESS_ERROR:", e);
            log.error("DK_SH_APP_ERR: error while blocking the calendar", e);
        }
    }
}
