package org.openlmis.core.exceptions;

public class SyncServerException extends LMISException {
    public SyncServerException(Exception e) {
        super(e);
    }

    public SyncServerException(String message) {
        super(message);
    }
}
