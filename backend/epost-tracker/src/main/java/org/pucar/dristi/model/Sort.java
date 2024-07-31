package org.pucar.dristi.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Sort {
    processNumber("process_number"),
    taskNumber("task_number"),
    trackingNumber("tracking_number"),
    deliveryStatus("delivery_status"),
    bookingDate("booking_date"),
    receivedDate("received_date");

    private String value;

    Sort(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static Sort fromValue(String text) {
        for (Sort b : Sort.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
