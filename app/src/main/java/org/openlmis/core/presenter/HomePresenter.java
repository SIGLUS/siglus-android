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

import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import org.openlmis.core.enumeration.StockOnHandStatus;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.BaseView;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class HomePresenter extends Presenter {

  HomeView view;

  @Inject
  StockRepository stockRepository;
  @Inject
  RnrFormRepository rnrFormRepository;

  @Override
  public void attachView(BaseView v) {
    this.view = (HomeView) v;
  }

  Subscription previousSubscribe;

  Observer<Map<String, Integer>> queryStockCountObserver = new Observer<Map<String, Integer>>() {
    @Override
    public void onCompleted() {
      // do nothing
    }

    @Override
    public void onError(Throwable e) {
      // do nothing
    }

    @Override
    public void onNext(Map<String, Integer> stockOnHandCountMap) {
      int regularAmount = 0;
      int outAmount = 0;
      int lowAmount = 0;
      int overAmount = 0;
      if (stockOnHandCountMap.containsKey(StockOnHandStatus.REGULAR_STOCK.name())) {
        regularAmount = stockOnHandCountMap.get(StockOnHandStatus.REGULAR_STOCK.name());
      }
      if (stockOnHandCountMap.containsKey(StockOnHandStatus.STOCK_OUT.name())) {
        outAmount = stockOnHandCountMap.get(StockOnHandStatus.STOCK_OUT.name());
      }
      if (stockOnHandCountMap.containsKey(StockOnHandStatus.LOW_STOCK.name())) {
        lowAmount = stockOnHandCountMap.get(StockOnHandStatus.LOW_STOCK.name());
      }
      if (stockOnHandCountMap.containsKey(StockOnHandStatus.OVER_STOCK.name())) {
        overAmount = stockOnHandCountMap.get(StockOnHandStatus.OVER_STOCK.name());
      }
      view.updateDashboard(regularAmount, outAmount, lowAmount, overAmount);
    }
  };

  @SuppressWarnings("squid:S1905")
  public void getDashboardData() {
    if (previousSubscribe != null && !previousSubscribe.isUnsubscribed()) {
      previousSubscribe.unsubscribe();
      subscriptions.remove(previousSubscribe);
    }
    previousSubscribe = Observable.create((Observable.OnSubscribe<Map<String, Integer>>) subscriber -> {
      subscriber.onNext(stockRepository.queryStockCountGroupByStockOnHandStatus());
      subscriber.onCompleted();
    }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.immediate()).subscribe(queryStockCountObserver);
    subscriptions.add(previousSubscribe);
  }

  public boolean hasRejectedRequisition() {
    List<RnRForm> rejectedRequisitions = rnrFormRepository.queryAllRejectedRequisitions();
    return !rejectedRequisitions.isEmpty();
  }

  public interface HomeView extends BaseView {
    void updateDashboard(int regularAmount, int outAmount, int lowAmount, int overAmount);
  }
}
