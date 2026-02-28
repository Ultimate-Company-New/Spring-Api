package springapi.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Constructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Html Helper Tests")
class HtmlHelperTest {

  // Total Tests: 4

  /**
   * Purpose: Verify null and empty HTML input handling for replaceBrTags. Expected Result: Null
   * remains null and empty remains empty. Assertions: Returned values match input edge-case values.
   */
  @Test
  @DisplayName("htmlHelper - ReplaceBrTags Handles NullAndEmpty - Success")
  void htmlHelper_s01_replaceBrTagsHandlesNullAndEmpty_success() {
    // Arrange
    String nullInput = null;
    String emptyInput = "";

    // Act
    String nullResult = HtmlHelper.replaceBrTags(nullInput);
    String emptyResult = HtmlHelper.replaceBrTags(emptyInput);

    // Assert
    assertNull(nullResult);
    assertEquals("", emptyResult);
  }

  /**
   * Purpose: Verify replaceBrTags normalizes lower/upper-case br tags. Expected Result: All
   * supported br variants convert to <br>
   * . Assertions: Output contains normalized <br>
   * tags only.
   */
  @Test
  @DisplayName("htmlHelper - ReplaceBrTags NormalizesVariants - Success")
  void htmlHelper_s02_replaceBrTagsNormalizesVariants_success() {
    // Arrange
    String html = "line1<br>line2<BR/>line3<BR >line4";

    // Act
    String result = HtmlHelper.replaceBrTags(html);

    // Assert
    assertEquals("line1<br/>line2<br/>line3<br/>line4", result);
  }

  /**
   * Purpose: Verify sanitizeHtml trims leading and trailing whitespace. Expected Result: Trimmed
   * content is returned for non-empty input. Assertions: Sanitized output equals expected trimmed
   * string.
   */
  @Test
  @DisplayName("htmlHelper - SanitizeHtml TrimsWhitespace - Success")
  void htmlHelper_s03_sanitizeHtmlTrimsWhitespace_success() {
    // Arrange
    String html = "   <p>content</p>   ";

    // Act
    String result = HtmlHelper.sanitizeHtml(html);

    // Assert
    assertEquals("<p>content</p>", result);
  }

  /**
   * Purpose: Verify utility-class private constructor can be invoked reflectively. Expected Result:
   * Constructor invocation succeeds. Assertions: Reflected instance is created.
   */
  @Test
  @DisplayName("htmlHelper - PrivateConstructor Reflection - Success")
  void htmlHelper_s04_privateConstructorReflection_success() throws Exception {
    // Arrange
    Constructor<HtmlHelper> constructor = HtmlHelper.class.getDeclaredConstructor();
    constructor.setAccessible(true);

    // Act
    HtmlHelper instance = constructor.newInstance();

    // Assert
    assertNotNull(instance);
  }
}
