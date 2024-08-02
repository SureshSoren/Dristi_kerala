package digit.mapper;

import digit.web.models.ScheduleHearing;
import digit.web.models.hearing.Hearing;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CustomMapper {

    CustomMapper INSTANCE = Mappers.getMapper(CustomMapper.class);

    @Mapping(source = "hearingId", target = "hearingBookingId")
    @Mapping(source = "hearingType", target = "eventType")
    @Mapping(source = "presidedBy.courtID", target = "courtId")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "date", target = "startTime")
    ScheduleHearing hearingToScheduleHearingConversion(Hearing hearing);
}
