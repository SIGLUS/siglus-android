package org.openlmis.core.service;

import com.google.gson.JsonParser;
import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.User;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.network.adapter.ProgramDataFormAdapter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.JsonFileReader;
import org.robolectric.RuntimeEnvironment;

import roboguice.RoboGuice;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class ProgramDataFormAdapterTest {
    private ProgramDataFormAdapter programDataAdapter;
    private ProgramRepository mockProgramRepository;
    private Program program;

    @Before
    public void setUp() throws LMISException {
        mockProgramRepository = mock(ProgramRepository.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        programDataAdapter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ProgramDataFormAdapter.class);
        UserInfoMgr.getInstance().setUser(new User("user", "password"));
        program = new Program();
        program.setProgramCode(Constants.RAPID_TEST_CODE);
        when(mockProgramRepository.queryByCode(Constants.RAPID_TEST_CODE)).thenReturn(program);
    }

    @Test
    public void shouldDeserializeProgramDataFormJson() throws LMISException {

        String json = JsonFileReader.readJson(getClass(), "SyncDownRapidTestsResponse.json");

        ProgramDataForm programDataForm = programDataAdapter.deserialize(new JsonParser().parse(json), null, null);
        assertThat(programDataForm.getProgram(), is(program));
        assertThat(programDataForm.getPeriodBegin(), is(DateUtil.parseString("2016-02-21", DateUtil.DB_DATE_FORMAT)));
        assertThat(programDataForm.getPeriodEnd(), is(DateUtil.parseString("2016-03-20", DateUtil.DB_DATE_FORMAT)));
        assertThat(programDataForm.getStatus(), is(ProgramDataForm.STATUS.AUTHORIZED));
        assertThat(programDataForm.isSynced(), is(true));
        assertThat(programDataForm.getSubmittedTime(), is(DateUtil.parseString("2016-11-25 12:03:00", DateUtil.DATE_TIME_FORMAT)));
        assertThat(programDataForm.getProgramDataFormItemListWrapper().size(), is(8));
        assertThat(programDataForm.getProgramDataFormItemListWrapper().get(0).getForm(), is(programDataForm));
        assertThat(programDataForm.getProgramDataFormItemListWrapper().get(0).getName(), is("PUB_PHARMACY"));
        assertThat(programDataForm.getProgramDataFormItemListWrapper().get(0).getProgramDataColumnCode(), is("HIV-DETERMINE-CONSUME"));
        assertThat(programDataForm.getProgramDataFormItemListWrapper().get(0).getValue(), is(10));
    }


    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ProgramRepository.class).toInstance(mockProgramRepository);
        }
    }

}
