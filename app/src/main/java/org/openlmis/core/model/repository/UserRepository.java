package org.openlmis.core.model.repository;

import lombok.Data;
import retrofit.Callback;

public class UserRepository extends RestRepository{

    public void getUser(String username, String password, Callback<UserResponse> callback) {
        lmisRestApi.authorizeUser(username, password, callback);
    }

    public @Data class UserResponse {
        boolean authenticated;
        String name;
        String error;
    }
}
