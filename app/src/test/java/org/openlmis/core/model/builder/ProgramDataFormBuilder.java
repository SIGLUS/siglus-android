package org.openlmis.core.model.builder;

import org.openlmis.core.model.Period;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ProgramDataForm;

import java.util.Date;

public class ProgramDataFormBuilder {
    private ProgramDataForm programDataForm;

    public ProgramDataFormBuilder() {
        programDataForm = new ProgramDataForm();
    }

    public ProgramDataFormBuilder setStatus(ProgramDataForm.STATUS status) {
        programDataForm.setStatus(status);
        return this;
    }

    public ProgramDataFormBuilder setProgram(Program program) {
        programDataForm.setProgram(program);
        return this;
    }

    public ProgramDataFormBuilder setSynced(boolean synced) {
        programDataForm.setSynced(synced);
        return this;
    }

    public ProgramDataFormBuilder setSubmittedTime(Date submittedTime) {
        programDataForm.setSubmittedTime(submittedTime);
        return this;
    }

    public ProgramDataFormBuilder setPeriod(Date periodBegin) {
        programDataForm.setPeriodBegin(periodBegin);
        programDataForm.setPeriodEnd(Period.of(periodBegin).getEnd().toDate());
        return this;
    }

    public ProgramDataForm build() {
        return programDataForm;
    }
}
