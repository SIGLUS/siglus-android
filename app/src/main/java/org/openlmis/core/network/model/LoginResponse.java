package org.openlmis.core.network.model;

import org.openlmis.core.model.User;

import lombok.Data;

@Data
public class LoginResponse {
    private User userInformation;
}
