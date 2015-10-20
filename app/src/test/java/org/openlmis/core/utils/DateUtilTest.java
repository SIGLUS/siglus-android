package org.openlmis.core.utils;

import org.junit.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;


public class DateUtilTest {

    @Test
    public void shouldTruncateTimeStampInDate() throws Exception {
        Date timeStampDate = DateUtil.parseString("2015-07-20 11:33:44", DateUtil.DATE_TIME_FORMAT);
        Date expectedDate = DateUtil.parseString("20/07/2015", DateUtil.SIMPLE_DATE_FORMAT);
        Date date = DateUtil.truncateTimeStampInDate(timeStampDate);

        assertThat(date.getTime()).isEqualTo(expectedDate.getTime());
    }
}