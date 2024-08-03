package digit.service;

import digit.config.Configuration;
import digit.enrichment.SummonsDeliveryEnrichment;
import digit.kafka.Producer;
import digit.repository.SummonsRepository;
import digit.util.ExternalChannelUtil;
import digit.util.FileStorageUtil;
import digit.util.PdfServiceUtil;
import digit.util.TaskUtil;
import digit.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.models.Document;
import org.egov.common.contract.models.Workflow;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class SummonsService {


    private final PdfServiceUtil pdfServiceUtil;

    private final Configuration config;

    private final FileStorageUtil fileStorageUtil;

    private final SummonsRepository summonsRepository;

    private final Producer producer;

    private final SummonsDeliveryEnrichment summonsDeliveryEnrichment;

    private final ExternalChannelUtil externalChannelUtil;

    private final TaskUtil taskUtil;

    @Autowired
    public SummonsService(PdfServiceUtil pdfServiceUtil, Configuration config, Producer producer,
                          FileStorageUtil fileStorageUtil, SummonsRepository summonsRepository,
                          SummonsDeliveryEnrichment summonsDeliveryEnrichment, ExternalChannelUtil externalChannelUtil,
                          TaskUtil taskUtil) {
        this.pdfServiceUtil = pdfServiceUtil;
        this.config = config;
        this.producer = producer;
        this.fileStorageUtil = fileStorageUtil;
        this.summonsRepository = summonsRepository;
        this.summonsDeliveryEnrichment = summonsDeliveryEnrichment;
        this.externalChannelUtil = externalChannelUtil;
        this.taskUtil = taskUtil;
    }

    public TaskResponse generateSummonsDocument(TaskRequest taskRequest) {
        String taskType = taskRequest.getTask().getTaskType();
        String pdfTemplateKey = getPdfTemplateKey(taskType);

        ByteArrayResource byteArrayResource = pdfServiceUtil.generatePdfFromPdfService(taskRequest, config.getEgovStateTenantId(), pdfTemplateKey);
        String fileStoreId = fileStorageUtil.saveDocumentToFileStore(byteArrayResource);

        Document document = createDocument(fileStoreId);
        taskRequest.getTask().addDocumentsItem(document);

        return taskUtil.callUpdateTask(taskRequest);
    }

    public SummonsDelivery sendSummonsViaChannels(TaskRequest request) {
        SummonsDelivery summonsDelivery = summonsDeliveryEnrichment.generateAndEnrichSummonsDelivery(request.getTask(), request.getRequestInfo());

        ChannelMessage channelMessage = externalChannelUtil.sendSummonsByDeliveryChannel(request, summonsDelivery);

        if (channelMessage.getAcknowledgementStatus().equalsIgnoreCase("success")) {
            summonsDelivery.setIsAcceptedByChannel(Boolean.TRUE);
            if (summonsDelivery.getChannelName() == ChannelName.SMS || summonsDelivery.getChannelName() == ChannelName.EMAIL) {
                summonsDelivery.setDeliveryStatus("SUMMONS_DELIVERED");
            } else {
                summonsDelivery.setDeliveryStatus("SUMMONS_IN_PROGRESS");
            }
            summonsDelivery.setChannelAcknowledgementId(channelMessage.getAcknowledgeUniqueNumber());
        }
        SummonsRequest summonsRequest = createSummonsRequest(request.getRequestInfo(), summonsDelivery);

        producer.push("insert-summons", summonsRequest);
        return summonsDelivery;
    }

    public List<SummonsDelivery> getSummonsDelivery(SummonsDeliverySearchRequest request) {
        return getSummonsDeliveryFromSearchCriteria(request.getSearchCriteria());
    }

    public ChannelMessage updateSummonsDeliveryStatus(UpdateSummonsRequest request) {
        SummonsDelivery summonsDelivery = fetchSummonsDelivery(request);

        enrichAndUpdateSummonsDelivery(summonsDelivery, request);

        SummonsRequest summonsRequest = createSummonsRequest(request.getRequestInfo(), summonsDelivery);
        producer.push("update-summons", summonsRequest);

        return createChannelMessage(summonsDelivery);
    }

    private void enrichAndUpdateSummonsDelivery(SummonsDelivery summonsDelivery, UpdateSummonsRequest request) {
        summonsDeliveryEnrichment.enrichForUpdate(summonsDelivery, request.getRequestInfo());
        ChannelReport channelReport = request.getChannelReport();
        summonsDelivery.setDeliveryStatus(channelReport.getDeliveryStatus());
        summonsDelivery.setAdditionalFields(channelReport.getAdditionalFields());
    }

    public void updateTaskStatus(SummonsRequest request) {
        TaskCriteria taskCriteria = TaskCriteria.builder().taskNumber(request.getSummonsDelivery().getTaskNumber()).build();
        TaskSearchRequest searchRequest = TaskSearchRequest.builder()
                .requestInfo(request.getRequestInfo()).criteria(taskCriteria).build();
        TaskListResponse taskListResponse = taskUtil.callSearchTask(searchRequest);
        Task task = taskListResponse.getList().get(0);
        if (task.getTaskType().equalsIgnoreCase("summon")) {
            Workflow workflow = Workflow.builder().action("SERVE").build();
            task.setWorkflow(workflow);
        } else if (task.getTaskType().equalsIgnoreCase("warrant")) {
            Workflow workflow = Workflow.builder().action("DELIVERED").build();
            task.setWorkflow(workflow);
        }
        TaskRequest taskRequest = TaskRequest.builder()
                .requestInfo(request.getRequestInfo()).task(task).build();
        taskUtil.callUpdateTask(taskRequest);
    }

    private String getPdfTemplateKey(String taskType) {
        return switch (taskType.toLowerCase()) {
            case "summon" -> config.getSummonsPdfTemplateKey();
            case "warrant" -> config.getWarrantPdfTemplateKey();
            case "bail" -> config.getBailPdfTemplateKey();
            default -> throw new CustomException("INVALID_TASK_TYPE", "Task Type must be valid. Provided: " + taskType);
        };
    }

    private SummonsDelivery fetchSummonsDelivery(UpdateSummonsRequest request) {
        SummonsDeliverySearchCriteria searchCriteria = SummonsDeliverySearchCriteria.builder()
                .summonsId(request.getChannelReport().getSummonId())
                .build();
        Optional<SummonsDelivery> optionalSummons = getSummonsDeliveryFromSearchCriteria(searchCriteria).stream().findFirst();
        if (optionalSummons.isEmpty()) {
            throw new CustomException("SUMMONS_UPDATE_ERROR", "Invalid summons delivery id was provided");
        }
        return optionalSummons.get();
    }

    private List<SummonsDelivery> getSummonsDeliveryFromSearchCriteria(SummonsDeliverySearchCriteria searchCriteria) {
        return summonsRepository.getSummons(searchCriteria);
    }

    private Document createDocument(String fileStoreId) {
        Field field = Field.builder().key("FILE_CATEGORY").value("GENERATE_SUMMONS_DOCUMENT").build();
        AdditionalFields additionalFields = AdditionalFields.builder().fields(Collections.singletonList(field)).build();
        return Document.builder()
                .fileStore(fileStoreId)
                .documentType("application/pdf")
                .additionalDetails(additionalFields)
                .build();
    }

    private SummonsRequest createSummonsRequest(RequestInfo requestInfo, SummonsDelivery summonsDelivery) {
        return SummonsRequest.builder()
                .requestInfo(requestInfo)
                .summonsDelivery(summonsDelivery)
                .build();
    }

    private ChannelMessage createChannelMessage(SummonsDelivery summonsDelivery) {
        return ChannelMessage.builder()
                .acknowledgeUniqueNumber(summonsDelivery.getSummonDeliveryId())
                .acknowledgementStatus("SUCCESS")
                .build();
    }
}
