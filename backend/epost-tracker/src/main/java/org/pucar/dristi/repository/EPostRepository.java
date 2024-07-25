package org.pucar.dristi.repository;

import lombok.extern.slf4j.Slf4j;
import org.pucar.dristi.model.EPostResponse;
import org.pucar.dristi.model.EPostTracker;
import org.pucar.dristi.model.EPostTrackerSearchCriteria;
import org.pucar.dristi.model.Pagination;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class EPostRepository {

    private final JdbcTemplate jdbcTemplate;
    private final EPostQueryBuilder queryBuilder;
    private final EPostRowMapper rowMapper;

    public EPostRepository(JdbcTemplate jdbcTemplate, EPostQueryBuilder queryBuilder, EPostRowMapper rowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.queryBuilder = queryBuilder;
        this.rowMapper = rowMapper;
    }

    public EPostResponse getEPostTrackerResponse(EPostTrackerSearchCriteria searchCriteria){
        List<EPostTracker> ePostTrackerList = getEPostTrackerList(searchCriteria);
        Integer totalRecords = getTotalCountQuery(searchCriteria);
        Pagination pagination = searchCriteria.getPagination();
        pagination.setTotalCount(totalRecords);
        return EPostResponse.builder().ePostTrackers(ePostTrackerList).pagination(pagination).build();
    }

    public List<EPostTracker> getEPostTrackerList(EPostTrackerSearchCriteria searchCriteria){
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getEPostTrackerSearchQuery(searchCriteria, preparedStmtList);
        queryBuilder.addPaginationQuery(query, preparedStmtList, searchCriteria.getPagination());
        log.debug("Final query: " + query);
        return jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
    }

    private Integer getTotalCountQuery(EPostTrackerSearchCriteria searchCriteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getEPostTrackerSearchQuery(searchCriteria, preparedStmtList);
        String countQuery = queryBuilder.getTotalCountQuery(query);
        log.info("Final count query :: {}", countQuery);
        return jdbcTemplate.queryForObject(countQuery, preparedStmtList.toArray(), Integer.class);
    }
}
