package springapi.constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Constants for product condition values.
 *
 * <p>These values must match the frontend PRODUCT_CONDITION_OPTIONS in appConstants.ts. Validation
 * is case-insensitive.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
public final class ProductConditionConstants {

  private ProductConditionConstants() {
    // Utility class - prevent instantiation
  }

  public static final String NEW_WITH_TAGS = "NEW_WITH_TAGS";
  public static final String NEW_WITHOUT_TAGS = "NEW_WITHOUT_TAGS";
  public static final String NEW_WITH_DEFECTS = "NEW_WITH_DEFECTS";
  public static final String PRE_OWNED = "PRE_OWNED";
  public static final String PRE_OWNED_WITH_DEFECTS = "PRE_OWNED_WITH_DEFECTS";

  /** All valid product condition values (uppercase for storage/comparison). */
  private static final List<String> VALID_CONDITIONS =
      Collections.unmodifiableList(
          Arrays.asList(
              NEW_WITH_TAGS,
              NEW_WITHOUT_TAGS,
              NEW_WITH_DEFECTS,
              PRE_OWNED,
              PRE_OWNED_WITH_DEFECTS));

  /**
   * Checks if the given condition is valid (case-insensitive).
   *
   * @param condition The condition value to validate
   * @return true if valid, false otherwise
   */
  public static boolean isValidCondition(String condition) {
    if (condition == null || condition.trim().isEmpty()) {
      return false;
    }
    String normalized = condition.trim().toUpperCase();
    return VALID_CONDITIONS.stream().anyMatch(valid -> valid.equals(normalized));
  }

  /**
   * Returns the normalized (uppercase) condition value if valid.
   *
   * @param condition The condition value
   * @return The normalized condition, or null if invalid
   */
  public static String normalizeCondition(String condition) {
    if (condition == null || condition.trim().isEmpty()) {
      return null;
    }
    String normalized = condition.trim().toUpperCase();
    return VALID_CONDITIONS.contains(normalized) ? normalized : null;
  }

  /** Returns a comma-separated list of valid conditions for error messages. */
  public static String getValidConditionsList() {
    return String.join(", ", VALID_CONDITIONS);
  }
}
