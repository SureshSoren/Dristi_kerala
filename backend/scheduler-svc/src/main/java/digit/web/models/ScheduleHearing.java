package digit.web.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import digit.models.coremodels.AuditDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.tracer.model.Error;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScheduleHearing {

    @JsonProperty("hearingBookingId")
    private String hearingBookingId;      // hearing id d

    @JsonProperty("tenantId")
    private String tenantId;                // tenant id d

    @JsonProperty("courtId")
    private String courtId;                 //prescribed by then court id  d

    @JsonProperty("judgeId")
    private String judgeId;                 // judge id  a

    @JsonProperty("caseId")
    private String caseId;                  // cnr number  a

//    @JsonProperty("date")
//    @JsonFormat(pattern = "yyyy-MM-dd")   ///start time to epoch  d
//    private LocalDate date;

    //TODO: this should be enum
    @JsonProperty("eventType")
    private String eventType;    // hearing type     d

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    //TODO: this should be enum
    @JsonProperty("status")
    private String status;       //  d

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
    public ScheduleHearing(ScheduleHearing hearingObject) {
        this.hearingBookingId = hearingObject.hearingBookingId;
        this.tenantId = hearingObject.tenantId;
        this.courtId = hearingObject.courtId;
        this.judgeId = hearingObject.judgeId;
        this.caseId = hearingObject.caseId;
        this.eventType = hearingObject.eventType;
        this.title = hearingObject.title;
        this.description = hearingObject.description;
        this.status = hearingObject.status;
        this.startTime = hearingObject.startTime;
        this.endTime = hearingObject.endTime;
        this.auditDetails = hearingObject.auditDetails;
        this.rowVersion = hearingObject.rowVersion;
        this.errors = hearingObject.errors;
        this.rescheduleRequestId = hearingObject.rescheduleRequestId;
        this.hearingTimeInMinutes = hearingObject.hearingTimeInMinutes;
    }

    public boolean overlapsWith(ScheduleHearing other) {
        return !(startTime >= other.endTime || endTime <= other.startTime);

    }

    public boolean isBefore(long time1, long time2) {
        return time1 < time2;
    }

    public boolean isAfter(long time1, long time2) {
        return time1 > time2;
    }

}
