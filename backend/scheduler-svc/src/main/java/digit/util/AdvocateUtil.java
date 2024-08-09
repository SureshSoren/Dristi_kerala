package digit.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import digit.config.Configuration;
import digit.repository.ServiceRequestRepository;
import digit.web.models.AdvocateSearchCriteria;
import digit.web.models.AdvocateSearchRequest;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class AdvocateUtil {

    private final ServiceRequestRepository repository;
    private final ObjectMapper mapper;
    private final Configuration config;
    @Autowired
    public AdvocateUtil(ServiceRequestRepository repository, ObjectMapper mapper, Configuration config) {
        this.repository = repository;
        this.mapper = mapper;
        this.config = config;
    }

    public Set<String> fetchAdvocateDetails(RequestInfo requestInfo, Set<String> representativeIds) {
        List<AdvocateSearchCriteria> criteria = new ArrayList<>();
        for(String id : representativeIds) {
            AdvocateSearchCriteria advocateSearchCriteria = new AdvocateSearchCriteria();
            advocateSearchCriteria.setId(id);
            criteria.add(advocateSearchCriteria);
        }

        AdvocateSearchRequest searchRequest = AdvocateSearchRequest.builder()
                .requestInfo(requestInfo)
                .tenantId(config.getEgovStateTenantId())
                .criteria(criteria)
                .build();
        StringBuilder uri = new StringBuilder(config.getAdvocateHost() + config.getAdvocateSearchEndPoint());

        Object response = repository.fetchResult(uri, searchRequest);
        JsonNode advocateDetails = null;
        try {
            JsonNode res = mapper.readTree(response.toString());
            advocateDetails = res.get("criteria").get(0).get("responseList");
        } catch (JsonProcessingException e) {
            log.error("Error while fetching advocate details");
            throw new CustomException("DK_RR_JSON_PROCESSING_ERR", "Invalid Json response");
        }

        Set<String> advocateIds = new HashSet<>();
        for(JsonNode node : advocateDetails) {
            advocateIds.add(node.get("individualId").toString());
        }
        return advocateIds;
    }
}
