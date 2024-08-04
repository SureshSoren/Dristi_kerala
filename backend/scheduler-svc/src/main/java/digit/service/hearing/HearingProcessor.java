package digit.service.hearing;


import digit.kafka.Producer;
import digit.mapper.CustomMapper;
import digit.service.HearingService;
import digit.web.models.Pair;
import digit.web.models.ScheduleHearing;
import digit.web.models.ScheduleHearingRequest;
import digit.web.models.hearing.Hearing;
import digit.web.models.hearing.HearingRequest;
import digit.web.models.hearing.PresidedBy;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

        Pair<Long, Long> startTimeAndEndTime = getStartTimeAndEndTime(hearing.getStartTime());


        ScheduleHearing scheduleHearing = customMapper.hearingToScheduleHearingConversion(hearing);

        scheduleHearing.setStartTime(startTimeAndEndTime.getKey());
        scheduleHearing.setEndTime(startTimeAndEndTime.getValue());

        // currently one judge only
        scheduleHearing.setJudgeId(presidedBy.getJudgeID().get(0));
        scheduleHearing.setCourtId(presidedBy.getCourtID());

        ScheduleHearingRequest request = ScheduleHearingRequest.builder()
                .hearing(Collections.singletonList(scheduleHearing))
                .requestInfo(requestInfo)
                .build();

        List<ScheduleHearing> scheduledHearings = hearingService.schedule(request);   // BLOCKED THE JUDGE CALENDAR
        ScheduleHearing scheduledHearing = scheduledHearings.get(0);

        hearing.setStartTime(scheduledHearing.getStartTime());
        hearing.setEndTime(scheduledHearing.getEndTime());

        hearingRequest.setHearing(hearing);

        producer.push("update topic", hearingRequest);
    }

    private Pair<Long, Long> getStartTimeAndEndTime(Long epochTime) {

        Instant instant = Instant.ofEpochSecond(epochTime);
        ZoneId systemZoneId = ZoneId.systemDefault();
        ZonedDateTime zonedDateTime = instant.atZone(systemZoneId);
        LocalDate localDate = zonedDateTime.toLocalDate();
        ZonedDateTime startOfDay = localDate.atStartOfDay(systemZoneId);
        ZonedDateTime endOfDay = startOfDay.plusDays(1);

        long startEpochMillis = startOfDay.toInstant().toEpochMilli();
        long endEpochMillis = endOfDay.toInstant().toEpochMilli();

        return new Pair<>(startEpochMillis, endEpochMillis);
    }

}
