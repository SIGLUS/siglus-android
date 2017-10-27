package org.openlmis.core.builders;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.PTVProgram;
import org.openlmis.core.model.PatientDispensation;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(LMISTestRunner.class)
public class PatientDispensationBuilderTest {

    private static final int PATIENT_DISPENSATIONS_SIZE = 2;
    private PatientDispensationBuilder patientDispensationBuilder;

    @Before
    public void setUp() {
        patientDispensationBuilder = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(PatientDispensationBuilder.class);
    }

    @Test
    public void shouldCreatePatientDispensationsForCurrentPTVProgram() {
        PTVProgram ptvProgram = new PTVProgram();
        List<PatientDispensation> actualPatientDispensations = patientDispensationBuilder.buildInitialPatientDispensations(ptvProgram);

        assertThat(actualPatientDispensations.size(), is(PATIENT_DISPENSATIONS_SIZE));
        assertThat(typeExists(actualPatientDispensations, PatientDispensation.Type.CHILD), is(true));
        assertThat(typeExists(actualPatientDispensations, PatientDispensation.Type.WOMAN), is(true));
    }

    private boolean typeExists(List<PatientDispensation> patientDispensations, PatientDispensation.Type type) {
        for (PatientDispensation patientDispensation : patientDispensations) {
            if (patientDispensation.getType().equals(type)) {
                return true;
            }
        }
        return false;
    }
}