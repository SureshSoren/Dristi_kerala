package org.pucar.dristi.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.pucar.dristi.config.Configuration;
import org.pucar.dristi.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CaseUtil {

	private RestTemplate restTemplate;

	private ObjectMapper mapper;

	private Configuration configs;

	@Autowired
	public CaseUtil(RestTemplate restTemplate, Configuration configs, ObjectMapper mapper) {
		this.restTemplate = restTemplate;
		this.configs = configs;
		this.mapper = mapper;
	}

	public Boolean fetchCaseDetails(RequestInfo requestInfo, String cnrNumber, String filingNumber) {
		StringBuilder uri = new StringBuilder();
		uri.append(configs.getCaseHost()).append(configs.getCasePath());

		CaseExistsRequest caseExistsRequest = new CaseExistsRequest();
		caseExistsRequest.setRequestInfo(requestInfo);
		CaseExists caseCriteria = new CaseExists();
		caseCriteria.setCnrNumber(cnrNumber);
		caseCriteria.setFilingNumber(filingNumber);
		List<CaseExists> criteriaList = new ArrayList<>();
		criteriaList.add(caseCriteria);
		caseExistsRequest.setCriteria(criteriaList);

		Object response = new HashMap<>();
		CaseExistsResponse caseResponse = new CaseExistsResponse();
		try {
			response = restTemplate.postForObject(uri.toString(), caseExistsRequest, Map.class);
			caseResponse = mapper.convertValue(response, CaseExistsResponse.class);
		} catch (Exception e) {
			log.error("ERROR_WHILE_FETCHING_FROM_CASE :: {}", e.toString());
		}

		if(caseResponse.getCriteria().isEmpty())
			return false;
		return caseResponse.getCriteria().get(0).getExists();
	}

	public CaseListResponse fetchCaseList(OrderSearchRequest request) {
		StringBuilder uri = new StringBuilder();
		uri.append(configs.getCaseHost()).append(configs.getCaseSearchPath());
		CaseSearchRequest caseSearchRequest = new CaseSearchRequest();
		caseSearchRequest.setRequestInfo(request.getRequestInfo());
		CaseCriteria caseCriteria = new CaseCriteria();
		caseCriteria.setCnrNumber(request.getCriteria().getCnrNumber());
		caseCriteria.setFilingNumber(request.getCriteria().getFilingNumber());
		List<CaseCriteria> criteriaList = new ArrayList<>();
		criteriaList.add(caseCriteria);
		caseSearchRequest.setCriteria(criteriaList);
		CaseListResponse caseListResponse = new CaseListResponse();
		try {
			Object response = restTemplate.postForObject(uri.toString(), caseSearchRequest, Map.class);
			caseListResponse = mapper.convertValue(response, CaseListResponse.class);
		} catch (Exception e) {
			log.error("ERROR_WHILE_FETCHING_FROM_CASE :: {}", e.toString());
		}
		if (caseListResponse.getCriteria().isEmpty()) {
			return null;
		}
		return caseListResponse;
	}
}