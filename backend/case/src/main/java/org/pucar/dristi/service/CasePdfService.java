package org.pucar.dristi.service;


import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.models.Document;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.config.Configuration;
import org.pucar.dristi.repository.CaseRepository;
import org.pucar.dristi.util.CasePdfUtil;
import org.pucar.dristi.util.FileStoreUtil;
import org.pucar.dristi.web.models.CaseRequest;
import org.pucar.dristi.web.models.CaseSearchRequest;
import org.pucar.dristi.web.models.CourtCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.CollectionUtils;

import java.util.Collections;

import static org.pucar.dristi.config.ServiceConstants.CASE_PDF_SERVICE_EXCEPTION;

@Service
@Slf4j
public class CasePdfService {

    private final Configuration config;

    private final CasePdfUtil casePdfUtil;

    private final CaseRepository caseRepository;

    private final FileStoreUtil fileStoreUtil;

    @Autowired
    public CasePdfService(Configuration config, CasePdfUtil casePdfUtil, CaseRepository caseRepository, FileStoreUtil fileStoreUtil) {
        this.config = config;
        this.casePdfUtil = casePdfUtil;
        this.caseRepository = caseRepository;
        this.fileStoreUtil = fileStoreUtil;
    }

    public CourtCase generatePdf(CaseSearchRequest body) {
        try {
            caseRepository.getApplications(body.getCriteria(), body.getRequestInfo());
            CourtCase courtCase = body.getCriteria().get(0).getResponseList().get(0);
            RequestInfo requestInfo = body.getRequestInfo();
            CaseRequest caseRequest = CaseRequest.builder().requestInfo(requestInfo).cases(courtCase).build();
            StringBuilder uri = new StringBuilder(config.getDristiCasePdfHost()).append(config.getDristiCasePdfPath());
            ByteArrayResource byteArrayResource = casePdfUtil.generateCasePdf(caseRequest, uri);
            Document document = fileStoreUtil.saveDocumentToFileStore(byteArrayResource.getByteArray(), courtCase.getTenantId());
            if (CollectionUtils.isEmpty(courtCase.getDocuments())) {
                courtCase.setDocuments(Collections.singletonList(document));
            } else {
                courtCase.getDocuments().add(document);
            }
            return courtCase;
        } catch (Exception e) {
            log.error("Error generating PDF for case, {}", e.getMessage());
            throw new CustomException(CASE_PDF_SERVICE_EXCEPTION, "Error generating PDF for case " + e.getMessage());
        }
    }
}
