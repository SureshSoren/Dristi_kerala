package org.egov.transformer.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.egov.transformer.models.Order;
import org.egov.transformer.models.OrderRequest;
import org.egov.transformer.models.Task;
import org.egov.transformer.models.TaskRequest;
import org.egov.transformer.service.OrderService;
import org.egov.transformer.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TaskConsumer {

    private static final Logger logger = LoggerFactory.getLogger(TaskConsumer.class);

    private final ObjectMapper objectMapper;
    private final TaskService taskService;

    @Autowired
    public TaskConsumer(ObjectMapper objectMapper, OrderService orderService, TaskService taskService) {
        this.objectMapper = objectMapper;
        this.taskService = taskService;
    }

    @KafkaListener(topics = { "${transformer.consumer.create.task.topic}","${transformer.consumer.update.task.topic}"})
    public void taskConsumerCreate(ConsumerRecord<String, Object> payload,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic){
        try {
            Task task = (objectMapper.readValue((String) payload.value(), new TypeReference<TaskRequest>() {})).getTask();
            logger.info(objectMapper.writeValueAsString(task));
            taskService.addTaskDetails(task);
        } catch (Exception exception) {
            log.error("error in saving task", exception);
        }
    }




}
