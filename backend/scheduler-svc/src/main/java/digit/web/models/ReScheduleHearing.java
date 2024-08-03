package digit.web.models;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import digit.models.coremodels.AuditDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReScheduleHearing {

    @JsonProperty("rescheduledRequestId")
    private String rescheduledRequestId;

    @JsonProperty("hearingBookingId")
    private String hearingBookingId;

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("judgeId")
    private String judgeId;

    @JsonProperty("caseId")
    private String caseId;

    @JsonProperty("requesterId")
    private String requesterId;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("availableAfter")
    private Long availableAfter;

    @JsonProperty("status")
    private Status status;

    @JsonProperty("actionComment")
    private String actionComment;

//    @JsonProperty("workflow")
//    private Workflow workflow;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails;

    @JsonProperty("rowVersion")
    private Integer rowVersion = null;

//    @JsonProperty("documents")
//    @Valid
//    private List<Document> documents = null;

    @JsonProperty("suggestedDates")             // additional details
    private List<Long> suggestedDates;

    @JsonProperty("availableDates")             // additional details
    private List<Long> availableDates;


    @JsonProperty("scheduleDate")
    @JsonFormat(pattern = "yyyy-MM-dd")// additional details
    private Long scheduleDate;


}
