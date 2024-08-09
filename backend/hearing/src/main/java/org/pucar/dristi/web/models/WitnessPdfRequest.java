package org.pucar.dristi.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WitnessPdfRequest {


    @JsonProperty("RequestInfo")
    private Object requestInfo;

    @JsonProperty("WitnessDepositions")
    private List<WitnessDeposition> witnessDepositions;
}
