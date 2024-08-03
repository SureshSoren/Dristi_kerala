package digit.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import digit.config.Configuration;
import digit.models.coremodels.ProcessInstance;
import digit.models.coremodels.ProcessInstanceRequest;
import digit.models.coremodels.ProcessInstanceResponse;
import digit.models.coremodels.State;
import digit.repository.ServiceRequestRepository;
import digit.web.models.ReScheduleHearing;
import digit.web.models.ReScheduleHearingRequest;
import digit.web.models.Workflow;
import digit.web.models.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.models.RequestInfoWrapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.common.contract.workflow.BusinessService;
import org.egov.common.contract.workflow.BusinessServiceResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Service
@Slf4j
public class WorkflowService {

    private final ObjectMapper mapper;

    private final ServiceRequestRepository repository;

    private final Configuration config;

    @Autowired
    public WorkflowService(ObjectMapper mapper, ServiceRequestRepository repository, Configuration config) {
        this.mapper = mapper;
        this.repository = repository;
        this.config = config;
    }

    public void updateWorkflowStatus(ReScheduleHearingRequest reScheduleHearingRequest) {
        reScheduleHearingRequest.getReScheduleHearing().forEach(application -> {
            ProcessInstance processInstance = getProcessInstanceForHearingReScheduler(application, reScheduleHearingRequest.getRequestInfo());
            ProcessInstanceRequest workflowRequest = new ProcessInstanceRequest(reScheduleHearingRequest.getRequestInfo(), Collections.singletonList(processInstance));
            State state = callWorkFlow(workflowRequest);
            application.setStatus(state.getApplicationStatus());
        });
    }

    public State callWorkFlow(ProcessInstanceRequest workflowReq) {
            ProcessInstanceResponse response = null;
            StringBuilder url = new StringBuilder(config.getWfHost().concat(config.getWfTransitionPath()));
            Object optional = repository.fetchResult(url, workflowReq);
            response = mapper.convertValue(optional, ProcessInstanceResponse.class);
            return response.getProcessInstances().get(0).getState();
    }

    private ProcessInstance getProcessInstanceForHearingReScheduler(ReScheduleHearing application, RequestInfo requestInfo) {

        log.info("operation= getProcessInstanceForHearingReScheduler, result=IN_PROGRESS, tenantId={}", application.getTenantId());
        Workflow workflow = new Workflow();

        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setBusinessId(application.getRescheduledRequestId());
        processInstance.setAction(workflow.getAction());
        processInstance.setModuleName("reschedule-hearing-services");
        processInstance.setTenantId(application.getTenantId());
        processInstance.setBusinessService("RESCHEDULER");
        processInstance.setDocuments(workflow.getDocuments());
        processInstance.setComment(workflow.getComment());

        if (!CollectionUtils.isEmpty(workflow.getAssignees())) {
            List<User> users = new ArrayList<>();

            workflow.getAssignees().forEach(uuid -> {
                User user = new User();
                user.setUuid(uuid);
                users.add(user);
            });

            processInstance.setAssignes(users);
        }
        log.info("operation= getProcessInstanceForHearingReScheduler, result=SUCCESS, judgeId={}, processInstanceAction={}", application.getJudgeId(), processInstance.getAction());
        return processInstance;
    }

    public ProcessInstance getCurrentWorkflow(RequestInfo requestInfo, String tenantId, String businessId) {
        log.info("operation=getCurrentWorkflow, result=IN_PROCESS, tenantId={}, businessId={}", tenantId, businessId);
        RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
        StringBuilder url = getSearchURLWithParams(tenantId, businessId);
        Object res;
        try {
            res = repository.fetchResult(url, requestInfoWrapper);
        } catch (Exception e) {
            throw new CustomException("SERVICE_CALL_EXCEPTION", "Failed to fetch workflow from workflow service");
        }
        ProcessInstanceResponse response = null;
        try {
            response = mapper.convertValue(res, ProcessInstanceResponse.class);
        } catch (Exception e) {
            throw new CustomException("PARSING_ERROR", "Failed to parse workflow search response");
        }

        if (response != null && !CollectionUtils.isEmpty(response.getProcessInstances()) && response.getProcessInstances().get(0) != null) {
            log.info("operation= getCurrentWorkflow, result=SUCCESS, ProcessInstance={}", response.getProcessInstances());
            return response.getProcessInstances().get(0);
        }
        return null;
    }


    private BusinessService getBusinessService(ReScheduleHearing application, RequestInfo requestInfo) {
        log.info("operation = getBusinessService, result=IN_PROGRESS, judgeId={}, tenantId={}", application.getJudgeId(), application.getTenantId());
        String tenantId = application.getTenantId();
        StringBuilder url = getSearchURLWithParams(tenantId, "RESCHEDULER");
        RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
        Object result;
        try {
            result = repository.fetchResult(url, requestInfoWrapper);
        } catch (Exception e) {
            throw new CustomException("SERVICE_CALL_EXCEPTION", "Failed to fetch business service from workflow service");
        }
        BusinessServiceResponse response = null;
        try {
            response = mapper.convertValue(result, BusinessServiceResponse.class);
        } catch (IllegalArgumentException e) {
            throw new CustomException("PARSING ERROR", "Failed to parse response of workflow business service search");
        }

        if (CollectionUtils.isEmpty(response.getBusinessServices()))
            throw new CustomException("BUSINESSSERVICE_NOT_FOUND", "The businessService " + "RESCHEDULER" + " is not found");

        log.info("operation=getBusinessService, result=SUCCESS, judgeId={}, BusinessServiceResponse={}", application.getJudgeId(), response.getBusinessServices());

        return response.getBusinessServices().get(0);
    }

    private StringBuilder getSearchURLWithParams(String tenantId, String businessService) {

        StringBuilder url = new StringBuilder(config.getWfHost());
        url.append(config.getWfBusinessServiceSearchPath());
        url.append("?tenantId=");
        url.append(tenantId);
        url.append("&businessServices=");
        url.append(businessService);
        return url;
    }
}
