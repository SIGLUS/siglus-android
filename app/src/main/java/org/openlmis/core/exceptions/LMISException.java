package org.openlmis.core.exceptions;


public class LMISException extends Exception {

    public LMISException(String msg){
        super(msg);
    }

    public LMISException(Exception e){
        super(e);
    }
}
