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


import android.support.annotation.NonNull;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.StockCardViewModel;
import org.roboguice.shaded.goole.common.base.Function;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

public class StockCardPresenter implements Presenter {

    @Inject
    StockRepository stockRepository;

    private StockCardListView view;

    protected List<StockCardViewModel> stockCardViewModels;

    public StockCardPresenter() {
        stockCardViewModels = new ArrayList<>();
    }


    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }


    public List<StockCardViewModel> getStockCardViewModels() {
        return stockCardViewModels;
    }

    public void loadStockCards() {
        view.loading();
        getLoadStockCardsObserver().subscribe(getLoadStockCardsSubscriber());
    }

    @NonNull
    public Observer<List<StockCard>> getLoadStockCardsSubscriber() {
        return new Observer<List<StockCard>>() {
            @Override
            public void onCompleted() {
                view.loaded();
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                ToastUtil.show(e.getMessage());
                view.loaded();
            }

            @Override
            public void onNext(List<StockCard> stockCards) {
                List<StockCardViewModel> stockCardViewModelList = from(stockCards).transform(new Function<StockCard, StockCardViewModel>() {
                    @Override
                    public StockCardViewModel apply(StockCard stockCard) {
                        return new StockCardViewModel(stockCard);
                    }
                }).toList();
                stockCardViewModels.clear();
                stockCardViewModels.addAll(stockCardViewModelList);
                view.refresh();
            }
        };
    }

    public Observable<List<StockCard>> getLoadStockCardsObserver() {
        return Observable.create(new Observable.OnSubscribe<List<StockCard>>() {
            @Override
            public void call(Subscriber<? super List<StockCard>> subscriber) {
                try {
                    subscriber.onNext(stockRepository.list());
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public void refreshStockCardViewModelsSOH() {
        for (StockCardViewModel stockCardViewModel : stockCardViewModels) {
            final StockCard stockCard = stockCardViewModel.getStockCard();
            stockRepository.refresh(stockCard);
            stockCardViewModel.setStockOnHand(stockCard.getStockOnHand());
        }
    }

    @Override
    public void attachView(BaseView v) {
        view = (StockCardListView) v;
    }

    public interface StockCardListView extends BaseView {
        void refresh();
    }
}
