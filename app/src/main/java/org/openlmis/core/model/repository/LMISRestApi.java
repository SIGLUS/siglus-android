package org.openlmis.core.model.repository;


import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Query;
import rx.Observable;

public interface LMISRestApi {

    @POST("/j_spring_security_check")
    void authorizeUser(@Query("j_username") String username, @Query("j_password") String password, Callback<UserRepository.UserResponse> callback);
}
