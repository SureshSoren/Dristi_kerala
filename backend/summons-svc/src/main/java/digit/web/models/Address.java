package digit.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address {

    @JsonProperty("state")
    private String state;

    @JsonProperty("city")
    private String city;

    @JsonProperty("district")
    private String district;

    @JsonProperty("pincode")
    private String pinCode;

    @JsonProperty("locality")
    private String locality;

    @JsonProperty("coordinate")
    private Coordinate coordinate;

}
