package org.openlmis.core.model.service;

import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.RnRForm;
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

    public Period generateNextPeriod(String programCode, Date physicalInventoryDate) throws LMISException {
        List<RnRForm> rnRForms = rnrFormRepository.listInclude(RnRForm.Emergency.No, programCode);

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
            date.set(periodBeginDate.getYear(), periodBeginDate.getMonthOfYear(), Period.END_DAY);
            periodEndDate = DateUtil.cutTimeStamp(new DateTime(date));
        } else {
            periodEndDate = new DateTime(physicalInventoryDate);
        }
        return new Period(periodBeginDate, periodEndDate);
    }

    private Period generatePeriodBasedOnDefaultDates(Date physicalInventoryDate, String programCode) throws LMISException {
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
        DateTime initializeDateTime = new DateTime(stockMovementRepository.queryEarliestStockMovementDateByProgram(programCode));
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
        return periodBeginDate;
    }

    private DateTime defaultEndDateTo20th(DateTime periodBeginDate) {
        Calendar date = Calendar.getInstance();
        date.set(periodBeginDate.getYear(), periodBeginDate.getMonthOfYear(), Period.END_DAY);
        return DateUtil.cutTimeStamp(new DateTime(date));
    }

    public boolean hasMissedPeriod(String programCode) throws LMISException {
        List<RnRForm> rnRForms = rnrFormRepository.listInclude(RnRForm.Emergency.No, programCode);

        if (rnRForms.size() == 0 || rnRForms.get(rnRForms.size() - 1).isAuthorized()) {
            DateTime nextPeriodInScheduleEnd = generateNextPeriod(programCode, null).getEnd();

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
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)) {
            if (stockMovementRepository.queryEarliestStockMovementDateByProgram(programCode) != null) {
                return DateUtil.calculateMonthOffset(new DateTime(), nextPeriodInScheduleBegin) + 1;
            }
        }

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
