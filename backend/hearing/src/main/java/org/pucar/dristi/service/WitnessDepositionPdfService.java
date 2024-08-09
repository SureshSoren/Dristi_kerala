package org.pucar.dristi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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

    public MultipartFile getWitnessDepositionPdf(HearingSearchRequest searchRequest) {
        HearingCriteria criteria = searchRequest.getCriteria();
        Pagination pagination = Pagination.builder().limit(1D).offSet(0D).build();
        HearingSearchRequest hearingSearchRequest = HearingSearchRequest.builder()
                .criteria(criteria).pagination(pagination).build();
        Optional<Hearing> optionalHearing = hearingRepository.getHearings(hearingSearchRequest).stream().findFirst();
        if (optionalHearing.isPresent()) {
            List<WitnessDeposition> witnessDepositions = createWitnessObjects(searchRequest.getRequestInfo(), optionalHearing.get());
            WitnessPdfRequest witnessPdfRequest = WitnessPdfRequest.builder()
                    .witnessDepositions(witnessDepositions).requestInfo(searchRequest.getRequestInfo()).build();
            return pdfRequestUtil.createPdfForWitness(witnessPdfRequest, searchRequest.getRequestInfo().getUserInfo().getTenantId());
        } else {
            log.error("Provided Pdf Request details: {} are not valid", searchRequest);
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



    private List<WitnessDeposition> createWitnessObjects(RequestInfo requestInfo, Hearing hearing) {
        JsonNode additionalDetailsNode = extractAdditionalDetails(hearing);

        CaseSearchRequest caseSearchRequest = createCaseSearchRequest(requestInfo, hearing);
        JsonNode caseDetails = caseUtil.searchCaseDetails(caseSearchRequest);

        List<WitnessDeposition> witnessDepositions = new ArrayList<>();

        if (additionalDetailsNode == null || !additionalDetailsNode.has("witnessDepositions")) {
            return witnessDepositions;
        }

        JsonNode witnessDepositionsNode = additionalDetailsNode.get("witnessDepositions");
        if (witnessDepositionsNode.isEmpty()) {
            return witnessDepositions;
        }

        String filingNumber = getFilingNumber(caseDetails);
        String caseYear = extractCaseYear(filingNumber);
        String courtCaseNumber = extractCaseNumber(filingNumber);

        List<String> witnessUuids = new ArrayList<>();
        for (JsonNode witnessDepositionNode : witnessDepositionsNode) {
            witnessUuids.add(witnessDepositionNode.path("uuid").asText());
        }

        List<Individual> individuals = individualService.getIndividuals(requestInfo, witnessUuids);

        Map<String, Individual> individualMap = individuals.stream()
                .collect(Collectors.toMap(Individual::getUserUuid, individual -> individual));

        for (JsonNode witnessDepositionNode : witnessDepositionsNode) {
            String witnessUuid = witnessDepositionNode.path("uuid").asText();
            Individual individual = individualMap.get(witnessUuid);

            if (individual != null) {
                WitnessDeposition witnessDeposition = buildWitness(individual, caseDetails, hearing, caseYear, courtCaseNumber, witnessDepositionNode);
                witnessDepositions.add(witnessDeposition);
            } else {
                log.error("Individual not found for UUID: {}", witnessUuid);
            }
        }

        return witnessDepositions;
    }

    private JsonNode extractAdditionalDetails(Hearing hearing) {
        return (ObjectNode) hearing.getAdditionalDetails();
    }

    private String getFilingNumber(JsonNode caseDetails) {
        return caseDetails.has("filingNumber") ? caseDetails.get("filingNumber").asText() : "";
    }

    private WitnessDeposition buildWitness(Individual individual, JsonNode caseDetails, Hearing hearing,
                                           String caseYear, String courtCaseNumber, JsonNode witnessDepositionNode) {
        return WitnessDeposition.builder()
                .hearingId(hearing.getHearingId())
                .caseName(caseDetails.get("caseTitle").asText())
                .courtId(caseDetails.get("courtId").asText())
                .filingNumber(caseDetails.get("filingNumber").asText())
                .caseYear(caseYear)
                .caseNumber(courtCaseNumber)
                .name(individual.getName().getGivenName() + " " + individual.getName().getFamilyName())
                .mobileNumber(individual.getMobileNumber())
                .deposition(witnessDepositionNode.path("deposition").asText())
                .fatherName(individual.getFatherName())
                .age(individual.getDateOfBirth() != null ? calculateAge(individual.getDateOfBirth()) : null)
                .gender(individual.getGender() != null ? individual.getGender() : null)
                .hearingDate(formatDateFromMillis(hearing.getEndTime()))
                .build();
    }

    private Integer calculateAge(Date dateOfBirth) {
        if (dateOfBirth == null) {
            return null;
        }
        LocalDate birthDate = dateOfBirth.toInstant().atZone(ZoneId.of("Asia/Kolkata")).toLocalDate();
        Period period = Period.between(birthDate, LocalDate.now());
        return period.getYears();
    }

    private String extractCaseNumber(String input) {
        if (input == null) {
            return "";
        }
        String regex = "-(\\d+)$";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            String numberStr = matcher.group(1);
            return numberStr.replaceFirst("^0+(?!$)", "");
        } else {
            return  "";
        }
    }

    public static String extractCaseYear(String input) {
        if (input == null) {
            return "";
        }
        String regex = "-(\\d{4})-";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }

    private String formatDateFromMillis(long millis) {
        try {
            ZonedDateTime dateTime = Instant.ofEpochMilli(millis)
                    .atZone(ZoneId.of("Asia/Kolkata"));

            String day = String.valueOf(dateTime.getDayOfMonth());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);
            String formattedMonthYear = dateTime.format(formatter);

            return String.format("%s day of %s", day, formattedMonthYear);
        } catch (Exception e) {
            log.error("Failed to get Date in String format from : {}", millis);
            return "";
        }
    }
}