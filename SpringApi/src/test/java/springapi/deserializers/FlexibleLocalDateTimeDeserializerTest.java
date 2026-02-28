package springapi.deserializers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import java.io.IOException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Flexible LocalDateTime Deserializer Tests")
class FlexibleLocalDateTimeDeserializerTest {

  // Total Tests: 7

  /**
   * Purpose: Verify empty date string returns null. Expected Result: Null is returned for empty
   * input. Assertions: Deserialized value is null.
   */
  @Test
  @DisplayName("flexibleLocalDateTimeDeserializer - EmptyString ReturnsNull - Success")
  void flexibleLocalDateTimeDeserializer_s01_emptyStringReturnsNull_success() throws IOException {
    // Arrange
    FlexibleLocalDateTimeDeserializer deserializer = new FlexibleLocalDateTimeDeserializer();

    // Act
    LocalDateTime result = deserializer.deserialize(parserFor(""), null);

    // Assert
    assertNull(result);
  }

  /**
   * Purpose: Verify ISO instant with Z suffix is parsed. Expected Result: UTC LocalDateTime is
   * returned. Assertions: Parsed datetime matches expected UTC timestamp.
   */
  @Test
  @DisplayName("flexibleLocalDateTimeDeserializer - InstantWithZ Parses - Success")
  void flexibleLocalDateTimeDeserializer_s02_instantWithZParses_success() throws IOException {
    // Arrange
    FlexibleLocalDateTimeDeserializer deserializer = new FlexibleLocalDateTimeDeserializer();

    // Act
    LocalDateTime result = deserializer.deserialize(parserFor("2026-03-01T17:00:00.000Z"), null);

    // Assert
    assertEquals(LocalDateTime.of(2026, 3, 1, 17, 0, 0), result);
  }

  /**
   * Purpose: Verify offset datetime format is parsed. Expected Result: LocalDateTime is extracted
   * from offset datetime. Assertions: Parsed value matches expected local datetime component.
   */
  @Test
  @DisplayName("flexibleLocalDateTimeDeserializer - OffsetDateTime Parses - Success")
  void flexibleLocalDateTimeDeserializer_s03_offsetDateTimeParses_success() throws IOException {
    // Arrange
    FlexibleLocalDateTimeDeserializer deserializer = new FlexibleLocalDateTimeDeserializer();

    // Act
    LocalDateTime result = deserializer.deserialize(parserFor("2026-03-01T17:00:00+05:30"), null);

    // Assert
    assertEquals(LocalDateTime.of(2026, 3, 1, 11, 30, 0), result);
  }

  /**
   * Purpose: Verify custom yyyy-MM-dd'T'HH:mm:ss format is parsed. Expected Result: LocalDateTime
   * is parsed using custom formatter. Assertions: Parsed value matches expected datetime.
   */
  @Test
  @DisplayName("flexibleLocalDateTimeDeserializer - CustomDateTimeFormat Parses - Success")
  void flexibleLocalDateTimeDeserializer_s04_customDateTimeFormatParses_success()
      throws IOException {
    // Arrange
    FlexibleLocalDateTimeDeserializer deserializer = new FlexibleLocalDateTimeDeserializer();

    // Act
    LocalDateTime result = deserializer.deserialize(parserFor("2026-03-01T17:00:00"), null);

    // Assert
    assertEquals(LocalDateTime.of(2026, 3, 1, 17, 0, 0), result);
  }

  /**
   * Purpose: Verify ISO local datetime with fractional seconds is parsed. Expected Result:
   * LocalDateTime.parse fallback handles fractional input. Assertions: Parsed value preserves
   * fractional second component.
   */
  @Test
  @DisplayName("flexibleLocalDateTimeDeserializer - IsoLocalDateTimeWithMillis Parses - Success")
  void flexibleLocalDateTimeDeserializer_s05_isoLocalDateTimeWithMillisParses_success()
      throws IOException {
    // Arrange
    FlexibleLocalDateTimeDeserializer deserializer = new FlexibleLocalDateTimeDeserializer();

    // Act
    LocalDateTime result = deserializer.deserialize(parserFor("2026-03-01T17:00:00.123"), null);

    // Assert
    assertEquals(LocalDateTime.of(2026, 3, 1, 17, 0, 0, 123000000), result);
  }

  /**
   * Purpose: Verify date-only input is parsed as start of day. Expected Result: LocalDate at
   * start-of-day is returned. Assertions: Parsed value equals yyyy-MM-ddT00:00:00.
   */
  @Test
  @DisplayName("flexibleLocalDateTimeDeserializer - DateOnly ParsesToStartOfDay - Success")
  void flexibleLocalDateTimeDeserializer_s06_dateOnlyParsesToStartOfDay_success()
      throws IOException {
    // Arrange
    FlexibleLocalDateTimeDeserializer deserializer = new FlexibleLocalDateTimeDeserializer();

    // Act
    LocalDateTime result = deserializer.deserialize(parserFor("2026-03-01"), null);

    // Assert
    assertEquals(LocalDateTime.of(2026, 3, 1, 0, 0, 0), result);
  }

  /**
   * Purpose: Verify invalid input throws IOException with parsing guidance. Expected Result:
   * IOException is thrown when no format matches. Assertions: Exception message starts with parse
   * failure text.
   */
  @Test
  @DisplayName("flexibleLocalDateTimeDeserializer - InvalidInput ThrowsIOException - Success")
  void flexibleLocalDateTimeDeserializer_s07_invalidInputThrowsIOException_success()
      throws IOException {
    // Arrange
    FlexibleLocalDateTimeDeserializer deserializer = new FlexibleLocalDateTimeDeserializer();
    JsonParser parser = parserFor("invalid-date");

    // Act
    IOException ex = assertThrows(IOException.class, () -> deserializer.deserialize(parser, null));

    // Assert
    assertTrue(ex.getMessage().startsWith("Cannot parse date/datetime:"));
  }

  private JsonParser parserFor(String value) throws IOException {
    JsonFactory factory = new JsonFactory();
    JsonParser parser = factory.createParser("\"" + value + "\"");
    parser.nextToken();
    return parser;
  }
}
