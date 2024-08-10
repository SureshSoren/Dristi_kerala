package org.pucar.dristi.util;


import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.repository.ServiceRequestRepository;
import org.pucar.dristi.web.models.CaseRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import static org.pucar.dristi.config.ServiceConstants.CASE_PDF_UTILITY_EXCEPTION;

@Component
@Slf4j
public class CasePdfUtil {

    private final ServiceRequestRepository serviceRequestRepository;

    @Autowired
    public CasePdfUtil(ServiceRequestRepository serviceRequestRepository) {
        this.serviceRequestRepository = serviceRequestRepository;
    }

    public MultipartFile generateCasePdf(CaseRequest caseRequest, StringBuilder uri) {
        try {
            Object response = serviceRequestRepository.fetchResult(uri, caseRequest);
            if (response instanceof MultipartFile) {
                return (MultipartFile) response;
            } else {
                throw new CustomException(CASE_PDF_UTILITY_EXCEPTION, "Failed to get valid file type: " + response.getClass());
            }
        } catch (Exception e) {
            log.error("Error generating PDF for case {}: {}", caseRequest, e.getMessage());
            throw new CustomException(CASE_PDF_UTILITY_EXCEPTION, "Error generating PDF for case: " + e.getMessage());
        }
    }
}
