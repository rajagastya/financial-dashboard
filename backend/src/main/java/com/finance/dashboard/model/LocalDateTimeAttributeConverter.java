package com.finance.dashboard.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Converter(autoApply = true)
public class LocalDateTimeAttributeConverter implements AttributeConverter<LocalDateTime, String> {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter SQL_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public String convertToDatabaseColumn(LocalDateTime attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.format(ISO_FORMATTER);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        try {
            // Most common format for LocalDateTime
            return LocalDateTime.parse(dbData, ISO_FORMATTER);
        } catch (DateTimeParseException ignored) {
        }

        try {
            // Fallback for legacy SQLite storage format
            return LocalDateTime.parse(dbData, SQL_FORMATTER);
        } catch (DateTimeParseException ignored) {
        }

        try {
            // Fallback for epoch milliseconds stored as text/number (possibly negative)
            long epochMs = Long.parseLong(dbData);
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMs), ZoneId.systemDefault());
        } catch (NumberFormatException ignored) {
        }

        throw new IllegalArgumentException("Cannot convert value to LocalDateTime: " + dbData);
    }
}
