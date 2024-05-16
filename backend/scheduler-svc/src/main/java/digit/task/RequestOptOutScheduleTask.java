package digit.task;


import digit.config.Configuration;
import digit.kafka.Producer;
import digit.repository.ReScheduleRequestRepository;
import digit.repository.RescheduleRequestOptOutRepository;
import digit.service.ReScheduleHearingService;
import digit.web.controllers.ReScheduleHearingController;
import digit.web.models.*;
import digit.web.models.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@EnableScheduling
public class RequestOptOutScheduleTask {

    @Autowired
    ReScheduleRequestRepository reScheduleRepository;

    @Autowired
    RescheduleRequestOptOutRepository requestOptOutRepository;

    @Autowired
    private Producer producer;

    @Autowired
    private Configuration config;

    @Scheduled(cron = "${config.optout.duedate}", zone = "Asia/Kolkata")
    public void updateAvailableDatesFromOptOuts(){
        try {
            log.info("operation = updateAvailableDatesFromOptOuts, result=IN_PROGRESS");
            Long dueDate = LocalDate.now().minusDays(config.getOptOutDueDate()).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
            List<ReScheduleHearing> reScheduleHearings = reScheduleRepository.getReScheduleRequest(ReScheduleHearingReqSearchCriteria.builder().tenantId(config.getEgovStateTenantId()).status(Status.APPROVED).dueDate(dueDate).build());

            for (ReScheduleHearing reScheduleHearing : reScheduleHearings) {
                List<OptOut> optOuts = requestOptOutRepository.getOptOut(OptOutSearchCriteria.builder().judgeId(reScheduleHearing.getJudgeId()).caseId(reScheduleHearing.getCaseId()).rescheduleRequestId(reScheduleHearing.getRescheduledRequestId()).tenantId(reScheduleHearing.getTenantId()).build());


                List<LocalDate> suggestedDays = new ArrayList<>(reScheduleHearing.getSuggestedDates());
                List<LocalDate> availableDates = new ArrayList<>(suggestedDays);

                for (OptOut optOut : optOuts) {
                    List<LocalDate> optOutDates = optOut.getOptoutDates();
                    availableDates.removeAll(optOutDates);
                }

                reScheduleHearing.setAvailableDates(availableDates);
                reScheduleHearing.setStatus(Status.REVIEW);
            }
            producer.push(config.getUpdateRescheduleRequestTopic(), reScheduleHearings);
            log.info("operation= updateAvailableDatesFromOptOuts, result=SUCCESS");
        } catch(Exception e){
            throw new CustomException("DK_SH_APP_ERR", "Error in setting available dates.");
        }
    }
}
