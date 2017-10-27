package org.openlmis.core.model.repository;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.PTVProgram;
import org.openlmis.core.model.PatientDispensation;
import org.openlmis.core.model.Period;
import org.openlmis.core.utils.PTVUtil;
import org.robolectric.RuntimeEnvironment;

import java.util.List;
import java.util.Random;

import roboguice.RoboGuice;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class PatientDispensationRepositoryTest {

    public static final int TOTAL_PATIENT_DISPENSATION_TYPES = 2;

    private PatientDispensationRepository patientDispensationRepository;
    private PTVProgram ptvProgram;

    @Before
    public void setUp() throws Exception {
        patientDispensationRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(PatientDispensationRepository.class);
        Period period = new Period(DateTime.now());
        ptvProgram = PTVUtil.createDummyPTVProgram(period);
    }

    @Test
    public void shouldReturnTrueWhenPatientDispensationsWereSaved() throws LMISException {
        ptvProgram.setId(1L);
        PatientDispensation patientDispensation = createPatientDispensationObject();
        PatientDispensation patientDispensation2 = createPatientDispensationObject();
        patientDispensation.setPtvProgram(ptvProgram);
        patientDispensation2.setPtvProgram(ptvProgram);

        boolean isSaved = patientDispensationRepository.save(newArrayList(patientDispensation, patientDispensation2));

        assertThat(isSaved, is(true));
    }

    @Test (expected = LMISException.class)
    public void shouldThrowAnExceptionWhenAnOccurredErrorSavingPatientDispensations() throws LMISException {
        PatientDispensation patientDispensation = createPatientDispensationObject();
        PatientDispensation patientDispensation2 = createPatientDispensationObject();

        patientDispensationRepository.save(newArrayList(patientDispensation, patientDispensation2));
    }

    @Test
    public void shouldReturnThePatientDispensationsByProgramIdWhenThisExists() throws LMISException {
        ptvProgram.setId(1L);
        PatientDispensation patientDispensationChild = createPatientDispensationObject();
        PatientDispensation patientDispensationWoman = createPatientDispensationObject();
        patientDispensationChild.setPtvProgram(ptvProgram);
        patientDispensationWoman.setPtvProgram(ptvProgram);
        List<PatientDispensation> patientDispensationsExpected = newArrayList(patientDispensationChild, patientDispensationWoman);

        patientDispensationRepository.save(patientDispensationsExpected);
        List<PatientDispensation> patientDispensationsActual = patientDispensationRepository.getAllByProgramId(ptvProgram.getId());

        assertThat(patientDispensationsActual.size(), is(TOTAL_PATIENT_DISPENSATION_TYPES));
        assertThat(patientDispensationsActual, is(patientDispensationsExpected));
    }

    @Test
    public void shouldReturnAnEmptyListWhenThereAreNotPatientDispensationSavedForCurrentProgram() throws LMISException {
        List<PatientDispensation> patientDispensations = patientDispensationRepository.getAllByProgramId(ptvProgram.getId());

        assertThat(patientDispensations.isEmpty(), is(true));
    }

    private PatientDispensation createPatientDispensationObject() {
        int typeRandomPosition = new Random().nextInt(2);
        long totalPatients = new Random().nextLong();
        PatientDispensation patientDispensation = new PatientDispensation();
        PatientDispensation.Type patientDispensationType = PatientDispensation.Type.values()[typeRandomPosition];
        patientDispensation.setType(patientDispensationType);
        patientDispensation.setTotal(totalPatients);
        return patientDispensation;
    }

}