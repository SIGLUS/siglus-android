package org.openlmis.core.model.service;

import com.google.inject.AbstractModule;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.repository.InventoryRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;

import roboguice.RoboGuice;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class PeriodServiceTest {

    private ProgramRepository mockProgramRepository;
    private RnrFormRepository mockRnrFormRepository;
    private StockRepository mockStockRepository;

    private PeriodService periodService;
    private Program programMMIA;
    private InventoryRepository mockInventoryRepository;

    @Before
    public void setup() throws LMISException {
        mockProgramRepository = mock(ProgramRepository.class);
        mockRnrFormRepository = mock(RnrFormRepository.class);
        mockStockRepository = mock(StockRepository.class);
        mockInventoryRepository = mock(InventoryRepository.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        periodService = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(PeriodService.class);

        programMMIA = new Program("MMIA", "MMIA", null);
        programMMIA.setId(1l);
        when(mockProgramRepository.queryByCode(anyString())).thenReturn(programMMIA);
    }

    @Test
    public void shouldGeneratePeriodWithPreviousRnrEndDateAsBeginAndNextMonthAsEndDate() throws Exception {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_requisition_period_logic_change, true);

        RnRForm previousRnrForm = new RnRForm();
        previousRnrForm.setProgram(programMMIA);
        previousRnrForm.setPeriodEnd(DateUtil.parseString("2020-10-18", DateUtil.DB_DATE_FORMAT));
        when(mockRnrFormRepository.list(programMMIA.getProgramCode())).thenReturn(newArrayList(previousRnrForm));

        Period period = periodService.generateNextPeriod(programMMIA.getProgramCode(), null);
        assertThat(period.getBegin().toDate(), is(previousRnrForm.getPeriodEnd()));
        assertThat(new DateTime(period.getEnd()).getMonthOfYear(), is(11));
    }

    @Test
    public void shouldGeneratePeriodOfJan21ToFebWhenRnrNotExists() throws Exception {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_requisition_period_logic_change, true);

        when(mockStockRepository.queryEarliestStockMovementDateByProgram(anyString())).thenReturn(new DateTime("2016-02-17").toDate());

        Period period = periodService.generateNextPeriod(programMMIA.getProgramCode(), null);
        assertThat(period.getBegin(), is(new DateTime("2016-01-21")));
        assertThat(new DateTime(period.getEnd()).getMonthOfYear(), is(2));
    }

    @Test
    public void shouldGeneratePeriodOfFeb18ToMarWhenRnrNotExists() throws Exception {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_requisition_period_logic_change, true);

        when(mockStockRepository.queryEarliestStockMovementDateByProgram(anyString())).thenReturn(DateUtil.parseString("2016-02-18 13:00:00", DateUtil.DATE_TIME_FORMAT));

        Period period = periodService.generateNextPeriod(programMMIA.getProgramCode(), null);
        
        assertThat(period.getBegin(), is(new DateTime(DateUtil.parseString("2016-02-18 00:00:00", DateUtil.DATE_TIME_FORMAT))));
        assertThat(new DateTime(period.getEnd()).getMonthOfYear(), is(3));
    }

    @Test
    public void shouldGeneratePeriodOfFeb21ToMarWhenRnrNotExists() throws Exception {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_requisition_period_logic_change, true);

        when(mockStockRepository.queryEarliestStockMovementDateByProgram(anyString())).thenReturn(new DateTime("2016-02-26").toDate());

        Period period = periodService.generateNextPeriod(programMMIA.getProgramCode(), null);
        assertThat(period.getBegin(), is(new DateTime("2016-02-21")));
        assertThat(new DateTime(period.getEnd()).getMonthOfYear(), is(3));
    }

    @Test
    public void shouldReturnTrueWhenPreviousPeriodIsMissed() throws Exception {
        periodService = spy(periodService);

        LMISTestApp.getInstance().setCurrentTimeMillis(DateUtil.parseString("2015-05-18 17:30:00", DateUtil.DATE_TIME_FORMAT).getTime());
        DateTime nextPeriodBegin = new DateTime(DateUtil.parseString("2015-01-21", DateUtil.DB_DATE_FORMAT));
        DateTime nextPeriodEnd = new DateTime(DateUtil.parseString("2015-02-20", DateUtil.DB_DATE_FORMAT));
        Period nextPeriodInSchedule = new Period(nextPeriodBegin, nextPeriodEnd);

        doReturn(nextPeriodInSchedule).when(periodService).generateNextPeriod("P1", null);
        assertTrue(periodService.hasMissedPeriod("P1"));
    }

    @Test
    public void shouldReturnFalseWhenPreviousPeriodIsNotMissed() throws Exception {
        periodService = spy(periodService);

        LMISTestApp.getInstance().setCurrentTimeMillis(DateUtil.parseString("2015-02-25 17:30:00", DateUtil.DATE_TIME_FORMAT).getTime());
        DateTime nextPeriodBegin = new DateTime(DateUtil.parseString("2015-01-21", DateUtil.DB_DATE_FORMAT));
        DateTime nextPeriodEnd = new DateTime(DateUtil.parseString("2015-02-20", DateUtil.DB_DATE_FORMAT));
        Period nextPeriodInSchedule = new Period(nextPeriodBegin, nextPeriodEnd);

        doReturn(nextPeriodInSchedule).when(periodService).generateNextPeriod("P1", null);

        assertFalse(periodService.hasMissedPeriod("P1"));
    }

    @Test
    public void shouldGetOffsetPeriodMonthWhenHasMissedPeriod() throws Exception {
        periodService = spy(periodService);

        LMISTestApp.getInstance().setCurrentTimeMillis(DateUtil.parseString("2015-05-18 17:30:00", DateUtil.DATE_TIME_FORMAT).getTime());
        DateTime nextPeriodBegin = new DateTime(DateUtil.parseString("2015-01-21", DateUtil.DB_DATE_FORMAT));
        DateTime nextPeriodEnd = new DateTime(DateUtil.parseString("2015-02-20", DateUtil.DB_DATE_FORMAT));
        Period nextPeriodInSchedule = new Period(nextPeriodBegin, nextPeriodEnd);
        doReturn(nextPeriodInSchedule).when(periodService).generateNextPeriod("P1", null);

        assertThat(periodService.getMissedPeriodOffsetMonth("P1"), is(4));
    }

    @Test
    public void shouldGetOffsetPeriodMonthWhenHasNoMissedPeriod() throws Exception {
        periodService = spy(periodService);

        LMISTestApp.getInstance().setCurrentTimeMillis(DateUtil.parseString("2015-05-17 17:30:00", DateUtil.DATE_TIME_FORMAT).getTime());
        DateTime nextPeriodBegin = new DateTime(DateUtil.parseString("2015-04-21", DateUtil.DB_DATE_FORMAT));
        DateTime nextPeriodEnd = new DateTime(DateUtil.parseString("2015-05-20", DateUtil.DB_DATE_FORMAT));
        Period nextPeriodInSchedule = new Period(nextPeriodBegin, nextPeriodEnd);
        doReturn(nextPeriodInSchedule).when(periodService).generateNextPeriod("P1", null);

        assertThat(periodService.getMissedPeriodOffsetMonth("P1"), is(0));
    }

    @Test
    public void shouldGetOffsetPeriodIsZeroMonthWhenHasMissedPeriod() throws Exception {
        periodService = spy(periodService);

        LMISTestApp.getInstance().setCurrentTimeMillis(DateUtil.parseString("2015-03-17 17:30:00", DateUtil.DATE_TIME_FORMAT).getTime());
        DateTime nextPeriodBegin = new DateTime(DateUtil.parseString("2015-01-21", DateUtil.DB_DATE_FORMAT));
        DateTime nextPeriodEnd = new DateTime(DateUtil.parseString("2015-02-20", DateUtil.DB_DATE_FORMAT));
        Period nextPeriodInSchedule = new Period(nextPeriodBegin, nextPeriodEnd);
        doReturn(nextPeriodInSchedule).when(periodService).generateNextPeriod("P1", null);

        assertThat(periodService.getMissedPeriodOffsetMonth("P1"), is(1));
    }

    @Test
    public void shouldGeneratePeriodOfDes21ToJanWhenRnrNotExists() throws Exception {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_requisition_period_logic_change, true);

        when(mockStockRepository.queryEarliestStockMovementDateByProgram(anyString())).thenReturn(new DateTime("2016-01-06").toDate());

        Period period = periodService.generateNextPeriod(programMMIA.getProgramCode(), null);
        assertThat(period.getBegin(), is(new DateTime("2015-12-21")));
        assertThat(new DateTime(period.getEnd()).getMonthOfYear(), is(1));
    }

    @Test
    public void shouldGeneratePeriodOfDes19ToJanWhenRnrNotExists() throws Exception {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_requisition_period_logic_change, true);

        when(mockStockRepository.queryEarliestStockMovementDateByProgram(anyString())).thenReturn(new DateTime("2015-12-19").toDate());

        Period period = periodService.generateNextPeriod(programMMIA.getProgramCode(), null);
        assertThat(period.getBegin(), is(new DateTime("2015-12-19")));
        assertThat(new DateTime(period.getEnd()).getMonthOfYear(), is(1));
    }

    @Test
    public void shouldReturnTrueIfRnrFromPreviousPeriodExistsButIsNotAuthorized() throws Exception {
        periodService = spy(periodService);

        LMISTestApp.getInstance().setCurrentTimeMillis(DateUtil.parseString("2015-06-18 17:30:00", DateUtil.DATE_TIME_FORMAT).getTime());
        DateTime nextPeriodBegin = new DateTime(DateUtil.parseString("2015-05-18", DateUtil.DB_DATE_FORMAT));
        DateTime nextPeriodEnd = new DateTime(DateUtil.parseString("2015-06-20", DateUtil.DB_DATE_FORMAT));
        Period nextPeriodInSchedule = new Period(nextPeriodBegin, nextPeriodEnd);

        doReturn(nextPeriodInSchedule).when(periodService).generateNextPeriod("P1", null);
        RnRForm rnRForm = new RnRForm();
        rnRForm.setStatus(RnRForm.STATUS.DRAFT_MISSED);
        rnRForm.setPeriodBegin(DateUtil.parseString("2015-04-18", DateUtil.DB_DATE_FORMAT));
        rnRForm.setPeriodEnd(DateUtil.parseString("2015-05-18", DateUtil.DB_DATE_FORMAT));
        when(mockRnrFormRepository.list("P1")).thenReturn(newArrayList(rnRForm));

        assertTrue(periodService.hasMissedPeriod("P1"));
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ProgramRepository.class).toInstance(mockProgramRepository);
            bind(RnrFormRepository.class).toInstance(mockRnrFormRepository);
            bind(StockRepository.class).toInstance(mockStockRepository);
            bind(InventoryRepository.class).toInstance(mockInventoryRepository);
        }
    }
}