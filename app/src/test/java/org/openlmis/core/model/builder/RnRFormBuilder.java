package org.openlmis.core.model.builder;

import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;

public class RnRFormBuilder {
    private RnRForm rnRForm;
    public RnRFormBuilder() {
        rnRForm = new RnRForm();
    }

    public RnRFormBuilder setComments(String comments) {
        rnRForm.setComments(comments);
        return this;
    }

    public RnRFormBuilder setStatus(RnRForm.STATUS status) {
        rnRForm.setStatus(status);
        return this;
    }

    public RnRFormBuilder setProgram(Program program) {
        rnRForm.setProgram(program);
        return this;
    }

    public RnRFormBuilder setSynced(boolean synced) {
        rnRForm.setSynced(synced);
        return this;
    }

    public RnRForm build() {
        return rnRForm;
    }
}
