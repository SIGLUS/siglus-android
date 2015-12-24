package org.openlmis.core.model;

import org.joda.time.DateTime;
import org.openlmis.core.utils.DateUtil;

import java.util.Date;

public class Period {

    public static final int BEGIN_DAY = 21;
    public static final int END_DAY = 20;
    private DateTime periodBegin;
    private DateTime periodEnd;

    public Period(DateTime dateTime) {
        if (dateTime.dayOfMonth().get() >= BEGIN_DAY) {
            periodBegin = DateUtil.formatePeriodDate(dateTime.withDayOfMonth(BEGIN_DAY));
            periodEnd = DateUtil.formatePeriodDate(nextMonth(dateTime).withDayOfMonth(END_DAY));
        } else {
            periodBegin = DateUtil.formatePeriodDate(lastMonth(dateTime).withDayOfMonth(BEGIN_DAY));
            periodEnd = DateUtil.formatePeriodDate(dateTime.withDayOfMonth(END_DAY));
        }
    }

    public static Period of(Date date) {
        return new Period(new DateTime(date));
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

    private DateTime lastMonth(DateTime dateTime) {
        return dateTime.minusMonths(1);
    }

    private DateTime nextMonth(DateTime dateTime) {
        return dateTime.plusMonths(1);
    }

}
