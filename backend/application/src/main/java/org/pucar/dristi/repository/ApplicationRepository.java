package org.pucar.dristi.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.models.Document;
import org.egov.tracer.model.CustomException;
import org.pucar.dristi.repository.queryBuilder.ApplicationQueryBuilder;
import org.pucar.dristi.repository.rowMapper.ApplicationRowMapper;
import org.pucar.dristi.repository.rowMapper.DocumentRowMapper;
import org.pucar.dristi.repository.rowMapper.StatuteSectionRowMapper;
import org.pucar.dristi.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.pucar.dristi.config.ServiceConstants.APPLICATION_EXIST_EXCEPTION;
import static org.pucar.dristi.config.ServiceConstants.APPLICATION_SEARCH_ERR;

@Slf4j
@Repository
public class ApplicationRepository {
    private final ApplicationQueryBuilder queryBuilder;
    private final JdbcTemplate jdbcTemplate;
    private final ApplicationRowMapper rowMapper;
    private final DocumentRowMapper documentRowMapper;
    private final StatuteSectionRowMapper statuteSectionRowMapper;

    @Autowired
    public ApplicationRepository(
            ApplicationQueryBuilder queryBuilder,
            JdbcTemplate jdbcTemplate,
            ApplicationRowMapper rowMapper,
            DocumentRowMapper documentRowMapper,
            StatuteSectionRowMapper statuteSectionRowMapper) {
        this.queryBuilder = queryBuilder;
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = rowMapper;
        this.documentRowMapper = documentRowMapper;
        this.statuteSectionRowMapper = statuteSectionRowMapper;
    }

    public List<Application> getApplications(ApplicationSearchRequest applicationSearchRequest) {

        try {
            List<Application> applicationList = new ArrayList<>();
            List<Object> preparedStmtList = new ArrayList<>();
            List<Object> preparedStmtListSt;
            List<Object> preparedStmtListDoc;

            String applicationQuery = queryBuilder.getApplicationSearchQuery(applicationSearchRequest.getCriteria().getId(), applicationSearchRequest.getCriteria().getFilingNumber(), applicationSearchRequest.getCriteria().getCnrNumber(),
                    applicationSearchRequest.getCriteria().getTenantId(), applicationSearchRequest.getCriteria().getStatus(),
                    applicationSearchRequest.getCriteria().getApplicationNumber(), preparedStmtList);
            applicationQuery = queryBuilder.addOrderByQuery(applicationQuery, applicationSearchRequest.getPagination());
            log.info("Final application search query: {}", applicationQuery);
            if(applicationSearchRequest.getPagination() !=  null) {
                Integer totalRecords = getTotalCountApplication(applicationQuery, preparedStmtList);
                log.info("Total count without pagination :: {}", totalRecords);
                applicationSearchRequest.getPagination().setTotalCount(Double.valueOf(totalRecords));
                applicationQuery = queryBuilder.addPaginationQuery(applicationQuery, applicationSearchRequest.getPagination(), preparedStmtList);
            }

            List<Application> list = jdbcTemplate.query(applicationQuery, preparedStmtList.toArray(), rowMapper);
            log.info("DB application list :: {}", list);
            if (list != null) {
                applicationList.addAll(list);
            }

            List<String> ids = new ArrayList<>();
            for (Application application : applicationList) {
                ids.add(application.getId().toString());
            }
            if (ids.isEmpty()) {
                return applicationList;
            }

            String statueAndSectionQuery = "";
            preparedStmtListSt = new ArrayList<>();
            statueAndSectionQuery = queryBuilder.getStatuteSectionSearchQuery(ids, preparedStmtListSt);
            log.info("Final statue and sections query: {}", statueAndSectionQuery);
            Map<UUID, StatuteSection> statuteSectionsMap = jdbcTemplate.query(statueAndSectionQuery, preparedStmtListSt.toArray(), statuteSectionRowMapper);
            log.info("DB statute sections map :: {}", statuteSectionsMap);
            if (statuteSectionsMap != null) {
                applicationList.forEach(application -> {
                    application.setStatuteSection(statuteSectionsMap.get(application.getId()));
                });
            }

            String documentQuery = "";
            preparedStmtListDoc = new ArrayList<>();
            documentQuery = queryBuilder.getDocumentSearchQuery(ids, preparedStmtListDoc);
            log.info("Final document query: {}", documentQuery);
            Map<UUID, List<Document>> documentMap = jdbcTemplate.query(documentQuery, preparedStmtListDoc.toArray(), documentRowMapper);
            log.info("DB document map :: {}", documentMap);
            if (documentMap != null) {
                applicationList.forEach(application -> {
                    application.setDocuments(documentMap.get(application.getId()));
                });
            }
            return applicationList;
        }
        catch (CustomException e){
            throw e;
        }
        catch (Exception e){
            log.error("Error while fetching application list {}", e.getMessage());
            throw new CustomException(APPLICATION_SEARCH_ERR,"Error while fetching application list: "+e.getMessage());
        }
    }

    public Integer getTotalCountApplication(String baseQuery, List<Object> preparedStmtList) {
        String countQuery = queryBuilder.getTotalCountQuery(baseQuery);
        log.info("Final count query :: {}", countQuery);
        return jdbcTemplate.queryForObject(countQuery, preparedStmtList.toArray(), Integer.class);
    }

    public List<ApplicationExists> checkApplicationExists(List<ApplicationExists> applicationExistsList) {
        try {
            for (ApplicationExists applicationExist : applicationExistsList) {
                if ((applicationExist.getFilingNumber() == null || applicationExist.getFilingNumber().isEmpty()) &&
                        (applicationExist.getCnrNumber() == null || applicationExist.getCnrNumber().isEmpty()) &&
                        (applicationExist.getApplicationNumber() == null || applicationExist.getApplicationNumber().isEmpty()) )
                    {
                        applicationExist.setExists(false);
                } else {
                    List<Object> preparedStmtList = new ArrayList<>();
                    String applicationExistQuery = queryBuilder.checkApplicationExistQuery(applicationExist.getFilingNumber(), applicationExist.getCnrNumber(), applicationExist.getApplicationNumber(), preparedStmtList);
                    log.info("Final application exist query: {}", applicationExistQuery);
                    Integer count = jdbcTemplate.queryForObject(applicationExistQuery, preparedStmtList.toArray(), Integer.class);
                    applicationExist.setExists(count != null && count > 0);
                }
            }
            return applicationExistsList;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error while checking application exist");
            throw new CustomException(APPLICATION_EXIST_EXCEPTION, "Custom exception while checking application exist : " + e.getMessage());
        }
    }
}
