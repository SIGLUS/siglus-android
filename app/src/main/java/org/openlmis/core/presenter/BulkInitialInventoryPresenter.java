package org.openlmis.core.presenter;

import android.support.annotation.Nullable;
import android.util.Log;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.DraftInitialInventory;
import org.openlmis.core.model.Inventory;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.BulkInitialInventoryViewModel;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

public class BulkInitialInventoryPresenter extends InventoryPresenter {
    private static final String TAG = BulkInitialInventoryPresenter.class.getSimpleName();

    @Override
    public Observable<List<InventoryViewModel>> loadInventory() {
        return Observable.create((Observable.OnSubscribe<List<InventoryViewModel>>) subscriber -> {
            try {
                List<Product> inventoryProducts = productRepository.listBasicProducts();
                List<Long> productIds = from(inventoryProducts).transform((product -> product.getId())).toList();
//                List<StockCard> inventoryStocks = stockRepository.listStockCardsByProductIds(productIds);
                Log.e(TAG, "inventoryProducts.size=" + inventoryProducts.size());
//                Log.e(TAG, "inventoryStocks.size=" + inventoryStocks.size());
                Log.e(TAG, "productIds.size=" + productIds.size());
                inventoryViewModelList.addAll(convertProductToStockCardViewModel(inventoryProducts));
                restoreDraftInventory();
                subscriber.onNext(inventoryViewModelList);
                subscriber.onCompleted();
            } catch (LMISException e) {
                new LMISException(e, TAG + ":" + "loadInventory").reportToFabric();
                subscriber.onError(e);
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    private void restoreDraftInventory() throws LMISException {
        List<DraftInitialInventory> draftInventoryList = inventoryRepository.queryAllInitialDraft();
        for (DraftInitialInventory draftInventory : draftInventoryList) {
            for (InventoryViewModel viewModel : inventoryViewModelList) {
                if (viewModel.getProductId() == draftInventory.getProduct().getId()) {
                    ((BulkInitialInventoryViewModel) viewModel).setInitialDraftInventory(draftInventory);
                }
            }
        }
    }

    @Nullable
    private List<BulkInitialInventoryViewModel> convertProductToStockCardViewModel(List<Product> products) throws LMISException {
        return from(products).transform(product -> {
            try {
                BulkInitialInventoryViewModel viewModel;
                if (product.isArchived()) {
                    viewModel = new BulkInitialInventoryViewModel(stockRepository.queryStockCardByProductId(product.getId()));
                    viewModel.setMovementType(MovementReasonManager.MovementType.INITIAL_INVENTORY);
                } else {
                    viewModel = new BulkInitialInventoryViewModel(product);
//                    viewModel.setViewType(BulkInventoryLotMovementAdapter.ITEM_LIST);
                    viewModel.setMovementType(MovementReasonManager.MovementType.INITIAL_INVENTORY);
                }
                viewModel.setChecked(false);
                setExistingLotViewModels(viewModel);
                return viewModel;
            } catch (LMISException e) {
                new LMISException(e, TAG + ":" + "convertProductToInitialInventoryViewModel").reportToFabric();
            }
            return null;
        }).toList();


    }

    private void setExistingLotViewModels(BulkInitialInventoryViewModel bulkInitialInventoryViewModel) {
        if (bulkInitialInventoryViewModel.getStockCard() == null) {
            return;
        }
        List<LotMovementViewModel> lotMovementViewModels = FluentIterable
                .from(bulkInitialInventoryViewModel.getStockCard().getNonEmptyLotOnHandList())
                .transform(lotOnHand -> new LotMovementViewModel(lotOnHand.getLot().getLotNumber(),
                        DateUtil.formatDate(lotOnHand.getLot().getExpirationDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR),
                        lotOnHand.getQuantityOnHand().toString(), MovementReasonManager.MovementType.RECEIVE))
                .toSortedList((lot1, lot2) -> {
                    Date localDate = DateUtil.parseString(lot1.getExpiryDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR);
                    if (localDate != null) {
                        return localDate.compareTo(DateUtil.parseString(lot2.getExpiryDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR));
                    } else {
                        return 0;
                    }
                });
        bulkInitialInventoryViewModel.setExistingLotMovementViewModelList(lotMovementViewModels);
    }


    public void addNonBasicProductsToInventory(List<Product> nonBasicProducts) {
        try {
            inventoryViewModelList.addAll(convertProductToStockCardViewModel(nonBasicProducts));
        } catch (LMISException e) {
            e.printStackTrace();
        }
    }

    public void removeNonBasicProductElement(InventoryViewModel inventoryViewModel) {
        inventoryViewModelList.remove(inventoryViewModel);

    }

    public Observable<Object> saveDraftInventoryObservable() {
        return Observable.create(subscriber -> {
            try {
                inventoryRepository.clearInitialDraft();
                for (InventoryViewModel inventoryViewModel: inventoryViewModelList) {
                    inventoryRepository.createInitialDraft(new DraftInitialInventory((BulkInitialInventoryViewModel)inventoryViewModel));
                }
                subscriber.onNext(null);
                subscriber.onCompleted();
            } catch (LMISException e) {
                e.printStackTrace();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Object> doInventory() {
        return Observable.create(subscriber -> {
            try {
                for (InventoryViewModel viewModel : inventoryViewModelList) {
                    if (viewModel.getProduct().isArchived()) {
                        StockCard stockCard = viewModel.getStockCard();
                        stockCard.getProduct().setArchived(false);
                        stockRepository.updateStockCardWithProduct(stockCard);
                        return;
                    }
                    StockCard stockCard = new StockCard();
                    stockCard.setProduct(viewModel.getProduct());
                    StockMovementItem movementItem = new StockMovementItem(stockCard, viewModel);
                    stockCard.setStockOnHand(movementItem.getStockOnHand());
                    stockRepository.addStockMovementAndUpdateStockCard(movementItem);
                }
                inventoryRepository.clearInitialDraft();
                saveInventoryDate();
                subscriber.onNext(null);
                subscriber.onCompleted();
            } catch (LMISException e) {
                subscriber.onError(e);
                new LMISException(e,"doInventory").reportToFabric();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
    private void saveInventoryDate() {
        inventoryRepository.save(new Inventory());
    }
    protected StockMovementItem calculateAdjustment(InventoryViewModel model, StockCard stockCard) {
        Long inventory = model.getLotListQuantityTotalAmount();
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

        item.populateLotAndResetStockOnHandOfLotAccordingPhysicalAdjustment(model.getExistingLotMovementViewModelList(), model.getNewLotMovementViewModelList());

        return item;
    }

}
