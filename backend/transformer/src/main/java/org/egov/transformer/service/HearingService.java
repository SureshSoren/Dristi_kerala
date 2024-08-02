package org.egov.transformer.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.transformer.config.TransformerProperties;
import org.egov.transformer.models.CourtCase;
import org.egov.transformer.models.Hearing;
import org.egov.transformer.models.HearingRequest;
import org.egov.transformer.producer.TransformerProducer;
import org.egov.transformer.util.CaseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HearingService {

    private final TransformerProducer producer;
    private final CaseUtil caseUtil;
    private final TransformerProperties properties;
     @Autowired
    public HearingService(TransformerProducer producer, CaseUtil caseUtil, TransformerProperties properties) {
         this.producer = producer;
        this.caseUtil = caseUtil;
         this.properties = properties;
     }

    public void addCaseDetailsToHearing(Hearing hearing,String topic)
    {
        CourtCase courtCase = caseUtil.fetchCase(hearing.getFilingNumber().get(0));
        hearing.setFilingDate(courtCase.getFilingDate());
        hearing.setRegistrationDate(courtCase.getRegistrationDate());
        HearingRequest hearingRequest = new HearingRequest();
        hearingRequest.setHearing(hearing);
        producer.push(properties.getSaveHearingTopic(),hearingRequest);

    }

}
