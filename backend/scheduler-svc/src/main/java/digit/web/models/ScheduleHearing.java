package digit.web.models;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import digit.models.coremodels.AuditDetails;
import digit.web.models.enums.EventType;
import digit.web.models.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.tracer.model.Error;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScheduleHearing {

    @JsonProperty("hearingBookingId")
    private String hearingBookingId;      // hearing id

    @JsonProperty("tenantId")
    private String tenantId;                // tenant id

    @JsonProperty("courtId")
    private String courtId;                 //prescribed by then court id

    @JsonProperty("judgeId")
    private String judgeId;                 // judge id

    @JsonProperty("caseId")
    private String caseId;                  // cnr number

    @JsonProperty("date")
    @JsonFormat(pattern = "yyyy-MM-dd")   ///start time to epoch
    private LocalDate date;

    //TODO: this should be enum
    @JsonProperty("eventType")
    private EventType eventType;    // hearing type

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    //TODO: this should be enum
    @JsonProperty("status")
    private Status status;

    @JsonProperty("startTime")
    private long startTime;

    @JsonProperty("endTime")
    private long endTime;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails;

    @JsonProperty("rowVersion")
    private Integer rowVersion = null;

    @JsonProperty("error")
    @JsonIgnore
    private Error errors = null;

    @JsonProperty("rescheduleRequestId")
    private String rescheduleRequestId = null;

    @JsonProperty("hearingTimeInMinutes")
    private Integer hearingTimeInMinutes = null;

    //  copy constructor
    public ScheduleHearing(ScheduleHearing another) {
        this.hearingBookingId = another.hearingBookingId;
        this.tenantId = another.tenantId;
        this.courtId = another.courtId;
        this.judgeId = another.judgeId;
        this.caseId = another.caseId;
        this.date = another.date;
        this.eventType = another.eventType;
        this.title = another.title;
        this.description = another.description;
        this.status = another.status;
        this.startTime = another.startTime;
        this.endTime = another.endTime;
        this.auditDetails = another.auditDetails;
        this.rowVersion = another.rowVersion;
        this.errors = another.errors;
        this.rescheduleRequestId = another.rescheduleRequestId;
        this.hearingTimeInMinutes = another.hearingTimeInMinutes;
    }

    public boolean overlapsWith(ScheduleHearing other) {
        return !(startTime >= other.endTime || endTime <= other.startTime);

    }
}
