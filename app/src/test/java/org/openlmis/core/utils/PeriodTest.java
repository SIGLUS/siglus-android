package org.openlmis.core.utils;

import org.joda.time.DateTime;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class PeriodTest {
    @Test
    public void shouldDetermineItsOwnBeginAndEnd() throws Exception {
        //given
        DateTime feb03 = DateTime.parse("2015-02-03");

        //when
        Period period = new Period(feb03);

        //then
        assertThat(period.getBegin(), is(DateTime.parse("2015-01-21")));
        assertThat(period.getEnd(), is(DateTime.parse("2015-02-20")));
    }
}