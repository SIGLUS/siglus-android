package org.openlmis.core.model;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(LMISTestRunner.class)
public class PeriodTest {
    private static final int INVENTORY_DAY = 18;
    private static final int PERIOD_END_DAY = 20;
    public static final int ONE_DAY = 1;

    private Period period;

    @Before
    public void setup() {
        period = new Period(DateTime.parse("2017-09-12"));
    }

    @Test
    public void shouldDetermineItsOwnBeginAndEnd() throws Exception {
        testPeriodBeginEnd("2015-02-03", "2015-01-21", "2015-02-20");//normal date
        testPeriodBeginEnd("2016-08-20", "2016-07-21", "2016-08-20");//on end
        testPeriodBeginEnd("2014-06-21", "2014-06-21", "2014-07-20");//on begin
        testPeriodBeginEnd("2014-12-25", "2014-12-21", "2015-01-20");//cross year
    }

    @Test
    public void shouldTellPreviousPeriod() throws Exception {
        Period period = new Period(DateTime.parse("2015-06-07"));
        Period prevPeriod = period.previous();
        assertThat(prevPeriod.getBegin(), is(DateTime.parse("2015-04-21")));
        assertThat(prevPeriod.getEnd(), is(DateTime.parse("2015-05-20")));
    }

    @Test
    public void shouldGetInventoryBeginAndEndDateWhenPeriodEndDateInCurrentMonth() throws Exception {
        Period period = new Period(DateTime.parse("2015-06-07"), DateTime.parse("2015-06-20"));

        assertThat(period.getInventoryBegin(), is(DateTime.parse("2015-06-18")));
        assertThat(period.getInventoryEnd(), is(DateTime.parse("2015-06-26")));
    }

    @Test
    public void shouldGetInventoryBeginAndEndDateWhenPeriodEndDateInNextMonth() throws Exception {
        Period period = new Period(DateTime.parse("2015-07-23"), DateTime.parse("2015-08-24"));

        assertThat(period.getInventoryBegin(), is(DateTime.parse("2015-08-18")));
        assertThat(period.getInventoryEnd(), is(DateTime.parse("2015-08-26")));
    }

    @Test
    public void shouldTellIfDateIsWithInSubmissionWindow() throws Exception {
        Boolean is17ThWithin = Period.isWithinSubmissionWindow(DateTime.parse("2015-05-17"));
        assertFalse(is17ThWithin);

        Boolean is18ThWithin = Period.isWithinSubmissionWindow(DateTime.parse("2014-12-18"));
        assertTrue(is18ThWithin);

        Boolean is25ThWithin = Period.isWithinSubmissionWindow(DateTime.parse("2016-01-25"));
        assertTrue(is25ThWithin);

        Boolean is26ThWithin = Period.isWithinSubmissionWindow(DateTime.parse("2017-08-26"));
        assertFalse(is26ThWithin);
    }

    @Test
    public void shouldGetNextPeriod() throws Exception {
        assertThat(period.next().getBegin(), is(DateTime.parse("2017-09-21")));
        assertThat(period.next().getEnd(), is(DateTime.parse("2017-10-20")));
    }

    @Test
    public void shouldCalculateOpeningRequisitionDate() throws Exception {
        Period period = new Period(DateTime.now());
        DateTime expectedDate = period.getEnd().minusDays(PERIOD_END_DAY - INVENTORY_DAY);
        assertThat(period.getOpeningRequisitionDate(), is(expectedDate));
    }

    @Test
    public void shouldReturnTrueWhenPeriodIsBeforeCurrent() {
        Period periodBefore = new Period(DateTime.parse("2017-08-12"));
        LMISTestApp.getInstance().setCurrentTimeMillis(DateTime.parse("2017-09-12").getMillis());
        assertThat(periodBefore.isOpenToRequisitions(), is(true));
    }

    @Test
    public void shouldReturnFalseWhenPeriodIsCurrentAndTodayIsBeforeRequisitionDate() {
        LMISTestApp.getInstance().setCurrentTimeMillis(DateTime.parse("2017-09-17").getMillis());
        assertThat(period.isOpenToRequisitions(), is(false));
    }

    @Test
    public void shouldReturnTrueWhenCurrentDateIsBetweenOpeningRequisitionIntervalOnCurrentPeriod() {
        DateTime testedDate = DateTime.parse("2017-09-18");
        Interval validInterval = new Interval(testedDate, DateTime.parse("2017-09-26"));
        while (validInterval.contains(testedDate)) {
            LMISTestApp.getInstance().setCurrentTimeMillis(testedDate.getMillis());
            assertThat(period.isOpenToRequisitions(), is(true));
            testedDate = testedDate.plusDays(ONE_DAY);
        }
    }

    @Test
    public void shouldReturnFalseWhenPeriodIsAfterCurrentOne() {
        Period period = new Period(DateTime.parse("2017-09-26"));
        LMISTestApp.getInstance().setCurrentTimeMillis(DateTime.parse("2017-09-17").getMillis());
        assertThat(period.isOpenToRequisitions(), is(false));
    }

    @Test
    public void shouldReturnFalseWhenPeriodIsGreaterThanCurrentOne() {
        Period period = new Period(DateTime.parse("2017-09-21"));
        LMISTestApp.getInstance().setCurrentTimeMillis(DateTime.parse("2017-09-12").getMillis());
        assertThat(period.isOpenToRequisitions(), is(false));
    }

    @Test
    public void shouldReturnAValidPeriodWhenNextPeriodIsAvailableForTheCurrentDate() {
        LMISTestApp.getInstance().setCurrentTimeMillis(DateTime.parse("2017-10-18").getMillis());
        assertThat(period.generateNextAvailablePeriod().get(), is(new Period(DateTime.parse("2017-10-12"))));
    }

    private void testPeriodBeginEnd(String anyDayInPeriod, String begin, String end) {
        DateTime anyDay = DateTime.parse(anyDayInPeriod);
        Period period = new Period(anyDay);
        assertThat(period.getBegin(), is(DateTime.parse(begin)));
        assertThat(period.getEnd(), is(DateTime.parse(end)));
    }
}