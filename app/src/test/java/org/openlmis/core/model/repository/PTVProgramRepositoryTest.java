package org.openlmis.core.model.repository;

import android.support.annotation.NonNull;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.enums.PatientDataStatusEnum;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.HealthFacilityService;
import org.openlmis.core.model.PTVProgram;
import org.openlmis.core.model.PTVProgramStockInformation;
import org.openlmis.core.model.PatientDispensation;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.ServiceDispensation;
import org.openlmis.core.utils.PTVUtil;
import org.robolectric.RuntimeEnvironment;

import java.sql.SQLException;
import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class PTVProgramRepositoryTest {

    @Rule
    public ExpectedException exceptionGrabber = ExpectedException.none();

    PTVProgramRepository ptvProgramRepository;

    PatientDispensationRepository patientDispensationRepository;

    private Product product;
    private PTVProgram ptvProgram;

    @Before
    public void setUp() throws Exception {
        ptvProgramRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(PTVProgramRepository.class);
        patientDispensationRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(PatientDispensationRepository.class);
        product = Product.dummyProduct();
        Period period = new Period(DateTime.now());
        ptvProgram = PTVUtil.createDummyPTVProgram(period);
    }

    @Test
    public void shouldSaveProgramRepository() throws LMISException, SQLException {
        PTVProgram expectedPtvProgram = ptvProgram;
        setPatientDispensationsToPTVProgram(expectedPtvProgram);
        PTVProgramStockInformation ptvProgramStockInformation = createValidPTVProgramStockInformation(expectedPtvProgram);
        setHealthFacilityAndPtvProgramStockInformationToServiceDispensation(ptvProgramStockInformation);
        expectedPtvProgram.setPtvProgramStocksInformation(newArrayList(ptvProgramStockInformation));
        expectedPtvProgram.setStatus(PatientDataStatusEnum.MISSING);

        PTVProgram ptvProgramSaved = ptvProgramRepository.save(expectedPtvProgram);

        assertThat(ptvProgramSaved, is(expectedPtvProgram));
    }

    @Test
    public void shouldDoRollbackWhenCurrentTransactionFailsTryingToSavePatientDispensations() throws LMISException, SQLException {
        ptvProgram.setPatientDispensations(newArrayList(new PatientDispensation()));
        exceptionGrabber.expect(SQLException.class);

        ptvProgramRepository.save(ptvProgram);
        List<PTVProgram> ptvPrograms = ptvProgramRepository.getAll();

        assertThat(ptvPrograms.isEmpty(), is(true));
    }

    @Test
    public void shouldDoRollbackWhenCurrentTransactionFailsTryingToSavePTVProgramStocksInformation() throws Exception {
        setPatientDispensationsToPTVProgram(ptvProgram);
        ptvProgram.setPtvProgramStocksInformation(newArrayList(new PTVProgramStockInformation()));
        exceptionGrabber.expect(SQLException.class);

        ptvProgramRepository.save(ptvProgram);
        List<PTVProgram> ptvPrograms = ptvProgramRepository.getAll();

        assertThat(ptvPrograms.isEmpty(), is(true));
    }

    @Test
    public void shouldDoRollbackWhenCurrentTransactionFailsTryingToSaveServiceDispensations() throws Exception {
        setPatientDispensationsToPTVProgram(ptvProgram);
        PTVProgramStockInformation ptvProgramStockInformation = createValidPTVProgramStockInformation(ptvProgram);
        ptvProgramStockInformation.setServiceDispensations(newArrayList(new ServiceDispensation()));
        ptvProgram.setPtvProgramStocksInformation(newArrayList(ptvProgramStockInformation));
        exceptionGrabber.expect(SQLException.class);

        ptvProgramRepository.save(ptvProgram);
        List<PTVProgram> ptvPrograms = ptvProgramRepository.getAll();

        assertThat(ptvPrograms.isEmpty(), is(true));
    }

    private void setPatientDispensationsToPTVProgram(PTVProgram ptvProgram) {
        PatientDispensation patientDispensation = new PatientDispensation();
        patientDispensation.setPtvProgram(ptvProgram);
        List<PatientDispensation> patientDispensations = newArrayList(patientDispensation);
        ptvProgram.setPatientDispensations(patientDispensations);
    }

    @NonNull
    private PTVProgramStockInformation createValidPTVProgramStockInformation(PTVProgram ptvProgram) {
        PTVProgramStockInformation ptvProgramStockInformation = new PTVProgramStockInformation();
        ptvProgramStockInformation.setPtvProgram(ptvProgram);
        ptvProgramStockInformation.setProduct(product);
        return ptvProgramStockInformation;
    }

    private void setHealthFacilityAndPtvProgramStockInformationToServiceDispensation(PTVProgramStockInformation ptvProgramStockInformation) {
        ServiceDispensation serviceDispensation = new ServiceDispensation();
        serviceDispensation.setHealthFacilityService(new HealthFacilityService());
        serviceDispensation.setPtvProgramStockInformation(ptvProgramStockInformation);
        ptvProgramStockInformation.setServiceDispensations(newArrayList(serviceDispensation));
    }
}