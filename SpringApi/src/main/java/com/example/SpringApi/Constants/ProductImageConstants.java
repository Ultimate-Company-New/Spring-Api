package com.example.SpringApi.Constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Constants for product image keys used across the application.
 *
 * <p>These constants define the image type keys for product images (main, top, bottom, etc.) and
 * should be used consistently in models, services, and tests.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
public final class ProductImageConstants {

  private ProductImageConstants() {
    // Utility class - prevent instantiation
  }

  // Required image keys
  public static final String MAIN = "main";
  public static final String TOP = "top";
  public static final String BOTTOM = "bottom";
  public static final String FRONT = "front";
  public static final String BACK = "back";
  public static final String RIGHT = "right";
  public static final String LEFT = "left";
  public static final String DETAILS = "details";

  // Optional image keys
  public static final String DEFECT = "defect";
  public static final String ADDITIONAL_1 = "additional_1";
  public static final String ADDITIONAL_2 = "additional_2";
  public static final String ADDITIONAL_3 = "additional_3";

  /** Required image types (main, top, bottom, front, back, right, left, details) */
  public static final List<String> REQUIRED_IMAGE_TYPES =
      Collections.unmodifiableList(
          Arrays.asList(MAIN, TOP, BOTTOM, FRONT, BACK, RIGHT, LEFT, DETAILS));

  /** Optional image types (defect, additional_1, additional_2, additional_3) */
  public static final List<String> OPTIONAL_IMAGE_TYPES =
      Collections.unmodifiableList(Arrays.asList(DEFECT, ADDITIONAL_1, ADDITIONAL_2, ADDITIONAL_3));
}
