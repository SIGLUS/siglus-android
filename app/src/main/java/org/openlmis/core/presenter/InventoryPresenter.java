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

import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.View;
import org.openlmis.core.view.viewmodel.StockCardViewModel;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.base.Predicate;

import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

public class InventoryPresenter implements Presenter {

    @Inject
    ProductRepository productRepository;

    @Inject
    StockRepository stockRepository;

    InventoryView view;

    @Inject
    Context context;

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

    public Observable<List<StockCardViewModel>> loadMasterProductList() {

        return Observable.create(new Observable.OnSubscribe<List<StockCardViewModel>>() {
            @Override
            public void call(Subscriber<? super List<StockCardViewModel>> subscriber) {
                try {
                    final List<Product> existProductList = from(stockRepository.list()).transform(new Function<StockCard, Product>() {
                        @Override
                        public Product apply(StockCard stockCard) {
                            return stockCard.getProduct();
                        }
                    }).toList();

                    List<StockCardViewModel> list = from(productRepository.list()).filter(new Predicate<Product>() {
                        @Override
                        public boolean apply(Product product) {
                            return !existProductList.contains(product);
                        }
                    }).transform(new Function<Product, StockCardViewModel>() {
                        @Override
                        public StockCardViewModel apply(Product product) {
                            return new StockCardViewModel(product);
                        }
                    }).toList();

                    subscriber.onNext(list);
                } catch (LMISException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    public Observable<List<StockCardViewModel>> loadStockCardList(){
        return Observable.create(new Observable.OnSubscribe<List<StockCardViewModel>>() {
            @Override
            public void call(Subscriber<? super List<StockCardViewModel>> subscriber) {
                List<StockCard> list;
                try{
                    list = stockRepository.list();
                    subscriber.onNext(from(list).transform(new Function<StockCard, StockCardViewModel>() {
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


    public void initStockCards(List<StockCardViewModel> list) {

        from(list).filter(new Predicate<StockCardViewModel>() {
            @Override
            public boolean apply(StockCardViewModel stockCardViewModel) {
                return stockCardViewModel.isChecked();
            }
        }).transform(new Function<StockCardViewModel, StockCard>() {
            @Override
            public StockCard apply(StockCardViewModel stockCardViewModel) {
                return initStockCard(stockCardViewModel);
            }
        }).toList();
    }

    private StockCard initStockCard(StockCardViewModel model){
        try {
            StockCard stockCard = new StockCard();
            stockCard.setProduct(productRepository.getById(model.getProductId()));
            stockCard.setStockOnHand(Long.parseLong(model.getQuantity()));
            stockCard.setExpireDates(model.formatExpiryDateString());

            stockRepository.initStockCard(stockCard);
            return stockCard;
        }catch (LMISException e){
            e.printStackTrace();
        }
        return null;
    }


    protected StockMovementItem calculateAdjustment(StockCardViewModel model) {
        long inventory = Long.parseLong(model.getQuantity());
        long stockOnHand = model.getStockOnHand();

        StockMovementItem item = new StockMovementItem();
        item.setMovementDate(new Date());
        item.setMovementQuantity(Math.abs(inventory - stockOnHand));

        if (inventory > stockOnHand) {
            item.setReason(context.getResources().getString(R.string.physical_inventory_positive));
            item.setMovementType(StockMovementItem.MovementType.POSITIVE_ADJUST);
        } else if (inventory < stockOnHand) {
            item.setReason(context.getResources().getString(R.string.physical_inventory_negative));
            item.setMovementType(StockMovementItem.MovementType.NEGATIVE_ADJUST);
        } else {
            item.setReason(context.getResources().getString(R.string.title_physical_inventory));
            item.setMovementType(StockMovementItem.MovementType.PHYSICAL_INVENTORY);
        }
        return item;
    }

    public void doPhysicalInventory(List<StockCardViewModel> list) {
        if (view.validateInventory()) {
            for (StockCardViewModel model : list) {
                try {
                    stockRepository.addStockMovement(model.getStockCardId(), calculateAdjustment(model));
                } catch (LMISException e) {
                    e.printStackTrace();
                }
            }
            view.goToMainPage();
        }
    }

    public void doInitialInventory(List<StockCardViewModel> list) {
        if (view.validateInventory()) {
            initStockCards(list);
            view.goToMainPage();
        }
    }

    public interface InventoryView extends View {
        void goToMainPage();

        boolean validateInventory();
    }
}
