package org.openlmis.core.model.repository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.PTVProgram;
import org.openlmis.core.model.PTVProgramStockInformation;
import org.openlmis.core.model.PatientDispensation;
import org.openlmis.core.model.Product;
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

    @Before
    public void setUp() throws Exception {
        ptvProgramRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(PTVProgramRepository.class);
        patientDispensationRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(PatientDispensationRepository.class);
    }

    @Test
    public void shouldSaveProgramRepository() throws LMISException, SQLException {
        PTVProgram ptvProgramExpected = PTVUtil.createDummyPTVProgram();
        Product product = Product.dummyProduct();
        product.setId(10L);
        PatientDispensation patientDispensation = new PatientDispensation();
        patientDispensation.setPtvProgram(ptvProgramExpected);
        List<PatientDispensation> patientDispensations = newArrayList(patientDispensation);
        ptvProgramExpected.setPatientDispensations(patientDispensations);
        PTVProgramStockInformation ptvProgramStockInformation = new PTVProgramStockInformation();
        ptvProgramStockInformation.setPtvProgram(ptvProgramExpected);
        ptvProgramStockInformation.setProduct(product);
        List<PTVProgramStockInformation> ptvProgramStocksInformation = newArrayList(ptvProgramStockInformation);
        ptvProgramExpected.setPtvProgramStocksInformation(ptvProgramStocksInformation);

        PTVProgram ptvProgramSaved = ptvProgramRepository.save(ptvProgramExpected);

        assertThat(ptvProgramSaved, is(ptvProgramExpected));
    }

    @Test
    public void shouldDoRollbackWhenCurrentTransactionFails() throws LMISException, SQLException {
        PTVProgram ptvProgramExpected = PTVUtil.createDummyPTVProgram();
        PatientDispensation patientDispensation = new PatientDispensation();
        List<PatientDispensation> patientDispensations = newArrayList(patientDispensation);
        ptvProgramExpected.setPatientDispensations(patientDispensations);
        exceptionGrabber.expect(SQLException.class);

        ptvProgramRepository.save(ptvProgramExpected);
        List<PTVProgram> ptvPrograms = ptvProgramRepository.getAll();

        assertThat(ptvPrograms.isEmpty(), is(true));
    }

    @Test
    public void shouldDoRollbackWhenCurrentTransactionFailsTryingToSavePTVProgramStocksInformation() throws Exception {
        PTVProgram ptvProgramExpected = PTVUtil.createDummyPTVProgram();
        PatientDispensation patientDispensation = new PatientDispensation();
        patientDispensation.setPtvProgram(ptvProgramExpected);
        List<PatientDispensation> patientDispensations = newArrayList(patientDispensation);
        ptvProgramExpected.setPatientDispensations(patientDispensations);
        PTVProgramStockInformation ptvProgramStockInformation = new PTVProgramStockInformation();
        List<PTVProgramStockInformation> ptvProgramStocksInformation = newArrayList(ptvProgramStockInformation);
        ptvProgramExpected.setPtvProgramStocksInformation(ptvProgramStocksInformation);
        exceptionGrabber.expect(SQLException.class);

        ptvProgramRepository.save(ptvProgramExpected);
        List<PTVProgram> ptvPrograms = ptvProgramRepository.getAll();

        assertThat(ptvPrograms.isEmpty(), is(true));
    }
}