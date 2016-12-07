package org.openlmis.core.presenter;

import com.google.inject.AbstractModule;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.builder.ProgramDataFormBuilder;
import org.openlmis.core.model.repository.ProgramDataFormRepository;
import org.openlmis.core.model.service.PeriodService;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.RapidTestReportViewModel;
import org.robolectric.RuntimeEnvironment;

import roboguice.RoboGuice;

import static junit.framework.Assert.assertNull;
import static org.assertj.core.util.Lists.newArrayList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class RapidTestReportsPresenterTest {

    private ProgramDataFormRepository programDataFormRepository;

    private PeriodService periodService;

    @InjectMocks
    private RapidTestReportsPresenter presenter;

    @Before
    public void setUp() {

        programDataFormRepository = mock(ProgramDataFormRepository.class);
        periodService = mock(PeriodService.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(ProgramDataFormRepository.class).toInstance(programDataFormRepository);
                bind(PeriodService.class).toInstance(periodService);
            }
        });

        presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(RapidTestReportsPresenter.class);
    }

    @Test
    public void shouldGenerateViewModelsForAllPeriods() throws Exception {
        //today period is 2016-12-21 to 2017-01-20
        LMISTestApp.getInstance().setCurrentTimeMillis(DateUtil.parseString("2017-3-18", DateUtil.DB_DATE_FORMAT).getTime());
        //first period is 2016-09-21 to 2016-10-20
        Period firstPeriod = new Period(new DateTime(DateUtil.parseString("2016-09-21", DateUtil.DB_DATE_FORMAT)));
        Period secondPeriod = new Period(new DateTime(DateUtil.parseString("2016-10-21", DateUtil.DB_DATE_FORMAT)));
        Period thirdPeriod = new Period(new DateTime(DateUtil.parseString("2016-11-21", DateUtil.DB_DATE_FORMAT)));
        Period fourthPeriod = new Period(new DateTime(DateUtil.parseString("2016-12-21", DateUtil.DB_DATE_FORMAT)));
        Period fifthPeriod = new Period(new DateTime(DateUtil.parseString("2017-01-21", DateUtil.DB_DATE_FORMAT)));
        Period sixthPeriod = new Period(new DateTime(DateUtil.parseString("2017-02-21", DateUtil.DB_DATE_FORMAT)));

        when(periodService.getFirstStandardPeriod()).thenReturn(firstPeriod);
        when(periodService.generateNextPeriod(firstPeriod)).thenReturn(secondPeriod);
        when(periodService.generateNextPeriod(secondPeriod)).thenReturn(thirdPeriod);
        when(periodService.generateNextPeriod(thirdPeriod)).thenReturn(fourthPeriod);
        when(periodService.generateNextPeriod(fourthPeriod)).thenReturn(fifthPeriod);
        when(periodService.generateNextPeriod(fifthPeriod)).thenReturn(sixthPeriod);
        when(periodService.generateNextPeriod(sixthPeriod)).thenReturn(null);

        Program programRapidTest = new Program("RapidTest", "Rapid Test", null, false, null);
        ProgramDataForm programDataForm1 = new ProgramDataFormBuilder()
                .setPeriod(DateUtil.parseString("2016-10-21", DateUtil.DB_DATE_FORMAT))
                .setStatus(ProgramDataForm.STATUS.SUBMITTED)
                .setProgram(programRapidTest)
                .build();
        ProgramDataForm programDataForm2 = new ProgramDataFormBuilder()
                .setPeriod(DateUtil.parseString("2016-12-21", DateUtil.DB_DATE_FORMAT))
                .setProgram(programRapidTest)
                .setStatus(ProgramDataForm.STATUS.AUTHORIZED)
                .build();
        ProgramDataForm programDataForm3 = new ProgramDataFormBuilder()
                .setPeriod(DateUtil.parseString("2017-01-21", DateUtil.DB_DATE_FORMAT))
                .setProgram(programRapidTest)
                .setStatus(ProgramDataForm.STATUS.DRAFT)
                .build();

        ProgramDataForm programDataForm4 = new ProgramDataFormBuilder()
                .setPeriod(DateUtil.parseString("2016-11-21", DateUtil.DB_DATE_FORMAT))
                .setProgram(programRapidTest)
                .setSynced(true)
                .setStatus(ProgramDataForm.STATUS.AUTHORIZED)
                .build();
        when(programDataFormRepository.listByProgramCode(Constants.RAPID_TEST_CODE))
                .thenReturn(newArrayList(programDataForm1, programDataForm2, programDataForm3, programDataForm4));

        presenter.generateViewModelsForAllPeriods();
        assertThat(presenter.getViewModelList().size(), is(6));
        assertNull(presenter.getViewModelList().get(5).getRapidTestForm().getStatus());
        assertThat(presenter.getViewModelList().get(4).getRapidTestForm().getStatus(), is(ProgramDataForm.STATUS.SUBMITTED));
        assertThat(presenter.getViewModelList().get(4).getStatus(), is(RapidTestReportViewModel.Status.INCOMPLETE));
        assertThat(presenter.getViewModelList().get(3).getRapidTestForm().getStatus(), is(ProgramDataForm.STATUS.AUTHORIZED));
        assertThat(presenter.getViewModelList().get(3).getStatus(), is(RapidTestReportViewModel.Status.SYNCED));
        assertThat(presenter.getViewModelList().get(2).getRapidTestForm().getStatus(), is(ProgramDataForm.STATUS.AUTHORIZED));
        assertThat(presenter.getViewModelList().get(2).getStatus(), is(RapidTestReportViewModel.Status.COMPLETED));
        assertThat(presenter.getViewModelList().get(1).getRapidTestForm().getStatus(), is(ProgramDataForm.STATUS.DRAFT));
        assertThat(presenter.getViewModelList().get(1).getStatus(), is(RapidTestReportViewModel.Status.INCOMPLETE));
        assertNull(presenter.getViewModelList().get(0).getRapidTestForm().getStatus());
    }
}