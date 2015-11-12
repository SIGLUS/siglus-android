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

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.BaseView;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class StockCardPresenter implements Presenter {

    @Inject
    StockRepository stockRepository;

    private StockCardListView view;

    private List<StockCard> stockCardList;

    public StockCardPresenter() {
        stockCardList = new ArrayList<>();
    }


    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }


    public List<StockCard> getStockCards() {
        return stockCardList;
    }

    public void loadStockCards() {

        view.loading();
        Observable.create(new Observable.OnSubscribe<List<StockCard>>() {
            @Override
            public void call(Subscriber<? super List<StockCard>> subscriber) {
                try {
                    subscriber.onNext(stockRepository.list());
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<List<StockCard>>() {
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
                stockCardList.clear();
                stockCardList.addAll(stockCards);
                view.refresh();
            }
        });
    }

    public void refreshStockCards() {
        for (StockCard stockCard : stockCardList) {
            stockRepository.refresh(stockCard);
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
