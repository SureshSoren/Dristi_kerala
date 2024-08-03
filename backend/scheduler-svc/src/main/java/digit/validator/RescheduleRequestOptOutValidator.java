package digit.validator;


import com.fasterxml.jackson.databind.JsonNode;
import digit.config.Configuration;
import digit.repository.RescheduleRequestOptOutRepository;
import digit.service.ReScheduleHearingService;
import digit.util.CaseUtil;
import digit.web.models.*;
import digit.web.models.cases.CaseCriteria;
import digit.web.models.cases.SearchCaseRequest;
import digit.web.models.enums.Status;
import org.apache.commons.lang3.ObjectUtils;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class RescheduleRequestOptOutValidator {


    private final RescheduleRequestOptOutRepository repository;

    private final ReScheduleHearingService reScheduleHearingService;

    private final Configuration config;

    private final CaseUtil caseUtil;

    @Autowired
    public RescheduleRequestOptOutValidator(RescheduleRequestOptOutRepository repository, ReScheduleHearingService reScheduleHearingService, Configuration config, CaseUtil caseUtil) {
        this.repository = repository;
        this.reScheduleHearingService = reScheduleHearingService;
        this.config = config;
        this.caseUtil = caseUtil;
    }


    public void validateRequest(OptOutRequest request) {

        request.getOptOuts().forEach(application -> {
            if (ObjectUtils.isEmpty(application.getTenantId()))
                throw new CustomException("DK_OO_APP_ERR", "tenantId is mandatory for opt out dates");
            if (ObjectUtils.isEmpty(application.getIndividualId()))
                throw new CustomException("DK_OO_APP_ERR", "individual id is mandatory for opt out dates");
            if (ObjectUtils.isEmpty(application.getRescheduleRequestId()))
                throw new CustomException("DK_OO_APP_ERR", "reschedule request id is mandatory for opt out dates");
            //number of opt out validation
            if (application.getOptoutDates().size() > config.getOptOutLimit()) {
                throw new CustomException("DK_OO_SELECTION_LIMIT_ERR", "you are eligible to opt out " + config.getOptOutLimit() + " only");
            }
            // already opt out check
            OptOutSearchCriteria optOutSearchCriteria = OptOutSearchCriteria.builder().individualId(application.getIndividualId()).rescheduleRequestId(application.getRescheduleRequestId()).build();
            List<OptOut> optOuts = repository.getOptOut(optOutSearchCriteria, null, null);
            if (!optOuts.isEmpty()) {
                throw new CustomException("DK_OO_APP_ERR", "Opt out request already exists.");
            }
        });

        //case service validation
        request.getOptOuts().forEach((application) -> {
            //case validation
            SearchCaseRequest searchCaseRequest = SearchCaseRequest.builder().RequestInfo(request.getRequestInfo()).tenantId(config.getEgovStateTenantId()).criteria(Collections.singletonList(CaseCriteria.builder().caseId(application.getCaseId()).build())).build();
            JsonNode representatives = caseUtil.getRepresentatives(searchCaseRequest);
            Set<String> representativeIds = caseUtil.getIdsFromJsonNodeArray(representatives);

            if (!representativeIds.contains(application.getIndividualId())) {
                throw new CustomException("DK_OO_APP_ERR", "Invalid individualId.");
            }
        });

        // validate reschedule request exist in db
        List<String> ids = request.getOptOuts().stream().map((OptOut::getRescheduleRequestId)).toList();

        List<ReScheduleHearing> search = reScheduleHearingService.search(ReScheduleHearingReqSearchRequest.builder()
                .requestInfo(request.getRequestInfo())
                .criteria(ReScheduleHearingReqSearchCriteria.builder().rescheduledRequestId(ids).build()).build(), null, null);

        if (ids.size() != search.size()) {
            throw new CustomException("DK_OO_APP_ERR", "Reschedule request does not exist in database");
        }
        Map<String, List<Long>> resultMap = new HashMap<>();
        //check weather request is approved or not
        search.forEach((element) -> {
            if (!element.getStatus().equals(Status.APPROVED)) {
                throw new CustomException("DK_OO_APP_ERR", "Opt-Out is not enable for reschedule request : " + element.getRescheduledRequestId());
            }
            resultMap.put(element.getRescheduledRequestId(), element.getSuggestedDates());
        });


        //opt out date validation
        for (OptOut optOut : request.getOptOuts()) {

            Set<LocalDate> optoutDates = optOut.getOptoutDates().stream().map(LocalDate::from).collect(Collectors.toSet());

            if(optoutDates.size() > config.getOptOutLimit()){
                throw new CustomException("DK_OO_SELECTION_LIMIT_ERR", "you are eligible to opt out " + config.getOptOutLimit() + "dates only");
            }

            String rescheduleRequestId = optOut.getRescheduleRequestId();

            if (resultMap.containsKey(rescheduleRequestId)) {
                resultMap.get(rescheduleRequestId)
                        .forEach(optoutDates::remove);
            }

            if (!optoutDates.isEmpty()) {
                throw new CustomException("DK_OO_APP_ERR", "opt out dates must be from suggested days");

            }

        }


    }

    public void validateUpdateRequest(OptOutRequest request) {


    }
}
