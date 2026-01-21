package org.egov.lm.service;

import java.util.List;
import java.util.UUID;

import org.egov.common.contract.request.RequestInfo;
import org.egov.lm.config.CaseConfiguration;
import org.egov.lm.models.AuditDetails;
import org.egov.lm.models.Case;
import org.egov.lm.models.Judgement;
import org.egov.lm.models.Petitioner;
import org.egov.lm.models.Respondent;
import org.egov.lm.models.enums.Status;
import org.egov.lm.util.CaseUtil;
import org.egov.lm.web.contracts.CaseRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class EnrichmentService {

	@Autowired
	private CaseUtil caseUtil;
	
	@Autowired
	private CaseConfiguration config;

	// Adding UUID to case,petitioners,respondents and document
	public void enrichCreateCase(CaseRequest caseRequest) {
		RequestInfo requestInfo = caseRequest.getRequestInfo();

		AuditDetails auditDetails = caseUtil.getAuditDetails(requestInfo.getUserInfo().getUuid().toString(), true);
		caseRequest.getCases().setId(UUID.randomUUID().toString());
		setIdgenId(caseRequest);
		caseRequest.getCases().setStatus(Status.REGISTERED);
		List<Petitioner> petitioners = caseRequest.getCases().getPetitioners();
		petitioners.forEach(petitioner -> petitioner.setPetitionerId(UUID.randomUUID().toString()));

		List<Respondent> respondents = caseRequest.getCases().getRespondents();
		respondents.forEach(respondent -> respondent.setRespondentId(UUID.randomUUID().toString()));

		caseRequest.getCases().getDocuments().forEach(doc -> {
			doc.setId(UUID.randomUUID().toString());
			if (null == doc.getStatus())
				doc.setStatus(Status.ACTIVE);
		});
		caseRequest.getCases().setAuditDetails(auditDetails);

	}

	private void setIdgenId(CaseRequest caseRequest) {
		Case cases = caseRequest.getCases();
		RequestInfo requestInfo = caseRequest.getRequestInfo();
		String tenantId = cases.getTenantId();
		
		String CaseId = caseUtil.getIdList(requestInfo, tenantId, config.getCaseIdGenName(), config.getCaseIdGenFormat(), 1).get(0);

		cases.setCaseId(CaseId);
	}

	public void enrichUpdateCase(CaseRequest caseRequest) {
		Case cases = caseRequest.getCases();
		RequestInfo requestInfo = caseRequest.getRequestInfo();

		AuditDetails auditDetails = caseUtil.getAuditDetails(requestInfo.getUserInfo().getUuid().toString(), false);
		cases.getAdvocates().forEach(advocate -> advocate.setAdvocateId(UUID.randomUUID().toString()));

		if(caseRequest.getCases().getWorkflow().getAction().equalsIgnoreCase("ALLOCATE_ADVOCATE"))
		cases.getJudgement().setJudgementId(UUID.randomUUID().toString());
	

		cases.setAuditDetails(auditDetails);
	}

}
