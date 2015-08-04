package org.openlmis.core.exceptions;

public class ViewNotMatchException extends LMISException{
    public ViewNotMatchException(String msg) {
        super(msg);
    }

    public ViewNotMatchException(Exception e) {
        super(e);
    }
}
