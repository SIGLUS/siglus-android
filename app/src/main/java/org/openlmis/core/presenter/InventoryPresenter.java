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
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.View;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.StockCardViewModel;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class InventoryPresenter implements Presenter {

    @Inject
    ProductRepository productRepository;

    @Inject
    StockRepository stockRepository;

    InventoryView view;

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void attachView(View v) {
        view = (InventoryView) v;
    }

    public List<Product> loadMasterProductList() {
        List<Product> list = null;
        try {
            list = productRepository.list();
        } catch (LMISException e) {
            e.printStackTrace();
        }

        return list;
    }

    public Observable<List<StockCardViewModel>> loadStockCardList(){
        return Observable.create(new Observable.OnSubscribe<List<StockCardViewModel>>() {
            @Override
            public void call(Subscriber<? super List<StockCardViewModel>> subscriber) {
                List<StockCard> list;
                try{
                    list = stockRepository.list();
                    subscriber.onNext(FluentIterable.from(list).transform(new Function<StockCard, StockCardViewModel>() {
                        @Override
                        public StockCardViewModel apply(StockCard stockCard) {
                            return new StockCardViewModel(stockCard);
                        }
                    }).toList());
                }catch (LMISException e){
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }


    public void initStockCard(List<InventoryViewModel> list) {
        List<StockCard> stockCards = new ArrayList<>();

        for (InventoryViewModel model : list) {
            if (model.isChecked()) {
                StockCard stockCard = new StockCard();
                stockCard.setProduct(model.getProduct());
                stockCard.setStockOnHand(Integer.parseInt(model.getQuantity()));
                stockCard.setExpireDates(model.getExpireDate());

                stockCards.add(stockCard);
            }
        }
        stockRepository.batchSave(stockCards);
    }

    public void submitInventory(List<InventoryViewModel> list) {
        if (view.validateInventory()) {
            initStockCard(list);
            view.goToMainPage();
        }
    }

    public interface InventoryView extends View {
        void goToMainPage();

        boolean validateInventory();
    }
}
