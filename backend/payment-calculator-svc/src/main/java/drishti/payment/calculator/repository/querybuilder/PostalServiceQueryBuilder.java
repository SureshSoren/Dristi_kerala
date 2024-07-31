package drishti.payment.calculator.repository.querybuilder;


import drishti.payment.calculator.web.models.PostalServiceSearchCriteria;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Component
public class PostalServiceQueryBuilder {

    private static final String BASE_APPLICATION_QUERY = "SELECT * ";
    private static final String FROM_TABLES = " FROM postal_service ps ";
    private static final String ORDER_BY = " ORDER BY ";
    private static final String GROUP_BY = " GROUP BY ";
    private static final String LIMIT_OFFSET = " LIMIT ? OFFSET ?";


    public String getPostalServiceQuery(PostalServiceSearchCriteria criteria, List<Object> preparedStmtList, Integer limit, Integer offset) {

            StringBuilder query = new StringBuilder(BASE_APPLICATION_QUERY);
            query.append(FROM_TABLES);

            if(!ObjectUtils.isEmpty(criteria.getId())){
                addClauseIfRequired(query, preparedStmtList);
                query.append(" ps.postal_service_id = ? ");
                preparedStmtList.add(criteria.getId());
            }
            if(!ObjectUtils.isEmpty(criteria.getPincode())){
                addClauseIfRequired(query, preparedStmtList);
                query.append(" ps.pincode = ? ");
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
