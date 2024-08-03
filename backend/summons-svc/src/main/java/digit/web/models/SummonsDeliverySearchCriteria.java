package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class SummonsDeliverySearchCriteria {

    @JsonProperty("orderId")
    private String orderId = null;

    @JsonProperty("summonsId")
    private String summonsId = null;
}