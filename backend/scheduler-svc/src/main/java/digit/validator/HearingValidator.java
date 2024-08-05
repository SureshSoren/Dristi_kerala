package digit.validator;


import com.fasterxml.jackson.databind.ObjectMapper;
import digit.config.Configuration;
import digit.repository.HearingRepository;
import digit.repository.ServiceRequestRepository;
import digit.web.models.*;
import digit.web.models.cases.CaseCriteria;
import digit.web.models.cases.SearchCaseRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class HearingValidator {

    @Autowired
    private HearingRepository repository;

    @Autowired
    Configuration config;

    @Autowired
    ServiceRequestRepository requestRepository;

    @Autowired
    private ObjectMapper mapper;

    public void validateHearing(ScheduleHearingRequest schedulingRequests, double totalHrs, Map<String, MdmsHearing> hearingTypeMap) {

//        schedulingRequests.getHearing().forEach(application -> {
//            if (ObjectUtils.isEmpty(application.getTenantId()))
//                throw new CustomException("DK_SH_APP_ERR", "tenantId is mandatory for schedule a hearing");

//            if (ObjectUtils.isEmpty(application.getEventType()))
//                throw new CustomException("DK_SH_APP_ERR", "Event type is mandatory for schedule a hearing");
//
//            if (ObjectUtils.isEmpty(application.getDate())) {
//                throw new CustomException("DK_SH_APP_ERR", "date is mandatory for schedule a hearing");
//            } else {
//                LocalDate date = application.getDate();
//                if (date.isBefore(LocalDate.now())) {
//                    throw new CustomException("DK_SH_APP_ERR", "cannot schedule a hearing for past date: " + date);
//
//                }
//            }
//            StringBuilder url = new StringBuilder(config.getCaseUrl() + config.getCaseEndpoint());
//            SearchCaseRequest caseSearchCriteria = SearchCaseRequest.builder().RequestInfo(schedulingRequests.getRequestInfo()).tenantId(config.getEgovStateTenantId()).criteria(Collections.singletonList(CaseCriteria.builder().caseId(application.getCaseId()).build())).build();
//            Object response = requestRepository.postMethod(url, caseSearchCriteria);
//
//        });
//
//        verifyHearingDates(schedulingRequests.getHearing(), totalHrs, hearingTypeMap);
    }

    private void verifyHearingDates(List<ScheduleHearing> hearingRequest, Double judgeBandwidth, Map<String, MdmsHearing> hearingTypeMap) {
        HashMap<String, Double> map = new HashMap<>(); // to avoid extra call to db

        for (ScheduleHearing hearing : hearingRequest) {

            Double requiredSlot = null;

            String eventType = "Eventype";

            ScheduleHearingSearchCriteria searchCriteria = ScheduleHearingSearchCriteria.builder()
                    .judgeId(hearing.getJudgeId()).build();

            if (hearingTypeMap.containsKey(eventType)) {
                MdmsHearing hearingType = hearingTypeMap.get(eventType);
                requiredSlot = hearingType.getHearingTime() / 60.00;


            } else {
                throw new CustomException("DK_SH_APP_ERR", "this hearing type does not exist in master data");
            }


            StringBuilder key = new StringBuilder();
            key.append(hearing.getJudgeId()).append("-");
            Double occupiedBandwidthOfJudgeForDay;

            if (map.containsKey(key.toString())) {
                occupiedBandwidthOfJudgeForDay = map.get(key.toString());
            } else {
                List<AvailabilityDTO> judgeHearing = repository.getAvailableDatesOfJudges(searchCriteria);
                if (!judgeHearing.isEmpty())
                    occupiedBandwidthOfJudgeForDay = judgeHearing.get(0).getOccupiedBandwidth();
                else occupiedBandwidthOfJudgeForDay = 0.0;
            }


            if ((judgeBandwidth - occupiedBandwidthOfJudgeForDay) >= requiredSlot) {
                // temporary booked the slot
                map.put(key.toString(), occupiedBandwidthOfJudgeForDay + requiredSlot);

            } else {
                throw new CustomException("DK_SH_APP_ERR", "cannot schedule hearing for given date");
            }


        }


    }

}

