package com.example.SpringApi.Helpers;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.crypto.SecretKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PasswordHelper Tests")
class PasswordHelperTest {

  // Total Tests: 10

  /**
   * Purpose: Verify random password generation returns non-empty and non-deterministic values.
   * Expected Result: Two generated strings are non-null and different. Assertions: Non-null and
   * inequality checks.
   */
  @Test
  @DisplayName("getRandomPassword - Non Deterministic Hash String - Success")
  void getRandomPassword_s01_nonDeterministicHashString_success() {
    // Arrange

    // Act
    String first = PasswordHelper.getRandomPassword();
    String second = PasswordHelper.getRandomPassword();

    // Assert
    assertNotNull(first);
    assertNotNull(second);
    assertNotEquals(first, second);
    assertTrue(first.startsWith("$2"));
    assertTrue(second.startsWith("$2"));
  }

  /**
   * Purpose: Verify hashing with salt supports round-trip password validation. Expected Result:
   * Matching password validates true, mismatched password validates false. Assertions:
   * checkPassword outcomes for correct and incorrect inputs.
   */
  @Test
  @DisplayName("getHashedPasswordAndSalt - Round Trip Validation - Success")
  void getHashedPasswordAndSalt_s02_roundTripValidation_success() {
    // Arrange
    String password = "StrongP@ss123";

    // Act
    String[] hashedAndSalt = PasswordHelper.getHashedPasswordAndSalt(password);

    // Assert
    assertEquals(2, hashedAndSalt.length);
    assertTrue(PasswordHelper.checkPassword(password, hashedAndSalt[1], hashedAndSalt[0]));
    assertFalse(PasswordHelper.checkPassword("WrongPassword", hashedAndSalt[1], hashedAndSalt[0]));
  }

  /**
   * Purpose: Verify token generation creates unique bcrypt values per call. Expected Result:
   * Non-null token values with randomization. Assertions: Generated token strings are non-null and
   * unequal.
   */
  @Test
  @DisplayName("getToken - Unique Token Per Call - Success")
  void getToken_s03_uniqueTokenPerCall_success() {
    // Arrange

    // Act
    String tokenOne = PasswordHelper.getToken("user@example.com");
    String tokenTwo = PasswordHelper.getToken("user@example.com");

    // Assert
    assertNotNull(tokenOne);
    assertNotNull(tokenTwo);
    assertNotEquals(tokenOne, tokenTwo);
    assertTrue(tokenOne.startsWith("$2"));
  }

  /**
   * Purpose: Verify secret-key creation uses HmacSHA256 with UTF-8 key bytes. Expected Result: Key
   * algorithm and encoded bytes match input text bytes. Assertions: Algorithm name and raw key
   * bytes.
   */
  @Test
  @DisplayName("getSecretKey - HmacSha256KeyMaterial - Success")
  void getSecretKey_s04_hmacSha256KeyMaterial_success() {
    // Arrange
    String rawKey = "jwt-secret-key";

    // Act
    SecretKey secretKey = PasswordHelper.getSecretKey(rawKey);

    // Assert
    assertNotNull(secretKey);
    assertEquals("HmacSHA256", secretKey.getAlgorithm());
    assertArrayEquals(
        rawKey.getBytes(java.nio.charset.StandardCharsets.UTF_8), secretKey.getEncoded());
  }

  /**
   * Purpose: Verify password strength scoring returns expected enum levels across inputs. Expected
   * Result: Score buckets map to BLANK through VERY_STRONG. Assertions: Strength enum for
   * representative passwords.
   */
  @Test
  @DisplayName("getPasswordStrength - Score Buckets - Success")
  void getPasswordStrength_s05_scoreBuckets_success() {
    // Arrange

    // Act
    PasswordHelper.PasswordStrength blank = PasswordHelper.getPasswordStrength("  ");
    PasswordHelper.PasswordStrength veryWeak = PasswordHelper.getPasswordStrength("abcde");
    PasswordHelper.PasswordStrength weak = PasswordHelper.getPasswordStrength("abcdefgh");
    PasswordHelper.PasswordStrength medium = PasswordHelper.getPasswordStrength("Abcdefgh");
    PasswordHelper.PasswordStrength strong = PasswordHelper.getPasswordStrength("Abcdefgh1");
    PasswordHelper.PasswordStrength veryStrong = PasswordHelper.getPasswordStrength("Abcdefgh1!");

    // Assert
    assertEquals(PasswordHelper.PasswordStrength.BLANK, blank);
    assertEquals(PasswordHelper.PasswordStrength.VERY_WEAK, veryWeak);
    assertEquals(PasswordHelper.PasswordStrength.WEAK, weak);
    assertEquals(PasswordHelper.PasswordStrength.MEDIUM, medium);
    assertEquals(PasswordHelper.PasswordStrength.STRONG, strong);
    assertEquals(PasswordHelper.PasswordStrength.VERY_STRONG, veryStrong);
  }

  /**
   * Purpose: Verify strong-password helper accepts compliant passwords and rejects weak ones.
   * Expected Result: True for compliant input and false for weak/short variants. Assertions:
   * isStrongPassword outcomes.
   */
  @Test
  @DisplayName("isStrongPassword - Compliance Check - Success")
  void isStrongPassword_s06_complianceCheck_success() {
    // Arrange

    // Act
    boolean strongWithDigit = PasswordHelper.isStrongPassword("Abcdefg1");
    boolean strongWithSpecial = PasswordHelper.isStrongPassword("Abcdefg!");
    boolean weakNoUpper = PasswordHelper.isStrongPassword("abcdefg1");
    boolean weakShort = PasswordHelper.isStrongPassword("Ab1!");

    // Assert
    assertTrue(strongWithDigit);
    assertTrue(strongWithSpecial);
    assertFalse(weakNoUpper);
    assertFalse(weakShort);
  }

  /**
   * Purpose: Verify isValidPassword fails when minimum length constraint is not met. Expected
   * Result: Validation false. Assertions: isValidPassword returns false.
   */
  @Test
  @DisplayName("isValidPassword - Fails Minimum Length - Success")
  void isValidPassword_s07_failsMinimumLength_success() {
    // Arrange

    // Act
    boolean isValid = PasswordHelper.isValidPassword("Ab1!", 8, 4, true, true, true, true);

    // Assert
    assertFalse(isValid);
  }

  /**
   * Purpose: Verify isValidPassword fails when unique-character threshold is not met. Expected
   * Result: Validation false for repeated characters. Assertions: isValidPassword returns false.
   */
  @Test
  @DisplayName("isValidPassword - Fails Unique Character Requirement - Success")
  void isValidPassword_s08_failsUniqueCharacterRequirement_success() {
    // Arrange

    // Act
    boolean isValid = PasswordHelper.isValidPassword("AAAAAA11!!", 8, 6, true, true, true, true);

    // Assert
    assertFalse(isValid);
  }

  /**
   * Purpose: Verify isValidPassword fails specific policy branches for missing
   * special/lower/upper/digit. Expected Result: All branch-specific checks return false.
   * Assertions: Validation result per missing policy category.
   */
  @Test
  @DisplayName("isValidPassword - Policy Branch Failures - Success")
  void isValidPassword_s09_policyBranchFailures_success() {
    // Arrange

    // Act
    boolean missingSpecial =
        PasswordHelper.isValidPassword("Abcdefg1", 8, 6, true, true, true, true);
    boolean missingLower =
        PasswordHelper.isValidPassword("ABCDEFG1!", 8, 6, true, true, true, true);
    boolean missingUpper =
        PasswordHelper.isValidPassword("abcdefg1!", 8, 6, true, true, true, true);
    boolean missingDigit = PasswordHelper.isValidPassword("Abcdefg!", 8, 6, true, true, true, true);

    // Assert
    assertFalse(missingSpecial);
    assertFalse(missingLower);
    assertFalse(missingUpper);
    assertFalse(missingDigit);
  }

  /**
   * Purpose: Verify isValidPassword passes with a fully compliant policy input and optional digit
   * disabled path. Expected Result: Validation true for required policy combinations. Assertions:
   * True result for strict and relaxed digit policy.
   */
  @Test
  @DisplayName("isValidPassword - Passes Policy Requirements - Success")
  void isValidPassword_s10_passesPolicyRequirements_success() {
    // Arrange

    // Act
    boolean strictValid = PasswordHelper.isValidPassword("Abcd123!", 8, 6, true, true, true, true);
    boolean relaxedDigitValid =
        PasswordHelper.isValidPassword("Abcdefg!", 8, 6, true, true, true, false);

    // Assert
    assertTrue(strictValid);
    assertTrue(relaxedDigitValid);
    assertEquals(8, PasswordHelper.PasswordOptions.REQUIRED_LENGTH);
  }
}

