package com.example.springapi.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Custom deserializer that accepts multiple date/datetime formats for LocalDateTime fields.
 *
 * <p>Supported formats: - ISO 8601 with timezone (e.g., "2026-03-01T17:00:00.000Z",
 * "2026-03-01T17:00:00Z") - ISO 8601 with offset (e.g., "2026-03-01T17:00:00+05:30") - ISO local
 * datetime (e.g., "2026-03-01T17:00:00", "2026-03-01T17:00:00.000") - Simple datetime (e.g.,
 * "yyyy-MM-dd'T'HH:mm:ss") - Date only (e.g., "yyyy-MM-dd") - converts to start of day
 *
 * <p>For timezone-aware formats, the datetime is converted to UTC then to LocalDateTime.
 *
 * @author SpringApi Team
 * @version 1.1
 * @since 2024-01-15
 */
public class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter DATETIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  @Override
  public LocalDateTime deserialize(JsonParser parser, DeserializationContext context)
      throws IOException {
    String dateString = parser.getText();

    if (dateString == null || dateString.trim().isEmpty()) {
      return null;
    }

    dateString = dateString.trim();

    // Try parsing as ISO 8601 instant with 'Z' suffix (e.g., "2026-03-01T17:00:00.000Z")
    try {
      Instant instant = Instant.parse(dateString);
      return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    } catch (DateTimeParseException e) {
      // Ignore and try next format
    }

    // Try parsing as ISO 8601 with offset (e.g., "2026-03-01T17:00:00+05:30")
    try {
      OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateString);
      return offsetDateTime.toLocalDateTime();
    } catch (DateTimeParseException e) {
      // Ignore and try next format
    }

    // Try parsing as full datetime first (yyyy-MM-dd'T'HH:mm:ss)
    try {
      return LocalDateTime.parse(dateString, DATETIME_FORMATTER);
    } catch (DateTimeParseException e) {
      // Ignore and try next format
    }

    // Try parsing with ISO LocalDateTime format (handles formats like yyyy-MM-dd'T'HH:mm:ss.SSS)
    try {
      return LocalDateTime.parse(dateString);
    } catch (DateTimeParseException e) {
      // Ignore and try next format
    }

    // Try parsing as date-only (yyyy-MM-dd) and convert to start of day
    try {
      LocalDate date = LocalDate.parse(dateString, DATE_FORMATTER);
      return date.atStartOfDay();
    } catch (DateTimeParseException e) {
      // Ignore and try next format
    }

    // Try parsing with ISO date format
    try {
      LocalDate date = LocalDate.parse(dateString);
      return date.atStartOfDay();
    } catch (DateTimeParseException e) {
      throw new IOException(
          "Cannot parse date/datetime: '"
              + dateString
              + "'. Expected formats: 'yyyy-MM-dd', 'yyyy-MM-dd'T'HH:mm:ss', or ISO "
              + "8601 with timezone (e.g., '2026-03-01T17:00:00.000Z')",
          e);
    }
  }
}
