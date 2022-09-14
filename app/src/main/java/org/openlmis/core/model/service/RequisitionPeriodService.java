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

package org.openlmis.core.model.service;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.Months;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.repository.ReportTypeFormRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.DateUtil;

public class RequisitionPeriodService {

  @Inject
  RnrFormRepository rnrFormRepository;

  @Inject
  StockRepository stockRepository;

  @Inject
  private StockMovementRepository stockMovementRepository;

  @Inject
  private ReportTypeFormRepository reportTypeFormRepository;

  @Inject
  private SharedPreferenceMgr sharedPreferenceMgr;

  public Period generateNextPeriod(String programCode, Date physicalInventoryDate) throws LMISException {
    return generateNextPeriod(programCode, physicalInventoryDate, reportTypeFormRepository.getReportType(programCode));
  }

  public Period generateNextPeriod(String programCode, Date physicalInventoryDate,
      ReportTypeForm typeForm) throws LMISException {
    List<RnRForm> rnRForms = rnrFormRepository.listInclude(RnRForm.Emergency.NO, programCode, typeForm);
    return generateNextPeriod(rnRForms, programCode, physicalInventoryDate);
  }

  public Period generateNextPeriod(List<RnRForm> rnRForms, String programCode,
      Date physicalInventoryDate) throws LMISException {
    if (rnRForms.isEmpty()) {
      return generatePeriodBasedOnDefaultDates(physicalInventoryDate, programCode);
    }
    RnRForm lastRnR = rnRForms.get(rnRForms.size() - 1);
    return generatePeriodBasedOnPreviousRnr(lastRnR, physicalInventoryDate);
  }

  private Period generatePeriodBasedOnPreviousRnr(RnRForm lastRnR, Date physicalInventoryDate) {
    DateTime periodBeginDate;
    DateTime periodEndDate;
    periodBeginDate = new DateTime(lastRnR.getPeriodEnd());

    if (physicalInventoryDate == null) {
      Calendar date = Calendar.getInstance();
      date.set(periodBeginDate.getYear(), periodBeginDate.getMonthOfYear(), Period.END_DAY,
          periodBeginDate.getHourOfDay(), periodBeginDate.getMinuteOfHour()); // The Nex Month
      periodEndDate = DateUtil.cutTimeStamp(new DateTime(date));
    } else {
      periodEndDate = new DateTime(physicalInventoryDate);
    }
    return new Period(periodBeginDate, periodEndDate);
  }

  private Period generatePeriodBasedOnDefaultDates(Date physicalInventoryDate, String programCode)
      throws LMISException {
    if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)) {
      return Period.generateForTraining(new Date(LMISApp.getInstance().getCurrentTimeMillis()));
    }
    DateTime periodBeginDate = calculatePeriodBeginDate(programCode);
    DateTime periodEndDate;
    if (physicalInventoryDate == null) {
      periodEndDate = defaultEndDateTo20th(periodBeginDate);
    } else {
      periodEndDate = new DateTime(physicalInventoryDate);
    }
    return new Period(periodBeginDate, periodEndDate);
  }

  private DateTime calculatePeriodBeginDate(String programCode) throws LMISException {
    DateTime initializeDateTime = null;
    ReportTypeForm reportTypeForm = reportTypeFormRepository.queryByCode(programCode);
    DateTime lastReportEndTime = reportTypeForm.getLastReportEndTimeForDateTime();
    if (lastReportEndTime != null && Months.monthsBetween(lastReportEndTime, new DateTime()).getMonths() > 12) {
      initializeDateTime = new DateTime().minusMonths(sharedPreferenceMgr.getMonthOffsetThatDefinedOldData())
          .toDateTime();
    }
    initializeDateTime = initializeDateTime == null ? new DateTime(stockMovementRepository
        .queryEarliestStockMovementDateByProgram(programCode)) : initializeDateTime;

    DateTime reportStartTime = reportTypePeriod(programCode);

    DateTime resultDate = initializeDateTime.isAfter(reportStartTime) ? initializeDateTime : reportStartTime;

    int initializeDayOfMonth = resultDate.getDayOfMonth();

    Calendar currentBeginDate = Calendar.getInstance();

    if (initializeDayOfMonth >= Period.BEGIN_DAY) {
      currentBeginDate.set(initializeDateTime.getYear(), initializeDateTime.getMonthOfYear(), Period.BEGIN_DAY);
    } else {
      currentBeginDate.set(initializeDateTime.getYear(), initializeDateTime.getMonthOfYear() - 1,
          Period.BEGIN_DAY);
    }
    return DateUtil.cutTimeStamp(new DateTime(currentBeginDate));
  }

  private DateTime reportTypePeriod(String programCode) throws LMISException {
    DateTime startTime = new DateTime(
        reportTypeFormRepository.getReportType(programCode).getStartTime());
    Calendar currentBeginDate = Calendar.getInstance();
    int initializeDayOfMonth = startTime.getDayOfMonth();
    if (initializeDayOfMonth < Period.BEGIN_DAY) {
      currentBeginDate.set(startTime.getYear(), startTime.getMonthOfYear() - 1, Period.BEGIN_DAY);
    } else {
      currentBeginDate.set(startTime.getYear(), startTime.getMonthOfYear(), Period.BEGIN_DAY);
    }
    return DateUtil.cutTimeStamp(new DateTime(currentBeginDate));
  }

  private DateTime defaultEndDateTo20th(DateTime periodBeginDate) {
    Calendar date = Calendar.getInstance();
    date.set(periodBeginDate.getYear(), periodBeginDate.getMonthOfYear(), Period.END_DAY);
    return DateUtil.cutTimeStamp(new DateTime(date));
  }

  public boolean hasMissedPeriod(String programCode) throws LMISException {
    ReportTypeForm reportTypeForm = reportTypeFormRepository.getReportType(programCode);
    return hasMissedPeriod(programCode, reportTypeForm);
  }

  public boolean hasMissedPeriod(String programCode, ReportTypeForm reportTypeForm)
      throws LMISException {
    List<RnRForm> rnRForms = rnrFormRepository
        .listInclude(RnRForm.Emergency.NO, programCode, reportTypeForm);

    if (rnRForms.size() == 0 || rnRForms.get(rnRForms.size() - 1).isAuthorized()) {
      DateTime nextPeriodInScheduleEnd = generateNextPeriod(rnRForms, programCode, null).getEnd();

      DateTime lastInventoryDateForNextPeriodInSchedule = nextPeriodInScheduleEnd
          .withDate(nextPeriodInScheduleEnd.getYear(),
              nextPeriodInScheduleEnd.getMonthOfYear(),
              Period.INVENTORY_END_DAY_NEXT);
      return lastInventoryDateForNextPeriodInSchedule
          .isBefore(LMISApp.getInstance().getCurrentTimeMillis());
    }

    Date lastRnrPeriodEndDate = rnRForms.get(rnRForms.size() - 1).getPeriodEnd();
    return new DateTime(lastRnrPeriodEndDate)
        .isBefore(LMISApp.getInstance().getCurrentTimeMillis());
  }

  public int getMissedPeriodOffsetMonth(String programCode) throws LMISException {
    DateTime nextPeriodInScheduleBegin = generateNextPeriod(programCode, null).getBegin();

    DateTime currentMonthInventoryBeginDate;
    currentMonthInventoryBeginDate = getCurrentPeriodBeginDate();
    return DateUtil.calculateDateMonthOffset(nextPeriodInScheduleBegin.toDate(),
        currentMonthInventoryBeginDate.toDate());
  }

  public int getMissedPeriodOffsetMonth(String programCode, ReportTypeForm typeForm)
      throws LMISException {
    DateTime nextPeriodInScheduleBegin = generateNextPeriod(programCode, null, typeForm).getBegin();
    DateTime currentMonthInventoryBeginDate;
    currentMonthInventoryBeginDate = getCurrentPeriodBeginDate();
    return DateUtil.calculateDateMonthOffset(nextPeriodInScheduleBegin.toDate(),
        currentMonthInventoryBeginDate.toDate());
  }

  public List<ReportTypeForm> getIncompleteReports() throws LMISException {
    List<ReportTypeForm> reportTypeForms = reportTypeFormRepository.listAll();
    List<ReportTypeForm> incompleteReports = new ArrayList<>();
    for (ReportTypeForm reportTypeForm : reportTypeForms) {
      List<RnRForm> rnRForms = rnrFormRepository.listInclude(RnRForm.Emergency.NO, reportTypeForm.getCode());
      int monthOffset;
      if (rnRForms.isEmpty() || rnRForms.get(rnRForms.size() - 1).isAuthorized()) {
        monthOffset = getMissedPeriodOffsetMonth(reportTypeForm.getCode());
      } else {
        monthOffset = getMissedPeriodOffsetMonth(reportTypeForm.getCode()) + 1;
      }
      if (monthOffset > 0) {
        incompleteReports.add(reportTypeForm);
      }
    }
    return incompleteReports;
  }

  public DateTime getCurrentPeriodBeginDate() {
    DateTime currentDate = new DateTime(LMISApp.getInstance().getCurrentTimeMillis());
    DateTime currentMonthBeginDate;
    if (currentDate.getDayOfMonth() >= Period.BEGIN_DAY) {
      currentMonthBeginDate = currentDate
          .withDate(currentDate.getYear(),
              currentDate.getMonthOfYear(),
              Period.BEGIN_DAY);
    } else {
      if (currentDate.getMonthOfYear() == 1) {
        currentMonthBeginDate = currentDate
            .withDate(currentDate.getYear() - 1,
                12,
                Period.BEGIN_DAY);
      } else {
        currentMonthBeginDate = currentDate
            .withDate(currentDate.getYear(),
                currentDate.getMonthOfYear() - 1,
                Period.BEGIN_DAY);
      }
    }
    return currentMonthBeginDate;
  }
}
