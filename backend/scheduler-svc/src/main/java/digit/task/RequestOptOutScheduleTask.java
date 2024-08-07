package digit.task;


import digit.config.Configuration;
import digit.kafka.Producer;
import digit.repository.ReScheduleRequestRepository;
import digit.repository.RescheduleRequestOptOutRepository;
import digit.service.OptOutConsumerService;
import digit.util.PendingTaskUtil;
import digit.web.models.*;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static digit.config.ServiceConstants.INACTIVE;

@Component
@Slf4j
@EnableScheduling
public class RequestOptOutScheduleTask {

    private final ReScheduleRequestRepository reScheduleRepository;

    private final RescheduleRequestOptOutRepository requestOptOutRepository;

    private final Producer producer;

    private final Configuration config;

    private final OptOutConsumerService optOutConsumerService;

    private final PendingTaskUtil pendingTaskUtil;

    @Autowired
    public RequestOptOutScheduleTask(ReScheduleRequestRepository reScheduleRepository, RescheduleRequestOptOutRepository requestOptOutRepository, Producer producer, Configuration config, OptOutConsumerService optOutConsumerService, PendingTaskUtil pendingTaskUtil) {
        this.reScheduleRepository = reScheduleRepository;
        this.requestOptOutRepository = requestOptOutRepository;
        this.producer = producer;
        this.config = config;
        this.optOutConsumerService = optOutConsumerService;
        this.pendingTaskUtil = pendingTaskUtil;

    }

    @Scheduled(cron = "${drishti.cron.opt-out.due.date}", zone = "Asia/Kolkata")
    public void updateAvailableDatesFromOptOuts() {
        try {
            log.info("operation = updateAvailableDatesFromOptOuts, result=IN_PROGRESS");
            Long dueDate = LocalDate.now().minusDays(config.getOptOutDueDate()).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
            List<ReScheduleHearing> reScheduleHearings = reScheduleRepository.getReScheduleRequest(ReScheduleHearingReqSearchCriteria.builder().tenantId(config.getEgovStateTenantId()).dueDate(dueDate).build(), null, null);

            for (ReScheduleHearing reScheduleHearing : reScheduleHearings) {
                List<OptOut> optOuts = requestOptOutRepository.getOptOut(OptOutSearchCriteria.builder().judgeId(reScheduleHearing.getJudgeId()).caseId(reScheduleHearing.getCaseId()).rescheduleRequestId(reScheduleHearing.getRescheduledRequestId()).tenantId(reScheduleHearing.getTenantId()).build(), null, null);


                List<Long> suggestedDays = new ArrayList<>(reScheduleHearing.getSuggestedDates());
                List<Long> availableDates = new ArrayList<>(suggestedDays);

                for (OptOut optOut : optOuts) {
                    List<Long> optOutDates = optOut.getOptoutDates();
                    availableDates.removeAll(optOutDates);
                }

                reScheduleHearing.setAvailableDates(availableDates);
                reScheduleHearing.setStatus(INACTIVE);

                //todo: audit details

                //open pending task for judge

                PendingTask pendingTask = pendingTaskUtil.createPendingTask(reScheduleHearing);
                PendingTaskRequest pendingTaskRequest = PendingTaskRequest.builder()
                        .pendingTask(pendingTask)
                        .requestInfo(new RequestInfo()).build();
                pendingTaskUtil.callAnalytics(pendingTaskRequest);

                //unblock judge calendar for suggested days - available days

                ReScheduleHearingRequest request = ReScheduleHearingRequest.builder().reScheduleHearing(Collections.singletonList(reScheduleHearing)).build();
                optOutConsumerService.unblockJudgeCalendarForSuggestedDays(request);
            }
            producer.push(config.getUpdateRescheduleRequestTopic(), reScheduleHearings);
            log.info("operation= updateAvailableDatesFromOptOuts, result=SUCCESS");
        } catch (Exception e) {
            throw new CustomException("DK_SH_APP_ERR", "Error in setting available dates.");
        }
    }
}
