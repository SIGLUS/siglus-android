package org.openlmis.core.model.service;

import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.DateUtil;

import java.util.Calendar;
import java.util.Date;

public class PeriodService {

    @Inject
    ProgramRepository programRepository;

    @Inject
    RnrFormRepository rnrFormRepository;

    @Inject
    StockRepository stockRepository;

    public Period generatePeriod(String programCode, Date physicalInventoryDate) throws LMISException {
        Program program = programRepository.queryByCode(programCode);
        RnRForm lastRnR = rnrFormRepository.queryLastAuthorizedRnr(program);

        if (lastRnR == null) {
            return generatePeriodBasedOnDefaultDates(physicalInventoryDate);
        }

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
        DateTime periodBeginDate = defaultBeginDateTo21st();
        DateTime periodEndDate;
        if (physicalInventoryDate == null) {
            periodEndDate = defaultEndDateTo20th(periodBeginDate);
        } else {
            periodEndDate = new DateTime(physicalInventoryDate);
        }
        return new Period(periodBeginDate, periodEndDate);
    }

    private DateTime defaultBeginDateTo21st() {
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
}
