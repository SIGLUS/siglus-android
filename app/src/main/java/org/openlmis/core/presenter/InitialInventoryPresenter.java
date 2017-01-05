package org.openlmis.core.presenter;

import android.support.annotation.Nullable;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.roboguice.shaded.goole.common.base.Function;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
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

                    inventoryViewModelList.addAll(from(inventoryProducts)
                            .transform(new Function<Product, InventoryViewModel>() {
                                @Override
                                public InventoryViewModel apply(Product product) {
                                    return convertProductToStockCardViewModel(product);
                                }
                            }).toList());
                    subscriber.onNext(inventoryViewModelList);
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
                viewModel.setMovementType(MovementReasonManager.MovementType.PHYSICAL_INVENTORY);
            } else {
                viewModel = new InventoryViewModel(product);
                viewModel.setMovementType(MovementReasonManager.MovementType.PHYSICAL_INVENTORY);
            }
            viewModel.setChecked(false);
            return viewModel;
        } catch (LMISException e) {
            e.reportToFabric();
        }
        return null;
    }

    public Observable<Object> initStockCardObservable() {
        return Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                initOrArchiveBackStockCards();
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    void initOrArchiveBackStockCards() {
        for (InventoryViewModel inventoryViewModel : inventoryViewModelList) {
            if (inventoryViewModel.isChecked()) {
                initOrArchiveBackStockCard(inventoryViewModel);
            }
        }
    }

    private void initOrArchiveBackStockCard(InventoryViewModel inventoryViewModel) {
        try {
            if (inventoryViewModel.getProduct().isArchived()) {
                StockCard stockCard = inventoryViewModel.getStockCard();
                stockCard.getProduct().setArchived(false);
                stockRepository.updateStockCardWithProduct(stockCard);
                return;
            }
            createStockCardAndInventoryMovementWithLot(inventoryViewModel);
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

}
