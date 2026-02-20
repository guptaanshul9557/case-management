package org.egov.lm.models;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.egov.lm.models.enums.Status;
import org.egov.lm.models.workflow.ProcessInstance;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Case {

	/* -------------------- Mandatory Identifiers -------------------- */
	@JsonProperty("id")
	private String id;
	
	@JsonProperty("caseId")
	private String caseId;

	@JsonProperty("tenantId")
	@NotBlank(message = "tenantId is mandatory for update")
	private String tenantId;

	/* -------------------- Updatable Case Attributes -------------------- */

	@JsonProperty("caseType")
	private String caseType;

	@JsonProperty("caseCategory")
	private String caseCategory;

	@JsonProperty("title")
	private String title;

	@JsonProperty("description")
	private String description;

	@JsonProperty("department")
	private String department;

	@JsonProperty("status")
	private Status status;

	/* -------------------- Parties -------------------- */

	@JsonProperty("advocates")
	@Valid
	private List<Advocate> advocates;

	@JsonProperty("petitioners")
	@Valid
	private List<Petitioner> petitioners;

	@JsonProperty("respondents")
	@Valid
	private List<Respondent> respondents;

	/* -------------------- Court & Hearing -------------------- */

	@JsonProperty("courtType")
	private String courtType;

	@JsonProperty("courtName")
	private String courtName;

	@JsonProperty("nextHearingDate")
	private Long nextHearingDate;

	@JsonProperty("judgement")
	private Judgement judgement;

	/* -------------------- Documents -------------------- */

	@JsonProperty("documents")
	@Valid
	private List<Document> documents;

	/* -------------------- Workflow -------------------- */

	@JsonProperty("workflow")
	private ProcessInstance workflow;

	/* -------------------- Extensible Data -------------------- */

	@JsonProperty("additionalDetails")
	private JsonNode additionalDetails;

	/* -------------------- Audit -------------------- */

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;


}
