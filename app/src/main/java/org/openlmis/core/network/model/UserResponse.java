package org.openlmis.core.network.model;


import lombok.Data;

@Data
public class UserResponse {

  private String access_token;
  private String token_type;
  private int expires_in;
  private String scope;
  private String referenceDataUserId;
  private String username;
}
