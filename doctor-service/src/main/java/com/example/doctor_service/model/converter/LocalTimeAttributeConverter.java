package com.example.doctor_service.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Converter(autoApply = false)
public class LocalTimeAttributeConverter implements AttributeConverter<LocalTime, String> {

    private static final DateTimeFormatter HH_MM_SS = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public String convertToDatabaseColumn(LocalTime attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.format(HH_MM_SS);
    }

    @Override
    public LocalTime convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        String value = dbData.trim();
        try {
            return LocalTime.parse(value, HH_MM_SS);
        } catch (DateTimeParseException ignored) {
            // Fall through and try HH:mm
        }

        try {
            return LocalTime.parse(value, HH_MM);
        } catch (DateTimeParseException ignored) {
            return LocalTime.parse(value);
        }
    }
}
