package org.openlmis.core.model;

import org.joda.time.DateTime;
import org.openlmis.core.utils.DateUtil;

import java.util.Date;

public class Period {

    public static final int BEGIN_DAY = 21;
    public static final int END_DAY = 20;
    public static final int INVENTORY_BEGIN_DAY = 18;
    public static final int INVENTORY_END_DAY = 26;

    private DateTime periodBegin;
    private DateTime periodEnd;
    private DateTime inventoryBegin;
    private DateTime inventoryEnd;

    public Period(DateTime dateTime) {
        if (dateTime.dayOfMonth().get() >= BEGIN_DAY) {
            periodBegin = DateUtil.cutTimeStamp(dateTime.withDayOfMonth(BEGIN_DAY));
            periodEnd = DateUtil.cutTimeStamp(nextMonth(dateTime).withDayOfMonth(END_DAY));
        } else {
            periodBegin = DateUtil.cutTimeStamp(lastMonth(dateTime).withDayOfMonth(BEGIN_DAY));
            periodEnd = DateUtil.cutTimeStamp(dateTime.withDayOfMonth(END_DAY));
        }
    }

    public static Period of(Date date) {
        return new Period(new DateTime(date));
    }

    public Period(DateTime begin, DateTime end) {
        this.periodBegin = begin;
        this.periodEnd = end;
        this.inventoryBegin = DateUtil.cutTimeStamp(periodEnd.withDayOfMonth(INVENTORY_BEGIN_DAY));
        this.inventoryEnd = DateUtil.cutTimeStamp(periodEnd.withDayOfMonth(INVENTORY_END_DAY));
    }

    public DateTime getBegin() {
        return periodBegin;
    }

    public DateTime getInventoryBegin() {
        return inventoryBegin;
    }

    public DateTime getInventoryEnd() {
        return inventoryEnd;
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
