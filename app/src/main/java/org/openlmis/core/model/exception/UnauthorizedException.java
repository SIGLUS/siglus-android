package org.openlmis.core.model.exception;


import retrofit.RetrofitError;

public class UnauthorizedException extends Exception{

    public UnauthorizedException(RetrofitError cause){
        super(cause.getMessage());
    }

}
