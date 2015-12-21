package org.openlmis.core.utils;

import org.joda.time.DateTime;

public class Period {

    public static final int BEGIN_DAY = 21;
    public static final int END_DAY = 20;
    private DateTime periodBegin;
    private DateTime periodEnd;

    public Period(DateTime dateTime) {
        if (dateTime.dayOfMonth().get() >= BEGIN_DAY) {
            periodBegin = dateTime.withDayOfMonth(BEGIN_DAY);
            periodEnd = dateTime.plusMonths(1).withDayOfMonth(END_DAY);
        } else {
            periodBegin = dateTime.minusMonths(1).withDayOfMonth(BEGIN_DAY);
            periodEnd = dateTime.withDayOfMonth(END_DAY);
        }
    }

    public DateTime getBegin() {
        return periodBegin;
    }

    public DateTime getEnd() {
        return periodEnd;
    }
}
