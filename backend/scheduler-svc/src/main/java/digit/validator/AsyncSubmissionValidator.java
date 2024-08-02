package digit.validator;

import digit.config.Configuration;
import digit.repository.HearingRepository;
import digit.web.models.AsyncSubmission;
import digit.web.models.ScheduleHearingSearchCriteria;
import digit.web.models.ScheduleHearing;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class AsyncSubmissionValidator {

    private HearingRepository repository;


    private Configuration config;

    @Autowired
    public AsyncSubmissionValidator(HearingRepository repository, Configuration config) {
        this.repository = repository;
        this.config = config;
    }

    public void validateSubmissionDates(AsyncSubmission asyncSubmission) {
        // Check if tenant Id is null or does not match the defined tenant ID
        if (asyncSubmission.getTenantId() == null || !config.getEgovStateTenantId().equalsIgnoreCase(asyncSubmission.getTenantId())) {
            throw new CustomException("DK_ASH_APP_ERR", "Tenant id is either null or invalid");
        }
        // Validate submission and response dates
        validateSubmissionAndResponseDates(asyncSubmission);

        // Build search criteria using case id and retrieve list of scheduled hearings
        ScheduleHearingSearchCriteria searchCriteria = ScheduleHearingSearchCriteria.builder()
                .caseId(asyncSubmission.getCaseId()).build();
        List<ScheduleHearing> scheduleHearingList = repository.getHearings(searchCriteria,null,null);

        // Find the latest hearing by start time
        Optional<ScheduleHearing> latestHearing = findLatestHearingByHearingDate(scheduleHearingList);
        // Proceed only if a latest hearing is found
        if (latestHearing.isPresent()) {
            // Check if the response date is after the latest hearing date
            if (LocalDate.parse(asyncSubmission.getResponseDate()).isAfter(latestHearing.get().getDate())) {
                throw new CustomException("DK_ASH_APP_ERR", "Async submission and response dates must be before hearing date");
            }
        }
    }

    private void validateSubmissionAndResponseDates(AsyncSubmission asyncSubmission) {
        LocalDate submissionDate = LocalDate.parse(asyncSubmission.getSubmissionDate());
        LocalDate responseDate = LocalDate.parse(asyncSubmission.getResponseDate());
        LocalDate currentDate = LocalDate.now();

        // Check if both dates are after the current date and submission date is before response date
        if (!submissionDate.isAfter(currentDate)) {
            throw new CustomException("DK_ASR_APP_ERR", "Submission date must be after current date");
        } else if ( !responseDate.isAfter(currentDate)) {
            throw new CustomException("DK_ASR_APP_ERR", "Response date must be after current date");
        } else if (!submissionDate.isBefore(responseDate)) {
            throw new CustomException("DK_ASR_APP_ERR", "Submission date must be before Response date");
        }
    }

    public static Optional<ScheduleHearing> findLatestHearingByHearingDate(List<ScheduleHearing> hearings) {
        return hearings.stream()
                // Filter out hearings that have a null date or a date before today
                .filter(hearing -> hearing.getDate() != null && hearing.getDate().isAfter(LocalDate.now()))
                // Find the hearing with the latest start time, considering nulls as the last in order
                .max(Comparator.comparing(ScheduleHearing::getStartTime, Comparator.nullsLast(LocalDateTime::compareTo)));
    }


    public void validateDates(AsyncSubmission asyncSubmission) {
        // Build search criteria using case Id and retrieve list of scheduled hearings
        ScheduleHearingSearchCriteria searchCriteria = ScheduleHearingSearchCriteria.builder()
                .caseId(asyncSubmission.getCaseId()).build();
        List<ScheduleHearing> scheduleHearingList = repository.getHearings(searchCriteria,null,null);

        // Find the latest hearing by start time
        Optional<ScheduleHearing> latestHearing = findLatestHearingByHearingDate(scheduleHearingList);

        // Proceed only if a latest hearing is found
        if (latestHearing.isPresent()) {
            ScheduleHearing hearing = latestHearing.get();
            LocalDate hearingDate = hearing.getDate();
            LocalDate responseDate = LocalDate.parse(asyncSubmission.getResponseDate());
            LocalDate currentDate = LocalDate.now();

            // Adjust response date if it is after the latest hearing date
            if (responseDate.isAfter(hearingDate)) {
                asyncSubmission.setResponseDate(hearingDate.minusDays(1).toString());
            }

            // Calculate no of days till latest hearing and set submission dates
            long daysUntilHearing = ChronoUnit.DAYS.between(currentDate, hearingDate);
            if (daysUntilHearing > 6) {
                asyncSubmission.setSubmissionDate(hearingDate.minusDays(6).toString());
            } else {
                asyncSubmission.setSubmissionDate(currentDate.plusDays(1).toString());
            }
        }
    }
}
