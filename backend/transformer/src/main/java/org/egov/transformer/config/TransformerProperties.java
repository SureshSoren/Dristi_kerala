package org.egov.transformer.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Component
public class TransformerProperties {

    @Value("${egov.case.host}")
    private String caseSearchUrlHost;

    @Value("${egov.case.path}")
    private String caseSearchUrlEndPoint;



    @Value("${transformer.producer.create.task.topic}")
    private String saveTaskTopic;

    @Value("${transformer.producer.update.task.topic}")
    private String updateTaskTopic;

    @Value("${transformer.producer.update.order.application.topic}")
    private String applicationUpdateTopic;

    @Value("${transformer.producer.save.order.topic}")
    private String saveOrderTopic;

    @Value("${transformer.producer.update.order.topic}")
    private String updateOrderTopic;

    @Value("${case.kafka.create.topic}")
    private String saveCaseTopic;

    @Value("${case.kafka.update.topic}")
    private String updateCaseTopic;

    @Value("${kafka.topics.hearing.create}")
    private String saveHearingTopic;

    @Value("${kafka.topics.hearing.update}")
    private String updateHearingTopic;

    @Value("${egov.case.host}")
    private String caseHostTopic;

    @Value("${egov.case.path}")
    private  String casePathTopic;



}
