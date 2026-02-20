package org.egov.lm.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.lm.config.CaseConfiguration;
import org.egov.lm.models.AuditDetails;
import org.egov.lm.repository.ServiceRequestRepository;
import org.egov.lm.web.contracts.IdGenerationRequest;
import org.egov.lm.web.contracts.IdRequest;
import org.egov.lm.web.contracts.IdGenerationResponse;
import org.egov.lm.web.contracts.IdResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;

@Getter
public class CommonUtils {

	
	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private CaseConfiguration configs;
	
    @Autowired
    private ServiceRequestRepository restRepo;

  
    /**
     * Method to return auditDetails for create/update flows
     *
     * @param by
     * @param isCreate
     * @return AuditDetails
     */
    public AuditDetails getAuditDetails(String by, Boolean isCreate) {
    	
        Long time = System.currentTimeMillis();
        
        if(isCreate)
            return AuditDetails.builder().createdBy(by).lastModifiedBy(by).createdTime(time).lastModifiedTime(time).build();
        else
            return AuditDetails.builder().lastModifiedBy(by).lastModifiedTime(time).build();
    }
    
	/**************************** ID GEN ****************************/

    /**
     * Returns a list of numbers generated from idgen
     * @param requestInfo RequestInfo from the request
     * @param tenantId tenantId of the city
     * @param idName code of the field defined in application properties for which ids are generated for
     * @param idformat format in which ids are to be generated
     * @param count Number of ids to be generated
     * @return List of ids generated using idGen service
     */
	public List<String> getIdList(RequestInfo requestInfo, String tenantId, String idName, String idformat, int count) {
		
		List<IdRequest> reqList = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			reqList.add(IdRequest.builder().idName(idName).format(idformat).tenantId(tenantId).build());
		}

		IdGenerationRequest request = IdGenerationRequest.builder().idRequests(reqList).requestInfo(requestInfo).build();
		StringBuilder uri = new StringBuilder(configs.getIdGenHost()).append(configs.getIdGenPath());
		IdGenerationResponse response = mapper.convertValue(restRepo.fetchResult(uri, request).get(), IdGenerationResponse.class);
		
		List<IdResponse> idResponses = response.getIdResponses();
		
        if (CollectionUtils.isEmpty(idResponses))
            throw new CustomException("IDGEN ERROR", "No ids returned from idgen Service");
        
		return idResponses.stream().map(IdResponse::getId).collect(Collectors.toList());
	}
    
}
