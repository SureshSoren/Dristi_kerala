package drishti.payment.calculator.repository.querybuilder;

import drishti.payment.calculator.web.models.HubSearchCriteria;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PostalHubQueryBuilder {


    private static final String BASE_APPLICATION_QUERY = "SELECT * ";
    private static final String FROM_TABLES = " FROM postal_hub ph ";
    private static final String LEFT_JOIN = " LEFT JOIN address a ON ph.addressid = a.id ";
    private static final String ORDER_BY = " ORDER BY ";
    private static final String GROUP_BY = " GROUP BY ";
    private static final String LIMIT_OFFSET = " LIMIT ? OFFSET ?";

    public String getPostalHubQuery(HubSearchCriteria criteria, List<Object> preparedStmtList, Integer limit, Integer offset) {

        StringBuilder query = new StringBuilder(BASE_APPLICATION_QUERY);
        query.append(FROM_TABLES);
        query.append(LEFT_JOIN);
        if(!ObjectUtils.isEmpty(criteria.getHubId())){
            addClauseIfRequired(query, preparedStmtList);
            query.append(" ph.hub_id = ? ");
            preparedStmtList.add(criteria.getHubId());
        }
        if(!ObjectUtils.isEmpty(criteria.getName())){
            addClauseIfRequired(query, preparedStmtList);
            query.append(" ph.name = ? ");
            preparedStmtList.add(criteria.getName());
        }
        if(!ObjectUtils.isEmpty(criteria.getPincode())){
            addClauseIfRequired(query, preparedStmtList);
            query.append(" ph.pincode = ? ");
            preparedStmtList.add(criteria.getPincode());
        }
        return query.toString();

    }


    private void addClauseIfRequired(StringBuilder query, List<Object> preparedStmtList) {
        if (preparedStmtList.isEmpty()) {
            query.append(" WHERE ");
        } else {
            query.append(" AND ");
        }
    }
}
