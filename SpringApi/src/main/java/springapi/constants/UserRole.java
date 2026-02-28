package springapi.constants;

/**
 * Enum representing the different user roles in the system. These roles must match the database.
 * constraint: chk_user_role
 */
public enum UserRole {
  SUPERADMIN("SUPERADMIN"),
  ADMIN("ADMIN"),
  MANAGER("MANAGER"),
  VIEWER("VIEWER"),
  CUSTOMER("CUSTOMER"),
  CUSTOM("CUSTOM");

  private final String value;

  UserRole(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  /**
   * Convert string to UserRole enum (case-insensitive).
   *
   * @param role The role string to convert
   * @return UserRole enum or null if invalid
   */
  public static UserRole fromString(String role) {
    if (role == null) {
      return null;
    }

    String upperRole = role.toUpperCase().trim();
    for (UserRole userRole : UserRole.values()) {
      if (userRole.value.equals(upperRole)) {
        return userRole;
      }
    }
    return null;
  }

  /**
   * Check if a string is a valid role.
   *
   * @param role The role string to validate
   * @return true if valid, false otherwise
   */
  public static boolean isValid(String role) {
    return fromString(role) != null;
  }

  @Override
  public String toString() {
    return this.value;
  }
}
