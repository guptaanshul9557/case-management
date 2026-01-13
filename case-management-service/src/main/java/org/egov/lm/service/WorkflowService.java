package org.egov.lm.service;

import java.util.Optional;

import org.egov.lm.config.CaseConfiguration;
import org.egov.lm.models.Case;
import org.egov.lm.models.enums.Status;
import org.egov.lm.models.workflow.ProcessInstanceRequest;
import org.egov.lm.models.workflow.ProcessInstanceResponse;
import org.egov.lm.models.workflow.State;
import org.egov.lm.repository.ServiceRequestRepository;
import org.egov.lm.util.CaseUtil;
import org.egov.lm.web.contracts.CaseRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class WorkflowService {

	@Autowired
	private CaseConfiguration configs;

	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	ServiceRequestRepository serviceRequestRepository;
	
	@Autowired
	private CaseUtil caseUtil;
	

	/**
	 * Method to integrate with workflow
	 *
	 * takes the trade-license request as parameter constructs the work-flow request
	 *
	 * and sets the resultant status from wf-response back to trade-license object
	 *
	 */
	public State callWorkFlow(ProcessInstanceRequest workflowReq) {

		ProcessInstanceResponse response = null;
		StringBuilder url = new StringBuilder(configs.getWfHost().concat(configs.getWfTransitionPath()));
		Optional<Object> optional = serviceRequestRepository.fetchResult(url, workflowReq);
		response = mapper.convertValue(optional.get(), ProcessInstanceResponse.class);
		return response.getProcessInstances().get(0).getState();
	}
	
	
  
	
	public State updateCaseWorkflow(CaseRequest request) {

          Case cases = request.getCases();
		
		ProcessInstanceRequest workflowReq = caseUtil.initiateCaseWorkFlow(request);
		State state = callWorkFlow(workflowReq);		
		request.getCases().setStatus(Status.fromValue(state.getApplicationStatus()));
		request.getCases().getWorkflow().setState(state);
		return state;



}
}