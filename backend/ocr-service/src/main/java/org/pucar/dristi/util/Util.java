package org.pucar.dristi.util;

import org.pucar.dristi.config.ServiceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class Util {

    private static final Logger logger = LoggerFactory.getLogger(Util.class);

    private final RestTemplate restTemplate;

    @Autowired
    public Util(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<Map<String, Object>> callOCR(String url, Resource resource, List<String> wordCheckList,
                                                       Integer distanceCutoff, String docType, Boolean extractData) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        body.add("file", resource);

        if (wordCheckList != null) {
            body.add(ServiceConstants.OCR_REQUEST_PARAMETER_WORDS_CHECK_LIST, wordCheckList);
        }
        if (distanceCutoff != null) {
            body.add(ServiceConstants.OCR_REQUEST_PARAMETER_DISTANCE_CUTOFF, distanceCutoff);
        }
        if (docType != null) {
            body.add(ServiceConstants.OCR_REQUEST_PARAMETER_DOCUMENT_TYPE, docType);
        }
        if (extractData != null) {
            body.add(ServiceConstants.OCR_REQUEST_PARAMETER_EXTRACT_DATA, extractData);
        }

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);


        return restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<Map<String, Object>>() {
                }
        );
    }
}
