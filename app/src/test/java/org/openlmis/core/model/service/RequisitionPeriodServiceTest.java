package org.openlmis.core.model.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

import com.google.inject.AbstractModule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRForm.Emergency;
import org.openlmis.core.model.builder.ReportTypeFormBuilder;
import org.openlmis.core.model.repository.InventoryRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.ReportTypeFormRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import androidx.test.core.app.ApplicationProvider;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
@SuppressWarnings("PMD")
public class RequisitionPeriodServiceTest {

  private ProgramRepository mockProgramRepository;
  private RnrFormRepository mockRnrFormRepository;
  private StockRepository mockStockRepository;

  private RequisitionPeriodService requisitionPeriodService;
  private Program programMMIA;
  private InventoryRepository mockInventoryRepository;
  private StockMovementRepository mockStockMovementRepository;
  private ReportTypeFormRepository mockReportTypeFormRepository;

  @Before
  public void setup() throws LMISException {
    mockProgramRepository = mock(ProgramRepository.class);
    mockRnrFormRepository = mock(RnrFormRepository.class);
    mockStockRepository = mock(StockRepository.class);
    mockInventoryRepository = mock(InventoryRepository.class);
    mockStockMovementRepository = mock(StockMovementRepository.class);
    mockReportTypeFormRepository = mock(ReportTypeFormRepository.class);

    RoboGuice.overrideApplicationInjector(ApplicationProvider.getApplicationContext(), new MyTestModule());
    requisitionPeriodService = RoboGuice.getInjector(ApplicationProvider.getApplicationContext())
        .getInstance(RequisitionPeriodService.class);

    programMMIA = new Program("MMIA", "MMIA", null, false, null, null);
    programMMIA.setId(1l);
  }

  @Test
  public void shouldGeneratePeriodWithPreviousRnrEndDateAsBeginAndNextMonthAsEndDate()
      throws Exception {
    RnRForm previousRnrForm = new RnRForm();
    previousRnrForm.setProgram(programMMIA);
    previousRnrForm.setPeriodEnd(DateUtil.parseString("2020-10-18", DateUtil.DB_DATE_FORMAT));
    DateTime dateTime = new DateTime(previousRnrForm.getPeriodEnd()).plusMonths(1);
    DateTime expectedPeriodEnd = DateUtil.cutTimeStamp(
        dateTime.withDate(dateTime.getYear(), dateTime.getMonthOfYear(), Period.END_DAY));
    when(mockRnrFormRepository.listInclude(any(), anyString(), any(ReportTypeForm.class)))
        .thenReturn(newArrayList(previousRnrForm));

    Period period = requisitionPeriodService.generateNextPeriod(programMMIA.getProgramCode(), null);
    assertThat(period.getBegin().toDate(), is(previousRnrForm.getPeriodEnd()));
    assertThat(new DateTime(period.getEnd()).getMonthOfYear(), is(11));

    assertThat(period.getEnd(), is(expectedPeriodEnd));
  }

  @Test
  public void shouldGeneratePeriodOfJan21ToFebWhenRnrNotExists() throws Exception {
    when(mockStockMovementRepository.queryEarliestStockMovementDateByProgram(anyString()))
        .thenReturn(new DateTime("2016-02-17").toDate());
    when(mockProgramRepository.queryByCode(anyString())).thenReturn(programMMIA);
    ReportTypeForm reportTypeForm = new ReportTypeFormBuilder().
        setActive(true).
        setCode(Constants.MMIA_REPORT).
        setName(Constants.MMIA_PROGRAM_CODE).
        setStartTime(
            new DateTime(DateUtil.parseString("2015-01-01", DateUtil.DB_DATE_FORMAT)).toDate()).
        build();
    when(mockReportTypeFormRepository.queryByCode(Constants.MMIA_REPORT))
        .thenReturn(reportTypeForm);
    when(mockReportTypeFormRepository.getReportType(anyString())).thenReturn(reportTypeForm);
    Period period = requisitionPeriodService.generateNextPeriod(programMMIA.getProgramCode(), null);
    assertThat(period.getBegin(), is(new DateTime("2016-02-21")));
    assertThat(new DateTime(period.getEnd()).getMonthOfYear(), is(3));
  }

  @Test
  public void shouldGenerate12MonthsAgoPeriodBasedOnLastReportEndDate() throws Exception {
    LMISTestApp.getInstance().setCurrentTimeMillis(
        DateUtil.parseString("2021-05-24", DateUtil.DB_DATE_FORMAT).getTime());

    ReportTypeForm reportTypeForm = new ReportTypeFormBuilder()
        .setActive(true)
        .setCode(Constants.RAPID_TEST_PROGRAM_CODE)
        .setName(Constants.RAPID_TEST_PROGRAM_CODE)
        .setStartTime(new DateTime(DateUtil.parseString("2015-01-01", DateUtil.DB_DATE_FORMAT)).toDate())
        .setLastReportEndTime("2020-01-20")
        .build();
    when(mockReportTypeFormRepository.queryByCode(programMMIA.getProgramCode()))
        .thenReturn(reportTypeForm);
    when(mockReportTypeFormRepository.getReportType(anyString())).thenReturn(reportTypeForm);
    Period period = requisitionPeriodService
        .generateNextPeriod(new ArrayList<>(), programMMIA.getProgramCode(), null);
    DateTime dateTime = new DateTime(LMISTestApp.getInstance().getCurrentTimeMillis());
    int year = new DateTime(LMISTestApp.getInstance().getCurrentTimeMillis()).minusMonths(13).getYear();
    int dayOfMonth = dateTime.dayOfMonth().get();
    int monthOfYear = dateTime.monthOfYear().get();
    if (dayOfMonth >= Period.BEGIN_DAY) {
      assertThat(period.getBegin(),
          is(new DateTime(DateUtil
              .parseString(String.format("%s-%s-21 12:00:00", year, monthOfYear), DateUtil.DB_DATE_FORMAT))));
      assertThat(period.getEnd(),
          is(new DateTime(
              DateUtil.parseString(String.format("%s-%s-20 12:00:00", year, monthOfYear + 1),
                  DateUtil.DB_DATE_FORMAT))));
    } else {
      assertThat(period.getBegin(),
          is(new DateTime(
              DateUtil.parseString(String.format("%s-%s-21 12:00:00", year, (monthOfYear - 1)),
                  DateUtil.DB_DATE_FORMAT))));
      assertThat(period.getEnd(),
          is(new DateTime(
              DateUtil.parseString(String.format("%s-%s-20 12:00:00", year, (monthOfYear)), DateUtil.DB_DATE_FORMAT))));
    }
  }

  @Test
  public void shouldGeneratePeriodOfFeb18ToMarWhenRnrNotExists() throws Exception {
    when(mockStockMovementRepository.queryEarliestStockMovementDateByProgram(anyString()))
        .thenReturn(DateUtil.parseString("2016-02-18 13:00:00", DateUtil.DATE_TIME_FORMAT));
    ReportTypeForm reportTypeForm = new ReportTypeFormBuilder()
        .setActive(true)
        .setCode(Constants.MMIA_REPORT)
        .setName(Constants.MMIA_PROGRAM_CODE)
        .setStartTime(new DateTime(DateUtil.parseString("2016-02-01", DateUtil.DB_DATE_FORMAT)).toDate())
        .build();
    when(mockReportTypeFormRepository.queryByCode(programMMIA.getProgramCode())).thenReturn(reportTypeForm);
    when(mockReportTypeFormRepository.getReportType(programMMIA.getProgramCode())).thenReturn(reportTypeForm);
    Period period = requisitionPeriodService.generateNextPeriod(programMMIA.getProgramCode(), null);

    assertThat(period.getBegin(),
        is(new DateTime(DateUtil.parseString("2016-02-21 00:00:00", DateUtil.DATE_TIME_FORMAT))));
    assertThat(new DateTime(period.getEnd()).getMonthOfYear(), is(3));
  }

  @Test
  public void shouldGetOffsetPeriodMonthWhenHasMissedPeriod() throws Exception {
    requisitionPeriodService = spy(requisitionPeriodService);

    LMISTestApp.getInstance().setCurrentTimeMillis(
        DateUtil.parseString("2015-05-18 17:30:00", DateUtil.DATE_TIME_FORMAT).getTime());
    DateTime nextPeriodBegin = new DateTime(
        DateUtil.parseString("2015-01-21", DateUtil.DB_DATE_FORMAT));
    DateTime nextPeriodEnd = new DateTime(
        DateUtil.parseString("2015-02-20", DateUtil.DB_DATE_FORMAT));
    Period nextPeriodInSchedule = new Period(nextPeriodBegin, nextPeriodEnd);
    doReturn(nextPeriodInSchedule).when(requisitionPeriodService).generateNextPeriod("P1", null);

    assertThat(requisitionPeriodService.getMissedPeriodOffsetMonth("P1"), is(3));
  }

  @Test
  public void shouldGetOffsetPeriodMonthWhenHasNoMissedPeriod() throws Exception {
    requisitionPeriodService = spy(requisitionPeriodService);

    LMISTestApp.getInstance().setCurrentTimeMillis(
        DateUtil.parseString("2015-05-17 17:30:00", DateUtil.DATE_TIME_FORMAT).getTime());
    DateTime nextPeriodBegin = new DateTime(
        DateUtil.parseString("2015-04-21", DateUtil.DB_DATE_FORMAT));
    DateTime nextPeriodEnd = new DateTime(
        DateUtil.parseString("2015-05-20", DateUtil.DB_DATE_FORMAT));
    Period nextPeriodInSchedule = new Period(nextPeriodBegin, nextPeriodEnd);
    doReturn(nextPeriodInSchedule).when(requisitionPeriodService).generateNextPeriod("P1", null);

    assertThat(requisitionPeriodService.getMissedPeriodOffsetMonth("P1"), is(0));
  }

  @Test
  public void shouldGetOffsetPeriodIsZeroMonthWhenHasMissedPeriod() throws Exception {
    requisitionPeriodService = spy(requisitionPeriodService);

    LMISTestApp.getInstance().setCurrentTimeMillis(
        DateUtil.parseString("2015-03-17 17:30:00", DateUtil.DATE_TIME_FORMAT).getTime());
    DateTime nextPeriodBegin = new DateTime(
        DateUtil.parseString("2015-01-21", DateUtil.DB_DATE_FORMAT));
    DateTime nextPeriodEnd = new DateTime(
        DateUtil.parseString("2015-02-20", DateUtil.DB_DATE_FORMAT));
    Period nextPeriodInSchedule = new Period(nextPeriodBegin, nextPeriodEnd);
    doReturn(nextPeriodInSchedule).when(requisitionPeriodService).generateNextPeriod("P1", null);

    assertThat(requisitionPeriodService.getMissedPeriodOffsetMonth("P1"), is(1));
  }

  @Test
  public void shouldReturnFalseWhenHasOverLimitIsCalledWithNull() throws LMISException {
    // given
    String programCode = "programCode";
    when(mockRnrFormRepository.listInclude(Emergency.YES, programCode)).thenReturn(null);
    // when
    boolean actualResult = requisitionPeriodService.hasOverLimit(programCode, 0, new Date());
    // then
    assertFalse(actualResult);
  }

  @Test
  public void shouldReturnFalseWhenHasOverLimitIsCalledWithEmptyForms() throws LMISException {
    // given
    String programCode = "programCode";
    when(mockRnrFormRepository.listInclude(Emergency.YES, programCode))
        .thenReturn(Collections.emptyList());
    // when
    boolean actualResult = requisitionPeriodService.hasOverLimit(programCode, 0, new Date());
    // then
    assertFalse(actualResult);
  }

  @Test
  public void shouldReturnFalseWhenHasOverLimitIsCalledWithNonEmptyFormsAndValidSizeIsLessThanLimit()
      throws LMISException {
    // given
    String programCode = "programCode";
    Date currentDate = DateUtil.getCurrentDate();

    RnRForm rnRForm = new RnRForm();
    rnRForm.setEmergency(true);
    rnRForm.setPeriodEnd(currentDate);

    when(mockRnrFormRepository.listInclude(Emergency.YES, programCode))
        .thenReturn(newArrayList(rnRForm));
    // when
    boolean actualResult = requisitionPeriodService.hasOverLimit(
        programCode, 2, DateUtil.getFirstDayForCurrentMonthByDate(currentDate)
    );
    // then
    assertFalse(actualResult);
  }

  @Test
  public void shouldReturnTrueWhenHasOverLimitIsCalledWithNonEmptyFormsAndValidSizeIsGELimit()
      throws LMISException {
    // given
    String programCode = "programCode";

    RnRForm rnRForm = new RnRForm();
    rnRForm.setEmergency(true);
    rnRForm.setPeriodEnd(DateUtil.parseString("2024-08-01", DateUtil.DB_DATE_FORMAT));

    when(mockRnrFormRepository.listInclude(Emergency.YES, programCode))
        .thenReturn(newArrayList(rnRForm));
    // when
    boolean actualResult = requisitionPeriodService.hasOverLimit(
        programCode, 1, DateUtil.getFirstDayForCurrentMonthByDate(
            DateUtil.parseString("2024-07-31", DateUtil.DB_DATE_FORMAT))
    );
    // then
    assertTrue(actualResult);
  }

  public class MyTestModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(ProgramRepository.class).toInstance(mockProgramRepository);
      bind(RnrFormRepository.class).toInstance(mockRnrFormRepository);
      bind(StockRepository.class).toInstance(mockStockRepository);
      bind(InventoryRepository.class).toInstance(mockInventoryRepository);
      bind(StockMovementRepository.class).toInstance(mockStockMovementRepository);
      bind(ReportTypeFormRepository.class).toInstance(mockReportTypeFormRepository);
    }
  }
}