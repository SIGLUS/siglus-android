package org.openlmis.core.builders;

import android.support.annotation.NonNull;

import org.openlmis.core.model.PTVProgram;
import org.openlmis.core.model.PatientDispensation;

import java.util.ArrayList;
import java.util.List;

public class PatientDispensationBuilder {

    public List<PatientDispensation> buildInitialPatientDispensations(PTVProgram ptvProgram) {
        List<PatientDispensation> patientDispensations = new ArrayList<>();
        PatientDispensation patientDispensationChild = createPatientDispensation(ptvProgram, PatientDispensation.Type.CHILD);
        patientDispensations.add(patientDispensationChild);
        PatientDispensation patientDispensationWoman = createPatientDispensation(ptvProgram, PatientDispensation.Type.WOMAN);
        patientDispensations.add(patientDispensationWoman);
        return patientDispensations;
    }

    @NonNull
    private PatientDispensation createPatientDispensation(PTVProgram ptvProgram, PatientDispensation.Type type) {
        PatientDispensation patientDispensation = new PatientDispensation();
        patientDispensation.setPtvProgram(ptvProgram);
        patientDispensation.setType(type);
        patientDispensation.setTotal(0L);
        return patientDispensation;
    }
}
