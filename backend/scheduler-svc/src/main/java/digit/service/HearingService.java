package digit.service;


import digit.config.Configuration;
import digit.config.ServiceConstants;
import digit.enrichment.HearingEnrichment;
import digit.kafka.producer.Producer;
import digit.repository.HearingRepository;
import digit.util.MasterDataUtil;
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


    private final HearingEnrichment hearingEnrichment;

    private final Producer producer;

    private final Configuration config;

    private final HearingRepository hearingRepository;

    private final ServiceConstants serviceConstants;

    private final MasterDataUtil helper;

    @Autowired
    public HearingService( HearingEnrichment hearingEnrichment, Producer producer, Configuration config, HearingRepository hearingRepository, ServiceConstants serviceConstants, MasterDataUtil helper) {
        this.hearingEnrichment = hearingEnrichment;
        this.producer = producer;
        this.config = config;
        this.hearingRepository = hearingRepository;
        this.serviceConstants = serviceConstants;
        this.helper = helper;
    }


    public List<ScheduleHearing> schedule(ScheduleHearingRequest schedulingRequests) {
        log.info("operation = schedule, result = IN_PROGRESS, ScheduleHearingRequest={}, Hearing={}", schedulingRequests, schedulingRequests.getHearing());

        List<MdmsSlot> defaultSlots = helper.getDataFromMDMS(MdmsSlot.class, serviceConstants.DEFAULT_SLOTTING_MASTER_NAME,serviceConstants.DEFAULT_COURT_MODULE_NAME);

        List<MdmsHearing> defaultHearings = helper.getDataFromMDMS(MdmsHearing.class, serviceConstants.DEFAULT_HEARING_MASTER_NAME,serviceConstants.DEFAULT_COURT_MODULE_NAME);

        Map<String, MdmsHearing> hearingTypeMap = defaultHearings.stream().collect(Collectors.toMap(
                MdmsHearing::getHearingType,
                obj -> obj
        ));

        hearingEnrichment.enrichScheduleHearing(schedulingRequests, defaultSlots, hearingTypeMap);
        log.info("operation = schedule, result = SUCCESS, ScheduleHearingRequest={}, Hearing={}", schedulingRequests, schedulingRequests.getHearing());
        return schedulingRequests.getHearing();
    }

    /**
     * This function update the hearing
     *
     * @param scheduleHearingRequest request object with request info and list of schedule hearing object
     * @return updated hearings with audit details
     */

    // to update the status of existing hearing to reschedule
    public List<ScheduleHearing> update(ScheduleHearingRequest scheduleHearingRequest) {
        log.info("operation = update, result = IN_PROGRESS, ScheduleHearingRequest={}, Hearing={}", scheduleHearingRequest, scheduleHearingRequest.getHearing());


        hearingEnrichment.enrichUpdateScheduleHearing(scheduleHearingRequest.getRequestInfo(), scheduleHearingRequest.getHearing());

        producer.push(config.getScheduleHearingUpdateTopic(), scheduleHearingRequest.getHearing());

        log.info("operation = update, result = SUCCESS, ScheduleHearing={}", scheduleHearingRequest.getHearing());

        return scheduleHearingRequest.getHearing();

    }

    /**
     * This function use to search in the hearing table with different search parameter
     *
     * @param request request object with request info and search criteria for hearings
     * @return list of schedule hearing object
     */

    public List<ScheduleHearing> search(HearingSearchRequest request, Integer limit, Integer offset) {

        return hearingRepository.getHearings(request.getCriteria(), limit, offset);

    }

    /**
     * This function provide the available date for judge and their occupied bandwidth after a start date ( fromDate )
     *
     * @param scheduleHearingSearchCriteria criteria and request info object
     * @return list of availability dto
     */

    public List<AvailabilityDTO> getAvailableDateForHearing(ScheduleHearingSearchCriteria scheduleHearingSearchCriteria) {

        return hearingRepository.getAvailableDatesOfJudges(scheduleHearingSearchCriteria);
    }

    /**
     * This function enrich the audit details as well as timing for hearing in updated date
     *
     * @param request        request object with request info and list of schedule hearings
     * @param defaultSlot    default slots for court
     * @param hearingTypeMap default hearings and their timing
     */

    public List<ScheduleHearing> updateBulk(ScheduleHearingRequest request, List<MdmsSlot> defaultSlot, Map<String, MdmsHearing> hearingTypeMap) {

        hearingEnrichment.enrichBulkReschedule(request, defaultSlot, hearingTypeMap);

        producer.push(config.getScheduleHearingUpdateTopic(), request.getHearing());

        return request.getHearing();
    }
}
