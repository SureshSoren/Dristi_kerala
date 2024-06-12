package digit.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import digit.config.Configuration;
import digit.repository.ServiceRequestRepository;
import digit.web.models.cases.SearchCaseRequest;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class CaseUtil {

    private final Configuration config;
    private final ObjectMapper mapper;
    private final ServiceRequestRepository requestRepository;

    @Autowired
    public CaseUtil(Configuration config, ObjectMapper mapper, ServiceRequestRepository requestRepository) {
        this.config = config;
        this.mapper = mapper;
        this.requestRepository = requestRepository;
    }

    public JsonNode getCases(SearchCaseRequest searchCaseRequest) {
        log.info("operation = getCases, result = IN_PROGRESS");

        StringBuilder url = new StringBuilder(config.getCaseUrl() + config.getCaseEndpoint());

        Object response = requestRepository.postMethod(url, searchCaseRequest);
        JsonNode caseList;
        try {
            JsonNode jsonNode = mapper.readTree(response.toString());
            caseList = jsonNode.get("cases").get(0).get("responseList");

        } catch (JsonProcessingException e) {
            log.error("operation = getCases, result = FAILURE");
            throw new CustomException("DK_RR_JSON_PROCESSING_ERR", "Invalid Json response");
        }

        log.info("operation = getCases, result = SUCCESS");
        return caseList;

    }


    public JsonNode getLitigants(SearchCaseRequest searchCaseRequest) {

        log.info("operation = getLitigants, result = IN_PROGRESS");

        JsonNode caseList = getCases(searchCaseRequest);

        if (caseList != null && caseList.isArray() && !caseList.isEmpty()) {
            log.info("operation = getLitigants, result = SUCCESS");
            return caseList.get(0).get("litigants");
        } else {
            log.error("operation = getLitigants, result = FAILURE");
            throw new CustomException("DK_RR_CASE_ERR", "case not found");
        }

    }

    public JsonNode getRepresentatives(SearchCaseRequest searchCaseRequest) {

        log.info("operation = getRepresentatives, result = IN_PROGRESS");
        JsonNode caseList = getCases(searchCaseRequest);
        if (caseList != null && caseList.isArray() && !caseList.isEmpty()) {
            log.info("operation = getRepresentatives, result = SUCCESS");
            return caseList.get(0).get("representatives");
        } else {
            log.error("operation = getRepresentatives, result = FAILURE");
            throw new CustomException("DK_RR_CASE_ERR", "case not found");
        }
    }

    public JsonNode getLinkedCases(SearchCaseRequest searchCaseRequest) {
        log.info("operation = getLinkedCases, result = IN_PROGRESS");
        JsonNode caseList = getCases(searchCaseRequest);
        if (caseList != null && caseList.isArray() && !caseList.isEmpty()) {
            log.info("operation = getLinkedCases, result = SUCCESS");
            return caseList.get(0).get("linkedCases");
        } else {
            log.error("operation = getLinkedCases, result = FAILURE");
            throw new CustomException("DK_RR_CASE_ERR", "case not found");
        }
    }


    // to use this method id field should be present in node
    public Set<String> getIdsFromJsonNodeArray(JsonNode nodeArray) {
        log.info("operation = getIdsFromJsonNodeArray, result = IN_PROGRESS");
        Set<String> response = new HashSet<>();
        if (nodeArray != null && nodeArray.isArray()) {
            for (JsonNode node : nodeArray) {
                JsonNode id = node.get("id");
                if (id != null) {
                    response.add(String.valueOf(id.asText()));
                }

            }
        }
        log.info("operation = getIdsFromJsonNodeArray, result = SUCCESS");
        return response;
    }
}
