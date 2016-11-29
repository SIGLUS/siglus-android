package org.openlmis.core.model.repository;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.builder.ProgramDataFormBuilder;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class ProgramDataFormRepositoryTest extends LMISRepositoryUnitTest {

    private ProgramRepository mockProgramRepository;
    private ProgramDataFormRepository programDataFormRepository;
    private Program programRapidTest;

    @Before
    public void setup() throws LMISException {
        mockProgramRepository = mock(ProgramRepository.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());

        programDataFormRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ProgramDataFormRepository.class);

        programRapidTest = new Program("RapidTest", "Rapid Test", null, false, null);
        programRapidTest.setId(1l);

        when(mockProgramRepository.queryByCode(anyString())).thenReturn(programRapidTest);
    }


    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ProgramRepository.class).toInstance(mockProgramRepository);
        }
    }

    @Test
    public void shouldSaveProgramDataFormsAndListAllProgramDataForms() throws Exception {
        ProgramDataForm programDataForm1 = new ProgramDataFormBuilder()
                .setPeriod(DateUtil.parseString("2016-09-23", DateUtil.DB_DATE_FORMAT))
                .setStatus(ProgramDataForm.STATUS.SUBMITTED)
                .setProgram(programRapidTest)
                .build();
        ProgramDataForm programDataForm2 = new ProgramDataFormBuilder()
                .setPeriod(DateUtil.parseString("2016-10-23", DateUtil.DB_DATE_FORMAT))
                .setProgram(programRapidTest)
                .setStatus(ProgramDataForm.STATUS.SUBMITTED)
                .build();
        programDataFormRepository.save(programDataForm1);
        programDataFormRepository.save(programDataForm2);

        List<ProgramDataForm> programDataFormRetrieved = programDataFormRepository.listAll();

        assertThat(programDataFormRetrieved.get(0).getPeriodBegin(), is(programDataForm1.getPeriodBegin()));
        assertThat(programDataFormRetrieved.get(0).getPeriodEnd(), is(programDataForm1.getPeriodEnd()));
        assertThat(programDataFormRetrieved.get(1).getPeriodBegin(), is(programDataForm2.getPeriodBegin()));
        assertThat(programDataFormRetrieved.get(1).getStatus(), is(programDataForm2.getStatus()));
    }
}