package digit.service;


import digit.config.Configuration;
import digit.config.ServiceConstants;
import digit.enrichment.HearingEnrichment;
import digit.helper.DefaultMasterDataHelper;
import digit.kafka.Producer;
import digit.repository.HearingRepository;
import digit.validator.HearingValidator;
import digit.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Contains methods related to schedule hearing , update hearing , search hearing , bulk update and available dates
 */
@Service
@Slf4j
public class HearingService {

    private final HearingValidator hearingValidator;

    private final HearingEnrichment hearingEnrichment;

    private final Producer producer;

    private final Configuration config;

    private final HearingRepository hearingRepository;

    private final ServiceConstants serviceConstants;

    private final DefaultMasterDataHelper helper;

    @Autowired
    public HearingService(HearingValidator hearingValidator, HearingEnrichment hearingEnrichment, Producer producer, Configuration config, HearingRepository hearingRepository, ServiceConstants serviceConstants, DefaultMasterDataHelper helper) {
        this.hearingValidator = hearingValidator;
        this.hearingEnrichment = hearingEnrichment;
        this.producer = producer;
        this.config = config;
        this.hearingRepository = hearingRepository;
        this.serviceConstants = serviceConstants;
        this.helper = helper;
    }

    /**
     * This function schedule a hearing for particular judge on provided date after validating
     * @param schedulingRequests request object with request info and list of schedule hearing object
     * @return list of schedule hearing with status schedule
     * @exception org.egov.tracer.model.CustomException if provided date is not available for hearing
     */

    public List<ScheduleHearing> schedule(ScheduleHearingRequest schedulingRequests) {
        log.info("operation = schedule, result = IN_PROGRESS, ScheduleHearingRequest={}, Hearing={}", schedulingRequests, schedulingRequests.getHearing());

        // master data for default slots of court
        List<MdmsSlot> defaultSlots = helper.getDataFromMDMS(MdmsSlot.class, serviceConstants.DEFAULT_SLOTTING_MASTER_NAME);

        double totalHrs = defaultSlots.stream().reduce(0.0, (total, slot) -> total + slot.getSlotDuration() / 60.0, Double::sum);
        // get hearings and default timing
        List<MdmsHearing> defaultHearings = helper.getDataFromMDMS(MdmsHearing.class, serviceConstants.DEFAULT_HEARING_MASTER_NAME);
        Map<String, MdmsHearing> hearingTypeMap = defaultHearings.stream().collect(Collectors.toMap(
                MdmsHearing::getHearingType,
                obj -> obj
        ));
        //validate hearing request here
        hearingValidator.validateHearing(schedulingRequests, totalHrs, hearingTypeMap);

        // enhance the hearing request here
        hearingEnrichment.enrichScheduleHearing(schedulingRequests, defaultSlots, hearingTypeMap);

        //push to kafka
        producer.push(config.getScheduleHearingTopic(), schedulingRequests.getHearing());

        return schedulingRequests.getHearing();
    }

    /**
     * This function update the hearing
     * @param scheduleHearingRequest request object with request info and list of schedule hearing object
     * @return updated hearings with audit details
     */

    // to update the status of existing hearing to reschedule
    public List<ScheduleHearing> update(ScheduleHearingRequest scheduleHearingRequest) {
        log.info("operation = update, result = IN_PROGRESS, ScheduleHearingRequest={}, Hearing={}", scheduleHearingRequest, scheduleHearingRequest.getHearing());

        //  enrich the audit details
        hearingEnrichment.enrichUpdateScheduleHearing(scheduleHearingRequest.getRequestInfo(), scheduleHearingRequest.getHearing());

        producer.push(config.getScheduleHearingUpdateTopic(), scheduleHearingRequest.getHearing());

        log.info("operation = update, result = SUCCESS, ScheduleHearing={}", scheduleHearingRequest.getHearing());

        return scheduleHearingRequest.getHearing();

    }

    /**
     * This function use to search in the hearing table with different search parameter
     * @param request request object with request info and search criteria for hearings
     * @return list of schedule hearing object
     */

    public List<ScheduleHearing> search(HearingSearchRequest request) {

        return hearingRepository.getHearings(request.getCriteria());

    }

    /**
     * This function provide the available date for judge and their occupied bandwidth after a start date ( fromDate )
     * @param hearingSearchCriteria criteria and request info object
     * @return list of availability dto
     */

    public List<AvailabilityDTO> getAvailableDateForHearing(HearingSearchCriteria hearingSearchCriteria) {

        return hearingRepository.getAvailableDatesOfJudges(hearingSearchCriteria);
    }

    /**
     * This function enrich the audit details as well as timing for hearing in updated date
     * @param request request object with request info and list of schedule hearings
     * @param defaultSlot default slots for court
     * @param hearingTypeMap default hearings and their timing
     */

    public void updateBulk(ScheduleHearingRequest request, List<MdmsSlot> defaultSlot, Map<String, MdmsHearing> hearingTypeMap) {

        hearingEnrichment.enrichBulkReschedule(request, defaultSlot, hearingTypeMap);

        producer.push(config.getScheduleHearingUpdateTopic(), request.getHearing());
    }
}
