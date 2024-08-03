package digit.web.models;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;




@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BulkReschedulingOfHearings {

    @JsonProperty("judgeId")
    private String judgeId;

    @JsonProperty("startTime")
    private Long startTime;

    @JsonProperty("endTime")
    private Long endTime;

    @JsonProperty("scheduleAfter")
    private Long scheduleAfter;
}
