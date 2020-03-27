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
import org.openlmis.core.model.Product.IsKit;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.model.service.StockService;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.base.Predicate;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static org.openlmis.core.presenter.StockCardPresenter.ArchiveStatus.Active;
import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

public class StockCardPresenter extends Presenter {


    private List<InventoryViewModel> inventoryViewModels;

    @Inject
    StockRepository stockRepository;
    @Inject
    ProductRepository productRepository;
    @Inject
    StockService stockService;
    @Inject
    StockMovementRepository stockMovementRepository;

    Observer<List<StockCard>> afterLoadHandler = getLoadStockCardsSubscriber();

    private StockCardListView view;

    public StockCardPresenter() {
        inventoryViewModels = new ArrayList<>();
    }

    public List<InventoryViewModel> getInventoryViewModels() {
        return inventoryViewModels;
    }

    public void loadStockCards(ArchiveStatus status) {
        loadStockCards(status, true);
    }

    public void loadStockCards(ArchiveStatus status, Boolean showLoading) {
        if (showLoading) {
            view.loading();
        }
        Subscription subscription = getLoadStockCardsObservable(status).subscribe(afterLoadHandler);
        subscriptions.add(subscription);
    }

    public void loadKits() {
        view.loading();
        Subscription subscription = createOrGetKitStockCardsObservable().subscribe(afterLoadHandler);
        subscriptions.add(subscription);
    }

    public void refreshStockCardsObservable() {
        view.loading();
        Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                refreshStockCardViewModelsSOH();
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<Void>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                ToastUtil.show(e.getMessage());
                view.loaded();
            }

            @Override
            public void onNext(Void aVoid) {
                view.refreshBannerText();
                loadStockCards(Active, false);
            }
        });
    }

    public void refreshStockCardViewModelsSOH() {
        for (InventoryViewModel inventoryViewModel : inventoryViewModels) {
            final StockCard stockCard = inventoryViewModel.getStockCard();
            stockRepository.refresh(stockCard);
            inventoryViewModel.setStockOnHand(stockCard.calculateSOHFromLots());
        }
    }

    @Override
    public void attachView(BaseView v) {
        view = (StockCardListView) v;
    }

    public void archiveBackStockCard(StockCard stockCard) {
        stockCard.getProduct().setArchived(false);

        stockCard.setExpireDates("");
        try {
            stockRepository.updateStockCardWithProduct(stockCard);
        } catch (LMISException e) {
            new LMISException(e, "StockCardPresenter.archiveBackStockCard").reportToFabric();
        }
    }

    private Observable<List<StockCard>> getLoadStockCardsObservable(final ArchiveStatus status) {
        return Observable.create(new Observable.OnSubscribe<List<StockCard>>() {
            @Override
            public void call(Subscriber<? super List<StockCard>> subscriber) {
                stockService.monthlyUpdateAvgMonthlyConsumption();
                subscriber.onNext(from(stockRepository.list()).filter(new Predicate<StockCard>() {
                    @Override
                    public boolean apply(StockCard stockCard) {
                        if (status.isArchived()) {
                            return showInArchiveView(stockCard);
                        }
                        return showInOverview(stockCard);
                    }
                }).toList());
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


    private void adjustmentMovement(List<StockCard> stockCardList) {
        for (StockCard stockCard : stockCardList) {
            try {
                List<StockMovementItem> movementItemList = stockMovementRepository.listLastTwoStockMovements(stockCard.getId());
            } catch (LMISException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean showInOverview(StockCard stockCard) {
        return !stockCard.getProduct().isKit() && (stockCard.calculateSOHFromLots() > 0 || (stockCard.getProduct().isActive() && !stockCard.getProduct().isArchived()));
    }

    private boolean showInArchiveView(StockCard stockCard) {
        return stockCard.calculateSOHFromLots() == 0 && (stockCard.getProduct().isArchived() || !stockCard.getProduct().isActive());
    }

    private Observer<List<StockCard>> getLoadStockCardsSubscriber() {
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
                List<InventoryViewModel> inventoryViewModelList = from(stockCards).transform(new Function<StockCard, InventoryViewModel>() {
                    @Override
                    public InventoryViewModel apply(StockCard stockCard) {
                        return new InventoryViewModel(stockCard);
                    }
                }).toList();
                inventoryViewModels.clear();
                inventoryViewModels.addAll(inventoryViewModelList);
                view.refresh(inventoryViewModels);
            }
        };
    }

    private Observable<List<StockCard>> createOrGetKitStockCardsObservable() {
        return Observable.create(new Observable.OnSubscribe<List<StockCard>>() {
            @Override
            public void call(Subscriber<? super List<StockCard>> subscriber) {
                try {
                    final List<Product> kits = productRepository.listActiveProducts(IsKit.Yes);
                    subscriber.onNext(createStockCardsIfNotExist(kits));
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private List<StockCard> createStockCardsIfNotExist(List<Product> kits) {
        return from(kits).transform(new Function<Product, StockCard>() {
            @Override
            public StockCard apply(Product product) {
                StockCard stockCard = null;
                try {
                    stockCard = stockRepository.queryStockCardByProductId(product.getId());
                    if (stockCard == null) {
                        stockCard = new StockCard();
                        stockCard.setProduct(product);
                        stockRepository.createOrUpdateStockCardWithStockMovement(stockCard);
                    }
                } catch (LMISException e) {
                    new LMISException(e, "createStockCardsIfNotExist").reportToFabric();
                }
                return stockCard;
            }
        }).toList();
    }

    public enum ArchiveStatus {
        Archived(true),
        Active(false);

        private boolean isArchived;

        ArchiveStatus(boolean isArchived) {
            this.isArchived = isArchived;
        }

        public boolean isArchived() {
            return isArchived;
        }
    }

    public interface StockCardListView extends BaseView {
        void refresh(List<InventoryViewModel> data);
        void refreshBannerText();
    }
}
