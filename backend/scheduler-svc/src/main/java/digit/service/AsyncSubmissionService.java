package digit.service;

import digit.config.Configuration;
import digit.enrichment.AsyncSubmissionEnrichment;
import digit.kafka.Producer;
import digit.repository.AsyncSubmissionRepository;
import digit.util.ResponseInfoFactory;
import digit.validator.AsyncSubmissionValidator;
import digit.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class AsyncSubmissionService {

    private AsyncSubmissionRepository repository;

    private Producer producer;

    private Configuration config;

    private WorkflowService workflowService;

    private AsyncSubmissionEnrichment enrichment;

    private AsyncSubmissionValidator validator;

    @Autowired
    public AsyncSubmissionService(AsyncSubmissionRepository repository, Producer producer,
                                  Configuration config, WorkflowService workflowService,
                                  AsyncSubmissionEnrichment enrichment, AsyncSubmissionValidator validator) {
        this.repository = repository;
        this.producer = producer;
        this.config = config;
        this.workflowService = workflowService;
        this.enrichment = enrichment;
        this.validator = validator;
    }


    public AsyncSubmission getDueDates(AsyncSubmissionRequest submissionRequest) {
        log.info("operation = getDueDates, result = IN_PROGRESS");
        AsyncSubmission asyncSubmission = submissionRequest.getAsyncSubmission();
        asyncSubmission.setSubmissionDate(LocalDate.now().plusDays(config.getMinAsyncSubmissionDays()).toString());
        asyncSubmission.setResponseDate(LocalDate.now().plusDays(config.getMinAsyncResponseDays()).toString());
//        validator.validateDates(asyncSubmission);
        log.info("operation = getDueDates, result = SUCCESS");
        return asyncSubmission;
    }

    public List<AsyncSubmission> getAsyncSubmissions(AsyncSubmissionSearchRequest searchRequest) {
        log.info("operation = getAsyncSubmissions, with searchRequest : {}", searchRequest.toString());
        return repository.getAsyncSubmissions(searchRequest.getAsyncSubmissionSearchCriteria());
    }

    public AsyncSubmissionResponse saveAsyncSubmissions(AsyncSubmissionRequest request) {
        log.info("operation = saveAsyncSubmissions, result = IN_PROGRESS");
        AsyncSubmission asyncSubmission = request.getAsyncSubmission();
        validator.validateSubmissionDates(asyncSubmission);
        enrichment.enrichAsyncSubmissions(request.getRequestInfo(), asyncSubmission);
        AsyncSubmissionResponse asyncSubmissionResponse = AsyncSubmissionResponse.builder()
                .responseInfo(ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true))
                .asyncSubmissions(Collections.singletonList(asyncSubmission))
                .build();
        producer.push(config.getAsyncSubmissionSaveTopic(), asyncSubmissionResponse);
        log.info("operation = saveAsyncSubmissions, result = SUCCESS");
        return asyncSubmissionResponse;
    }

    public AsyncSubmissionResponse updateAsyncSubmissions(AsyncSubmissionRequest request) {
        log.info("operation = updateAsyncSubmissions, result = IN_PROGRESS");
        AsyncSubmission asyncSubmission = request.getAsyncSubmission();
        if (StringUtils.isEmpty(asyncSubmission.getSubmissionId())) {
            throw new CustomException("DK_AS_APP_ERR", "async submission id should not be null");
        }
        AsyncSubmissionSearchCriteria searchCriteria = AsyncSubmissionSearchCriteria.builder()
                .submissionIds(Collections.singletonList(asyncSubmission.getSubmissionId())).build();
        List<AsyncSubmission> asyncSubmissions = repository.getAsyncSubmissions(searchCriteria);
        if (CollectionUtils.isEmpty(asyncSubmissions) && asyncSubmissions.size() != 1) {
            throw new CustomException("DK_AS_APP_ERR", "async submission id provided must be valid");
        }
        validator.validateSubmissionDates(asyncSubmission);
        enrichment.enrichUpdateAsyncSubmission(request.getRequestInfo(), asyncSubmission);
        AsyncSubmissionResponse asyncSubmissionResponse = AsyncSubmissionResponse.builder()
                .responseInfo(ResponseInfoFactory.createResponseInfo(request.getRequestInfo(), true))
                .asyncSubmissions(Collections.singletonList(asyncSubmission))
                .build();
        producer.push(config.getAsyncSubmissionUpdateTopic(), asyncSubmissionResponse);
        log.info("operation = updateAsyncSubmissions, result = SUCCESS");
        return asyncSubmissionResponse;
    }
}
