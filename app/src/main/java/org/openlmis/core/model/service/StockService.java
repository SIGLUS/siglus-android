package org.openlmis.core.model.service;

import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.StockMovementIsNullException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.DateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StockService {

    private final int LOW_STOCK_CALCULATE_MONTH_QUANTITY = 3;

    @Inject
    StockRepository stockRepository;

    protected Date queryFirstPeriodBegin(final StockCard stockCard) throws LMISException {
        StockMovementItem stockMovementItem = stockRepository.queryFirstStockMovementItem(stockCard);
        if (stockMovementItem == null) {
            throw new StockMovementIsNullException(stockCard);
        }
        return stockMovementItem.getMovementPeriod().getBegin().toDate();
    }

    public void monthlyUpdateAvgMonthlyConsumption() {
        DateTime recordLowStockAvgPeriod = SharedPreferenceMgr.getInstance().getLatestUpdateLowStockAvgTime();
        Period period = Period.of(DateUtil.today());
        if (recordLowStockAvgPeriod.isBefore(period.getBegin())) {
           immediatelyUpdateAvgMonthlyConsumption();
        }
    }

    public void immediatelyUpdateAvgMonthlyConsumption() {
        try {
            List<StockCard> stockCards = stockRepository.list();
            for (StockCard stockCard : stockCards) {
                stockCard.setAvgMonthlyConsumption(calculateAverageMonthlyConsumption(stockCard));
                stockRepository.createOrUpdate(stockCard);
            }
            SharedPreferenceMgr.getInstance().updateLatestLowStockAvgTime();
        } catch (LMISException e) {
            e.reportToFabric();
        }
    }

    protected float calculateAverageMonthlyConsumption(StockCard stockCard) {
        Date firstPeriodBegin;
        try {
            firstPeriodBegin = queryFirstPeriodBegin(stockCard);
        } catch (LMISException e) {
            e.reportToFabric();
            return 0;
        }

        List<Long> issuePerMonths = new ArrayList<>();
        Period period = Period.of(DateUtil.today());
        int periodQuantity = DateUtil.calculateDateMonthOffset(firstPeriodBegin, period.getBegin().toDate());

        if (periodQuantity < LOW_STOCK_CALCULATE_MONTH_QUANTITY) {
            return 0;
        }

        for (int i = 0; i < periodQuantity; i++) {
            period = period.previous();
            Long totalIssuesEachMonth = calculateTotalIssuesPerMonth(stockCard, period);

            if (totalIssuesEachMonth == null) {
                continue;
            }

            issuePerMonths.add(totalIssuesEachMonth);

            if (issuePerMonths.size() == LOW_STOCK_CALCULATE_MONTH_QUANTITY) {
                break;
            }
        }

        if (issuePerMonths.size() < LOW_STOCK_CALCULATE_MONTH_QUANTITY) {
            return 0;
        }
        return getTotalIssues(issuePerMonths) * 1f / LOW_STOCK_CALCULATE_MONTH_QUANTITY;
    }

    private long getTotalIssues(List<Long> issuePerMonths) {
        long total = 0;
        for (Long totalIssues : issuePerMonths) {
            total += totalIssues;
        }
        return total;
    }

    private Long calculateTotalIssuesPerMonth(StockCard stockCard, Period period) {
        long totalIssued = 0;
        List<StockMovementItem> stockMovementItems;
        try {
            stockMovementItems = stockRepository.queryStockItems(stockCard, period.getBegin().toDate(), period.getEnd().toDate());
        } catch (LMISException e) {
            e.reportToFabric();
            return null;
        }
        if (stockMovementItems.isEmpty()) {
            return 0L;
        }
        for (StockMovementItem item : stockMovementItems) {
            if (item.getStockOnHand() == 0) {
                return null;
            }
            if (StockMovementItem.MovementType.ISSUE == item.getMovementType()) {
                totalIssued += item.getMovementQuantity();
            }
        }
        return totalIssued;
    }

}
