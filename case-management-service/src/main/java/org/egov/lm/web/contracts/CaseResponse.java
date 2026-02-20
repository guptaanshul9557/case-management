package org.egov.lm.web.contracts;

import java.util.List;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.lm.models.Case;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaseResponse {

	@JsonProperty("ResponseInfo")
	private ResponseInfo responseInfo;

	@JsonProperty("Cases")
	private List<Case> cases;

	@JsonProperty("count")
	private Integer count;

}
