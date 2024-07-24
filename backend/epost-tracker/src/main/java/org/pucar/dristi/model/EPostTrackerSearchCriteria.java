package org.pucar.dristi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Validated
//@jakarta.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2024-04-18T11:14:50.003326400+05:30[Asia/Calcutta]")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EPostTrackerSearchCriteria {

    @JsonProperty("processNumber")
    private String processNumber;

    @JsonProperty("trackingNumber")
    private  String trackingNumber;

    @JsonProperty("deliveryStatus")
    private String deliveryStatus;

    @JsonProperty("bookingDate")
    private String bookingDate;

    @JsonProperty("receivedDate")
    private  String receivedDate;
}
