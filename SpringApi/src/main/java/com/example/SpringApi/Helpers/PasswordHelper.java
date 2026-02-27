package com.example.SpringApi.Helpers;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.mindrot.jbcrypt.BCrypt;

public class PasswordHelper {
  private static final SecureRandom RANDOM = new SecureRandom();
  private static final Set<Character> SPECIAL_CHARS =
      Set.of('!', '@', '#', '$', '%', '^', '&', '*', '?', '_', '~', 'Â', '£', '(', ')', '.', ',');

  private PasswordHelper() {}

  public static class PasswordOptions {
    public static final int REQUIRED_LENGTH = 8;
    public static final int REQUIRED_UNIQUE_CHARS = 6;
    public static final boolean REQUIRE_NON_ALPHANUMERIC = true;
    public static final boolean REQUIRE_LOWERCASE = true;
    public static final boolean REQUIRE_UPPERCASE = true;
    public static final boolean REQUIRE_DIGIT = true;

    private PasswordOptions() {}
  }

  public enum PasswordStrength {
    BLANK,
    VERY_WEAK,
    WEAK,
    MEDIUM,
    STRONG,
    VERY_STRONG
  }

  // Generates a random password
  public static String getRandomPassword() {
    return BCrypt.gensalt(15, RANDOM);
  }

  // Gets the hashed password and salt for the given plain-text password
  public static String[] getHashedPasswordAndSalt(String plainTextPassword) {
    String salt = BCrypt.gensalt();
    String hashedPassword = BCrypt.hashpw(plainTextPassword + salt, BCrypt.gensalt());
    return new String[] {salt, hashedPassword};
  }

  // Gets a token based on the login name
  public static String getToken(String loginName) {
    String saltedLogin = loginName + BCrypt.gensalt();
    return BCrypt.hashpw(saltedLogin, BCrypt.gensalt());
  }

  // convert string to secret key
  public static SecretKey getSecretKey(String myKey) {
    byte[] keyBytes = myKey.getBytes(StandardCharsets.UTF_8);
    return new SecretKeySpec(keyBytes, 0, keyBytes.length, "HmacSHA256");
  }

  // Checks if the provided plain-text password matches the hashed password and salt
  public static boolean checkPassword(String plainTextPassword, String hashPassword, String salt) {
    String password = plainTextPassword + salt;
    return BCrypt.checkpw(password, hashPassword);
  }

  public static PasswordStrength getPasswordStrength(String password) {
    int score = 0;

    if (password == null || password.trim().isEmpty()) {
      return PasswordStrength.BLANK;
    }

    if (hasMinimumLength(password, 5)) {
      score++;
    }

    if (hasMinimumLength(password, 8)) {
      score++;
    }

    if (hasUpperCaseLetter(password) && hasLowerCaseLetter(password)) {
      score++;
    }

    if (hasDigit(password)) {
      score++;
    }

    if (hasSpecialChar(password)) {
      score++;
    }

    return PasswordStrength.values()[score];
  }

  public static boolean isStrongPassword(String password) {
    return hasMinimumLength(password, 8)
        && hasUpperCaseLetter(password)
        && hasLowerCaseLetter(password)
        && (hasDigit(password) || hasSpecialChar(password));
  }

  public static boolean isValidPassword(
      String password,
      int requiredLength,
      int requiredUniqueChars,
      boolean requireNonAlphanumeric,
      boolean requireLowercase,
      boolean requireUppercase,
      boolean requireDigit) {

    if (!hasMinimumLength(password, requiredLength)) {
      return false;
    }

    if (!hasMinimumUniqueChars(password, requiredUniqueChars)) {
      return false;
    }

    if (requireNonAlphanumeric && !hasSpecialChar(password)) {
      return false;
    }

    if (requireLowercase && !hasLowerCaseLetter(password)) {
      return false;
    }

    if (requireUppercase && !hasUpperCaseLetter(password)) {
      return false;
    }

    return !requireDigit || hasDigit(password);
  }

  // Helper Methods

  private static boolean hasMinimumLength(String password, int minLength) {
    return password.length() >= minLength;
  }

  private static boolean hasMinimumUniqueChars(String password, int minUniqueChars) {
    Set<Character> charSet = new HashSet<>();
    for (char c : password.toCharArray()) {
      charSet.add(c);
    }
    return charSet.size() >= minUniqueChars;
  }

  private static boolean hasDigit(String password) {
    return password.chars().anyMatch(Character::isDigit);
  }

  private static boolean hasSpecialChar(String password) {
    return password.chars().anyMatch(ch -> SPECIAL_CHARS.contains((char) ch));
  }

  private static boolean hasUpperCaseLetter(String password) {
    return password.chars().anyMatch(Character::isUpperCase);
  }

  private static boolean hasLowerCaseLetter(String password) {
    return password.chars().anyMatch(Character::isLowerCase);
  }
}

