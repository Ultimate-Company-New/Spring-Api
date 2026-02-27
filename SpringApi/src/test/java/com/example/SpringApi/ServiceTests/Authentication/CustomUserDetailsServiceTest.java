package com.example.springapi.ServiceTests.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.example.springapi.authentication.CustomUserDetailsService;
import com.example.springapi.models.databasemodels.User;
import com.example.springapi.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Tests")
class CustomUserDetailsServiceTest {

  // Total Tests: 3

  @Mock private UserRepository userRepository;

  /**
   * Purpose: Verify user details are created correctly for active and unlocked users. Expected
   * Result: Enabled and non-locked user details with ROLE_ authority. Assertions: Username,
   * enabled/locked flags, and authority mapping.
   */
  @Test
  @DisplayName("loadUserByUsername - Active User Mapping - Success")
  void loadUserByUsername_s01_activeUserMapping_success() {
    // Arrange
    CustomUserDetailsService service = new CustomUserDetailsService(userRepository);
    User dbUser = new User();
    dbUser.setLoginName("nahush");
    dbUser.setPassword("hashed");
    dbUser.setRole("ADMIN");
    dbUser.setIsDeleted(false);
    dbUser.setLocked(false);
    when(userRepository.findByLoginName(eq("nahush"))).thenReturn(dbUser);

    // Act
    UserDetails userDetails = service.loadUserByUsername("nahush");

    // Assert
    assertEquals("nahush", userDetails.getUsername());
    assertTrue(userDetails.isEnabled());
    assertTrue(userDetails.isAccountNonLocked());
    assertTrue(
        userDetails.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())));
  }

  /**
   * Purpose: Verify deleted or locked flags map to disabled/non-unlocked account flags. Expected
   * Result: Disabled and locked status propagated to UserDetails. Assertions: Enabled false and
   * accountNonLocked false.
   */
  @Test
  @DisplayName("loadUserByUsername - Deleted And Locked Flags - Success")
  void loadUserByUsername_s02_deletedAndLockedFlags_success() {
    // Arrange
    CustomUserDetailsService service = new CustomUserDetailsService(userRepository);
    User dbUser = new User();
    dbUser.setLoginName("locked-user");
    dbUser.setPassword("hashed");
    dbUser.setRole("USER");
    dbUser.setIsDeleted(true);
    dbUser.setLocked(true);
    when(userRepository.findByLoginName(eq("locked-user"))).thenReturn(dbUser);

    // Act
    UserDetails userDetails = service.loadUserByUsername("locked-user");

    // Assert
    assertFalse(userDetails.isEnabled());
    assertFalse(userDetails.isAccountNonLocked());
  }

  /**
   * Purpose: Verify missing users throw UsernameNotFoundException with exact message. Expected
   * Result: Exception thrown. Assertions: Exception type and exact message.
   */
  @Test
  @DisplayName("loadUserByUsername - User Missing Throws UsernameNotFoundException - Success")
  void loadUserByUsername_s03_userMissingThrowsUsernameNotFoundException_success() {
    // Arrange
    CustomUserDetailsService service = new CustomUserDetailsService(userRepository);
    when(userRepository.findByLoginName(eq("missing"))).thenReturn(null);

    // Act
    UsernameNotFoundException exception =
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("missing"));

    // Assert
    assertEquals("User not found with username: missing", exception.getMessage());
  }
}
