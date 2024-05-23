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

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

import java.util.ArrayList;
import java.util.List;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.widget.SignatureDialog;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ExpiredStockCardListPresenter extends StockCardPresenter implements
    SignatureDialog.DialogDelegate {

  public void loadExpiredStockCards() {
    view.loading();

    Subscription subscription = loadExpiredStockCardsObservable().subscribe(afterLoadHandler);
    subscriptions.add(subscription);
  }

  private Observable<List<StockCard>> loadExpiredStockCardsObservable() {
    return Observable.create((Observable.OnSubscribe<List<StockCard>>) subscriber -> {
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

  @Override
  public void onSign(String sign) {
    view.loading();
    // TODO: 1. handle the removed lots - negative adjustment

    // TODO: 2. generate the excel to specific file path - maybe the root path
  }

  private List<LotOnHand> getCheckedLots() {
    List<LotOnHand> checkedLots = new ArrayList<>();

    for (int i = 0; i < inventoryViewModels.size(); i++) {
      InventoryViewModel inventoryViewModel = inventoryViewModels.get(i);
      checkedLots.addAll(
          from(inventoryViewModel.getStockCard().getLotOnHandListWrapper())
              .filter(lotOnHand -> lotOnHand.isChecked())
              .toList()
      );
    }

    return checkedLots;
  }
}