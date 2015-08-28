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

package org.openlmis.core.component.stocklist;


import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.repository.RnrFormItemRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.View;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class Presenter implements org.openlmis.core.presenter.Presenter {

    @Inject
    StockRepository stockRepository;

    @Inject
    RnrFormItemRepository rnrFormItemRepository;

    StockCardListView view;

    List<StockCard> stockCardList;

    public static final int STOCK_ON_HAND_NORMAL = 1;
    public static final int STOCK_ON_HAND_LOW_STOCK = 2;
    public static final int STOCK_ON_HAND_STOCK_OUT = 3;

    public Presenter(){
        stockCardList = new ArrayList<>();
    }


    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }


    public List<StockCard> getStockCards(){
        return stockCardList;
    }

    public void loadStockCards() {

        if (stockCardList.size() > 0){
            return;
        }

        view.loading();
        Observable.create(new Observable.OnSubscribe<List<StockCard>>() {
            @Override
            public void call(Subscriber<? super List<StockCard>> subscriber) {
                try {
                    subscriber.onNext(stockRepository.list());
                }catch (LMISException e){
                    subscriber.onError(e);
                }finally {
                    subscriber.onCompleted();
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
            }

            @Override
            public void onNext(List<StockCard> stockCards) {
                stockCardList.addAll(stockCards);
                view.refresh();
            }
        });
    }

    @Override
    public void attachView(View v) {
        view = (StockCardListView)v;
    }

    public interface StockCardListView extends View {
        void refresh();
    }

    public int getStockOnHandLevel(StockCard stockCard) {
        int lowStockAvg = getLowStockAvg(stockCard);
        long stockOnHand = stockCard.getStockOnHand();
        if (stockOnHand > lowStockAvg) {
            return STOCK_ON_HAND_NORMAL;
        } else if (stockOnHand > 0) {
            return STOCK_ON_HAND_LOW_STOCK;
        } else {
            return STOCK_ON_HAND_STOCK_OUT;
        }
    }

    private int getLowStockAvg(StockCard stockCard) {
        try {
            List<RnrFormItem> rnrFormItemList = rnrFormItemRepository.queryListForLowStockByProductId(stockCard.getProduct());
            long total = 0;
            for (RnrFormItem item : rnrFormItemList) {
                total += item.getIssued();
            }
            if (rnrFormItemList.size() > 0) {
                return (int) Math.ceil((total / rnrFormItemList.size()) * 0.05);
            }
        } catch (LMISException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
