package org.egov.lm.repository.builder;

import java.util.List;
import java.util.Set;

import javax.validation.Valid;

import org.egov.lm.config.CaseConfiguration;
import org.egov.lm.models.CaseCriteria;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

@Component
public class CaseQueryBuilder {

	private final CaseConfiguration config;

	public CaseQueryBuilder(CaseConfiguration config) {
		this.config = config;
	}

	private static final String AND = " AND ";
	private static final String COMMA = ",";

	/*
	 * PAGINATED CASE IDS QUERY (ROOT ENTITY ONLY)
	 */
	private static final String BASE_CASE_ID_QUERY = "SELECT c.caseid " + "FROM eg_lm_case c ";

	private static final String CASE_ID_ORDER_BY = " ORDER BY c.createdtime DESC ";

	private static final String PAGINATION = " OFFSET ? LIMIT ? ";
	
	private static final String LEFT_JOIN = " LEFT JOIN ";

	/*
	 * FULL DATA FETCH QUERY
	 */
	private static final String CASE_DETAILS_QUERY = "SELECT "
			+ "c.id, c.caseid, c.tenantid, c.casetype, c.casecategory, c.title, c.description, "
			+ " c.department, c.courttype, c.courtname, c.nexthearingdate, c.status, "
			+ " c.createdby, c.createdtime, c.lastmodifiedby, c.lastmodifiedtime, c.additionaldetails, " +

			" adv.advocateid , adv.role AS advocate_role, adv.name AS advocate_name," +

			" j.judgementid AS judgementid, " + " j.remark AS judgement_remark, " + " j.orderdetail AS orderdetail, " +

			" pet.petitionerid AS petitioner_id, pet.name AS petitioner_name, pet.mobilenumber AS petitioner_mobile,pet.email as petitioner_email, " +

			" res.respondentid AS respondent_id, res.name AS respondent_name, res.mobilenumber AS respondent_mobile, res.email as respondent_email," +

			" doc.id AS document_id, doc.documenttype, doc.filestoreid, doc.documentuid " +

			"FROM eg_lm_case c " + LEFT_JOIN +"eg_lm_case_advocate adv ON adv.caseid = c.caseid"
			+ LEFT_JOIN +"eg_lm_case_petitioner pet ON pet.caseid = c.caseid"
			+ LEFT_JOIN +"eg_lm_case_respondent res ON res.caseid = c.caseid"
			+ LEFT_JOIN +"eg_lm_case_document doc ON doc.caseid = c.caseid "
			+ LEFT_JOIN +"eg_lm_judgement j ON j.caseid = c.caseid ";
	
	private static final String TOTAL_COUNT = "SELECT COUNT(*) FROM eg_lm_case WHERE tenantid = ?"; 
    /*
	 * PUBLIC METHODS
	 */

	public String buildPaginatedCaseIdQuery(CaseCriteria criteria, List<Object> preparedStmtList) {

		StringBuilder query = new StringBuilder(CASE_DETAILS_QUERY);
		boolean andAdded = false;

		andAdded = addTenantFilter(criteria, query, preparedStmtList, andAdded);
		andAdded = addCaseIdFilter(criteria, query, preparedStmtList, andAdded);
		andAdded = addCaseTypeFilter(criteria, query, preparedStmtList, andAdded);
		andAdded = addCaseCategoryFilter(criteria, query, preparedStmtList, andAdded);
		andAdded = addCourtFilter(criteria, query, preparedStmtList, andAdded);
		andAdded = addAdvocateFilter(criteria, query, preparedStmtList, andAdded);

		query.append(CASE_ID_ORDER_BY);
		addPagination(criteria, preparedStmtList);
		query.append(PAGINATION);

		return query.toString();
	}

	public String buildCaseDetailsQuery(Set<String> caseIds) {
		String placeholders = createPlaceholders(caseIds.size());
		return String.format(CASE_DETAILS_QUERY, placeholders);
	}

	/*
	 * FILTER BUILDERS
	 */

	private boolean addTenantFilter(CaseCriteria criteria, StringBuilder query, List<Object> params, boolean andAdded) {

		if (!ObjectUtils.isEmpty(criteria.getTenantId())) {
			query.append(andAdded ? AND : " WHERE ");
			query.append("c.tenantid = ?");
			params.add(criteria.getTenantId());
			return true;
		}
		return andAdded;
	}

	private boolean addCaseIdFilter(CaseCriteria criteria, StringBuilder query, List<Object> params, boolean andAdded) {

		if (!CollectionUtils.isEmpty(criteria.getCaseIds())) {
			query.append(andAdded ? AND : " WHERE ");
			query.append("c.caseid IN (").append(createPlaceholders(criteria.getCaseIds().size())).append(")");
			params.addAll(criteria.getCaseIds());
			return true;
		}
		return andAdded;
	}

	private boolean addCaseTypeFilter(CaseCriteria criteria, StringBuilder query, List<Object> params,
			boolean andAdded) {

		if (!CollectionUtils.isEmpty(criteria.getCaseTypes())) {
			query.append(andAdded ? AND : " WHERE ");
			query.append("c.casetype IN (").append(createPlaceholders(criteria.getCaseTypes().size())).append(")");
			params.addAll(criteria.getCaseTypes());
			return true;
		}
		return andAdded;
	}

	private boolean addCaseCategoryFilter(CaseCriteria criteria, StringBuilder query, List<Object> params,
			boolean andAdded) {

		if (!CollectionUtils.isEmpty(criteria.getCaseCategories())) {
			query.append(andAdded ? AND : " WHERE ");
			query.append("c.casecategory IN (").append(createPlaceholders(criteria.getCaseCategories().size()))
					.append(")");
			params.addAll(criteria.getCaseCategories());
			return true;
		}
		return andAdded;
	}

	private boolean addCourtFilter(CaseCriteria criteria, StringBuilder query, List<Object> params, boolean andAdded) {

		if (criteria.getCourtType() != null) {
			query.append(andAdded ? AND : " WHERE ");
			query.append("c.courttype = ?");
			params.add(criteria.getCourtType());
			andAdded = true;
		}

		if (criteria.getCourtName() != null) {
			query.append(andAdded ? AND : " WHERE ");
			query.append("c.courtname = ?");
			params.add(criteria.getCourtName());
			return true;
		}
		return andAdded;
	}

	private boolean addAdvocateFilter(CaseCriteria criteria, StringBuilder query, List<Object> params,
			boolean andAdded) {

		if (!CollectionUtils.isEmpty(criteria.getAdvocateIds())) {
			query.append(andAdded ? AND : " WHERE ");
			query.append("EXISTS (").append("SELECT 1 FROM eg_lm_case_advocate a ").append("WHERE a.caseid = c.caseid ")
					.append("AND a.advocateid IN (").append(createPlaceholders(criteria.getAdvocateIds().size()))
					.append("))");
			params.addAll(criteria.getAdvocateIds());
			return true;
		}
		return andAdded;
	}

	/*
	 * UTILITIES
	 */

	private void addPagination(CaseCriteria criteria, List<Object> params) {
		long limit = config.getDefaultLimit();
		long offset = config.getDefaultOffset();

		if (criteria.getLimit() != null) {
			limit = Math.min(criteria.getLimit(), config.getMaxSearchLimit());
		}

		if (criteria.getOffset() != null) {
			offset = criteria.getOffset();
		}

		params.add(offset);
		params.add(limit);
	}

	private String createPlaceholders(int count) {
		return String.join(COMMA, java.util.Collections.nCopies(count, "?"));
	}

	public String buildTenantCaseCountQuery(@Valid CaseCriteria caseCriteria, List<Object> preparedStmtList) {
		preparedStmtList.add(caseCriteria.getTenantId());
	    return TOTAL_COUNT;
	}

}
