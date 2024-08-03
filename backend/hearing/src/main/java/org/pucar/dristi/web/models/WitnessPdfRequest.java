package org.pucar.dristi.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WitnessPdfRequest {


    @JsonProperty("RequestInfo")
    private Object requestInfo;

    @JsonProperty("Witness")
    private Witness witness;
}
