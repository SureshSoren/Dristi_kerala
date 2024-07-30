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

    @Value("${transformer.producer.create.update.order.topic}")
    private String orderCreateTopic;

    @Value("${egov.case.host}")
    private String caseSearchUrlHost;

    @Value("${egov.case.path}")
    private String caseSearchUrlEndPoint;

    @Value("${transformer.producer.update.order.case.topic}")
    private String caseUpdateTopic;

    @Value("${transformer.producer.update.order.application.topic}")
    private String applicationUpdateTopic;

}
