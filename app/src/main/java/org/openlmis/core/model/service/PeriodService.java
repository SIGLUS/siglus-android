package org.openlmis.core.model.service;

import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.DateUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PeriodService {

    @Inject
    ProgramRepository programRepository;

    @Inject
    RnrFormRepository rnrFormRepository;

    @Inject
    StockRepository stockRepository;

    public Period generateNextPeriod(String programCode, Date physicalInventoryDate) throws LMISException {
        List<RnRForm> rnRForms = rnrFormRepository.list(programCode);

        if (rnRForms.isEmpty()) {
            return generatePeriodBasedOnDefaultDates(physicalInventoryDate);
        }

        RnRForm lastRnR = rnRForms.get(rnRForms.size() - 1);
        return generatePeriodBasedOnPreviousRnr(lastRnR, physicalInventoryDate);
    }

    private Period generatePeriodBasedOnPreviousRnr(RnRForm lastRnR, Date physicalInventoryDate) {
        DateTime periodBeginDate, periodEndDate;
        periodBeginDate = new DateTime(lastRnR.getPeriodEnd());

        if (physicalInventoryDate == null) {
            Calendar date = Calendar.getInstance();
            date.set(periodBeginDate.getYear(), periodBeginDate.getMonthOfYear(), Period.END_DAY);
            periodEndDate = new DateTime(date);
        } else {
            periodEndDate = new DateTime(physicalInventoryDate);
        }
        return new Period(periodBeginDate, periodEndDate);
    }

    private Period generatePeriodBasedOnDefaultDates(Date physicalInventoryDate) {
        DateTime periodBeginDate = calculatePeriodBeginDate();
        DateTime periodEndDate;
        if (physicalInventoryDate == null) {
            periodEndDate = defaultEndDateTo20th(periodBeginDate);
        } else {
            periodEndDate = new DateTime(physicalInventoryDate);
        }
        return new Period(periodBeginDate, periodEndDate);
    }

    private DateTime calculatePeriodBeginDate() {
        DateTime initializeDateTime = new DateTime(stockRepository.queryEarliestStockMovementDate());
        int initializeDayOfMonth = initializeDateTime.getDayOfMonth();

        Calendar currentBeginDate = Calendar.getInstance();

        if(initializeDayOfMonth >= Period.INVENTORY_BEGIN_DAY && initializeDayOfMonth < Period.INVENTORY_END_DAY_NEXT) {
            currentBeginDate.set(initializeDateTime.getYear(), initializeDateTime.getMonthOfYear() - 1, initializeDayOfMonth);
        } else {
            currentBeginDate.set(initializeDateTime.getYear(), initializeDateTime.getMonthOfYear() - 1, Period.BEGIN_DAY);
        }

        DateTime periodBeginDate = DateUtil.cutTimeStamp(new DateTime(currentBeginDate));

        if (initializeDayOfMonth < Period.INVENTORY_BEGIN_DAY) {
            periodBeginDate = periodBeginDate.minusMonths(1);
        }
        return periodBeginDate;
    }

    private DateTime defaultEndDateTo20th(DateTime periodBeginDate) {
        Calendar date = Calendar.getInstance();
        date.set(periodBeginDate.getYear(), periodBeginDate.getMonthOfYear(), Period.END_DAY);
        return new DateTime(date);
    }

    public boolean hasMissedPeriod(String programCode) throws LMISException {
        DateTime nextPeriodInScheduleEnd = generateNextPeriod(programCode, null).getEnd();

        DateTime lastInventoryDateForNextPeriodInSchedule = nextPeriodInScheduleEnd
                .withDate(nextPeriodInScheduleEnd.getYear(),
                        nextPeriodInScheduleEnd.getMonthOfYear(),
                        Period.INVENTORY_END_DAY_NEXT);

        return lastInventoryDateForNextPeriodInSchedule.isBefore(LMISApp.getInstance().getCurrentTimeMillis());
    }

    public int getMissedPeriodOffsetMonth(String programCode) throws LMISException {
        DateTime nextPeriodInScheduleBegin = generateNextPeriod(programCode, null).getBegin();

        DateTime currentMonthInventoryBeginDate;
        currentMonthInventoryBeginDate = getCurrentMonthInventoryBeginDate();

        return (currentMonthInventoryBeginDate.getYear() * 12 + currentMonthInventoryBeginDate.getMonthOfYear()) - (nextPeriodInScheduleBegin.getYear() * 12 + nextPeriodInScheduleBegin.getMonthOfYear());
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
            currentMonthInventoryBeginDate = currentDate
                    .withDate(currentDate.getYear(),
                            currentDate.getMonthOfYear() - 1,
                            Period.INVENTORY_BEGIN_DAY);
        }
        return currentMonthInventoryBeginDate;
    }
}
