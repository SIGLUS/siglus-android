package org.openlmis.core.presenter;

import android.support.annotation.Nullable;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.roboguice.shaded.goole.common.base.Function;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

public class InitialInventoryPresenter extends InventoryPresenter {

    @Override
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

    public void doInventory(final List<InventoryViewModel> list) {
        if (view.validateInventory()) {
            view.loading();
            Subscription subscription = initStockCardObservable(list).subscribe(nextMainPageAction);
            subscriptions.add(subscription);
        }
    }

    private Observable<Object> initStockCardObservable(final List<InventoryViewModel> list) {
        return Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                initOrArchiveBackStockCards(list);
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    void initOrArchiveBackStockCards(List<InventoryViewModel> list) {
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
}
