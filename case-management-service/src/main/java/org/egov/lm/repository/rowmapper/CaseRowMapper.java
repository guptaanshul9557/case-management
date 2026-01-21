package org.egov.lm.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.egov.lm.models.*;
import org.egov.lm.models.enums.Status;
import org.egov.lm.repository.aggregate.CaseAggregate;
import org.egov.tracer.model.CustomException;
import org.postgresql.util.PGobject;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CaseRowMapper implements ResultSetExtractor<List<Case>> {

    private final ObjectMapper objectMapper;

    public CaseRowMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Case> extractData(ResultSet rs) throws SQLException, DataAccessException {

        Map<String, CaseAggregate> aggregateMap = new LinkedHashMap<>();

        while (rs.next()) {

            String caseId = rs.getString("caseid");

            CaseAggregate aggregate = aggregateMap.computeIfAbsent(
                    caseId,
                    id -> new CaseAggregate(buildCase(rs))
            );

            addAdvocate(rs, aggregate);
            addPetitioner(rs, aggregate);
            addRespondent(rs, aggregate);
            addDocument(rs, aggregate);
        }

        List<Case> result = new ArrayList<>();
        for (CaseAggregate agg : aggregateMap.values()) {
            result.add(agg.toCase());
        }
        return result;
    }


    private Case buildCase(ResultSet rs) {
        try {
            return Case.builder()
            		.id(rs.getString("id"))
                    .caseId(rs.getString("caseid"))
                    .tenantId(rs.getString("tenantid"))
                    .caseType(rs.getString("casetype"))
                    .caseCategory(rs.getString("casecategory"))
                    .title(rs.getString("title"))
                    .description(rs.getString("description"))
                    .department(rs.getString("department"))
                    .courtType(rs.getString("courttype"))
                    .courtName(rs.getString("courtname"))
                    .nextHearingDate(rs.getObject("nexthearingdate", Long.class))
                    .status(Status.fromValue(rs.getString("status")))
                    .additionalDetails(readJson(rs, "additionaldetails"))
                    .auditDetails(buildAudit(rs))
                    .judgement(buildJudgement(rs))
                    .build();
        } catch (SQLException e) {
            throw new CustomException("CASE_MAPPING_ERROR", e.getMessage());
        }
    }

    private void addAdvocate(ResultSet rs, CaseAggregate agg) throws SQLException {
        String advocateId = rs.getString("advocateid");
        if (advocateId == null) return;

        Advocate advocate = Advocate.builder()
                .advocateId(advocateId)
                .name(rs.getString("advocate_name"))
                .role(rs.getString("advocate_role"))
                .build();

        agg.addAdvocate(advocate);
    }

    private void addPetitioner(ResultSet rs, CaseAggregate agg) throws SQLException {
        String petitionerId = rs.getString("petitioner_id");
        if (petitionerId == null) return;

        Petitioner petitioner = Petitioner.builder()
                .petitionerId(petitionerId)
                .name(rs.getString("petitioner_name"))
                .mobileNumber(rs.getString("petitioner_mobile"))
                .email(rs.getString("petitioner_email"))
                .build();

        agg.addPetitioner(petitioner);
    }

    private void addRespondent(ResultSet rs, CaseAggregate agg) throws SQLException {
        String respondentId = rs.getString("respondent_id");
        if (respondentId == null) return;

        Respondent respondent = Respondent.builder()
                .respondentId(respondentId)
                .name(rs.getString("respondent_name"))
                .mobileNumber(rs.getString("respondent_mobile"))
                .email(rs.getString("respondent_email"))
                .build();

        agg.addRespondent(respondent);
    }

    private void addDocument(ResultSet rs, CaseAggregate agg) throws SQLException {
        String documentId = rs.getString("document_id");
        if (documentId == null) return;

        Document document = Document.builder()
                .id(documentId)
                .documentType(rs.getString("documenttype"))
                .fileStoreId(rs.getString("filestoreid"))
                .documentUid(rs.getString("documentuid"))
                .build();

        agg.addDocument(document);
    }

    private AuditDetails buildAudit(ResultSet rs) throws SQLException {
        return AuditDetails.builder()
                .createdBy(rs.getString("createdby"))
                .createdTime(rs.getObject("createdtime", Long.class))
                .lastModifiedBy(rs.getString("lastmodifiedby"))
                .lastModifiedTime(rs.getObject("lastmodifiedtime", Long.class))
                .build();
    }

    private Judgement buildJudgement(ResultSet rs) throws SQLException {
        String judgementId = rs.getString("judgementid");
        if (judgementId == null) return null;

        return Judgement.builder()
                .judgementId(judgementId)
                .remark(rs.getString("judgement_remark"))
                .orderDetail(rs.getString("orderdetail"))
                .build();
    }

    private JsonNode readJson(ResultSet rs, String column) {
        try {
            PGobject obj = (PGobject) rs.getObject(column);
            return obj == null ? null : objectMapper.readTree(obj.getValue());
        } catch (Exception e) {
            throw new CustomException(
                    "JSON_PARSE_ERROR",
                    "Failed to parse JSON column: " + column
            );
        }
    }
}
