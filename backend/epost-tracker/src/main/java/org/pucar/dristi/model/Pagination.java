package org.pucar.dristi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

/**
 * Pagination details
 */
@Schema(description = "Pagination details")
@Validated
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Pagination {
	@JsonProperty("limit")
	@DecimalMax("100")
	private Integer limit = 10;

	@JsonProperty("offSet")
	private Integer offSet = 0;

	@JsonProperty("totalCount")
	private Integer totalCount = null;

	@JsonProperty("sortBy")
	private String sortBy = null;
}
