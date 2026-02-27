package com.example.springapi.authentication;

import com.example.springapi.models.databasemodels.User;
import com.example.springapi.repositories.UserRepository;
import java.util.Collections;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Represents the custom user details service component.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByLoginName(username);
    if (user == null) {
      throw new UsernameNotFoundException("User not found with username: " + username);
    }

    return new org.springframework.security.core.userdetails.User(
        user.getLoginName(),
        user.getPassword(),
        user.getIsDeleted() == null || !user.getIsDeleted(), // enabled
        true, // account non-expired
        true, // credentials non-expired
        user.getLocked() == null || !user.getLocked(), // account non-locked
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole())));
  }
}
