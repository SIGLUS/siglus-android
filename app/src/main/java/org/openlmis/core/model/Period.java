/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

package org.openlmis.core.model;

import java.io.Serializable;
import java.util.Date;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.utils.DateUtil;
import org.roboguice.shaded.goole.common.base.Optional;

public class Period implements Serializable {

  public static final int BEGIN_DAY = 21;
  public static final int END_DAY = 20;
  public static final int INVENTORY_TRAINING_BEGIN_DAY = 26;
  public static final int INVENTORY_TRAINING_END_DAY_NEXT = 26;
  public static final int INVENTORY_BEGIN_DAY = 18;
  public static final int INVENTORY_END_DAY_NEXT = 26;
  public static final int DEFAULT_INVENTORY_DAY = 20;

  private final DateTime periodBegin;
  private final DateTime periodEnd;
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
    period.inventoryBegin = DateUtil
        .cutTimeStamp(period.getBegin().withDayOfMonth(INVENTORY_TRAINING_BEGIN_DAY));
    period.inventoryEnd = DateUtil
        .cutTimeStamp(period.getEnd().withDayOfMonth(INVENTORY_TRAINING_END_DAY_NEXT));
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

  public static boolean isWithinSubmissionWindow(DateTime date) {
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
    return LMISApp.getContext()
        .getString(R.string.label_period_date, DateUtil.formatDate(periodBegin.toDate()),
            DateUtil.formatDate(periodEnd.toDate()));
  }

  public Period next() {
    return new Period(nextMonth(periodBegin), nextMonth(periodEnd));
  }

  public Optional<Period> generateNextAvailablePeriod() {
    Period nextPeriod = this.next();
    if (nextPeriod.isOpenToRequisitions()) {
      return Optional.of(nextPeriod);
    }
    return Optional.absent();
  }

  public boolean isOpenToRequisitions() {
    return isOpenForCurrentDate() || isBeforeCurrent();
  }

  private boolean isOpenForCurrentDate() {
    DateTime today = new DateTime(LMISApp.getInstance().getCurrentTimeMillis());
    Interval requisitionInterval = new Interval(getOpeningRequisitionDate(),
        getClosingRequisitionDate());
    return requisitionInterval.contains(today);
  }

  private boolean isBeforeCurrent() {
    DateTime today = new DateTime(LMISApp.getInstance().getCurrentTimeMillis());
    Period currentPeriod = new Period(today);
    return periodEnd.isBefore(currentPeriod.getBegin());
  }

  public DateTime getOpeningRequisitionDate() {
    return periodEnd.minusDays(END_DAY - INVENTORY_BEGIN_DAY);
  }

  private DateTime getClosingRequisitionDate() {
    return periodEnd.plusDays(INVENTORY_END_DAY_NEXT - END_DAY);
  }

  @Override
  public boolean equals(Object object) {
    if (object == null || getClass() != object.getClass()) {
      return false;
    }
    Period period = (Period) object;
    return this.toString().equals(period.toString());
  }

  @Override
  public int hashCode() {
    int result = periodBegin != null ? periodBegin.hashCode() : 0;
    result = 31 * result + (periodEnd != null ? periodEnd.hashCode() : 0);
    result = 31 * result + (inventoryBegin != null ? inventoryBegin.hashCode() : 0);
    result = 31 * result + (inventoryEnd != null ? inventoryEnd.hashCode() : 0);
    return result;
  }
}
