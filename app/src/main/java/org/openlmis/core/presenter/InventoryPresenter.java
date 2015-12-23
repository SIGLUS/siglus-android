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
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.DraftInventory;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.StockCardViewModel;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.base.Predicate;

import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

public class InventoryPresenter extends Presenter {

    @Inject
    ProductRepository productRepository;

    @Inject
    StockRepository stockRepository;

    InventoryView view;

    @Override
    public void attachView(BaseView v) {
        view = (InventoryView) v;
    }

    public Observable<List<StockCardViewModel>> loadMasterProductList() {

        return Observable.create(new Observable.OnSubscribe<List<StockCardViewModel>>() {
            @Override
            public void call(final Subscriber<? super List<StockCardViewModel>> subscriber) {
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
                            return product.isArchived() || !existProductList.contains(product);
                        }
                    }).transform(new Function<Product, StockCardViewModel>() {
                        @Override
                        public StockCardViewModel apply(Product product) {
                            if (product.isArchived()) {
                                try {
                                    return new StockCardViewModel(stockRepository.queryStockCardByProductId(product.getId()));
                                } catch (LMISException e) {
                                    e.reportToFabric();
                                }
                            }
                            return new StockCardViewModel(product);
                        }
                    }).toList();

                    subscriber.onNext(list);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    e.reportToFabric();
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    public Observable<List<StockCardViewModel>> loadPhysicalStockCards() {
        return Observable.create(new Observable.OnSubscribe<List<StockCardViewModel>>() {
            @Override
            public void call(Subscriber<? super List<StockCardViewModel>> subscriber) {
                try {
                    List<StockCardViewModel> stockCardViewModels = from(stockRepository.list()).filter(new Predicate<StockCard>() {
                        @Override
                        public boolean apply(StockCard stockCard) {
                            return !stockCard.getProduct().isArchived();
                        }
                    }).transform(new Function<StockCard, StockCardViewModel>() {
                        @Override
                        public StockCardViewModel apply(StockCard stockCard) {
                            return new StockCardViewModel(stockCard);
                        }
                    }).toList();

                    restoreDraftInventory(stockCardViewModels);
                    subscriber.onNext(stockCardViewModels);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    protected void restoreDraftInventory(List<StockCardViewModel> stockCardViewModels) throws LMISException {
        List<DraftInventory> draftList = stockRepository.listDraftInventory();

        for (StockCardViewModel model : stockCardViewModels) {
            for (DraftInventory draftInventory : draftList) {
                if (model.getStockCardId() == draftInventory.getStockCard().getId()) {
                    model.initExpiryDates(draftInventory.getExpireDates());
                    model.setQuantity(String.valueOf(draftInventory.getQuantity()));
                }
            }
        }
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

    private StockCard initStockCard(StockCardViewModel model) {
        try {
            boolean isArchivedStockCard = model.getStockCard() != null;

            StockCard stockCard = isArchivedStockCard ? model.getStockCard() : new StockCard();
            stockCard.setStockOnHand(Long.parseLong(model.getQuantity()));
            stockCard.setExpireDates(model.formatExpiryDateString());

            if (isArchivedStockCard) {
                stockCard.getProduct().setArchived(false);
                stockRepository.reInventoryArchivedStockCard(stockCard);
            } else {
                stockCard.setProduct(productRepository.getById(model.getProductId()));
                stockRepository.initStockCard(stockCard);
            }
            return stockCard;
        } catch (LMISException e) {
            e.reportToFabric();
        }
        return null;
    }


    protected StockMovementItem calculateAdjustment(StockCardViewModel model, StockCard stockCard) {
        long inventory = Long.parseLong(model.getQuantity());
        long stockOnHand = model.getStockOnHand();

        StockMovementItem item = new StockMovementItem();
        item.setSignature(model.getSignature());
        item.setMovementDate(new Date());
        item.setMovementQuantity(Math.abs(inventory - stockOnHand));
        item.setStockOnHand(inventory);
        item.setStockCard(stockCard);

        if (inventory > stockOnHand) {
            item.setReason(MovementReasonManager.INVENTORY_POSITIVE);
            item.setMovementType(StockMovementItem.MovementType.POSITIVE_ADJUST);
        } else if (inventory < stockOnHand) {
            item.setReason(MovementReasonManager.INVENTORY_NEGATIVE);
            item.setMovementType(StockMovementItem.MovementType.NEGATIVE_ADJUST);
        } else {
            item.setReason(MovementReasonManager.INVENTORY);
            item.setMovementType(StockMovementItem.MovementType.PHYSICAL_INVENTORY);
        }
        return item;
    }

    public void savePhysicalInventory(List<StockCardViewModel> list) {
        view.loading();
        Subscription subscription = saveDraftInventoryObservable(list).subscribe(nextMainPageAction, errorAction);
        subscriptions.add(subscription);
    }

    private Observable<Object> saveDraftInventoryObservable(final List<StockCardViewModel> list) {
        return Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                try {
                    for (StockCardViewModel model : list) {
                        stockRepository.saveDraftInventory(model.parseDraftInventory());
                    }
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    subscriber.onError(e);
                    e.reportToFabric();
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public void signPhysicalInventory() {
        if (view.validateInventory()) {
            view.showSignDialog();
        }
    }

    public void doPhysicalInventory(List<StockCardViewModel> list, final String sign) {
        view.loading();

        for (StockCardViewModel viewModel : list) {
            viewModel.setSignature(sign);
        }
        Subscription subscription = stockMovementObservable(list).subscribe(nextMainPageAction, errorAction);
        subscriptions.add(subscription);
    }

    protected Observable<Object> stockMovementObservable(final List<StockCardViewModel> list) {
        return Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                try {
                    for (StockCardViewModel model : list) {
                        StockCard stockCard = model.getStockCard();
                        stockCard.setExpireDates(model.formatExpiryDateString());
                        stockCard.setStockOnHand(Long.parseLong(model.getQuantity()));
                        stockRepository.addStockMovementAndUpdateStockCard(stockCard, calculateAdjustment(model, stockCard));
                    }
                    stockRepository.clearDraftInventory();
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    subscriber.onError(e);
                    e.printStackTrace();
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    protected Action1<Object> nextMainPageAction = new Action1<Object>() {
        @Override
        public void call(Object o) {
            view.loaded();
            view.goToMainPage();
        }
    };

    protected Action1<Throwable> errorAction = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            view.loaded();
            view.showErrorMessage(throwable.getMessage());
        }
    };

    public void doInitialInventory(final List<StockCardViewModel> list) {
        if (view.validateInventory()) {
            view.loading();
            Subscription subscription = initStockCardObservable(list).subscribe(nextMainPageAction);
            subscriptions.add(subscription);
        }
    }

    protected Observable<Object> initStockCardObservable(final List<StockCardViewModel> list) {
        return Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                initStockCards(list);
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public interface InventoryView extends BaseView {
        void goToMainPage();

        boolean validateInventory();

        void showErrorMessage(String msg);

        void showSignDialog();
    }
}
