package org.egov.lm.repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.egov.common.contract.request.RequestInfo;
import org.egov.lm.models.Advocate;
import org.egov.lm.models.Case;
import org.egov.lm.models.CaseCriteria;
import org.egov.lm.models.user.User;
import org.egov.lm.models.user.UserDetailResponse;
import org.egov.lm.models.user.UserSearchRequest;
import org.egov.lm.repository.builder.AdvocateQueryBuilder;
import org.egov.lm.repository.builder.CaseQueryBuilder;
import org.egov.lm.repository.rowmapper.AdvocateRowMapper;
import org.egov.lm.repository.rowmapper.CaseRowMapper;
import org.egov.lm.service.UserService;
import org.egov.lm.web.contracts.CaseRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

@Repository
public class CaseRepository {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private CaseQueryBuilder queryBuilder;

	@Autowired
	private UserService userService;
	
	@Autowired
	private CaseRowMapper rowMapper;
	
	@Autowired
	private AdvocateRowMapper advocateRowMapper;
	
	@Autowired
	private AdvocateQueryBuilder builder;

	public List<Case> getAllRegisterdCases(CaseCriteria criteria) {
		List<Object> preparedStmtList = new ArrayList<>();
		String query = queryBuilder.buildPaginatedCaseIdQuery(criteria, preparedStmtList);

		return jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);

	}

	public List<Advocate> getAdvocates(@Valid CaseRequest caseRequest) {
		List<Object> preparedStmtList = new ArrayList<>();
		String query = builder.fetchCaseAdvocates(caseRequest,preparedStmtList);

		return jdbcTemplate.query(query, preparedStmtList.toArray(), advocateRowMapper);
	}

	public Integer getCaseCount(@Valid CaseCriteria caseCriteria) {
		List<Object> preparedStmtList = new ArrayList<>();
		String query = queryBuilder.buildTenantCaseCountQuery(caseCriteria,preparedStmtList);
		Integer count =  jdbcTemplate.queryForObject(query, preparedStmtList.toArray(), Integer.class);
		return count;
	}

}
