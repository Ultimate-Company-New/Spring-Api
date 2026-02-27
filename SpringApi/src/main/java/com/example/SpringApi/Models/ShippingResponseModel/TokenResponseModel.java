package com.example.springapi.models.shippingresponsemodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents the token response model component.
 */
@Getter
@Setter
public class TokenResponseModel {
  @JsonProperty("id")
  private int id;

  @JsonProperty("first_name")
  private String firstName;

  @JsonProperty("last_name")
  private String lastName;

  @JsonProperty("email")
  private String email;

  @JsonProperty("company_id")
  private int companyId;

  @JsonProperty("created_at")
  private String createdAt;

  @JsonProperty("token")
  private String token;
}
