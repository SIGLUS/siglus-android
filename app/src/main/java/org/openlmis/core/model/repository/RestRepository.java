package org.openlmis.core.model.repository;


import org.openlmis.core.model.exception.UnauthorizedException;

import retrofit.ErrorHandler;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RestRepository {

    public static String END_POINT = "http://52.69.106.103:8888/";

    protected RestAdapter restAdapter;
    protected LMISRestApi lmisRestApi;

    public RestRepository() {

        restAdapter = new RestAdapter.Builder()
                .setEndpoint(END_POINT)
                .setErrorHandler(new MyErrorHandler())
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        lmisRestApi = restAdapter.create(LMISRestApi.class);
    }

    class MyErrorHandler implements ErrorHandler {
        @Override public Throwable handleError(RetrofitError cause) {
            Response r = cause.getResponse();
            if (r != null && r.getStatus() == 401) {
                return new UnauthorizedException(cause);
            }
            return cause;
        }
    }

}
