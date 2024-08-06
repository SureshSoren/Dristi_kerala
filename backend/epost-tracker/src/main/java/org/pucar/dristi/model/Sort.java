package org.pucar.dristi.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Sort {
    process_number("processNumber"),
    task_number("taskNumber"),
    tracking_number("trackingNumber"),
    delivery_status("deliveryStatus"),
    booking_date("bookingDate"),
    received_date("receivedDate");

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
