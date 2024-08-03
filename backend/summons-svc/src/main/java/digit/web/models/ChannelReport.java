package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChannelReport {

    @JsonProperty("summonId")
    private String summonId;

    @JsonProperty("deliveryStatus")
    private String deliveryStatus;

    @JsonProperty("additionalFields")
    private AdditionalFields additionalFields;
}
