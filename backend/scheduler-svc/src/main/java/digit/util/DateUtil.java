package digit.util;

import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;

@Component
public class DateUtil {


    public LocalDateTime getLocalDateTimeFromEpoch(long startTime) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneId.systemDefault());
    }

    public LocalTime getLocalTime(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        // Parse the time string into a LocalTime object
        return LocalTime.parse(time, formatter);
    }

    public LocalDateTime getLocalDateTime(LocalDateTime dateTime, String newTime) {

        LocalTime time = getLocalTime(newTime);

        return dateTime.with(time);

    }

    public LocalDate getLocalDateFromEpoch(long startTime) {
        return Instant.ofEpochSecond(startTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}
