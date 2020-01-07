package org.openlmis.core.model.service;

import com.google.inject.AbstractModule;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.builder.ReportTypeBuilder;
import org.openlmis.core.model.builder.ReportTypeFormBuilder;
import org.openlmis.core.model.repository.InventoryRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.ReportTypeFormRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;

import java.util.Date;

import roboguice.RoboGuice;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
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

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        requisitionPeriodService = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(RequisitionPeriodService.class);

        programMMIA = new Program("MMIA", "MMIA", null, false, null, null);
        programMMIA.setId(1l);
    }

    @Test
    public void shouldGeneratePeriodWithPreviousRnrEndDateAsBeginAndNextMonthAsEndDate() throws Exception {
        RnRForm previousRnrForm = new RnRForm();
        previousRnrForm.setProgram(programMMIA);
        previousRnrForm.setPeriodEnd(DateUtil.parseString("2020-10-18", DateUtil.DB_DATE_FORMAT));
        DateTime dateTime = new DateTime(previousRnrForm.getPeriodEnd()).plusMonths(1);
        DateTime expectedPeriodEnd = DateUtil.cutTimeStamp(dateTime.withDate(dateTime.getYear(), dateTime.getMonthOfYear(), Period.END_DAY));
        when(mockRnrFormRepository.listInclude(any(), anyString(), any(ReportTypeForm.class))).thenReturn(newArrayList(previousRnrForm));

        Period period = requisitionPeriodService.generateNextPeriod(programMMIA.getProgramCode(), null);
        assertThat(period.getBegin().toDate(), is(previousRnrForm.getPeriodEnd()));
        assertThat(new DateTime(period.getEnd()).getMonthOfYear(), is(11));

        assertThat(period.getEnd(), is(expectedPeriodEnd));
    }

    @Test
    //TODO later
    @Ignore
    public void shouldGeneratePeriodOfJan21ToFebWhenRnrNotExists() throws Exception {
        when(mockStockMovementRepository.queryEarliestStockMovementDateByProgram(anyString())).thenReturn(new DateTime("2016-02-17").toDate());
        when(mockProgramRepository.queryByCode(anyString())).thenReturn(programMMIA);
        ReportTypeForm reportTypeForm = new ReportTypeFormBuilder().
                setActive(true).
                setCode(Constants.MMIA_REPORT).
                setName(Constants.MMIA_PROGRAM_CODE).
                setStartTime(new DateTime(DateUtil.parseString("2015-01-01", DateUtil.DB_DATE_FORMAT)).toDate()).
                build();
        when(mockReportTypeFormRepository.queryByCode(Constants.MMIA_REPORT))
                .thenReturn(reportTypeForm);
        when(mockReportTypeFormRepository.getReportType(anyString())).thenReturn(reportTypeForm);
        Period period = requisitionPeriodService.generateNextPeriod(programMMIA.getProgramCode(), null);
        assertThat(period.getBegin(), is(new DateTime("2016-01-21")));
        assertThat(new DateTime(period.getEnd()).getMonthOfYear(), is(2));
    }

    @Test
    //TODO later
    @Ignore
    public void shouldGeneratePeriodOfFeb18ToMarWhenRnrNotExists() throws Exception {
        when(mockStockMovementRepository.queryEarliestStockMovementDateByProgram(anyString())).thenReturn(DateUtil.parseString("2016-02-18 13:00:00", DateUtil.DATE_TIME_FORMAT));

        Period period = requisitionPeriodService.generateNextPeriod(programMMIA.getProgramCode(), null);

        assertThat(period.getBegin(), is(new DateTime(DateUtil.parseString("2016-02-18 00:00:00", DateUtil.DATE_TIME_FORMAT))));
        assertThat(new DateTime(period.getEnd()).getMonthOfYear(), is(3));
    }

    @Test
    //TODO later
    @Ignore
    public void shouldGeneratePeriodOfFeb21ToMarWhenRnrNotExists() throws Exception {
        when(mockStockMovementRepository.queryEarliestStockMovementDateByProgram(anyString())).thenReturn(new DateTime("2016-02-26").toDate());

        Period period = requisitionPeriodService.generateNextPeriod(programMMIA.getProgramCode(), null);
        assertThat(period.getBegin(), is(new DateTime("2016-02-21")));
        assertThat(new DateTime(period.getEnd()).getMonthOfYear(), is(3));
    }

    @Test
    //TODO later
    @Ignore
    public void shouldReturnTrueWhenPreviousPeriodIsMissed() throws Exception {
        requisitionPeriodService = spy(requisitionPeriodService);

        LMISTestApp.getInstance().setCurrentTimeMillis(DateUtil.parseString("2015-05-18 17:30:00", DateUtil.DATE_TIME_FORMAT).getTime());
        DateTime nextPeriodBegin = new DateTime(DateUtil.parseString("2015-01-21", DateUtil.DB_DATE_FORMAT));
        DateTime nextPeriodEnd = new DateTime(DateUtil.parseString("2015-02-20", DateUtil.DB_DATE_FORMAT));
        Period nextPeriodInSchedule = new Period(nextPeriodBegin, nextPeriodEnd);

        doReturn(nextPeriodInSchedule).when(requisitionPeriodService).generateNextPeriod("P1", null);
        assertTrue(requisitionPeriodService.hasMissedPeriod("P1"));
    }

    @Test
    public void shouldReturnFalseWhenPreviousPeriodIsNotMissed() throws Exception {
        requisitionPeriodService = spy(requisitionPeriodService);

        LMISTestApp.getInstance().setCurrentTimeMillis(DateUtil.parseString("2015-02-25 17:30:00", DateUtil.DATE_TIME_FORMAT).getTime());
        DateTime nextPeriodBegin = new DateTime(DateUtil.parseString("2015-01-21", DateUtil.DB_DATE_FORMAT));
        DateTime nextPeriodEnd = new DateTime(DateUtil.parseString("2015-02-20", DateUtil.DB_DATE_FORMAT));
        Period nextPeriodInSchedule = new Period(nextPeriodBegin, nextPeriodEnd);

        doReturn(nextPeriodInSchedule).when(requisitionPeriodService).generateNextPeriod("P1", null);

        assertFalse(requisitionPeriodService.hasMissedPeriod("P1"));
    }

    @Test
    public void shouldGetOffsetPeriodMonthWhenHasMissedPeriod() throws Exception {
        requisitionPeriodService = spy(requisitionPeriodService);

        LMISTestApp.getInstance().setCurrentTimeMillis(DateUtil.parseString("2015-05-18 17:30:00", DateUtil.DATE_TIME_FORMAT).getTime());
        DateTime nextPeriodBegin = new DateTime(DateUtil.parseString("2015-01-21", DateUtil.DB_DATE_FORMAT));
        DateTime nextPeriodEnd = new DateTime(DateUtil.parseString("2015-02-20", DateUtil.DB_DATE_FORMAT));
        Period nextPeriodInSchedule = new Period(nextPeriodBegin, nextPeriodEnd);
        doReturn(nextPeriodInSchedule).when(requisitionPeriodService).generateNextPeriod("P1", null);

        assertThat(requisitionPeriodService.getMissedPeriodOffsetMonth("P1"), is(4));
    }

    @Test
    public void shouldGetOffsetPeriodMonthWhenHasNoMissedPeriod() throws Exception {
        requisitionPeriodService = spy(requisitionPeriodService);

        LMISTestApp.getInstance().setCurrentTimeMillis(DateUtil.parseString("2015-05-17 17:30:00", DateUtil.DATE_TIME_FORMAT).getTime());
        DateTime nextPeriodBegin = new DateTime(DateUtil.parseString("2015-04-21", DateUtil.DB_DATE_FORMAT));
        DateTime nextPeriodEnd = new DateTime(DateUtil.parseString("2015-05-20", DateUtil.DB_DATE_FORMAT));
        Period nextPeriodInSchedule = new Period(nextPeriodBegin, nextPeriodEnd);
        doReturn(nextPeriodInSchedule).when(requisitionPeriodService).generateNextPeriod("P1", null);

        assertThat(requisitionPeriodService.getMissedPeriodOffsetMonth("P1"), is(0));
    }

    @Test
    public void shouldGetOffsetPeriodIsZeroMonthWhenHasMissedPeriod() throws Exception {
        requisitionPeriodService = spy(requisitionPeriodService);

        LMISTestApp.getInstance().setCurrentTimeMillis(DateUtil.parseString("2015-03-17 17:30:00", DateUtil.DATE_TIME_FORMAT).getTime());
        DateTime nextPeriodBegin = new DateTime(DateUtil.parseString("2015-01-21", DateUtil.DB_DATE_FORMAT));
        DateTime nextPeriodEnd = new DateTime(DateUtil.parseString("2015-02-20", DateUtil.DB_DATE_FORMAT));
        Period nextPeriodInSchedule = new Period(nextPeriodBegin, nextPeriodEnd);
        doReturn(nextPeriodInSchedule).when(requisitionPeriodService).generateNextPeriod("P1", null);

        assertThat(requisitionPeriodService.getMissedPeriodOffsetMonth("P1"), is(1));
    }

    @Test
    //TODO later
    @Ignore
    public void shouldGeneratePeriodOfDes21ToJanWhenRnrNotExists() throws Exception {
        when(mockStockMovementRepository.queryEarliestStockMovementDateByProgram(anyString())).thenReturn(new DateTime("2016-01-06").toDate());

        Period period = requisitionPeriodService.generateNextPeriod(programMMIA.getProgramCode(), null);
        assertThat(period.getBegin(), is(new DateTime("2015-12-21")));
        assertThat(new DateTime(period.getEnd()).getMonthOfYear(), is(1));
    }

    @Test
    //TODO later
    @Ignore
    public void shouldGeneratePeriodOfDes19ToJanWhenRnrNotExists() throws Exception {
        DateTime dateTime = new DateTime("2015-12-19").plusMonths(1);
        DateTime expectedPeriodEnd = DateUtil.cutTimeStamp(dateTime.withDate(dateTime.getYear(), dateTime.getMonthOfYear(), Period.END_DAY));
        when(mockStockMovementRepository.queryEarliestStockMovementDateByProgram(anyString())).thenReturn(new DateTime("2015-12-19").toDate());

        Period period = requisitionPeriodService.generateNextPeriod(programMMIA.getProgramCode(), null);
        assertThat(period.getBegin(), is(new DateTime("2015-12-19")));
        assertThat(new DateTime(period.getEnd()).getMonthOfYear(), is(1));
        assertThat(period.getEnd(), is(expectedPeriodEnd));
    }

    @Test
    //TODO later
    @Ignore
    public void shouldReturnTrueIfRnrFromPreviousPeriodExistsButIsNotAuthorized() throws Exception {
        requisitionPeriodService = spy(requisitionPeriodService);

        LMISTestApp.getInstance().setCurrentTimeMillis(DateUtil.parseString("2015-06-18 17:30:00", DateUtil.DATE_TIME_FORMAT).getTime());
        DateTime nextPeriodBegin = new DateTime(DateUtil.parseString("2015-05-18", DateUtil.DB_DATE_FORMAT));
        DateTime nextPeriodEnd = new DateTime(DateUtil.parseString("2015-06-20", DateUtil.DB_DATE_FORMAT));
        Period nextPeriodInSchedule = new Period(nextPeriodBegin, nextPeriodEnd);

        doReturn(nextPeriodInSchedule).when(requisitionPeriodService).generateNextPeriod("P1", null);
        RnRForm rnRForm = new RnRForm();
        rnRForm.setStatus(RnRForm.STATUS.DRAFT_MISSED);
        rnRForm.setPeriodBegin(DateUtil.parseString("2015-04-18", DateUtil.DB_DATE_FORMAT));
        rnRForm.setPeriodEnd(DateUtil.parseString("2015-05-18", DateUtil.DB_DATE_FORMAT));
        when(mockRnrFormRepository.listInclude(RnRForm.Emergency.No, "P1")).thenReturn(newArrayList(rnRForm));

        assertTrue(requisitionPeriodService.hasMissedPeriod("P1"));
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ProgramRepository.class).toInstance(mockProgramRepository);
            bind(RnrFormRepository.class).toInstance(mockRnrFormRepository);
            bind(StockRepository.class).toInstance(mockStockRepository);
            bind(InventoryRepository.class).toInstance(mockInventoryRepository);
            bind(StockMovementRepository.class).toInstance(mockStockMovementRepository);
        }
    }
}