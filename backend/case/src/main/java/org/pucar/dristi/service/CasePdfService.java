package org.pucar.dristi.service;


import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.config.Configuration;
import org.pucar.dristi.util.CasePdfUtil;
import org.pucar.dristi.web.models.CaseRequest;
import org.pucar.dristi.web.models.CaseSearchRequest;
import org.pucar.dristi.web.models.CourtCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static org.pucar.dristi.config.ServiceConstants.CASE_PDF_SERVICE_EXCEPTION;

@Service
@Slf4j
public class CasePdfService {

    private final Configuration config;

    private final CasePdfUtil casePdfUtil;

    @Autowired
    public CasePdfService(Configuration config, CasePdfUtil casePdfUtil) {
        this.config = config;
        this.casePdfUtil = casePdfUtil;
    }

    public MultipartFile generatePdf(CaseSearchRequest body) {
        try {
            CourtCase courtCase = body.getCriteria().get(0).getResponseList().get(0);
            RequestInfo requestInfo = body.getRequestInfo();
            CaseRequest caseRequest = CaseRequest.builder().requestInfo(requestInfo).cases(courtCase).build();
            StringBuilder uri = new StringBuilder(config.getDristiCasePdfHost()).append(config.getDristiCasePdfPath());
            return casePdfUtil.generateCasePdf(caseRequest, uri);
        } catch (Exception e) {
            log.error("Error generating PDF for case, {}", e.getMessage());
            throw new CustomException(CASE_PDF_SERVICE_EXCEPTION, "Error generating PDF for case " + e.getMessage());
        }
    }
}
