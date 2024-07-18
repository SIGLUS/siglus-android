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

import static org.openlmis.core.utils.DateUtil.today;
import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;
import org.openlmis.core.event.CmmCalculateEvent;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.StockMovementIsNullException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.Cmm;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.CmmRepository;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.service.DirtyDataManager;
import org.openlmis.core.utils.DateUtil;
import org.roboguice.shaded.goole.common.base.Optional;
import org.roboguice.shaded.goole.common.collect.Ordering;

public class StockService {

  private static final int HIGH_STOCK_CALCULATE_MONTH_QUANTITY = 3;

  @Inject
  StockRepository stockRepository;
  @Inject
  CmmRepository cmmRepository;
  @Inject
  StockMovementRepository stockMovementRepository;
  @Inject
  DirtyDataManager dirtyDataManager;

  protected Date queryFirstPeriodBegin(final StockCard stockCard) throws LMISException {
    StockMovementItem stockMovementItem = stockMovementRepository
        .queryFirstStockMovementByStockCardId(stockCard.getId());
    if (stockMovementItem == null) {
      throw new StockMovementIsNullException(stockCard);
    }
    try {
      return stockMovementItem.getMovementPeriod().getBegin().toDate();
    } catch (IllegalArgumentException e) {
      throw new LMISException(
          e,
          "convert period by movement date failed: movementDate="
              + stockMovementItem.getMovementDate()
              + ", id=" + stockMovementItem.getId()
      );
    }
  }

  public void monthlyUpdateAvgMonthlyConsumption() {
    DateTime recordLowStockAvgPeriod = SharedPreferenceMgr.getInstance()
        .getLatestUpdateLowStockAvgTime();
    Period period = Period.of(today());
    if (recordLowStockAvgPeriod.isBefore(period.getBegin())) {
      EventBus.getDefault().post(new CmmCalculateEvent(true));
      immediatelyUpdateAvgMonthlyConsumption();
      EventBus.getDefault().post(new CmmCalculateEvent(false));
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

  @SuppressWarnings("squid:S135")
  protected float calculateAverageMonthlyConsumption(StockCard stockCard) {
    Date firstPeriodBegin;
    try {
      firstPeriodBegin = queryFirstPeriodBegin(stockCard);
    } catch (StockMovementIsNullException e) {
      // no stock movement item
      reportError(e, stockCard, "stock movement item is null");
      return -1;
    } catch (LMISException e) {
      // db query issue
      reportError(e, stockCard, "queryFirstPeriodBegin db issue");
      return -1;
    }
    if (firstPeriodBegin == null) {
      // stock movement movementData is null
      reportError(null, stockCard, "firstPeriodBegin is null");
      return -1;
    }

    List<Long> issuePerMonths = new ArrayList<>();
    Period period = Period.of(today());
    int periodQuantity = DateUtil.calculateDateMonthOffset(firstPeriodBegin, period.getBegin().toDate());

    for (int i = 0; i < periodQuantity; i++) {
      period = period.previous();
      Long totalIssuesEachMonth = calculateTotalIssuesPerPeriod(stockCard, period);

      // stock out
      if (totalIssuesEachMonth == null) {
        continue;
      }

      issuePerMonths.add(totalIssuesEachMonth);
      if (issuePerMonths.size() == HIGH_STOCK_CALCULATE_MONTH_QUANTITY) {
        break;
      }
    }
    if (issuePerMonths.isEmpty()) {
      // normally
      return -1;
    }

    return getTotalIssues(issuePerMonths) * 1f / issuePerMonths.size();
  }

  private void reportError(Throwable e, StockCard stockCard, String errorMessage) {
    String productCode = "";
    if (stockCard != null) {
      Product product = stockCard.getProduct();
      productCode = product != null ? product.getCode() : "";
    }

    new LMISException(e,
        "StockService:calculateAverage"
            + "\nproductCode=" + productCode
            + "\n" + errorMessage
    ).reportToFabric();
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
      List<StockMovementItem> stockMovementItems = stockMovementRepository
          .queryStockMovementsByMovementDate(stockCard.getId(), period.getBegin().toDate(),
              period.getEnd().toDate());
      // the query above is actually wasteful, the movement items have already been queried
      // and associated to the stock card
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

  private boolean periodHasStockOut(StockCard stockCard, List<StockMovementItem> stockMovementItems,
      final Period period) {
    if (stockMovementItems.isEmpty()) {
      return isStockOutStatusInherited(stockCard, period);
    } else {
      return hasStockOutInThisPeriod(stockMovementItems);
    }
  }

  private boolean hasStockOutInThisPeriod(List<StockMovementItem> stockMovementItems) {
    HashMap<String, ArrayList<StockMovementItem>> dateToMovementMap = new HashMap<>();
    for (StockMovementItem item : stockMovementItems) {
      String formatDate = DateUtil.formatDate(item.getMovementDate(), DateUtil.DB_DATE_FORMAT);
      ArrayList<StockMovementItem> movementsInOneDay = dateToMovementMap.get(formatDate);
      if (movementsInOneDay == null) {
        movementsInOneDay = new ArrayList<>();
        dateToMovementMap.put(formatDate, movementsInOneDay);
      }
      movementsInOneDay.add(item);
    }
    return from(dateToMovementMap.values())
        .transform(movementList ->
            from(movementList)
                .toSortedList((o1, o2) -> Long.compare(o1.getCreatedTime().getTime(), o2.getCreatedTime().getTime()))
                .get(movementList.size() - 1)
                .getStockOnHand() == 0)
        .anyMatch(stockOut -> stockOut != null && stockOut);
  }

  @SuppressWarnings("squid:S1905")
  private boolean isStockOutStatusInherited(StockCard stockCard, final Period period) {
    List<StockMovementItem> orderedMovements = Ordering.from(
            (Comparator<StockMovementItem>) (lhs, rhs) -> lhs.getMovementDate().compareTo(rhs.getMovementDate()))
        .sortedCopy(stockCard.getStockMovementItemsWrapper());

    Optional<StockMovementItem> lastMovementBeforePeriod = from(orderedMovements)
        .filter(stockMovementItem -> new DateTime(stockMovementItem.getMovementDate())
            .isBefore(period.getBegin()))
        .last();

    return lastMovementBeforePeriod.isPresent()
        && lastMovementBeforePeriod.get().getStockOnHand() == 0;
  }
}
