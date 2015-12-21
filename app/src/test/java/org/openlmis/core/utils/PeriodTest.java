package org.openlmis.core.utils;

import org.joda.time.DateTime;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

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
    public void shouldTellNextPeriod() throws Exception {
        //given
        Period period = new Period(DateTime.parse("2015-12-07"));

        //when
        Period nextPeriod = period.next();

        //then
        assertThat(nextPeriod.getBegin(), is(DateTime.parse("2015-12-21")));
        assertThat(nextPeriod.getEnd(), is(DateTime.parse("2016-01-20")));
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