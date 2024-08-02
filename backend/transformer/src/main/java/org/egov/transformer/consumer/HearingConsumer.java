package org.egov.transformer.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.egov.transformer.config.TransformerProperties;
import org.egov.transformer.models.CaseRequest;
import org.egov.transformer.models.CourtCase;
import org.egov.transformer.models.Hearing;
import org.egov.transformer.models.HearingRequest;
import org.egov.transformer.service.CaseService;
import org.egov.transformer.service.HearingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

public class HearingConsumer {
    private static final Logger logger = LoggerFactory.getLogger(HearingConsumer.class);
    private final ObjectMapper objectMapper;

    private  final HearingService hearingService;
    private final TransformerProperties transformerProperties;

    @Autowired
    public HearingConsumer(ObjectMapper objectMapper, HearingService hearingService, TransformerProperties transformerProperties) {
        this.objectMapper = objectMapper;
        this.hearingService = hearingService;
        this.transformerProperties = transformerProperties;
    }
    @KafkaListener(topics = {"${kafka.topics.hearing.create}"})
    public void saveHearing(ConsumerRecord<String, Object> payload,
                              @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        publishHearing(payload, transformerProperties.getSaveHearingTopic());
    }
    @KafkaListener(topics = {"${kafka.topics.hearing.update}"})
    public void updateHearing(ConsumerRecord<String, Object> payload,
                           @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        publishHearing(payload, transformerProperties.getUpdateHearingTopic());
    }


    private void publishHearing(ConsumerRecord<String, Object> payload,
                             @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            Hearing hearing = (objectMapper.readValue((String) payload.value(), new TypeReference<HearingRequest>() {
            })).getHearing();
            logger.info(objectMapper.writeValueAsString(hearing));
            hearingService.addCaseDetailsToHearing(hearing,topic);
        } catch (Exception exception) {
            logger.error("error in saving hearing", exception);
        }
    }
}
