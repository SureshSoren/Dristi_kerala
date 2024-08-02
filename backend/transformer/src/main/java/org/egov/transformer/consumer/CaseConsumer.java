package org.egov.transformer.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.egov.transformer.config.TransformerProperties;
import org.egov.transformer.models.CaseRequest;
import org.egov.transformer.models.CourtCase;
import org.egov.transformer.models.Order;
import org.egov.transformer.models.OrderRequest;
import org.egov.transformer.service.CaseService;
import org.egov.transformer.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CaseConsumer {


    private static final Logger logger = LoggerFactory.getLogger(OrderConsumer.class);
    private final ObjectMapper objectMapper;

    private  final CaseService caseService;
    private final TransformerProperties transformerProperties;

    @Autowired
    public CaseConsumer(ObjectMapper objectMapper, CaseService caseService, TransformerProperties transformerProperties) {
        this.objectMapper = objectMapper;
        this.caseService = caseService;
        this.transformerProperties = transformerProperties;
    }

    @KafkaListener(topics = {"${case.kafka.create.topic}"})
    public void saveCase(ConsumerRecord<String, Object> payload,
                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        publishCase(payload, transformerProperties.getSaveCaseTopic());
    }

    @KafkaListener(topics = {"${case.kafka.update.topic}"})
    public void updateCase(ConsumerRecord<String, Object> payload,
                            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        publishCase(payload, transformerProperties.getUpdateCaseTopic());
    }


    private void publishCase(ConsumerRecord<String, Object> payload,
                              @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
           CourtCase courtCase = (objectMapper.readValue((String) payload.value(), new TypeReference<CaseRequest>() {
            })).getCases();
            logger.info(objectMapper.writeValueAsString(courtCase));
        } catch (Exception exception) {
            log.error("error in saving order", exception);
        }
    }
}
