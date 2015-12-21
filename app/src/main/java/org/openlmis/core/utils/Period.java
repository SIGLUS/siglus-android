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
            periodEnd = nextMonth(dateTime).withDayOfMonth(END_DAY);
        } else {
            periodBegin = lastMonth(dateTime).withDayOfMonth(BEGIN_DAY);
            periodEnd = dateTime.withDayOfMonth(END_DAY);
        }
    }

    private Period(DateTime begin, DateTime end) {
        this.periodBegin = begin;
        this.periodEnd = end;
    }

    public DateTime getBegin() {
        return periodBegin;
    }

    public DateTime getEnd() {
        return periodEnd;
    }

    public Period previous() {
        return new Period(lastMonth(periodBegin), lastMonth(periodEnd));
    }

    public Period next() {
        return new Period(nextMonth(periodBegin), nextMonth(periodEnd));
    }

    private DateTime lastMonth(DateTime dateTime) {
        return dateTime.minusMonths(1);
    }

    private DateTime nextMonth(DateTime dateTime) {
        return dateTime.plusMonths(1);
    }
}
