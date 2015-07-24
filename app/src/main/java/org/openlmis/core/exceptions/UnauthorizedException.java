package org.openlmis.core.exceptions;


import retrofit.RetrofitError;

public class UnauthorizedException extends LMISException {

    public UnauthorizedException(RetrofitError cause){
        super(cause.getMessage());
    }

}
