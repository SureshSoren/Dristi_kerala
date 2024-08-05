package digit.service;


import com.fasterxml.jackson.databind.JsonNode;
import digit.config.Configuration;
import digit.config.ServiceConstants;
import digit.enrichment.ReScheduleRequestEnrichment;
import digit.kafka.Producer;
import digit.repository.ReScheduleRequestRepository;
import digit.util.CaseUtil;
import digit.util.DateUtil;
import digit.util.MasterDataUtil;
import digit.validator.ReScheduleRequestValidator;
import digit.web.models.*;
import digit.web.models.cases.CaseCriteria;
import digit.web.models.cases.SearchCaseRequest;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Contains methods related to raised reschedule request, update request,search , bulk rescheduling
 */
@Service
@Slf4j
public class ReScheduleHearingService {

    private final Configuration config;
    private final ReScheduleRequestRepository repository;
    private final ReScheduleRequestValidator validator;
    private final ReScheduleRequestEnrichment enrichment;
    private final Producer producer;
    private final WorkflowService workflowService;
    private final HearingService hearingService;
    private final CalendarService calendarService;
    private final HearingScheduler hearingScheduler;
    private final ServiceConstants serviceConstants;
    private final MasterDataUtil helper;
    private final CaseUtil caseUtil;
    private final Configuration configuration;

    private final DateUtil dateUtil;

    @Autowired
    public ReScheduleHearingService(Configuration config, ReScheduleRequestRepository repository, ReScheduleRequestValidator validator, ReScheduleRequestEnrichment enrichment, Producer producer, WorkflowService workflowService, HearingService hearingService, CalendarService calendarService, HearingScheduler hearingScheduler, ServiceConstants serviceConstants, MasterDataUtil helper, CaseUtil caseUtil, Configuration configuration, DateUtil dateUtil) {
        this.config = config;
        this.repository = repository;
        this.validator = validator;
        this.enrichment = enrichment;
        this.producer = producer;
        this.workflowService = workflowService;
        this.hearingService = hearingService;
        this.calendarService = calendarService;
        this.hearingScheduler = hearingScheduler;
        this.serviceConstants = serviceConstants;
        this.helper = helper;
        this.caseUtil = caseUtil;
        this.configuration = configuration;
        this.dateUtil = dateUtil;
    }

    /**
     * @param reScheduleHearingsRequest
     * @return
     */

    public List<ReScheduleHearing> create(ReScheduleHearingRequest reScheduleHearingsRequest) {
        log.info("operation = create, result = IN_PROGRESS,  RescheduledRequest = {}", reScheduleHearingsRequest.getReScheduleHearing());

        List<ReScheduleHearing> reScheduleHearing = reScheduleHearingsRequest.getReScheduleHearing();


        enrichment.enrichRescheduleRequest(reScheduleHearingsRequest);

        try {


            RequestInfo requestInfo = reScheduleHearingsRequest.getRequestInfo();

            List<ReScheduleHearing> hearingDetails = reScheduleHearingsRequest.getReScheduleHearing();
            String tenantId = hearingDetails.get(0).getTenantId();

            for (ReScheduleHearing hearingDetail : hearingDetails) {

                SearchCaseRequest searchCaseRequest = SearchCaseRequest.builder().RequestInfo(requestInfo).tenantId("kl").criteria(Collections.singletonList(CaseCriteria.builder().caseId(hearingDetail.getCaseId()).build())).build();
                JsonNode litigants = caseUtil.getLitigants(searchCaseRequest);
                Set<String> litigantIds = caseUtil.getIndividualIds(litigants);
                JsonNode representatives = caseUtil.getRepresentatives(searchCaseRequest);
                Set<String> representativeIds = caseUtil.getIdsFromJsonNodeArray(representatives);
                int noOfAttendees = representativeIds.size();
                Integer numberOfSuggestedDays = Math.toIntExact(configuration.getOptOutLimit() * noOfAttendees + 1);

                litigantIds = caseUtil.getLitigantsFromRepresentatives(litigantIds, representatives);

                hearingDetail.setRepresentatives(representativeIds);
                hearingDetail.setLitigants(litigantIds);

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
                hearing.setStatus("RESCHEDULE");


                //reschedule hearing to unblock the calendar
                hearingService.update(ScheduleHearingRequest.builder()
                        .requestInfo(requestInfo).hearing(hearings).build());


                List<ScheduleHearing> udpateHearingList = new ArrayList<>();

                for (AvailabilityDTO availabilityDTO : availability) {
                    //TODO: update logic to assign start time and end time

                    ScheduleHearing scheduleHearing = new ScheduleHearing(hearing);


                    long blockedDate = Long.parseLong(availabilityDTO.getDate());
                    scheduleHearing.setStartTime(blockedDate);
                    scheduleHearing.setEndTime(blockedDate);
                    scheduleHearing.setRescheduleRequestId(hearingDetail.getRescheduledRequestId());
                    scheduleHearing.setStatus("BLOCKED");

                    udpateHearingList.add(scheduleHearing);

                }
                hearingService.schedule(ScheduleHearingRequest.builder()
                        .requestInfo(requestInfo).hearing(udpateHearingList).build());

            }
            log.info("operation = updateRequestForBlockCalendar, result = SUCCESS");

        } catch (Exception e) {

            log.error("DK_SH_APP_ERR: error while blocking the calendar", e);
        }


        producer.push(config.getRescheduleRequestCreateTopic(), reScheduleHearing);

        log.info("operation = create, result=SUCCESS, ReScheduleHearing={}", reScheduleHearing);

        return reScheduleHearing;

    }

    /**
     * @param reScheduleHearingsRequest
     * @return
     */
    public List<ReScheduleHearing> update(ReScheduleHearingRequest reScheduleHearingsRequest) {
        log.info("operation = update, result = IN_PROGRESS,  RescheduledRequest = {}", reScheduleHearingsRequest.getReScheduleHearing());

        List<ReScheduleHearing> existingReScheduleHearingsReq = validator.validateExistingApplication(reScheduleHearingsRequest);

        enrichment.enrichRequestOnUpdate(reScheduleHearingsRequest, existingReScheduleHearingsReq);

        workflowService.updateWorkflowStatus(reScheduleHearingsRequest);

        // here if its approved we need to calculate date
        // then schedule dummy hearings for judge to block the calendar
        hearingScheduler.scheduleHearingForApprovalStatus(reScheduleHearingsRequest);

        producer.push(config.getUpdateRescheduleRequestTopic(), reScheduleHearingsRequest.getReScheduleHearing());
        log.info("operation = create, result = SUCCESS, ReScheduleHearing={}", existingReScheduleHearingsReq);

        return reScheduleHearingsRequest.getReScheduleHearing();

    }

    /**
     * @param request
     * @return
     */
    public List<ReScheduleHearing> search(ReScheduleHearingReqSearchRequest request, Integer limit, Integer offset) {
        return repository.getReScheduleRequest(request.getCriteria(), limit, offset);
    }

    /**
     * @param request
     * @return
     */
    public List<ReScheduleHearing> bulkReschedule(BulkReScheduleHearingRequest request) {
        log.info("operation = bulkReschedule, result = IN_PROGRESS,  BulkRescheduling = {}", request.getBulkRescheduling());

        validator.validateBulkRescheduleRequest(request);

        List<MdmsSlot> defaultSlots = helper.getDataFromMDMS(MdmsSlot.class, serviceConstants.DEFAULT_SLOTTING_MASTER_NAME);

        double totalHrs = defaultSlots.stream().reduce(0.0, (total, slot) -> total + slot.getSlotDuration() / 60.0, Double::sum);
        List<MdmsHearing> defaultHearings = helper.getDataFromMDMS(MdmsHearing.class, serviceConstants.DEFAULT_HEARING_MASTER_NAME);
        Map<String, MdmsHearing> hearingTypeMap = defaultHearings.stream().collect(Collectors.toMap(
                MdmsHearing::getHearingType,
                obj -> obj
        ));
        BulkReschedulingOfHearings bulkRescheduling = request.getBulkRescheduling();

        String tenantId = request.getRequestInfo().getUserInfo().getTenantId();
        String judgeId = bulkRescheduling.getJudgeId();
        Long endTime = bulkRescheduling.getEndTime();
        Long startTime = bulkRescheduling.getStartTime();
        Long fromDate = bulkRescheduling.getScheduleAfter();

        ScheduleHearingSearchCriteria criteria = ScheduleHearingSearchCriteria.builder().judgeId(judgeId).startDateTime(startTime).endDateTime(endTime).tenantId(tenantId).build();
//                .status(Arrays.asList(Status.SCHEDULED.toString(), Status.BLOCKED.toString())).build();

        List<ScheduleHearing> hearings = hearingService.search(HearingSearchRequest.builder().requestInfo(request.getRequestInfo()).criteria(criteria).build(), null, null);

        if (CollectionUtils.isEmpty(hearings)) {
            return new ArrayList<>();
        }

        List<ReScheduleHearing> reScheduleHearings = createReschedulingRequest(hearings, request.getRequestInfo().getUserInfo().getUuid());
        // create rescheduling req in db
        create(ReScheduleHearingRequest.builder().reScheduleHearing(reScheduleHearings).requestInfo(request.getRequestInfo()).build());

        //get available date
        List<AvailabilityDTO> availability = calendarService.getJudgeAvailability(
                JudgeAvailabilitySearchRequest.builder().requestInfo(request.getRequestInfo())
                        .criteria(JudgeAvailabilitySearchCriteria.builder()
                                .judgeId(judgeId)
                                .fromDate(fromDate)
                                .courtId("0001")
                                .numberOfSuggestedDays(hearings.size() + 10)
                                .tenantId(tenantId)// need to configure some where
                                .build()).build()
        );

        // assign slots and push for schedule hearing
        int index = 0;
        Double requiredSlot = null;
//        for (ScheduleHearing hearing : hearings) {
//            String eventType = hearing.getEventType().toString();
//
//            MdmsHearing hearingType = hearingTypeMap.get(eventType);
//            requiredSlot = hearingType.getHearingTime() / 60.00;
//
//            Double occupiedBandwidth = availability.get(index).getOccupiedBandwidth();
////            if (totalHrs - occupiedBandwidth > requiredSlot) {  // need to configure
////                hearing.setDate(LocalDate.parse(availability.get(index).getDate()));
////                availability.get(index).setOccupiedBandwidth(occupiedBandwidth + requiredSlot);  // need to configure
////            } else {
////                hearing.setDate(LocalDate.parse(availability.get(++index).getDate()));
////                availability.get(index).setOccupiedBandwidth(availability.get(index).getOccupiedBandwidth() + requiredSlot);  // need to configure
////            }
//        }
        // try to make it async
        // updated hearing in hearing table
        hearingService.updateBulk(ScheduleHearingRequest.builder()
                .hearing(hearings).requestInfo(request.getRequestInfo()).build(), defaultSlots, hearingTypeMap);

        log.info("operation = bulkReschedule, result = SUCCESS, ReScheduleHearing={}", reScheduleHearings);

        return reScheduleHearings;
    }

    /**
     * @param hearings
     * @param requesterId
     * @return
     */
    private List<ReScheduleHearing> createReschedulingRequest(List<ScheduleHearing> hearings, String requesterId) {
        log.info("operation=createReschedulingRequest, result=IN_PROGRESS, hearings={}", hearings);
        List<ReScheduleHearing> resultList = new ArrayList<>();

        Workflow workflow = Workflow.builder().action("AUTO_SCHEDULE").assignees(new ArrayList<>()).comment("bulk reschedule by :" + requesterId).build();
        for (ScheduleHearing hearing : hearings) {

            ReScheduleHearing reScheduleHearingReq = ReScheduleHearing.builder()
                    .hearingBookingId(hearing.getHearingBookingId())
                    .judgeId(hearing.getJudgeId())
                    .caseId(hearing.getCaseId())
                    .tenantId(hearing.getTenantId())
                    .requesterId(requesterId)
                    .reason("reschedule by judge")
                    .build();

            resultList.add(reScheduleHearingReq);

        }
        log.info("operation= createReschedulingRequest, result=SUCCESS, ReScheduleHearing={}", resultList);
        return resultList;
    }
}