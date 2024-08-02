package digit.service.hearing;


import digit.kafka.Producer;
import digit.mapper.CustomMapper;
import digit.service.HearingService;
import digit.web.models.ScheduleHearing;
import digit.web.models.ScheduleHearingRequest;
import digit.web.models.hearing.Hearing;
import digit.web.models.hearing.HearingRequest;
import digit.web.models.hearing.PresidedBy;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class HearingProcessor {

    private final CustomMapper customMapper;

    private final HearingService hearingService;

    private final Producer producer;

    @Autowired
    public HearingProcessor(CustomMapper customMapper, HearingService hearingService, Producer producer) {
        this.customMapper = customMapper;
        this.hearingService = hearingService;
        this.producer = producer;
    }


    public void processCreateHearingRequest(HearingRequest hearingRequest) {

        Hearing hearing = hearingRequest.getHearing();
        RequestInfo requestInfo = hearingRequest.getRequestInfo();
        PresidedBy presidedBy = hearing.getPresidedBy();

        ScheduleHearing scheduleHearing = customMapper.hearingToScheduleHearingConversion(hearing);

        ScheduleHearingRequest request = ScheduleHearingRequest.builder()
                .hearing(Collections.singletonList(scheduleHearing))
                .requestInfo(requestInfo)
                .build();

        List<ScheduleHearing> scheduledHearings = hearingService.schedule(request);
        ScheduleHearing scheduledHearing = scheduledHearings.get(0);

        hearing.setStartTime(scheduledHearing.getStartTime());
        hearing.setEndTime(scheduledHearing.getEndTime());

        hearingRequest.setHearing(hearing);

        producer.push("update topic", hearingRequest);
    }

}
