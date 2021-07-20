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

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.StockMovementHistoryViewModel;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class StockMovementHistoryPresenter extends Presenter {

  @Getter
  List<StockMovementHistoryViewModel> stockMovementModelList = new ArrayList<>();

  StockMovementHistoryView view;

  public static final long MAXROWS = 30L;
  private long stockCardId;

  @Inject
  private StockMovementRepository stockMovementRepository;

  @Override
  public void attachView(BaseView v) {
    this.view = (StockMovementHistoryView) v;
  }

  public void loadStockMovementViewModels(final long startIndex) {
    Subscription subscription = Observable
        .create((OnSubscribe<List<StockMovementHistoryViewModel>>) subscriber -> {
          try {
            List<StockMovementHistoryViewModel> list = from(stockMovementRepository
                .queryStockMovementHistory(stockCardId, startIndex, MAXROWS))
                .transform(StockMovementHistoryViewModel::new).toList();
            subscriber.onNext(list);
          } catch (LMISException e) {
            subscriber.onError(e);
          }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
        .subscribe(stockMovementViewModels -> {
          if (stockMovementViewModels.isEmpty()) {
            view.refreshStockMovement(false);
          } else {
            stockMovementModelList.addAll(stockMovementModelList.size(), stockMovementViewModels);
            view.refreshStockMovement(true);
          }
          view.loaded();
        });
    subscriptions.add(subscription);
  }

  public void setStockCardId(long stockId) {
    this.stockCardId = stockId;
  }

  public interface StockMovementHistoryView extends BaseView {

    void refreshStockMovement(boolean hasNewData);
  }
}
