package org.egov.transformer.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * CaseCriteria
 */
@Validated
@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-04-15T11:31:40.281899+05:30[Asia/Kolkata]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaseCriteria {
    @JsonProperty("caseId")
    private String caseId = null;

    @JsonProperty("defaultFields")
    private Boolean defaultFields = false;

    @JsonProperty("cnrNumber")
    private String cnrNumber = null;

    @JsonProperty("filingNumber")
    private String filingNumber = null;

    @JsonProperty("courtCaseNumber")
    private String courtCaseNumber = null;

    @JsonProperty("filingFromDate")
    @Valid
    private Long filingFromDate = null;

    @JsonProperty("filingToDate")
    @Valid
    private Long filingToDate = null;

    @JsonProperty("registrationFromDate")
    @Valid
    private Long registrationFromDate = null;

    @JsonProperty("registrationToDate")
    @Valid
    private Long registrationToDate = null;
    //todo judgeid, stage, substage

    @JsonProperty("judgeId")
    private String judgeId = null;

    @JsonProperty("stage")
    private String stage = null;

    @JsonProperty("substage")
    private String substage = null;

    @JsonProperty("litigantId")
    @Valid
    private String litigantId = null;

    @JsonProperty("advocateId")
    @Valid
    private String advocateId = null;

    @JsonProperty("status")
    @Valid
    private String status = null;

    @JsonProperty("responseList")
    @Valid
    private List<CourtCase> responseList = null;


    @JsonProperty("pagination")

    @Valid
    private Pagination pagination = null;



    public CaseCriteria addResponseListItem(CourtCase responseListItem) {
        if (this.responseList == null) {
            this.responseList = new ArrayList<>();
        }
        this.responseList.add(responseListItem);
        return this;
    }

}
