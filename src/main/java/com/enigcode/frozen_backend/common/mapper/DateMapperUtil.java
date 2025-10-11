package com.enigcode.frozen_backend.common.mapper;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DateMapperUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public String map(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(FORMATTER);
    }

    public LocalDateTime map(String dateString) {
        if (dateString == null) {
            return null;
        }
        return LocalDateTime.parse(dateString, FORMATTER);
    }
}
