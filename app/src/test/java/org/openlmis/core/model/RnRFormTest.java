package org.openlmis.core.model;

import org.junit.Test;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.openlmis.core.utils.DateUtil.SIMPLE_DATE_FORMAT;
import static org.openlmis.core.utils.DateUtil.parseString;

public class RnRFormTest {

    @Test
    public void shouldGetPeriodByPeriodBegin() throws Exception {
        Date periodBegin = parseString("21/09/2015", SIMPLE_DATE_FORMAT);
        Date periodEnd = parseString("20/10/2015", SIMPLE_DATE_FORMAT);

        testPeriodBeginEnd(periodBegin, periodEnd);
    }

    @Test
    public void shouldGetPeriodByPeriodBeginAtCriticalPoint() throws Exception {
        Date periodBegin = parseString("21/12/2015", SIMPLE_DATE_FORMAT);
        Date periodEnd = parseString("20/01/2016", SIMPLE_DATE_FORMAT);

        testPeriodBeginEnd(periodBegin, periodEnd);
    }

    private void testPeriodBeginEnd(Date givenBegin, Date expectedEnd) {
        RnRForm rnRForm = new RnRForm();
        rnRForm.setPeriodBegin(givenBegin);
        rnRForm.matchPeriodEndByBegin();

        assertThat(rnRForm.getPeriodBegin(), is(givenBegin));
        assertThat(rnRForm.getPeriodEnd(), is(expectedEnd));
    }

}
