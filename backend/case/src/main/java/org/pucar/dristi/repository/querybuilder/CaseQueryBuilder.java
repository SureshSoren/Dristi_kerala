package org.pucar.dristi.repository.querybuilder;

import static org.pucar.dristi.config.ServiceConstants.CASE_SEARCH_QUERY_EXCEPTION;
import static org.pucar.dristi.config.ServiceConstants.DOCUMENT_SEARCH_QUERY_EXCEPTION;
import static org.pucar.dristi.config.ServiceConstants.LINKED_CASE_SEARCH_QUERY_EXCEPTION;
import static org.pucar.dristi.config.ServiceConstants.LITIGANT_SEARCH_QUERY_EXCEPTION;
import static org.pucar.dristi.config.ServiceConstants.REPRESENTATIVES_SEARCH_QUERY_EXCEPTION;
import static org.pucar.dristi.config.ServiceConstants.REPRESENTING_SEARCH_QUERY_EXCEPTION;
import static org.pucar.dristi.config.ServiceConstants.STATUTE_SECTION_SEARCH_QUERY_EXCEPTION;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.web.models.CaseCriteria;
import org.pucar.dristi.web.models.Pagination;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CaseQueryBuilder {
    private static final String BASE_CASE_QUERY = " SELECT cases.id as id, cases.tenantid as tenantid, cases.casenumber as casenumber, cases.resolutionmechanism as resolutionmechanism, cases.casetitle as casetitle, cases.casedescription as casedescription, " +
            "cases.filingnumber as filingnumber, cases.casenumber as casenumber, cases.accesscode as accesscode, cases.courtcasenumber as courtcasenumber, cases.cnrNumber as cnrNumber, " +
            " cases.courtid as courtid, cases.benchid as benchid, cases.judgeid as judgeid, cases.stage as stage, cases.substage as substage, cases.filingdate as filingdate, cases.judgementdate as judgementdate, cases.registrationdate as registrationdate, cases.natureofpleading as natureofpleading, cases.status as status, cases.remarks as remarks, cases.isactive as isactive, cases.casedetails as casedetails, cases.additionaldetails as additionaldetails, cases.casecategory as casecategory, cases.createdby as createdby," +
            " cases.lastmodifiedby as lastmodifiedby, cases.createdtime as createdtime, cases.lastmodifiedtime as lastmodifiedtime ";
    private static final String FROM_CASES_TABLE = " FROM dristi_cases cases";
    private static final String ORDERBY_CLAUSE = " ORDER BY cases.{orderBy} {sortingOrder} ";
    private static final String DEFAULT_ORDERBY_CLAUSE = " ORDER BY cases.createdtime DESC ";

    private static final String DOCUMENT_SELECT_QUERY_CASE = "Select doc.id as id, doc.documenttype as documenttype, doc.filestore as filestore," +
            " doc.documentuid as documentuid, doc.additionaldetails as docadditionaldetails, doc.case_id as case_id, doc.linked_case_id as linked_case_id, doc.litigant_id as litigant_id, doc.representative_id as representative_id, doc.representing_id as representing_id ";
    private static final String FROM_DOCUMENTS_TABLE = " FROM dristi_case_document doc";

    private  static  final String TOTAL_COUNT_QUERY = "SELECT COUNT(*) FROM ({baseQuery}) total_result";


    private static final String BASE_LINKED_CASE_QUERY = " SELECT lics.id as id, lics.casenumbers as casenumbers, lics.case_id as case_id," +
            "lics.relationshiptype as relationshiptype," +
            " lics.isactive as isactive, lics.additionaldetails as additionaldetails, lics.createdby as createdby," +
            " lics.lastmodifiedby as lastmodifiedby, lics.createdtime as createdtime, lics.lastmodifiedtime as lastmodifiedtime ";
    private static final String FROM_LINKED_CASE_TABLE = " FROM dristi_linked_case lics";


    private static final String BASE_LITIGANT_QUERY = " SELECT ltg.id as id, ltg.tenantid as tenantid, ltg.partycategory as partycategory, ltg.case_id as case_id, " +
            "ltg.individualid as individualid, " +
            " ltg.organisationid as organisationid, ltg.partytype as partytype, ltg.isactive as isactive, ltg.additionaldetails as additionaldetails, ltg.createdby as createdby," +
            " ltg.lastmodifiedby as lastmodifiedby, ltg.createdtime as createdtime, ltg.lastmodifiedtime as lastmodifiedtime ";
    private static final String FROM_LITIGANT_TABLE = " FROM dristi_case_litigants ltg";


    private static final String BASE_STATUTE_SECTION_QUERY = " SELECT stse.id as id, stse.tenantid as tenantid, stse.statutes as statutes, stse.case_id as case_id, " +
            "stse.sections as sections," +
            " stse.subsections as subsections, stse.additionaldetails as additionaldetails, stse.createdby as createdby," +
            " stse.lastmodifiedby as lastmodifiedby, stse.createdtime as createdtime, stse.lastmodifiedtime as lastmodifiedtime ";
    private static final String FROM_STATUTE_SECTION_TABLE = " FROM dristi_case_statutes_and_sections stse";


    private static final String BASE_REPRESENTATIVES_QUERY = " SELECT rep.id as id, rep.tenantid as tenantid, rep.advocateid as advocateid, rep.case_id as case_id, " +
            " rep.isactive as isactive, rep.additionaldetails as additionaldetails, rep.createdby as createdby," +
            " rep.lastmodifiedby as lastmodifiedby, rep.createdtime as createdtime, rep.lastmodifiedtime as lastmodifiedtime ";
    private static final String FROM_REPRESENTATIVES_TABLE = " FROM dristi_case_representatives rep";

    private static final String BASE_REPRESENTING_QUERY = " SELECT rpst.id as id, rpst.tenantid as tenantid, rpst.partycategory as partycategory, rpst.representative_id as representative_id, " +
            "rpst.individualid as individualid, rpst.case_id as case_id, " +
            " rpst.organisationid as organisationid, rpst.partytype as partytype, rpst.isactive as isactive, rpst.additionaldetails as additionaldetails, rpst.createdby as createdby," +
            " rpst.lastmodifiedby as lastmodifiedby, rpst.createdtime as createdtime, rpst.lastmodifiedtime as lastmodifiedtime ";
    private static final String FROM_REPRESENTING_TABLE = " FROM dristi_case_representing rpst";

    private static final String BASE_CASE_EXIST_QUERY = "SELECT COUNT(*) FROM dristi_cases cases WHERE ";

    public static final String AND = " AND ";

    public String checkCaseExistQuery(String caseId, String courtCaseNumber, String cnrNumber, String filingNumber) {
        try {
            StringBuilder query = new StringBuilder(BASE_CASE_EXIST_QUERY);
            List<String> conditions = new ArrayList<>();

            if (caseId != null && !caseId.isEmpty()) {
                conditions.add("cases.id = '" + caseId + "'");
            }
            if (courtCaseNumber != null && !courtCaseNumber.isEmpty()) {
                conditions.add("cases.courtcasenumber = '" + courtCaseNumber + "'");
            }
            if (cnrNumber != null && !cnrNumber.isEmpty()) {
                conditions.add("cases.cnrnumber = '" + cnrNumber + "'");
            }
            if (filingNumber != null && !filingNumber.isEmpty()) {
                conditions.add("cases.filingnumber = '" + filingNumber + "'");
            }

            if (!conditions.isEmpty()) {
                query.append(String.join(AND, conditions)).append(";");
            }

            return query.toString();
        } catch (Exception e) {
            log.error("Error while building case exist query", e);
            throw new CustomException(CASE_SEARCH_QUERY_EXCEPTION, "Error occurred while building the case exist query: " + e.getMessage());
        }
    }

    public String getCasesSearchQuery(CaseCriteria criteria, List<Object> preparedStmtList, RequestInfo requestInfo) {
        try {
            StringBuilder query = new StringBuilder(BASE_CASE_QUERY);
            query.append(FROM_CASES_TABLE);
            boolean firstCriteria = true; // To check if it's the first criteria
            if (criteria != null) {

                firstCriteria = addCriteria(criteria.getCaseId(), query, firstCriteria, "cases.id = ?", preparedStmtList);

                firstCriteria = addCriteria(criteria.getCnrNumber(), query, firstCriteria, "cases.cnrNumber = ?", preparedStmtList);

                firstCriteria = addCriteria(criteria.getFilingNumber() == null? null : "%" + criteria.getFilingNumber() + "%", query, firstCriteria, "LOWER(cases.filingnumber) LIKE LOWER(?)", preparedStmtList);

                firstCriteria = addCriteria(criteria.getCourtCaseNumber(), query, firstCriteria, "cases.courtcasenumber = ?", preparedStmtList);

                firstCriteria = addCriteria(criteria.getJudgeId(), query, firstCriteria, "cases.judgeid = ?", preparedStmtList);

                firstCriteria = addCriteria(criteria.getStage(), query, firstCriteria, "cases.stage = ?", preparedStmtList);

                firstCriteria = addCriteria(criteria.getSubstage(), query, firstCriteria, "cases.substage = ?", preparedStmtList);

                firstCriteria = addLitigantCriteria(criteria, preparedStmtList, requestInfo, query, firstCriteria);

                firstCriteria = addAdvocateCriteria(criteria, preparedStmtList, requestInfo, query, firstCriteria);

                firstCriteria = addCriteria(criteria.getStatus(), query, firstCriteria, "cases.status = ?", preparedStmtList);

                firstCriteria = addFilingDateCriteria(criteria, firstCriteria, query);

                addRegistrationDateCriteria(criteria, firstCriteria, query);
            }

            return query.toString();
        } catch (Exception e) {
            log.error("Error while building case search query :: {}",e.toString());
            throw new CustomException(CASE_SEARCH_QUERY_EXCEPTION, "Exception occurred while building the case search query: " + e.getMessage());
        }
    }

    private static void addRegistrationDateCriteria(CaseCriteria criteria, boolean firstCriteria, StringBuilder query) {
        if (criteria.getRegistrationFromDate() != null && criteria.getRegistrationToDate() != null) {
            if (!firstCriteria)
                query.append("OR cases.registrationdate BETWEEN ").append(criteria.getRegistrationFromDate()).append(AND).append(criteria.getRegistrationToDate()).append(" ");
            else {
                query.append("WHERE cases.registrationdate BETWEEN ").append(criteria.getRegistrationFromDate()).append(AND).append(criteria.getRegistrationToDate()).append(" ");
            }
            firstCriteria = false;
        }
    }

    private static boolean addFilingDateCriteria(CaseCriteria criteria, boolean firstCriteria, StringBuilder query) {
        if (criteria.getFilingFromDate() != null && criteria.getFilingToDate() != null) {
            if (!firstCriteria)
                query.append("OR cases.filingdate BETWEEN ").append(criteria.getFilingFromDate()).append(AND).append(criteria.getFilingToDate()).append(" ");
            else {
                query.append("WHERE cases.filingdate BETWEEN ").append(criteria.getFilingFromDate()).append(AND).append(criteria.getFilingToDate()).append(" ");
            }
            firstCriteria = false;
        }
        return firstCriteria;
    }

    private boolean addAdvocateCriteria(CaseCriteria criteria, List<Object> preparedStmtList, RequestInfo requestInfo, StringBuilder query, boolean firstCriteria) {
        if (criteria.getAdvocateId() != null && !criteria.getAdvocateId().isEmpty()) {
            addClauseIfRequired(query, firstCriteria);
            query.append("((cases.id IN ( SELECT advocate.case_id from dristi_case_representatives advocate WHERE advocate.advocateId = ?) AND cases.status not in ('DRAFT_IN_PROGRESS')) OR cases.status ='DRAFT_IN_PROGRESS' AND cases.createdby = ?) AND (cases.status NOT IN ('DELETED_DRAFT'))");
            preparedStmtList.add(criteria.getAdvocateId());
            preparedStmtList.add(requestInfo.getUserInfo().getUuid());
            firstCriteria = false;
        }
        return firstCriteria;
    }

    private boolean addLitigantCriteria(CaseCriteria criteria, List<Object> preparedStmtList, RequestInfo requestInfo, StringBuilder query, boolean firstCriteria) {
        if (criteria.getLitigantId() != null && !criteria.getLitigantId().isEmpty()) {
            addClauseIfRequired(query, firstCriteria);
            query.append("((cases.id IN ( SELECT litigant.case_id from dristi_case_litigants litigant WHERE litigant.individualId = ?) AND cases.status not in ('DRAFT_IN_PROGRESS')) OR cases.status ='DRAFT_IN_PROGRESS' AND cases.createdby = ?) AND (cases.status NOT IN ('DELETED_DRAFT'))");
            preparedStmtList.add(criteria.getLitigantId());
            preparedStmtList.add(requestInfo.getUserInfo().getUuid());
            firstCriteria = false;
        }
        return firstCriteria;
    }

    private boolean addCriteria(String criteria, StringBuilder query, boolean firstCriteria, String str, List<Object> preparedStmtList) {
        if (criteria != null && !criteria.isEmpty()) {
            addClauseIfRequired(query, firstCriteria);
            query.append(str);
            preparedStmtList.add(criteria);
            firstCriteria = false;
        }
        return firstCriteria;
    }

    private void addClauseIfRequired(StringBuilder query, boolean isFirstCriteria) {
        if (isFirstCriteria) {
            query.append(" WHERE ");
        } else {
            query.append(AND);
        }
    }

    public String getDocumentSearchQuery(List<String> ids, List<Object> preparedStmtList) {
        try {
            StringBuilder query = new StringBuilder(DOCUMENT_SELECT_QUERY_CASE);
            query.append(FROM_DOCUMENTS_TABLE);
            if (!ids.isEmpty()) {
                query.append(" WHERE doc.case_id IN (")
                        .append(ids.stream().map(id -> "?").collect(Collectors.joining(",")))
                        .append(")");
                preparedStmtList.addAll(ids);
            }

            return query.toString();
        }  catch(CustomException e){
            throw e;
        } catch (Exception e) {
            log.error("Error while building document search query :: {}",e.toString());
            throw new CustomException(DOCUMENT_SEARCH_QUERY_EXCEPTION, "Exception occurred while building the query: " + e.getMessage());
        }
    }

    public String getLinkedCaseSearchQuery(List<String> ids, List<Object> preparedStmtList) {
        try {
            StringBuilder query = new StringBuilder(BASE_LINKED_CASE_QUERY);
            query.append(FROM_LINKED_CASE_TABLE);
            if (!ids.isEmpty()) {
                query.append(" WHERE lics.case_id IN (")
                        .append(ids.stream().map(id -> "?").collect(Collectors.joining(",")))
                        .append(")");
                preparedStmtList.addAll(ids);
            }

            return query.toString();
        } catch (Exception e) {
            log.error("Error while building linked case search query :: {}",e.toString());
            throw new CustomException(LINKED_CASE_SEARCH_QUERY_EXCEPTION, "Exception occurred while building the query: " + e.getMessage());
        }
    }

    public String getLitigantSearchQuery(List<String> ids, List<Object> preparedStmtList) {
        try {
            StringBuilder query = new StringBuilder(BASE_LITIGANT_QUERY);
            query.append(FROM_LITIGANT_TABLE);
            if (!ids.isEmpty()) {
                query.append(" WHERE ltg.case_id IN (")
                        .append(ids.stream().map(id -> "?").collect(Collectors.joining(",")))
                        .append(")");
                preparedStmtList.addAll(ids);
            }

            return query.toString();
        } catch (Exception e) {
            log.error("Error while building litigant search query :: {}",e.toString());
            throw new CustomException(LITIGANT_SEARCH_QUERY_EXCEPTION, "Exception occurred while building the query: " + e.getMessage());
        }
    }

    public String getStatuteSectionSearchQuery(List<String> ids, List<Object> preparedStmtList) {
        try {
            StringBuilder query = new StringBuilder(BASE_STATUTE_SECTION_QUERY);
            query.append(FROM_STATUTE_SECTION_TABLE);
            if (!ids.isEmpty()) {
                query.append(" WHERE stse.case_id IN (")
                        .append(ids.stream().map(id -> "?").collect(Collectors.joining(",")))
                        .append(")");
                preparedStmtList.addAll(ids);
            }

            return query.toString();
        } catch (Exception e) {
            log.error("Error while building statute section search query :: {}",e.toString());
            throw new CustomException(STATUTE_SECTION_SEARCH_QUERY_EXCEPTION, "Exception occurred while building the query: " + e.getMessage());
        }
    }

    public String getRepresentativesSearchQuery(List<String> ids, List<Object> preparedStmtList) {
        try {
            StringBuilder query = new StringBuilder(BASE_REPRESENTATIVES_QUERY);
            query.append(FROM_REPRESENTATIVES_TABLE);
            if (!ids.isEmpty()) {
                query.append(" WHERE rep.case_id IN (")
                        .append(ids.stream().map(id -> "?").collect(Collectors.joining(",")))
                        .append(")");
                preparedStmtList.addAll(ids);
            }

            return query.toString();
        } catch (Exception e) {
            log.error("Error while building representatives search query :: {}",e.toString());
            throw new CustomException(REPRESENTATIVES_SEARCH_QUERY_EXCEPTION, "Exception occurred while building the query: " + e.getMessage());
        }
    }

    public String getRepresentingSearchQuery(List<String> ids, List<Object> preparedStmtList) {
        try {
            StringBuilder query = new StringBuilder(BASE_REPRESENTING_QUERY);
            query.append(FROM_REPRESENTING_TABLE);
            if (!ids.isEmpty()) {
                query.append(" WHERE rpst.representative_id IN (")
                        .append(ids.stream().map(id -> "?").collect(Collectors.joining(",")))
                        .append(")");
                preparedStmtList.addAll(ids);
            }

            return query.toString();
        } catch (Exception e) {
            log.error("Error while building representing search query :: {}",e.toString());
            throw new CustomException(REPRESENTING_SEARCH_QUERY_EXCEPTION, "Exception occurred while building the query: " + e.getMessage());
        }
    }

    public String getLinkedCaseDocumentSearchQuery(List<String> ids, List<Object> preparedStmtList) {
        try {
            StringBuilder query = new StringBuilder(DOCUMENT_SELECT_QUERY_CASE);
            query.append(FROM_DOCUMENTS_TABLE);
            if (!ids.isEmpty()) {
                query.append(" WHERE doc.linked_case_id IN (")
                        .append(ids.stream().map(id -> "?").collect(Collectors.joining(",")))
                        .append(")");
                preparedStmtList.addAll(ids);
            }

            return query.toString();
        } catch (Exception e) {
            log.error("Error while building linked case document search query :: {}",e.toString());
            throw new CustomException(DOCUMENT_SEARCH_QUERY_EXCEPTION, "Exception occurred while building the query: " + e.getMessage());
        }
    }

    public String getLitigantDocumentSearchQuery(List<String> ids, List<Object> preparedStmtList) {
        try {
            StringBuilder query = new StringBuilder(DOCUMENT_SELECT_QUERY_CASE);
            query.append(FROM_DOCUMENTS_TABLE);
            if (!ids.isEmpty()) {
                query.append(" WHERE doc.litigant_id IN (")
                        .append(ids.stream().map(id -> "?").collect(Collectors.joining(",")))
                        .append(")");
                preparedStmtList.addAll(ids);
            }

            return query.toString();
        } catch (Exception e) {
            log.error("Error while building litigant document search query :: {}",e.toString());
            throw new CustomException(DOCUMENT_SEARCH_QUERY_EXCEPTION, "Exception occurred while building the query: " + e.getMessage());
        }
    }

    public String getRepresentativeDocumentSearchQuery(List<String> ids, List<Object> preparedStmtList) {
        try {
            StringBuilder query = new StringBuilder(DOCUMENT_SELECT_QUERY_CASE);
            query.append(FROM_DOCUMENTS_TABLE);
            if (!ids.isEmpty()) {
                query.append(" WHERE doc.representative_id IN (")
                        .append(ids.stream().map(id -> "?").collect(Collectors.joining(",")))
                        .append(")");
                preparedStmtList.addAll(ids);
            }

            return query.toString();
        } catch (Exception e) {
            log.error("Error while building representative document search query :: {}",e.toString());
            throw new CustomException(DOCUMENT_SEARCH_QUERY_EXCEPTION, "Exception occurred while building the query: " + e.getMessage());
        }
    }

    public String getRepresentingDocumentSearchQuery(List<String> ids, List<Object> preparedStmtList) {
        try {
            StringBuilder query = new StringBuilder(DOCUMENT_SELECT_QUERY_CASE);
            query.append(FROM_DOCUMENTS_TABLE);
            if (!ids.isEmpty()) {
                query.append(" WHERE doc.representing_id IN (")
                        .append(ids.stream().map(id -> "?").collect(Collectors.joining(",")))
                        .append(")");
                preparedStmtList.addAll(ids);
            }

            return query.toString();
        } catch (Exception e) {
            log.error("Error while building representing document search query :: {}",e.toString());
            throw new CustomException(DOCUMENT_SEARCH_QUERY_EXCEPTION, "Exception occurred while building the query: " + e.getMessage());
        }
    }

    public String getTotalCountQuery(String baseQuery) {
        return TOTAL_COUNT_QUERY.replace("{baseQuery}", baseQuery);
    }
    public String addPaginationQuery(String query, List<Object> preparedStatementList, Pagination pagination) {
        preparedStatementList.add(pagination.getLimit());
        preparedStatementList.add(pagination.getOffSet());
        return query + " LIMIT ? OFFSET ?";

    }
    public String addOrderByQuery(String query, Pagination pagination) {
        if (pagination == null || pagination.getSortBy() == null || pagination.getOrder() == null) {
            return query + DEFAULT_ORDERBY_CLAUSE;
        } else {
            query = query + ORDERBY_CLAUSE;
        }
        return query.replace("{orderBy}", pagination.getSortBy()).replace("{sortingOrder}", pagination.getOrder().name());
    }
}