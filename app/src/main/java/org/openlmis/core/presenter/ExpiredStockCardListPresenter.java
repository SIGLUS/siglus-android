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

package org.openlmis.core.presenter;

import static org.openlmis.core.manager.MovementReasonManager.EXPIRED_RETURN_TO_SUPPLIER_AND_DISCARD;
import static org.openlmis.core.utils.DateUtil.DOCUMENT_NO_DATE_TIME_FORMAT;
import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ExpiredStockCardListPresenter extends StockCardPresenter {

  public void loadExpiredStockCards() {
    view.loading();

    actualLoadExpiredStockCards();
  }

  private void actualLoadExpiredStockCards() {
    Subscription subscription = loadExpiredStockCardsObservable().subscribe(afterLoadHandler);
    subscriptions.add(subscription);
  }

  private Observable<List<StockCard>> loadExpiredStockCardsObservable() {
    return Observable.create((OnSubscribe<List<StockCard>>) subscriber -> {
      subscriber.onNext(from(stockRepository.list()).filter(stockCard -> {
        if (stockCard != null && isActiveProduct(stockCard)
            && !isArchivedProduct(stockCard)) {
          List<LotOnHand> expiredLots = filterExpiredAndNonEmptyLot(stockCard);
          if (expiredLots.size() > 0) {
            lotsOnHands.put(
                String.valueOf(stockCard.getId()),
                String.valueOf(stockCard.calculateSOHFromLots(expiredLots))
            );
            stockCard.setLotOnHandListWrapper(expiredLots);
            return true;
          }
        }
        return false;
      }).toList());
      subscriber.onCompleted();
    }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
  }

  private List<LotOnHand> filterExpiredAndNonEmptyLot(StockCard stockCard) {
    List<LotOnHand> lotOnHandListWrapper = stockCard.getLotOnHandListWrapper();
    return from(lotOnHandListWrapper)
        .filter(lotOnHand -> lotOnHand.getLot().isExpired() && lotOnHand.getQuantityOnHand() > 0)
        .toList();
  }

  public boolean isCheckedLotsExisting() {
    for (InventoryViewModel inventoryViewModel : inventoryViewModels) {
      List<LotOnHand> lots = inventoryViewModel.getStockCard().getLotOnHandListWrapper();

      for (LotOnHand lotOnHand : lots) {
        if (lotOnHand.isChecked()) {
          return true;
        }
      }
    }

    return false;
  }

  public void removeCheckedExpiredProducts(String sign) {
    view.loading();
    // TODO: 1. handle the removed lots - negative adjustment
    Observable<List<LotOnHand>> removeExpiredStocksObservable =
        removeExpiredStocksObservable(sign);
    Subscription subscribe = removeExpiredStocksObservable.subscribe(
        checkedLots -> actualLoadExpiredStockCards(),
        throwable -> view.loaded()
    );
    subscriptions.add(subscribe);
    // TODO: 2. generate the excel to specific file path - maybe the root path
  }

  private Observable<List<LotOnHand>> removeExpiredStocksObservable(String sign) {
    return Observable.create((OnSubscribe<List<LotOnHand>>) subscriber -> {
      List<LotOnHand> checkedLots = getCheckedLots();

      try {
        stockRepository.addStockMovementsAndUpdateStockCards(
            convertLotOnHandsToStockMovementItems(checkedLots, sign)
        );
      } catch (LMISException e) {
        subscriber.onError(e);
      }

      subscriber.onNext(checkedLots);
    }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
  }

  List<StockMovementItem> convertLotOnHandsToStockMovementItems(
      List<LotOnHand> checkedLots,
      String signature
  ) {
    ArrayList<StockMovementItem> stockMovementItems = new ArrayList<>();

    HashMap<StockCard, List<LotOnHand>> stockCardLotOnHandHashMap = connectStockCardAndLotOnHands(
        checkedLots);

    for (StockCard stockCard : stockCardLotOnHandHashMap.keySet()) {
      List<LotOnHand> lotOnHands = stockCardLotOnHandHashMap.get(stockCard);
      if (lotOnHands != null) {

        StockMovementItem stockMovementItem = convertLotOnHandsToStockMovementItem(
            stockCard,
            lotOnHands,
            signature
        );

        stockMovementItems.add(stockMovementItem);
      }
    }

    return stockMovementItems;
  }

  @NotNull
  private StockMovementItem convertLotOnHandsToStockMovementItem(
      StockCard stockCard,
      @NotNull List<LotOnHand> lotOnHands,
      String signature
  ) {
    // quality
    long movementQuality = 0L;
    for (LotOnHand lotOnHand : lotOnHands) {
      movementQuality += lotOnHand.getQuantityOnHand();
    }
    final long latestStockOnHand = stockCard.getStockOnHand() - movementQuality;
    // documentNumber
    Date currentDate = DateUtil.getCurrentDate();
    String documentNumber = generateDocumentNumber(currentDate);
    // reason
    final String reason = EXPIRED_RETURN_TO_SUPPLIER_AND_DISCARD;

    stockCard.setStockOnHand(latestStockOnHand);

    StockMovementItem stockMovementItem = new StockMovementItem(
        documentNumber, movementQuality, reason, MovementType.NEGATIVE_ADJUST, stockCard,
        latestStockOnHand, signature, currentDate, currentDate, currentDate
    );

    stockMovementItem.setLotMovementItemListWrapper(from(lotOnHands).transform(lotOnHand -> {
      return new LotMovementItem(
          lotOnHand.getLot(), lotOnHand.getQuantityOnHand(), stockMovementItem, reason,
          documentNumber);
    }).toList());

    return stockMovementItem;
  }

  private HashMap<StockCard, List<LotOnHand>> connectStockCardAndLotOnHands(
      List<LotOnHand> allLotOnHands) {
    HashMap<StockCard, List<LotOnHand>> stockCardLotOnHandHashMap = new HashMap<>();

    for (LotOnHand lotOnHand : allLotOnHands) {
      StockCard stockCard = lotOnHand.getStockCard();
      List<LotOnHand> lotOnHands = stockCardLotOnHandHashMap.get(stockCard);
      if (lotOnHands == null) {
        ArrayList<LotOnHand> newLotOnHands = new ArrayList<>();
        newLotOnHands.add(lotOnHand);
        stockCardLotOnHandHashMap.put(stockCard, newLotOnHands);
      } else {
        lotOnHands.add(lotOnHand);
      }
    }

    return stockCardLotOnHandHashMap;
  }

  @NonNull
  private String generateDocumentNumber(Date currentDate) {
    return UserInfoMgr.getInstance().getFacilityCode() + "_"
        + DateUtil.formatDate(currentDate, DOCUMENT_NO_DATE_TIME_FORMAT);
  }

  private List<LotOnHand> getCheckedLots() {
    List<LotOnHand> checkedLots = new ArrayList<>();

    for (InventoryViewModel inventoryViewModel : inventoryViewModels) {
      checkedLots.addAll(
          from(inventoryViewModel.getStockCard().getLotOnHandListWrapper())
              .filter(lotOnHand -> lotOnHand.isChecked())
              .toList()
      );
    }

    return checkedLots;
  }
}