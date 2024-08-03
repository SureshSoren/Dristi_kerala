package org.pucar.dristi.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.models.individual.Individual;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.repository.HearingRepository;
import org.pucar.dristi.util.CaseUtil;
import org.pucar.dristi.util.PdfRequestUtil;
import org.pucar.dristi.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;

@Service
@Slf4j
public class WitnessDepositionPdfService {

    private final HearingRepository hearingRepository;
    private final IndividualService individualService;
    private final CaseUtil caseUtil;
    private final PdfRequestUtil pdfRequestUtil;

    @Autowired
    public WitnessDepositionPdfService(HearingRepository hearingRepository, IndividualService individualService, CaseUtil caseUtil, PdfRequestUtil pdfRequestUtil) {
        this.hearingRepository = hearingRepository;
        this.individualService = individualService;
        this.caseUtil = caseUtil;
        this.pdfRequestUtil = pdfRequestUtil;
    }

    public MultipartFile getWitnessDepositionPdf(PdfRequest pdfRequest) {
        HearingCriteria criteria = HearingCriteria.builder().hearingId(pdfRequest.getHearingId()).build();
        Pagination pagination = Pagination.builder().limit(1D).offSet(0D).build();
        HearingSearchRequest hearingSearchRequest = HearingSearchRequest.builder()
                .criteria(criteria).pagination(pagination).build();
        Optional<Hearing> optionalHearing = hearingRepository.getHearings(hearingSearchRequest).stream().findFirst();
        if (optionalHearing.isPresent()) {
            Individual individual = individualService.getIndividual(pdfRequest.getRequestInfo(), pdfRequest.getWitnessUuid());
            Witness witness = createWitnessObject(pdfRequest.getRequestInfo(), optionalHearing.get(), individual);
            log.info("witness: {}", witness);
            WitnessPdfRequest witnessPdfRequest = WitnessPdfRequest.builder()
                    .witness(witness).requestInfo(pdfRequest.getRequestInfo()).build();
            return pdfRequestUtil.createPdfForWitness(witnessPdfRequest, pdfRequest.getTenantId());
        } else {
            log.error("Provided Pdf Request details: {} are not valid", pdfRequest);
            throw new CustomException("VALIDATION_EXCEPTION", "Provided Pdf Request details are not valid");
        }
    }

    public CaseSearchRequest createCaseSearchRequest(RequestInfo requestInfo, Hearing hearing) {
        CaseSearchRequest caseSearchRequest = new CaseSearchRequest();
        caseSearchRequest.setRequestInfo(requestInfo);
        CaseCriteria caseCriteria = CaseCriteria.builder().filingNumber(hearing.getFilingNumber().get(0)).build();
        caseSearchRequest.addCriteriaItem(caseCriteria);
        return caseSearchRequest;
    }

    private Witness createWitnessObject(RequestInfo requestInfo, Hearing hearing, Individual individual) {
        Object object = hearing.getAdditionalDetails();
        CaseSearchRequest caseSearchRequest = createCaseSearchRequest(requestInfo,hearing);
        JsonNode jsonNode = caseUtil.searchCaseDetails(caseSearchRequest);
        if (object instanceof Map) {
            Map<String, Object> additionalDetails = (Map<String, Object>) object;
            if (additionalDetails.containsKey("witnessDepositions")) {
                List<Map<String, Object>> witnessDepositions = (List<Map<String, Object>>) additionalDetails.get("witnessDepositions");
                if (!witnessDepositions.isEmpty()) {
                    String[] parts = jsonNode.get("filingNumber").asText().split("-");
                    String caseYear = parts[3];
                    String courtCaseNumber = parts[4];
                    Map<String, Object> witnessDeposition = witnessDepositions.get(0);
                    return Witness.builder()
                            .caseName(jsonNode.get("caseTitle").asText())
                            .courtName(jsonNode.get("courtId").asText())
                            .filingNumber(jsonNode.get("filingNumber").asText())
                            .caseYear(caseYear)
                            .courtCaseNumber(courtCaseNumber)
                            .name(individual.getName().getGivenName() + " " + individual.getName().getFamilyName())
                            .mobileNumber(individual.getMobileNumber())
                            .deposition((String) witnessDeposition.get("deposition"))
                            .userUuid(individual.getUserUuid())
                            .userId(individual.getUserId())
                            .fatherName(individual.getFatherName())
                            .age(individual.getDateOfBirth() != null ? calculateAge(individual.getDateOfBirth()) : null)
                            .gender(individual.getGender() != null ? individual.getGender() : null)
                            .build();
                }
            }
        }
        return null;
    }

    private Integer calculateAge(Date dateOfBirth) {
        LocalDate birthDate = dateOfBirth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}