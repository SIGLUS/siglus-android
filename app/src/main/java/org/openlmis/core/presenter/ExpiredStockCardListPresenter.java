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

import java.util.List;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.StockCard;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ExpiredStockCardListPresenter extends StockCardPresenter {

  public void loadExpiredStockCards() {
    view.loading();

    lotsOnHands.putAll(stockRepository.lotOnHands());
    Subscription subscription = loadExpiredStockCardsObservable().subscribe(afterLoadHandler);
    subscriptions.add(subscription);
  }

  private Observable<List<StockCard>> loadExpiredStockCardsObservable() {
    return Observable.create((Observable.OnSubscribe<List<StockCard>>) subscriber -> {
      subscriber.onNext(from(stockRepository.list()).filter(stockCard -> {
        if (stockCard != null && isActiveProduct(stockCard)
            && !isArchivedProduct(stockCard)) {
          List<LotOnHand> expiredLot = filterExpiredAndNonEmptyLot(stockCard);
          if (expiredLot.size() > 0) {
            stockCard.setLotOnHandListWrapper(expiredLot);
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
}