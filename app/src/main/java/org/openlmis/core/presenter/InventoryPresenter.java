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

import android.support.annotation.Nullable;

import com.google.inject.Inject;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.DraftInventory;
import org.openlmis.core.model.DraftLotItem;
import org.openlmis.core.model.Inventory;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.InventoryRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.base.Predicate;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import org.roboguice.shaded.goole.common.collect.ImmutableList;

import java.util.Comparator;
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

    @Inject
    InventoryRepository inventoryRepository;

    @Inject
    SharedPreferenceMgr sharedPreferenceMgr;

    InventoryView view;

    @Override
    public void attachView(BaseView v) {
        view = (InventoryView) v;
    }

    public Observable<List<InventoryViewModel>> loadInventory() {

        return Observable.create(new Observable.OnSubscribe<List<InventoryViewModel>>() {
            @Override
            public void call(final Subscriber<? super List<InventoryViewModel>> subscriber) {
                try {
                    List<Product> inventoryProducts = productRepository.listProductsArchivedOrNotInStockCard();

                    List<InventoryViewModel> availableStockCardsForAddNewDrug = from(inventoryProducts)
                            .transform(new Function<Product, InventoryViewModel>() {
                                @Override
                                public InventoryViewModel apply(Product product) {
                                    return convertProductToStockCardViewModel(product);
                                }
                            }).toList();
                    subscriber.onNext(availableStockCardsForAddNewDrug);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    e.reportToFabric();
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    @Nullable
    private InventoryViewModel convertProductToStockCardViewModel(Product product) {
        try {
            InventoryViewModel viewModel;
            if (product.isArchived()) {
                viewModel = new InventoryViewModel(stockRepository.queryStockCardByProductId(product.getId()));
            } else {
                viewModel = new InventoryViewModel(product);
            }
            viewModel.setChecked(false);
            return viewModel;
        } catch (LMISException e) {
            e.reportToFabric();
        }
        return null;
    }

    public Observable<List<InventoryViewModel>> loadPhysicalInventory() {
        return Observable.create(new Observable.OnSubscribe<List<InventoryViewModel>>() {
            @Override
            public void call(Subscriber<? super List<InventoryViewModel>> subscriber) {
                try {
                    List<StockCard> validStockCardsForPhysicalInventory = getValidStockCardsForPhysicalInventory();
                    List<InventoryViewModel> inventoryViewModels = convertStockCardsToStockCardViewModels(validStockCardsForPhysicalInventory);

                    restoreDraftInventory(inventoryViewModels);
                    inventoryRepository.clearDraft();
                    subscriber.onNext(inventoryViewModels);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    private List<InventoryViewModel> convertStockCardsToStockCardViewModels(List<StockCard> validStockCardsForPhysicalInventory) {
        return FluentIterable.from(validStockCardsForPhysicalInventory).transform(new Function<StockCard, InventoryViewModel>() {
            @Override
            public InventoryViewModel apply(StockCard stockCard) {
                InventoryViewModel inventoryViewModel = new InventoryViewModel(stockCard);
                if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_lot_management)) {
                    try {
                        setExistingLotViewModels(inventoryViewModel);
                    } catch (LMISException e) {
                        e.reportToFabric();
                    }
                }
                return inventoryViewModel;
            }
        }).toList();
    }

    private List<StockCard> getValidStockCardsForPhysicalInventory() throws LMISException {
        return from(stockRepository.list()).filter(new Predicate<StockCard>() {
            @Override
            public boolean apply(StockCard stockCard) {
                return !stockCard.getProduct().isKit() && stockCard.getProduct().isActive() && !stockCard.getProduct().isArchived();
            }
        }).toList();
    }

    protected void restoreDraftInventory(List<InventoryViewModel> inventoryViewModels) throws LMISException {
        List<DraftInventory> draftList = inventoryRepository.queryAllDraft();

        for (InventoryViewModel model : inventoryViewModels) {
            for (DraftInventory draftInventory : draftList) {
                if (model.getStockCardId() == draftInventory.getStockCard().getId()) {
                    model.initExpiryDates(draftInventory.getExpireDates());
                    model.setQuantity(formatQuantity(draftInventory.getQuantity()));
                    if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_lot_management)) {
                        populateLotMovementModelWithDraftLotItem(model, draftInventory);
                    }
                }
            }
        }
    }

    private void populateLotMovementModelWithDraftLotItem(InventoryViewModel model, DraftInventory draftInventory) {
        List<LotMovementViewModel> existingLotMovementViewModelList = model.getExistingLotMovementViewModelList();
        List<LotMovementViewModel> newAddedLotMovementVieModelList = model.getLotMovementViewModelList();
        for (DraftLotItem draftLotItem : draftInventory.getDraftLotItemListWrapper()) {
            if (draftLotItem.isNewAdded()) {
                if (isNotInExistingLots(draftLotItem, existingLotMovementViewModelList)) {
                    LotMovementViewModel newLotMovementViewModel = new LotMovementViewModel();
                    newLotMovementViewModel.setQuantity(formatQuantity(draftLotItem.getQuantity()));
                    newLotMovementViewModel.setLotNumber(draftLotItem.getLotNumber());
                    newLotMovementViewModel.setExpiryDate(DateUtil.formatDate(draftLotItem.getExpirationDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR));
                    newAddedLotMovementVieModelList.add(newLotMovementViewModel);
                }
            } else {
                for (LotMovementViewModel existingLotMovementViewModel : existingLotMovementViewModelList) {
                    if (draftLotItem.getLotNumber().equals(existingLotMovementViewModel.getLotNumber())) {
                        existingLotMovementViewModel.setQuantity(formatQuantity(draftLotItem.getQuantity()));
                    }
                }
            }
        }
    }

    private boolean isNotInExistingLots(DraftLotItem draftLotItem, List<LotMovementViewModel> existingLotMovementViewModelList) {
        for (LotMovementViewModel lotMovementViewModel : existingLotMovementViewModelList) {
            if (draftLotItem.getLotNumber().equals(lotMovementViewModel.getLotNumber())) {
                return false;
            }
        }
        return true;
    }

    private String formatQuantity(Long quantity) {
        return quantity == null ? "" : quantity.toString();
    }

    protected void initOrArchiveBackStockCards(List<InventoryViewModel> list) {
        for (InventoryViewModel model : list) {
            if (model.isChecked()) {
                initOrArchiveBackStockCard(model);
            }
        }
    }

    private void initOrArchiveBackStockCard(InventoryViewModel model) {
        try {
            if (model.getProduct().isArchived()) {
                StockCard stockCard = model.getStockCard();
                stockCard.getProduct().setArchived(false);
                stockRepository.updateStockCardWithProduct(stockCard);
                return;
            }
            if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_lot_management)) {
                createStockCardAndInventoryMovementWithLot(model);
            } else {
                createStockCardAndInventoryMovement(model);
            }
        } catch (LMISException e) {
            e.reportToFabric();
        }
    }

    private void createStockCardAndInventoryMovementWithLot(InventoryViewModel model) throws LMISException {
        StockCard stockCard = new StockCard();
        stockCard.setProduct(model.getProduct());
        StockMovementItem movementItem = new StockMovementItem(stockCard, model);
        stockCard.setStockOnHand(movementItem.getStockOnHand());
        stockRepository.addStockMovementAndUpdateStockCard(movementItem);
    }

    private StockCard createStockCardAndInventoryMovement(InventoryViewModel model) throws LMISException {
        StockCard stockCard = new StockCard();
        stockCard.setProduct(model.getProduct());
        stockCard.setStockOnHand(Long.parseLong(model.getQuantity()));
        if (stockCard.getStockOnHand() != 0) {
            stockCard.setExpireDates(DateUtil.formatExpiryDateString(model.getExpiryDates()));
        } else {
            stockCard.setExpireDates("");
        }

        stockRepository.createOrUpdateStockCardWithStockMovement(stockCard);
        return stockCard;
    }


    protected StockMovementItem calculateAdjustment(InventoryViewModel model, StockCard stockCard) {
        long inventory;
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_lot_management)) {
            inventory = model.getLotListQuantityTotalAmount();
        } else {
            inventory = Long.parseLong(model.getQuantity());
        }
        long stockOnHand = model.getStockOnHand();
        StockMovementItem item = new StockMovementItem();
        item.setSignature(model.getSignature());
        item.setMovementDate(new Date());
        item.setMovementQuantity(Math.abs(inventory - stockOnHand));
        item.setStockOnHand(inventory);
        item.setStockCard(stockCard);

        if (inventory > stockOnHand) {
            item.setReason(MovementReasonManager.INVENTORY_POSITIVE);
            item.setMovementType(MovementReasonManager.MovementType.POSITIVE_ADJUST);
        } else if (inventory < stockOnHand) {
            item.setReason(MovementReasonManager.INVENTORY_NEGATIVE);
            item.setMovementType(MovementReasonManager.MovementType.NEGATIVE_ADJUST);
        } else {
            item.setReason(MovementReasonManager.INVENTORY);
            item.setMovementType(MovementReasonManager.MovementType.PHYSICAL_INVENTORY);
        }

        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_lot_management)) {
            item.populateLotAndResetStockOnHandOfLotAccordingPhysicalAdjustment(model.getExistingLotMovementViewModelList(), model.getLotMovementViewModelList());
        }

        return item;
    }

    public void savePhysicalInventory(List<InventoryViewModel> list) {
        view.loading();
        Subscription subscription = saveDraftInventoryObservable(list).subscribe(nextMainPageAction, errorAction);
        subscriptions.add(subscription);
    }

    private Observable<Object> saveDraftInventoryObservable(final List<InventoryViewModel> list) {
        return Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                try {
                    for (InventoryViewModel model : list) {
                        inventoryRepository.createDraft(model.parseDraftInventory());
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

    public void doPhysicalInventory(List<InventoryViewModel> list, final String sign) {
        view.loading();

        for (InventoryViewModel viewModel : list) {
            viewModel.setSignature(sign);
        }
        Subscription subscription = stockMovementObservable(list).subscribe(nextMainPageAction, errorAction);
        subscriptions.add(subscription);
    }

    protected Observable<Object> stockMovementObservable(final List<InventoryViewModel> list) {
        return Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                try {
                    for (InventoryViewModel model : list) {
                        StockCard stockCard = model.getStockCard();
                        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_lot_management)) {
                            stockCard.setStockOnHand(model.getLotListQuantityTotalAmount());
                        } else {
                            stockCard.setStockOnHand(Long.parseLong(model.getQuantity()));
                        }

                        if (stockCard.getStockOnHand() == 0) {
                            stockCard.setExpireDates("");
                        }

                        stockRepository.addStockMovementAndUpdateStockCard(calculateAdjustment(model, stockCard));
                    }
                    inventoryRepository.clearDraft();
                    sharedPreferenceMgr.setLatestPhysicInventoryTime(DateUtil.formatDate(new Date(), DateUtil.DATE_TIME_FORMAT));
                    saveInventoryDate();

                    subscriber.onNext(null);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    subscriber.onError(e);
                    e.reportToFabric();
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private void saveInventoryDate() {
        inventoryRepository.save(new Inventory());
    }

    protected Action1<Object> nextMainPageAction = new Action1<Object>() {
        @Override
        public void call(Object o) {
            view.loaded();
            view.goToParentPage();
        }
    };

    protected Action1<Throwable> errorAction = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            view.loaded();
            view.showErrorMessage(throwable.getMessage());
        }
    };

    public void doInitialInventory(final List<InventoryViewModel> list) {
        if (view.validateInventory()) {
            view.loading();
            Subscription subscription = initStockCardObservable(list).subscribe(nextMainPageAction);
            subscriptions.add(subscription);
        }
    }

    protected Observable<Object> initStockCardObservable(final List<InventoryViewModel> list) {
        return Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                initOrArchiveBackStockCards(list);
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


    private void setExistingLotViewModels(InventoryViewModel inventoryViewModel) throws LMISException {
        ImmutableList<LotMovementViewModel> lotMovementViewModels = null;
        try {
            lotMovementViewModels = FluentIterable.from(stockRepository.getNonEmptyLotOnHandByStockCard(inventoryViewModel.getStockCard().getId())).transform(new Function<LotOnHand, LotMovementViewModel>() {
                @Override
                public LotMovementViewModel apply(LotOnHand lotOnHand) {
                    return new LotMovementViewModel(lotOnHand.getLot().getLotNumber(),
                            DateUtil.formatDate(lotOnHand.getLot().getExpirationDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR),
                            lotOnHand.getQuantityOnHand().toString(), MovementReasonManager.MovementType.RECEIVE);
                }
            }).toSortedList(new Comparator<LotMovementViewModel>() {
                @Override
                public int compare(LotMovementViewModel lot1, LotMovementViewModel lot2) {
                    return DateUtil.parseString(lot1.getExpiryDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR).compareTo(DateUtil.parseString(lot2.getExpiryDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR));
                }
            });
        } catch (LMISException e) {
            e.printStackTrace();
        }
        inventoryViewModel.setExistingLotMovementViewModelList(lotMovementViewModels);
    }

    public interface InventoryView extends BaseView {
        void goToParentPage();

        boolean validateInventory();

        void showErrorMessage(String msg);

        void showSignDialog();
    }
}
