package org.openlmis.core.model.service;

import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.StockMovementIsNullException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.Cmm;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.CmmRepository;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.service.DirtyDataManager;
import org.openlmis.core.utils.DateUtil;
import org.roboguice.shaded.goole.common.base.Optional;
import org.roboguice.shaded.goole.common.collect.Ordering;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static org.openlmis.core.utils.DateUtil.today;
import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

public class StockService {

    private final int HIGH_STOCK_CALCULATE_MONTH_QUANTITY = 3;
    private final int LOW_STOCK_CALCULATE_MONTH_QUANTITY = 2;

    @Inject
    StockRepository stockRepository;
    @Inject
    CmmRepository cmmRepository;
    @Inject
    StockMovementRepository stockMovementRepository;
    @Inject
    DirtyDataManager dirtyDataManager;

    public StockService() {
    }

    protected Date queryFirstPeriodBegin(final StockCard stockCard) throws LMISException {
        StockMovementItem stockMovementItem = stockMovementRepository.queryFirstStockMovementByStockCardId(stockCard.getId());
        if (stockMovementItem == null) {
            throw new StockMovementIsNullException(stockCard);
        }
        return stockMovementItem.getMovementPeriod().getBegin().toDate();
    }

    public void monthlyUpdateAvgMonthlyConsumption() {
        DateTime recordLowStockAvgPeriod = SharedPreferenceMgr.getInstance().getLatestUpdateLowStockAvgTime();
        Period period = Period.of(today());
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
                cmmRepository.save(Cmm.initWith(stockCard, Period.of(today())));
            }
            SharedPreferenceMgr.getInstance().updateLatestLowStockAvgTime();
        } catch (LMISException e) {
            new LMISException(e, "StockService:immediatelyUpdate").reportToFabric();
        }
    }

    protected float calculateAverageMonthlyConsumption(StockCard stockCard) {
        Date firstPeriodBegin;
        try {
            firstPeriodBegin = queryFirstPeriodBegin(stockCard);
        } catch (LMISException e) {
            new LMISException(e, "StockService:calculateAverage").reportToFabric();
            return -1;
        }
        if (firstPeriodBegin == null) {
            return -1;
        }

        List<Long> issuePerMonths = new ArrayList<>();
        Period period = Period.of(today());
        int periodQuantity = DateUtil.calculateDateMonthOffset(firstPeriodBegin, period.getBegin().toDate());

        for (int i = 0; i < periodQuantity; i++) {
            period = period.previous();
            Long totalIssuesEachMonth = calculateTotalIssuesPerPeriod(stockCard, period);

            if (hasStockOutTotoalIssue(totalIssuesEachMonth)) {
                continue;
            }

            issuePerMonths.add(totalIssuesEachMonth);
            if (issuePerMonths.size() == HIGH_STOCK_CALCULATE_MONTH_QUANTITY) {
                break;
            }
        }
        if (issuePerMonths.size() < 1) {
            return -1;
        }

        return getTotalIssues(issuePerMonths) * 1f / issuePerMonths.size();
    }

    private Boolean hasStockOutTotoalIssue(Long totalIssuesEachMonth) {
        return totalIssuesEachMonth == null;
    }

    private long getTotalIssues(List<Long> issuePerMonths) {
        long total = 0;
        for (Long totalIssues : issuePerMonths) {
            total += totalIssues;
        }
        return total;
    }

    private Long calculateTotalIssuesPerPeriod(StockCard stockCard, Period period) {
        long totalIssued = 0;
        try {
            List<StockMovementItem> stockMovementItems = stockMovementRepository.queryStockMovementsByMovementDate(stockCard.getId(), period.getBegin().toDate(), period.getEnd().toDate());
            //the query above is actually wasteful, the movement items have already been queried and associated to the stock card

            if (periodHasStockOut(stockCard, stockMovementItems, period)) {
                return null;
            }

            for (StockMovementItem item : stockMovementItems) {
                if (MovementReasonManager.MovementType.ISSUE == item.getMovementType()) {
                    totalIssued += item.getMovementQuantity();
                }
            }
            return totalIssued;
        } catch (LMISException e) {
            new LMISException(e, "StockService:calculateTotalI").reportToFabric();
            return null;
        }
    }

    private boolean periodHasStockOut(StockCard stockCard, List<StockMovementItem> stockMovementItems, final Period period) {
        if (stockMovementItems.isEmpty()) {
            return isStockOutStatusInherited(stockCard, period);
        } else {
            return hasStockOutInThisPeriod(stockMovementItems);
        }
    }

    private boolean hasStockOutInThisPeriod(List<StockMovementItem> stockMovementItems) {
        return from(stockMovementItems).anyMatch(stockMovementItem -> stockMovementItem.getStockOnHand() == 0);
    }

    private boolean isStockOutStatusInherited(StockCard stockCard, final Period period) {
        List<StockMovementItem> orderedMovements = Ordering.from((Comparator<StockMovementItem>) (lhs, rhs) -> lhs.getMovementDate().compareTo(rhs.getMovementDate()))
                .sortedCopy(stockCard.getStockMovementItemsWrapper());

        Optional<StockMovementItem> lastMovementBeforePeriod = from(orderedMovements)
                .filter(stockMovementItem -> new DateTime(stockMovementItem.getMovementDate()).isBefore(period.getBegin()))
                .last();

        return lastMovementBeforePeriod.isPresent() && lastMovementBeforePeriod.get().getStockOnHand() == 0;
    }
}
