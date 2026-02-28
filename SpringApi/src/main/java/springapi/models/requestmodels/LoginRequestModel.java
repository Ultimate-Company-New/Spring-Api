package springapi.models.requestmodels;

import lombok.Getter;
import lombok.Setter;

/** Represents the login request model component. */
@Setter
@Getter
public class LoginRequestModel {
  private Long userId;
  private String loginName;
  private String password;
  private Long clientId;
  private String apiKey;
  private String token;
  private String phone;
  private String firstName;
  private String lastName;
  private String confirmPassword;
  private String dob;
  private String role;
  private Boolean isTermsAndConditions;
}
