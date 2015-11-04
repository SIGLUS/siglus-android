package org.openlmis.core.model;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.core.utils.DateUtil;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class RnRFormTest {
    @Before
    public void setUp() {
    }

    @Test
    public void shouldGetPeriodByPeriodBegin() throws Exception {
        RnRForm rnRForm=new RnRForm();
        Date periodBegin = DateUtil.parseString("21/09/2015", DateUtil.SIMPLE_DATE_FORMAT);
        rnRForm.setPeriodBegin(periodBegin);
        RnRForm.setPeriodByPeriodBegin(rnRForm);

        assertThat(rnRForm.getPeriodBegin(), is(periodBegin));
        Date periodEnd = DateUtil.parseString("20/10/2015", DateUtil.SIMPLE_DATE_FORMAT);
        assertThat(rnRForm.getPeriodEnd(),is(periodEnd));
    }

    @Test
    public void shouldGetPeriodByPeriodBeginAtCriticalPoint() throws Exception {
        RnRForm rnRForm=new RnRForm();
        Date periodBegin = DateUtil.parseString("21/12/2015", DateUtil.SIMPLE_DATE_FORMAT);
        rnRForm.setPeriodBegin(periodBegin);
        RnRForm.setPeriodByPeriodBegin(rnRForm);

        assertThat(rnRForm.getPeriodBegin(), is(periodBegin));
        Date periodEnd = DateUtil.parseString("20/01/2016", DateUtil.SIMPLE_DATE_FORMAT);
        assertThat(rnRForm.getPeriodEnd(),is(periodEnd));
    }


}
