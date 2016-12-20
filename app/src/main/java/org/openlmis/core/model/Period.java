package org.openlmis.core.model;

import org.joda.time.DateTime;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.utils.DateUtil;

import java.util.Date;

public class Period {

    public static final int BEGIN_DAY = 21;
    public static final int END_DAY = 20;
    public static final int INVENTORY_BEGIN_DAY = 18;
    public static final int INVENTORY_END_DAY_NEXT = 26;
    public static final int DEFAULT_INVENTORY_DAY = 20;

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

    public static Period generateForTraining(Date date) {
        Period period = new Period(new DateTime(date));
        period.inventoryBegin = period.getBegin();
        period.inventoryEnd = period.getEnd();
        return period;
    }

    public Period(DateTime begin, DateTime end) {
        this.periodBegin = begin;
        this.periodEnd = end;
        this.inventoryBegin = DateUtil.cutTimeStamp(periodEnd.withDayOfMonth(INVENTORY_BEGIN_DAY));
        this.inventoryEnd = DateUtil.cutTimeStamp(periodEnd.withDayOfMonth(INVENTORY_END_DAY_NEXT));
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

    public static Boolean isWithinSubmissionWindow(DateTime date) {
        int day = date.dayOfMonth().get();
        return day >= INVENTORY_BEGIN_DAY && day < INVENTORY_END_DAY_NEXT;
    }

    private DateTime lastMonth(DateTime dateTime) {
        return dateTime.minusMonths(1);
    }

    private DateTime nextMonth(DateTime dateTime) {
        return dateTime.plusMonths(1);
    }

    public String toString() {
        return LMISApp.getContext().getString(R.string.label_period_date, DateUtil.formatDate(periodBegin.toDate()), DateUtil.formatDate(periodEnd.toDate()));
    }

    public Period next() {
        return new Period(nextMonth(periodBegin), nextMonth(periodEnd));
    }
}
