package digit.mapper;

import digit.web.models.ScheduleHearing;
import digit.web.models.hearing.Hearing;

public interface CustomMapper {


    ScheduleHearing hearingToScheduleHearingConversion(Hearing hearing);
}
