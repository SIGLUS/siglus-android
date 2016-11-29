package org.openlmis.core.model;

import org.joda.time.DateTime;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class PeriodTest {
    @Test
    public void shouldDetermineItsOwnBeginAndEnd() throws Exception {
        testPeriodBeginEnd("2015-02-03", "2015-01-21", "2015-02-20");//normal date
        testPeriodBeginEnd("2016-08-20", "2016-07-21", "2016-08-20");//on end
        testPeriodBeginEnd("2014-06-21", "2014-06-21", "2014-07-20");//on begin
        testPeriodBeginEnd("2014-12-25", "2014-12-21", "2015-01-20");//cross year
    }

    @Test
    public void shouldTellPreviousPeriod() throws Exception {
        //given
        Period period = new Period(DateTime.parse("2015-06-07"));

        //when
        Period prevPeriod = period.previous();

        //then
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
        Period period = new Period(DateTime.parse("2016-09-12"));

        assertThat(period.next().getBegin(), is(DateTime.parse("2016-09-21")));
        assertThat(period.next().getEnd(), is(DateTime.parse("2016-10-20")));
    }

    private void testPeriodBeginEnd(String anyDayInPeriod, String begin, String end) {
        //given
        DateTime anyDay = DateTime.parse(anyDayInPeriod);

        //when
        Period period = new Period(anyDay);

        //then
        assertThat(period.getBegin(), is(DateTime.parse(begin)));
        assertThat(period.getEnd(), is(DateTime.parse(end)));
    }
}