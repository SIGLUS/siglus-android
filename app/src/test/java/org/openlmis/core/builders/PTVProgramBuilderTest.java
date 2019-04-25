package org.openlmis.core.builders;

import com.google.inject.AbstractModule;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.PTVProgram;
import org.openlmis.core.model.PTVProgramStockInformation;
import org.openlmis.core.model.PatientDispensation;
import org.openlmis.core.model.ViaReportStatus;
import org.openlmis.core.model.Period;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class PTVProgramBuilderTest {

    private PTVProgramBuilder ptvProgramBuilder;
    private PatientDispensationBuilder patientDispensationBuilder;
    private PTVProgramStockInformationBuilder ptvProgramStockInformationBuilder;

    @Before
    public void setUp() throws Exception {
        ptvProgramStockInformationBuilder = mock(PTVProgramStockInformationBuilder.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new PTVProgramBuilderTest.MyTestModule());
        ptvProgramBuilder = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(PTVProgramBuilder.class);
        patientDispensationBuilder = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(PatientDispensationBuilder.class);
    }

    @Test
    public void shouldCreatePTVProgramForCurrentPeriod() throws LMISException {
        Period period = new Period(DateTime.now());
        PTVProgram expectedPTVProgram = new PTVProgram();
        expectedPTVProgram.setStartPeriod(period.getBegin().toDate());
        expectedPTVProgram.setEndPeriod(period.getEnd().toDate());
        expectedPTVProgram.setStatus(ViaReportStatus.MISSING);
        expectedPTVProgram.setCreatedBy("");
        expectedPTVProgram.setVerifiedBy("");
        List<PatientDispensation> patientDispensations = patientDispensationBuilder.buildInitialPatientDispensations(expectedPTVProgram);
        expectedPTVProgram.setPatientDispensations(patientDispensations);
        when(ptvProgramStockInformationBuilder.buildPTVProgramStockInformation(expectedPTVProgram)).thenReturn(new ArrayList<PTVProgramStockInformation>());
        List<PTVProgramStockInformation> ptvProgramStocksInformation = ptvProgramStockInformationBuilder.buildPTVProgramStockInformation(expectedPTVProgram);
        expectedPTVProgram.setPtvProgramStocksInformation(ptvProgramStocksInformation);

        PTVProgram actualPTVProgram = ptvProgramBuilder.buildInitialPTVProgram(period);

        assertEquals(actualPTVProgram, expectedPTVProgram);
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(PTVProgramStockInformationBuilder.class).toInstance(ptvProgramStockInformationBuilder);
        }
    }
}