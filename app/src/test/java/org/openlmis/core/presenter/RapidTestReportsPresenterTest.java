package org.openlmis.core.presenter;

import static junit.framework.Assert.assertNull;
import static org.assertj.core.util.Lists.newArrayList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

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
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.model.builder.ProgramDataFormBuilder;
import org.openlmis.core.model.builder.ReportTypeFormBuilder;
import org.openlmis.core.model.repository.ProgramDataFormRepository;
import org.openlmis.core.model.repository.ReportTypeFormRepository;
import org.openlmis.core.model.service.ProgramDataFormPeriodService;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.RapidTestReportViewModel;
import org.roboguice.shaded.goole.common.base.Optional;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class RapidTestReportsPresenterTest {

  private ProgramDataFormRepository programDataFormRepository;

  private ReportTypeFormRepository reportTypeFormRepository;

  private ProgramDataFormPeriodService periodService;

  @InjectMocks
  private RapidTestReportsPresenter presenter;

  @Before
  public void setUp() {

    programDataFormRepository = mock(ProgramDataFormRepository.class);
    reportTypeFormRepository = mock(ReportTypeFormRepository.class);
    periodService = mock(ProgramDataFormPeriodService.class);

    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(ProgramDataFormRepository.class).toInstance(programDataFormRepository);
        bind(ReportTypeFormRepository.class).toInstance(reportTypeFormRepository);
        bind(ProgramDataFormPeriodService.class).toInstance(periodService);
      }
    });

    presenter = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(RapidTestReportsPresenter.class);
  }

  @Test
  public void shouldGenerateViewModelsForAllPeriods() throws Exception {
    //today period is 2016-12-21 to 2017-01-20
    LMISTestApp.getInstance()
        .setCurrentTimeMillis(DateUtil.parseString("2017-3-18", DateUtil.DB_DATE_FORMAT).getTime());
    //first period is 2016-09-21 to 2016-10-20
    Period firstPeriod = spy(
        new Period(new DateTime(DateUtil.parseString("2016-09-21", DateUtil.DB_DATE_FORMAT))));
    Period secondPeriod = spy(
        new Period(new DateTime(DateUtil.parseString("2016-10-21", DateUtil.DB_DATE_FORMAT))));
    Period thirdPeriod = spy(
        new Period(new DateTime(DateUtil.parseString("2016-11-21", DateUtil.DB_DATE_FORMAT))));
    Period fourthPeriod = spy(
        new Period(new DateTime(DateUtil.parseString("2016-12-21", DateUtil.DB_DATE_FORMAT))));
    Period fifthPeriod = spy(
        new Period(new DateTime(DateUtil.parseString("2017-01-21", DateUtil.DB_DATE_FORMAT))));
    Period sixthPeriod = spy(
        new Period(new DateTime(DateUtil.parseString("2017-02-21", DateUtil.DB_DATE_FORMAT))));

    Optional<Period> emptyOptional = Optional.absent();
    when(periodService.getFirstStandardPeriod()).thenReturn(Optional.of(firstPeriod));
    when(firstPeriod.generateNextAvailablePeriod()).thenReturn(Optional.of(secondPeriod));
    when(secondPeriod.generateNextAvailablePeriod()).thenReturn(Optional.of(thirdPeriod));
    when(thirdPeriod.generateNextAvailablePeriod()).thenReturn(Optional.of(fourthPeriod));
    when(fourthPeriod.generateNextAvailablePeriod()).thenReturn(Optional.of(fifthPeriod));
    when(fifthPeriod.generateNextAvailablePeriod()).thenReturn(Optional.of(sixthPeriod));
    when(sixthPeriod.generateNextAvailablePeriod()).thenReturn(emptyOptional);

    Program programRapidTest = new Program(Constants.RAPID_TEST_CODE,
        "Rapid Test",
        null,
        false,
        null,
        null);
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
        .thenReturn(
            newArrayList(programDataForm1, programDataForm2, programDataForm3, programDataForm4));
    ReportTypeForm reportTypeForm = new ReportTypeFormBuilder().
        setActive(true)
        .setCode(Constants.RAPID_REPORT)
        .setName(Constants.RAPID_TEST_CODE)
        .setStartTime(
            new DateTime(DateUtil.parseString("2016-09-10", DateUtil.DB_DATE_FORMAT)).toDate())
        .build();
    when(reportTypeFormRepository.queryByCode(Constants.RAPID_REPORT))
        .thenReturn(reportTypeForm);

    presenter.loadViewModels();
    assertThat(presenter.getViewModelList().size(), is(13));
    for (RapidTestReportViewModel rapidTestReportViewModel : presenter.getViewModelList()) {
      assertNull(rapidTestReportViewModel.getRapidTestForm().getStatus());
      assertThat(rapidTestReportViewModel.getStatus(), is(RapidTestReportViewModel.Status.MISSING));
    }
  }

//    @Test
//    public void shouldaa() throws Exception {
//        //today period is 2016-12-21 to 2017-01-20
//        LMISTestApp.getInstance().setCurrentTimeMillis(DateUtil.parseString("2020-3-18", DateUtil.DB_DATE_FORMAT).getTime());
//        //first period is 2016-09-21 to 2016-10-20
//        Period firstPeriod = spy(new Period(new DateTime(DateUtil.parseString("2019-09-21", DateUtil.DB_DATE_FORMAT))));
//        Period secondPeriod = spy(new Period(new DateTime(DateUtil.parseString("2019-10-21", DateUtil.DB_DATE_FORMAT))));
//        Period thirdPeriod = spy(new Period(new DateTime(DateUtil.parseString("2019-11-21", DateUtil.DB_DATE_FORMAT))));
//        Period fourthPeriod = spy(new Period(new DateTime(DateUtil.parseString("2019-12-21", DateUtil.DB_DATE_FORMAT))));
//        Period fifthPeriod = spy(new Period(new DateTime(DateUtil.parseString("2020-01-21", DateUtil.DB_DATE_FORMAT))));
//        Period sixthPeriod = spy(new Period(new DateTime(DateUtil.parseString("2020-02-21", DateUtil.DB_DATE_FORMAT))));
//
//        Optional<Period> emptyOptional = Optional.absent();
//        when(periodService.getFirstStandardPeriod()).thenReturn(Optional.of(firstPeriod));
//        when(firstPeriod.generateNextAvailablePeriod()).thenReturn(Optional.of(secondPeriod));
//        when(secondPeriod.generateNextAvailablePeriod()).thenReturn(Optional.of(thirdPeriod));
//        when(thirdPeriod.generateNextAvailablePeriod()).thenReturn(Optional.of(fourthPeriod));
//        when(fourthPeriod.generateNextAvailablePeriod()).thenReturn(Optional.of(fifthPeriod));
//        when(fifthPeriod.generateNextAvailablePeriod()).thenReturn(Optional.of(sixthPeriod));
//        when(sixthPeriod.generateNextAvailablePeriod()).thenReturn(emptyOptional);
//
//        Program programRapidTest = new Program(Constants.RAPID_TEST_CODE,
//                "Rapid Test",
//                null,
//                false,
//                null,
//                null);
//        ProgramDataForm programDataForm1 = new ProgramDataFormBuilder()
//                .setPeriod(DateUtil.parseString("2019-10-21", DateUtil.DB_DATE_FORMAT))
//                .setStatus(ProgramDataForm.STATUS.SUBMITTED)
//                .setProgram(programRapidTest)
//                .build();
//
//        ProgramDataForm programDataForm4 = new ProgramDataFormBuilder()
//                .setPeriod(DateUtil.parseString("2019-11-21", DateUtil.DB_DATE_FORMAT))
//                .setProgram(programRapidTest)
//                .setSynced(true)
//                .setStatus(ProgramDataForm.STATUS.AUTHORIZED)
//                .build();
//
//        ProgramDataForm programDataForm2 = new ProgramDataFormBuilder()
//                .setPeriod(DateUtil.parseString("2019-12-21", DateUtil.DB_DATE_FORMAT))
//                .setProgram(programRapidTest)
//                .setStatus(ProgramDataForm.STATUS.AUTHORIZED)
//                .build();
//        ProgramDataForm programDataForm3 = new ProgramDataFormBuilder()
//                .setPeriod(DateUtil.parseString("2020-01-21", DateUtil.DB_DATE_FORMAT))
//                .setProgram(programRapidTest)
//                .setStatus(ProgramDataForm.STATUS.DRAFT)
//                .build();
//
//        when(programDataFormRepository.listByProgramCode(Constants.RAPID_TEST_CODE))
//                .thenReturn(newArrayList(programDataForm1, programDataForm2, programDataForm3, programDataForm4));
//        ReportTypeForm reportTypeForm = new ReportTypeFormBuilder()
//                .setActive(true)
//                .setCode(Constants.RAPID_REPORT)
//                .setName(Constants.RAPID_TEST_CODE)
//                .setStartTime(new DateTime(DateUtil.parseString("2019-06-21", DateUtil.DB_DATE_FORMAT)).toDate())
//                .build();
//        when(reportTypeFormRepository.queryByCode(Constants.RAPID_REPORT))
//                .thenReturn(reportTypeForm);
//
//        presenter.loadViewModels();
//        assertThat(presenter.getViewModelList().size(), is(1));
//        for (RapidTestReportViewModel rapidTestReportViewModel : presenter.getViewModelList()) {
//            assertThat(rapidTestReportViewModel.getRapidTestForm().getStatus(), is(ProgramDataForm.STATUS.SUBMITTED));
//            assertThat(rapidTestReportViewModel.getStatus(), is(RapidTestReportViewModel.Status.INCOMPLETE));
//        }
//    }
}