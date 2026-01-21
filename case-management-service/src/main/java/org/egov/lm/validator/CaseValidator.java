package org.egov.lm.validator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.egov.common.contract.request.RequestInfo;
import org.egov.lm.models.Advocate;
import org.egov.lm.models.CaseCriteria;
import org.egov.lm.models.Document;
import org.egov.lm.models.Petitioner;
import org.egov.lm.models.Respondent;
import org.egov.lm.web.contracts.CaseRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

import org.egov.tracer.model.CustomException;

@Slf4j
@Component
public class CaseValidator {
	 
    private static final String EMAIL_REG = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
    private static final String MOB_REG = "\\d{10}";
    private static final String NAME_REG = "^[A-Za-z\\s]+$";
    private boolean isCreate;

    public void validateCreateRequest(CaseRequest request) {
    	
    	this.isCreate = true;

        Map<String, String> errorMap = new HashMap<>();
        
        validateRequest(request, errorMap);

    }
    
    public void validateUpdateRequest(CaseRequest request) {
    	
    	   this.isCreate = false;
    	
    	   Map<String, String> errorMap = new HashMap<>();
    	   
    	   validateRequest(request, errorMap);
    	
    }
    
    private void validateRequest(CaseRequest request, Map<String, String> errorMap) {
       
    	validateMandatoryFields(request, errorMap);
    	validateCaseFields(request, errorMap);
    	validateParties(request, errorMap);
    	validateDocuments(request, errorMap);
    	
    	if (!errorMap.isEmpty()) {
            throw new CustomException(errorMap);
        }
    }

    private void validateMandatoryFields(CaseRequest request, Map<String, String> errorMap) {
        if (StringUtils.isEmpty(request.getCases().getTenantId())) {
            errorMap.put("TENANT_ID_MANDATORY", "Tenant ID is mandatory for case creation");
        }
        if(StringUtils.isEmpty(request.getCases().getCaseId()) && !this.isCreate) {
        	errorMap.put("CASE_ID_MANDATORY", "Case ID is mandatory for update");
        }
    }

    private void validateCaseFields(CaseRequest request, Map<String, String> errorMap) {
        validateNotEmpty(request.getCases().getCaseType(), "CASE_TYPE_MANDATORY", "Case Type", errorMap);
        validateNotEmpty(request.getCases().getCaseCategory(), "CASE_CATEGORY_MANDATORY", "Case Category", errorMap);
        validateNotEmpty(request.getCases().getTitle(), "TITLE_MANDATORY", "Title", errorMap);
        validateNotEmpty(request.getCases().getDepartment(), "DEPARTMENT_MANDATORY", "Department", errorMap);
    }

    private void validateParties(CaseRequest request, Map<String, String> errorMap) {
        // Validate advocates
    	if(!this.isCreate) {
    		validateAdvocates(request.getCases().getAdvocates(), errorMap);
    	}
        
        
        // Validate petitioners
        validatePetitioners(request.getCases().getPetitioners(), errorMap);
        
        // Validate respondents
        validateRespondents(request.getCases().getRespondents(), errorMap);
    }

    private void validateAdvocates(List<Advocate> advocates, Map<String, String> errorMap) {
        if (CollectionUtils.isEmpty(advocates)) {
            errorMap.put("ADVOCATE_MANDATORY", "At least one advocate must be provided for case creation");
        } else {
            for (Advocate advocate : advocates) {
                validateAdvocate(advocate, errorMap);
            }
        }
    }

    private void validatePetitioners(List<Petitioner> petitioners, Map<String, String> errorMap) {
        if (CollectionUtils.isEmpty(petitioners)) {
            errorMap.put("PETITIONER_MANDATORY", "At least one petitioner must be provided for case creation");
        } else {
            for (Petitioner petitioner : petitioners) {
                validatePetitioner(petitioner, errorMap);
            }
        }
    }

    private void validateRespondents(List<Respondent> respondents, Map<String, String> errorMap) {
        if (CollectionUtils.isEmpty(respondents)) {
            errorMap.put("RESPONDENT_MANDATORY", "At least one respondent must be provided for case creation");
        } else {
            for (Respondent respondent : respondents) {
                validateRespondent(respondent, errorMap);
            }
        }
    }

    private void validateAdvocate(Advocate advocate, Map<String, String> errorMap) {
        validateNotEmpty(advocate.getAdvocateId(), "ADVOCATE_ID_MANDATORY", "Advocate ID", errorMap);
        validateNotEmpty(advocate.getName(), "ADVOCATE_NAME_MANDATORY", "Advocate name", errorMap);
        validateName(advocate.getName(), "ADVOCATE_NAME_INVALID", "Advocate name", errorMap);
        validateMobileNumber(advocate.getMobileNumber(), "ADVOCATE_MOBILE_INVALID", "Advocate mobile", errorMap);
        validateEmail(advocate.getEmail(), "ADVOCATE_EMAIL_INVALID", "Advocate email", errorMap);

        if (StringUtils.hasText(advocate.getBarRegistrationNumber()) && advocate.getBarRegistrationNumber().trim().isEmpty()) {
            errorMap.put("ADVOCATE_BAR_REGISTRATION_INVALID", "Advocate Bar Registration Number, if provided, cannot be empty");
        }

        if (advocate.getIsGovernmentAdvocate() == null) {
            errorMap.put("ADVOCATE_IS_GOVERNMENT_FLAG_MANDATORY", "Is Government Advocate flag is mandatory");
        }
    }

    private void validatePetitioner(Petitioner petitioner, Map<String, String> errorMap) {
    	
    	if(!this.isCreate) {
    		validateNotEmpty(petitioner.getPetitionerId(), "PETITIONER_ID_MANDATORY", "Petitioner Id", errorMap);
    	}
    	
        validateNotEmpty(petitioner.getName(), "PETITIONER_NAME_MANDATORY", "Petitioner name", errorMap);
        validateName(petitioner.getName(), "PETITIONER_NAME_INVALID", "Petitioner name", errorMap);
        
        validateNotEmpty(petitioner.getMobileNumber(), "PETITIONER_MOBILE_MANDATORY", "Petitioner mobile", errorMap);
        validateMobileNumber(petitioner.getMobileNumber(), "PETITIONER_MOBILE_INVALID", "Petitioner mobile", errorMap);
        
        validateNotEmpty(petitioner.getEmail(), "PETITIONER_EMAIL_MANDATORY", "Petitioner email", errorMap);
        validateEmail(petitioner.getEmail(), "PETITIONER_EMAIL_INVALID", "Petitioner email", errorMap);
    }

    private void validateRespondent(Respondent respondent, Map<String, String> errorMap) {
    	
    	if(!this.isCreate) {
    		validateNotEmpty(respondent.getRespondentId(), "RESPONDENT_ID_MANDATORY", "Respondent Id", errorMap);
    	}
    	
        validateNotEmpty(respondent.getName(), "RESPONDENT_NAME_MANDATORY", "Respondent name", errorMap);
        validateName(respondent.getName(), "RESPONDENT_NAME_INVALID", "Respondent name", errorMap);

        if ("DEPARTMENT".equalsIgnoreCase(respondent.getType()) && StringUtils.isEmpty(respondent.getDepartment())) {
            errorMap.put("RESPONDENT_DEPARTMENT_MANDATORY", "Department must be provided for Respondent type 'DEPARTMENT'");
        }
        
        validateNotEmpty(respondent.getMobileNumber(), "RESPONDENT_MOBILE_MANDATORY", "Respondent mobile", errorMap);
        validateMobileNumber(respondent.getMobileNumber(), "RESPONDENT_MOBILE_INVALID", "Respondent mobile", errorMap);
        
        validateNotEmpty(respondent.getEmail(), "RESPONDENT_EMAIL_MANDATORY", "Respondent email", errorMap);
        validateEmail(respondent.getEmail(), "RESPONDENT_EMAIL_INVALID", "Respondent email", errorMap);

    }

    private void validateNotEmpty(String value, String errorKey, String fieldName, Map<String, String> errorMap) {
        if (StringUtils.isEmpty(value)) {
            errorMap.put(errorKey, fieldName + " is mandatory");
        }
    }

    private void validateMobileNumber(String mobileNumber, String errorKey, String fieldName, Map<String, String> errorMap) {
        if (StringUtils.isEmpty(mobileNumber) || !mobileNumber.matches(MOB_REG)) {
            errorMap.put(errorKey, fieldName + " must be a valid 10-digit number");
        }
    }

    private void validateEmail(String email, String errorKey, String fieldName, Map<String, String> errorMap) {
        if (StringUtils.hasText(email) && !email.matches(EMAIL_REG)) {
            errorMap.put(errorKey, fieldName + " must be a valid email format");
        }
    }

    private void validateName(String name, String errorKey, String fieldName, Map<String, String> errorMap) {
        if (StringUtils.hasText(name) && !name.matches(NAME_REG)) {
            errorMap.put(errorKey, fieldName + " must only contain letters and spaces");
        }
    }

    private void validateDocuments(CaseRequest request, Map<String, String> errorMap) {
        List<Document> documents = request.getCases().getDocuments();
        if (CollectionUtils.isEmpty(documents)) {
            errorMap.put("DOCUMENTS_MANDATORY", "At least one document must be provided for case creation");
        }else {
        	for(Document document : documents) {
        		if(!this.isCreate) {
            		validateNotEmpty(document.getId(), "DOCUMENT_ID_MANDATORY", "Document Id", errorMap);
            	}
        		validateNotEmpty(document.getDocumentUid() , "DOCUMENT_UID_MANDATORY", "DocumentUId", errorMap);
        		validateNotEmpty(document.getFileStoreId() , "FILE_STORE_ID_MANDATORY", "File store Id", errorMap);
        		validateNotEmpty(document.getDocumentType() , "DOCUMENT_TYPE_MANDATORY", "Document Type", errorMap);
        	}
        }
    }

	public void valildateCaseCriteria(@Valid CaseCriteria caseCriteria, RequestInfo requestInfo) {
		if (StringUtils.isEmpty(caseCriteria.getTenantId())) {
			throw new CustomException("EG_LM_INVALID_SEARCH", " TenantId is mandatory for search ");
        }
      
		
	}
	
}
