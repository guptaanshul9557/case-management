package org.egov.lm.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.egov.common.contract.request.RequestInfo;
import org.egov.lm.config.CaseConfiguration;
import org.egov.lm.models.Advocate;
import org.egov.lm.models.AuditDetails;
import org.egov.lm.models.Case;
import org.egov.lm.models.CaseCriteria;
import org.egov.lm.models.enums.Status;
import org.egov.lm.models.workflow.State;
import org.egov.lm.producer.Producer;
import org.egov.lm.repository.CaseRepository;
import org.egov.lm.validator.CaseValidator;
import org.egov.lm.web.contracts.CaseRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
public class CaseService {

	@Autowired
	private CaseConfiguration caseConfiguration;

	@Autowired
	private Producer producer;

	@Autowired
	private CaseRepository caseRepository;

	@Autowired
	private WorkflowService wfService;

	@Autowired
	private EnrichmentService enrichmentService;

	@Autowired
	private CaseValidator caseValidator;

	public Case fileCase(@Valid CaseRequest caseRequest) {

		// validate
		caseValidator.validateCreateRequest(caseRequest);

		// enrich
		enrichmentService.enrichCreateCase(caseRequest);

		if (caseConfiguration.getIsWorkflowEnabled()) {
			wfService.updateCaseWorkflow(caseRequest);
		}
		producer.push(caseConfiguration.getSaveCaseTopic(), caseRequest);
		return caseRequest.getCases();
	}

	public List<Case> searchCases(@Valid CaseCriteria criteria) {

		if (criteria.isAudit() && (CollectionUtils.isEmpty(criteria.getCaseIds()))) {

			throw new CustomException("EG_LM_CASE_AUDIT_ERROR", "Case Ids are null");
		}

		return caseRepository.getAllRegisterdCases(criteria);

	}

	public Case updateCase(@Valid CaseRequest caseRequest) {
		State state = null;
		enrichmentService.enrichUpdateCase(caseRequest);
		if (caseConfiguration.getIsWorkflowEnabled()) {
			state = wfService.updateCaseWorkflow(caseRequest);
		}

		producer.push(caseConfiguration.getUpdateCaseTopic(), caseRequest);
		return caseRequest.getCases();
	}

	public Integer getCaseCount(@Valid CaseCriteria caseCriteria) {
		Integer count = caseRepository.getCaseCount(caseCriteria);
		return count;
	}

}
