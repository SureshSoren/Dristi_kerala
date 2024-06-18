package drishti.payment.calculator.repository.querybuilder;

import drishti.payment.calculator.web.models.HubSearchCriteria;
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

        return BASE_APPLICATION_QUERY + FROM_TABLES + LEFT_JOIN;

    }
}
