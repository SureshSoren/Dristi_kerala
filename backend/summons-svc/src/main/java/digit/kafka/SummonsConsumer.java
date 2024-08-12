package digit.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import digit.service.DemandService;
import digit.service.SummonsService;
import digit.web.models.DeliveryStatus;
import digit.web.models.SummonsRequest;
import digit.web.models.TaskRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import static digit.config.ServiceConstants.*;

import java.util.HashMap;

@Component
@Slf4j
@EnableAsync
public class SummonsConsumer {

    private final SummonsService summonsService;

    private final ObjectMapper objectMapper;

    private final DemandService demandService;

    @Autowired
    public SummonsConsumer(SummonsService summonsService, ObjectMapper objectMapper, DemandService demandService) {
        this.summonsService = summonsService;
        this.objectMapper = objectMapper;
        this.demandService = demandService;
    }

    @KafkaListener(topics = {"${kafka.topic.save.task.application}"})
    @Async
    public void listenForGenerateSummonsDocument(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            TaskRequest taskRequest = objectMapper.convertValue(record, TaskRequest.class);
            String taskType = taskRequest.getTask().getTaskType();
            if (taskType.equalsIgnoreCase(SUMMON) || taskType.equalsIgnoreCase(WARRANT)) {
                log.info(taskRequest.getTask().toString());
                summonsService.generateSummonsDocument(taskRequest);
            }
        } catch (final Exception e) {
            log.error("Error while listening to value: {}: ", record, e);
        }
    }

    @KafkaListener(topics = {"${kafka.topic.save.task.application}"})
    @Async
    public void listenForGenerateSummonsBill(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            TaskRequest taskRequest = objectMapper.convertValue(record, TaskRequest.class);
            String taskType = taskRequest.getTask().getTaskType();
            if (taskType.equalsIgnoreCase(SUMMON) && taskRequest.getTask().getStatus().equalsIgnoreCase("PAYMENT_PENDING")) {
                log.info(taskRequest.getTask().toString());
                demandService.fetchPaymentDetailsAndGenerateDemandAndBill(taskRequest);
            }
        } catch (final Exception e) {
            log.error("Error while listening to value: {}: ", record, e);
        }
    }

    @KafkaListener(topics = {"${kafka.topic.insert.summons}"})
    @Async
    public void listenForInsertSummons(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            SummonsRequest request = objectMapper.convertValue(record, SummonsRequest.class);
            log.info(request.toString());
            if (request.getSummonsDelivery().getDeliveryStatus().equals(DeliveryStatus.DELIVERED)) {
                summonsService.updateTaskStatus(request);
            }
        } catch (final Exception e) {
            log.error("Error while listening to value: {}: ", record, e);
        }
    }

    @KafkaListener(topics = {"${kafka.topic.update.summons}"})
    @Async
    public void listenForUpdateSummons(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            SummonsRequest request = objectMapper.convertValue(record, SummonsRequest.class);
            log.info(request.toString());
            if (request.getSummonsDelivery().getDeliveryStatus().equals(DeliveryStatus.DELIVERED)) {
                summonsService.updateTaskStatus(request);
            }
        } catch (final Exception e) {
            log.error("Error while listening to value: {}: ", record, e);
        }
    }

    @KafkaListener(topics = {"${kafka.topic.issue.summons.application}"})
    @Async
    public void listenForSendSummons(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            TaskRequest taskRequest = objectMapper.convertValue(record, TaskRequest.class);
            log.info(taskRequest.getTask().toString());
            summonsService.sendSummonsViaChannels(taskRequest);
        } catch (final Exception e) {
            log.error("Error while listening to value: {}: ", record, e);
        }
    }
}
