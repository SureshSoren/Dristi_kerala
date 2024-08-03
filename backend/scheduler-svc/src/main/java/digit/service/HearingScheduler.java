package digit.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import digit.config.Configuration;
import digit.config.ServiceConstants;
import digit.util.MasterDataUtil;
import digit.kafka.Producer;
import digit.repository.ReScheduleRequestRepository;
import digit.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class HearingScheduler {

    private final Producer producer;

    private final ReScheduleRequestRepository repository;

    private final Configuration configuration;

    private final ObjectMapper mapper;

    private final CalendarService calendarService;

    private final HearingService hearingService;

    private final MasterDataUtil helper;

    private final ServiceConstants serviceConstants;



    @Autowired
    public HearingScheduler(Producer producer, ReScheduleRequestRepository repository, Configuration configuration, ObjectMapper mapper, CalendarService calendarService, HearingService hearingService, MasterDataUtil helper, ServiceConstants serviceConstants) {
        this.producer = producer;
        this.repository = repository;
        this.configuration = configuration;
        this.mapper = mapper;
        this.calendarService = calendarService;
        this.hearingService = hearingService;
        this.helper = helper;
        this.serviceConstants = serviceConstants;
    }


    public void scheduleHearingForApprovalStatus(ReScheduleHearingRequest reScheduleHearingsRequest) {
        try {
            log.info("operation = scheduleHearingForApprovalStatus, result = IN_PROGRESS, RescheduledRequest = {}", reScheduleHearingsRequest.getReScheduleHearing());
            List<ReScheduleHearing> hearingsNeedToBeSchedule = new ArrayList<>();
            List<String> ids = new ArrayList<>();
            HashMap<String, LocalDate> dateMap = new HashMap<>();
            List<ReScheduleHearing> blockedHearings = new ArrayList<>();
//            for (ReScheduleHearing element : reScheduleHearingsRequest.getReScheduleHearing()) {
//                if (Objects.equals(element.getWorkflow().getAction(), "SCHEDULE")) {
//                    hearingsNeedToBeSchedule.add(element);
//                    ids.add(element.getHearingBookingId());
//                    dateMap.put(element.getHearingBookingId(), element.getScheduleDate());
//                }
//                if (Objects.equals(element.getWorkflow().getAction(), "APPROVE")) {
//                    blockedHearings.add(element);
//
//                }
//            }

            ReScheduleHearingRequest request = ReScheduleHearingRequest.builder().reScheduleHearing(blockedHearings)
                    .requestInfo(reScheduleHearingsRequest.getRequestInfo()).build();

            if (!blockedHearings.isEmpty()) producer.push("schedule-hearing-to-block-calendar", request);

            if (!hearingsNeedToBeSchedule.isEmpty()) {
                List<ScheduleHearing> hearings = hearingService.search(HearingSearchRequest.builder().criteria(ScheduleHearingSearchCriteria.builder()
                                .hearingIds(ids).build())
                        .build(), null, null);
                for (ScheduleHearing hearing : hearings) {
                    hearing.setStatus(Status.SCHEDULED.toString());
//                    hearing.setDate(dateMap.get(hearing.getHearingBookingId()));
                }

                List<MdmsSlot> defaultSlots = helper.getDataFromMDMS(MdmsSlot.class, serviceConstants.DEFAULT_SLOTTING_MASTER_NAME);

                List<MdmsHearing> defaultHearings = helper.getDataFromMDMS(MdmsHearing.class, serviceConstants.DEFAULT_HEARING_MASTER_NAME);
                Map<String, MdmsHearing> hearingTypeMap = defaultHearings.stream().collect(Collectors.toMap(
                        MdmsHearing::getHearingType,
                        obj -> obj
                ));

                ScheduleHearingRequest updateRequest = ScheduleHearingRequest.builder().hearing(hearings)
                        .requestInfo(reScheduleHearingsRequest.getRequestInfo()).build();
                hearingService.updateBulk(updateRequest, defaultSlots, hearingTypeMap);
            }


        } catch (Exception e) {
            log.info("operation = scheduleHearingForApprovalStatus, result = FAILURE, message={}", e.getMessage());
        }

    }
}
