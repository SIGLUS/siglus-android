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


import android.content.Context;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;
import org.roboguice.shaded.goole.common.base.Function;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

public class StockMovementHistoryPresenter extends Presenter {

    @Getter
    List<StockMovementViewModel> stockMovementModelList = new ArrayList<>();

    @Inject
    StockRepository stockRepository;

    StockMovementHistoryView view;


    @Inject
    Context context;
    public static final long MAXROWS = 30L;
    private long stockCardId;

    @Override
    public void attachView(BaseView v) {
        this.view = (StockMovementHistoryView) v;
    }

    public void loadStockMovementViewModels(final long startIndex) {
        Subscription subscription = Observable.create(new Observable.OnSubscribe<List<StockMovementViewModel>>() {
            @Override
            public void call(Subscriber<? super List<StockMovementViewModel>> subscriber) {
                try {
                    List<StockMovementViewModel> list = from(stockRepository.queryStockItemsHistory(stockCardId, startIndex, MAXROWS)).transform(new Function<StockMovementItem, StockMovementViewModel>() {
                        @Override
                        public StockMovementViewModel apply(StockMovementItem stockMovementItem) {
                            return new StockMovementViewModel(stockMovementItem);
                        }
                    }).toList();

                    subscriber.onNext(list);
                } catch (LMISException e) {
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(new Action1<List<StockMovementViewModel>>() {
            @Override
            public void call(List<StockMovementViewModel> stockMovementViewModels) {
                if (stockMovementViewModels.size() == 0) {
                    view.refreshStockMovement(false);
                } else {
                    stockMovementModelList.addAll(0, stockMovementViewModels);

                    view.refreshStockMovement(true);
                }
                view.loaded();
            }
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
