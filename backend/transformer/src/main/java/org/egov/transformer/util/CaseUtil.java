package org.egov.transformer.util;

import org.egov.transformer.config.TransformerProperties;
import org.egov.transformer.models.CaseCriteria;
import org.egov.transformer.models.CaseListResponse;
import org.egov.transformer.models.CaseSearchRequest;
import org.egov.transformer.models.CourtCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class CaseUtil {

    private final TransformerProperties properties;
    private final RestTemplate restTemplate;
    @Autowired
    public CaseUtil(TransformerProperties properties, RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    public String getFullName(String firstName, String middleName, String lastName) {
        if (firstName == null) {
            throw new IllegalArgumentException("First name cannot be null");
        }

        StringBuilder fullName = new StringBuilder(firstName);

        if (middleName != null && !middleName.isEmpty()) {
            fullName.append(" ").append(middleName);
        }

        if (lastName != null && !lastName.isEmpty()) {
            fullName.append(" ").append(lastName);
        }

        return fullName.toString();
    }

    public CourtCase fetchCase(String filingNumber)
    {StringBuilder uri = new StringBuilder();
        uri.append(properties.getCaseHostTopic()).append(properties.getCasePathTopic());
        CaseCriteria caseCriteria = new CaseCriteria();
        caseCriteria.setFilingNumber(filingNumber);
        List<CaseCriteria>caseCriteriaList = new ArrayList<>();
        caseCriteriaList.add(caseCriteria);
        CaseSearchRequest caseSearchRequest = new CaseSearchRequest();
        caseSearchRequest.setCriteria(caseCriteriaList);

        CaseListResponse caseListResponse = restTemplate.postForObject(uri.toString(), caseSearchRequest, CaseListResponse.class);

        CaseCriteria criteria = caseListResponse.getCriteria().get(0);
        return criteria.getResponseList().get(0);


    }

}
