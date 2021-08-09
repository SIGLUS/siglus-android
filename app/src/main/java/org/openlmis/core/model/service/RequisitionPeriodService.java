package org.openlmis.core.model.service;

import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

    public Period generateNextPeriod(String programCode, Date physicalInventoryDate, ReportTypeForm typeForm) throws LMISException {
        List<RnRForm> rnRForms = rnrFormRepository.listInclude(RnRForm.Emergency.No, programCode, typeForm);
        return generateNextPeriod(rnRForms, programCode, physicalInventoryDate);
    }

    public Period generateNextPeriod(List<RnRForm> rnRForms, String programCode, Date physicalInventoryDate) throws LMISException {
        if (rnRForms.isEmpty()) {
            return generatePeriodBasedOnDefaultDates(physicalInventoryDate, programCode);
        }
        RnRForm lastRnR = rnRForms.get(rnRForms.size() - 1);
        return generatePeriodBasedOnPreviousRnr(lastRnR, physicalInventoryDate);
    }

    private Period generatePeriodBasedOnPreviousRnr(RnRForm lastRnR, Date physicalInventoryDate) throws LMISException {
        DateTime periodBeginDate, periodEndDate;
        periodBeginDate = new DateTime(lastRnR.getPeriodEnd());

        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)) {
            return Period.generateForTraining(periodBeginDate.plusDays(1).toDate());
        }

        if (physicalInventoryDate == null) {
            Calendar date = Calendar.getInstance();
            date.set(periodBeginDate.getYear(), periodBeginDate.getMonthOfYear(), Period.END_DAY,
                    periodBeginDate.getHourOfDay(),periodBeginDate.getMinuteOfHour());// The Nex Month
            periodEndDate = DateUtil.cutTimeStamp(new DateTime(date));
        } else {
            periodEndDate = new DateTime(physicalInventoryDate);
        }
        return new Period(periodBeginDate, periodEndDate);
    }

    private Period generatePeriodBasedOnDefaultDates(Date physicalInventoryDate, String programCode) throws LMISException {
        DateTime periodBeginDate = calculatePeriodBeginDate(programCode);
        DateTime periodEndDate;
        if (physicalInventoryDate == null) {
            periodEndDate = defaultEndDateTo20th(periodBeginDate);
        } else {
            periodEndDate = new DateTime(physicalInventoryDate);
        }
        return new Period(periodBeginDate, periodEndDate);
    }

    private DateTime calculatePeriodBeginDate(String programCode) throws LMISException{
        DateTime initializeDateTime = null;
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        ReportTypeForm reportTypeForm = reportTypeFormRepository.queryByCode(programCode);
        if (reportTypeForm.lastReportEndTime != null) {
            DateTime lastReportEndTime = dateTimeFormatter.parseDateTime(reportTypeForm.lastReportEndTime);
            if (Months.monthsBetween(lastReportEndTime, new DateTime()).getMonths() > 12){
                initializeDateTime = new DateTime().plusMonths(-sharedPreferenceMgr.getMonthOffsetThatDefinedOldData()).toDateTime();
            }
        }
        initializeDateTime = initializeDateTime == null? new DateTime(stockMovementRepository.queryEarliestStockMovementDateByProgram(programCode)):initializeDateTime;

        int initializeDayOfMonth = initializeDateTime.getDayOfMonth();

        Calendar currentBeginDate = Calendar.getInstance();

        if (initializeDayOfMonth >= Period.INVENTORY_BEGIN_DAY && initializeDayOfMonth < Period.INVENTORY_END_DAY_NEXT) {
            currentBeginDate.set(initializeDateTime.getYear(), initializeDateTime.getMonthOfYear() - 1, initializeDayOfMonth);
        } else {
            currentBeginDate.set(initializeDateTime.getYear(), initializeDateTime.getMonthOfYear() - 1, Period.BEGIN_DAY);
        }

        DateTime periodBeginDate = DateUtil.cutTimeStamp(new DateTime(currentBeginDate));

        if (initializeDayOfMonth < Period.INVENTORY_BEGIN_DAY) {
            periodBeginDate = periodBeginDate.minusMonths(1);
        }

        DateTime reportStartTime = reportTypePeriod(programCode);
        if (reportStartTime.isAfter(initializeDateTime)) {
            periodBeginDate = reportStartTime;
        }
        return periodBeginDate;
    }

    private DateTime reportTypePeriod(String programCode) throws LMISException {
        DateTime startTime = new DateTime(reportTypeFormRepository.getReportType(programCode).getStartTime());
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

    public boolean hasMissedPeriod(String programCode, ReportTypeForm reportTypeForm) throws LMISException {
        List<RnRForm> rnRForms = rnrFormRepository.listInclude(RnRForm.Emergency.No, programCode, reportTypeForm);

        if (rnRForms.size() == 0 || rnRForms.get(rnRForms.size() - 1).isAuthorized()) {
            DateTime nextPeriodInScheduleEnd = generateNextPeriod(rnRForms, programCode, null).getEnd();

            DateTime lastInventoryDateForNextPeriodInSchedule = nextPeriodInScheduleEnd
                    .withDate(nextPeriodInScheduleEnd.getYear(),
                            nextPeriodInScheduleEnd.getMonthOfYear(),
                            Period.INVENTORY_END_DAY_NEXT);
            return lastInventoryDateForNextPeriodInSchedule.isBefore(LMISApp.getInstance().getCurrentTimeMillis());
        }

        Date lastRnrPeriodEndDate = rnRForms.get(rnRForms.size() - 1).getPeriodEnd();
        return new DateTime(lastRnrPeriodEndDate).isBefore(LMISApp.getInstance().getCurrentTimeMillis());
    }

    public int getMissedPeriodOffsetMonth(String programCode) throws LMISException {
        DateTime nextPeriodInScheduleBegin = generateNextPeriod(programCode, null).getBegin();

        DateTime currentMonthInventoryBeginDate;
        currentMonthInventoryBeginDate = getCurrentMonthInventoryBeginDate();
        return DateUtil.calculateDateMonthOffset(nextPeriodInScheduleBegin.toDate(), currentMonthInventoryBeginDate.toDate());
    }

    public int getMissedPeriodOffsetMonth(String programCode, ReportTypeForm typeForm) throws LMISException {
        DateTime nextPeriodInScheduleBegin = generateNextPeriod(programCode, null, typeForm).getBegin();
        DateTime currentMonthInventoryBeginDate;
        currentMonthInventoryBeginDate = getCurrentMonthInventoryBeginDate();
        return DateUtil.calculateDateMonthOffset(nextPeriodInScheduleBegin.toDate(), currentMonthInventoryBeginDate.toDate());
    }

    public int getIncompletePeriodOffsetMonth(String programCode) throws LMISException {
        List<RnRForm> rnRForms = rnrFormRepository.listInclude(RnRForm.Emergency.No, programCode);
        if (rnRForms.size() == 0 || rnRForms.get(rnRForms.size() - 1).isAuthorized()) {
            return getMissedPeriodOffsetMonth(programCode);
        } else return getMissedPeriodOffsetMonth(programCode) + 1;
    }

    public DateTime getCurrentMonthInventoryBeginDate() {
        DateTime currentDate = new DateTime(LMISApp.getInstance().getCurrentTimeMillis());
        DateTime currentMonthInventoryBeginDate;
        if (currentDate.getDayOfMonth() >= Period.INVENTORY_BEGIN_DAY) {
            currentMonthInventoryBeginDate = currentDate
                    .withDate(currentDate.getYear(),
                            currentDate.getMonthOfYear(),
                            Period.INVENTORY_BEGIN_DAY);
        } else {
            if (currentDate.getMonthOfYear() == 1) {
                currentMonthInventoryBeginDate = currentDate
                        .withDate(currentDate.getYear() - 1,
                                12,
                                Period.INVENTORY_BEGIN_DAY);
            } else {
                currentMonthInventoryBeginDate = currentDate
                        .withDate(currentDate.getYear(),
                                currentDate.getMonthOfYear() - 1,
                                Period.INVENTORY_BEGIN_DAY);
            }
        }
        return currentMonthInventoryBeginDate;
    }
}
