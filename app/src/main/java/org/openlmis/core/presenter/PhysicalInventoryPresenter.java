package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.DraftInventory;
import org.openlmis.core.model.Inventory;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.service.StockService;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.viewmodel.PhysicalInventoryViewModel;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.base.Predicate;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

public class PhysicalInventoryPresenter extends InventoryPresenter {
    @Inject
    StockService stockService;

    @Override
    public Observable<List<InventoryViewModel>> loadInventory() {
        return Observable.create(new Observable.OnSubscribe<List<InventoryViewModel>>() {
            @Override
            public void call(Subscriber<? super List<InventoryViewModel>> subscriber) {
                try {
                    List<StockCard> validStockCardsForPhysicalInventory = getValidStockCardsForPhysicalInventory();
                    inventoryViewModelList.addAll(convertStockCardsToStockCardViewModels(validStockCardsForPhysicalInventory));
                    restoreDraftInventory();
                    subscriber.onNext(inventoryViewModelList);
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
                InventoryViewModel inventoryViewModel = new PhysicalInventoryViewModel(stockCard);
                setExistingLotViewModels(inventoryViewModel);
                return inventoryViewModel;
            }
        }).toList();
    }

    protected List<StockCard> getValidStockCardsForPhysicalInventory() throws LMISException {
        return from(stockRepository.list()).filter(new Predicate<StockCard>() {
            @Override
            public boolean apply(StockCard stockCard) {
                return !stockCard.getProduct().isKit() && stockCard.getProduct().isActive() && !stockCard.getProduct().isArchived();
            }
        }).toList();
    }

    protected void restoreDraftInventory() throws LMISException {
        List<DraftInventory> draftList = inventoryRepository.queryAllDraft();

        for (DraftInventory draftInventory : draftList) {//total : N
            for (InventoryViewModel viewModel : inventoryViewModelList) { // total: N+1
                if (viewModel.getStockCardId() == draftInventory.getStockCard().getId()) {
                    ((PhysicalInventoryViewModel) viewModel).setDraftInventory(draftInventory);
                }
            }
        }
    }

    private void populateLotMovementModelWithExistingSoh(InventoryViewModel viewModel) {
        for (LotMovementViewModel lotMovementViewModel : viewModel.getExistingLotMovementViewModelList()) {
            lotMovementViewModel.setQuantity(lotMovementViewModel.getLotSoh());
        }
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

    public Observable<Object> saveDraftInventoryObservable() {
        return Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                try {
                    inventoryRepository.clearDraft();
                    for (InventoryViewModel model : inventoryViewModelList) {
                        inventoryRepository.createDraft(new DraftInventory((PhysicalInventoryViewModel) model));
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

    /**
     * 如果在 {@link #doInventory(String)} 时, {@link #inventoryViewModelList} 和 {@link #restoreDraftInventory()}
     * 中的{@code draftInventory} 不同会怎样？
     * 如果在inventory部分，并将其save。然后在issue saved的product！会怎样？
     *
     */
    public Observable<Object> doInventory(final String sign) {
        return Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                try {
                    for (InventoryViewModel viewModel : inventoryViewModelList) {
                        viewModel.setSignature(sign);
                        StockCard stockCard = viewModel.getStockCard();

                        stockCard.setStockOnHand(viewModel.getLotListQuantityTotalAmount());

                        if (stockCard.getStockOnHand() == 0) {
                            stockCard.setExpireDates("");
                        }

                        stockRepository.addStockMovementAndUpdateStockCard(calculateAdjustment(viewModel, stockCard));
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

    private void setExistingLotViewModels(InventoryViewModel inventoryViewModel) {
        List<LotMovementViewModel> lotMovementViewModels = FluentIterable.from(inventoryViewModel.getStockCard().getNonEmptyLotOnHandList()).transform(new Function<LotOnHand, LotMovementViewModel>() {
            @Override
            public LotMovementViewModel apply(LotOnHand lotOnHand) {
                return new LotMovementViewModel(lotOnHand.getLot().getLotNumber(),
                        DateUtil.formatDate(lotOnHand.getLot().getExpirationDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR),
                        lotOnHand.getQuantityOnHand().toString(), MovementReasonManager.MovementType.RECEIVE);
            }
        }).toSortedList(new Comparator<LotMovementViewModel>() {
            @Override
            public int compare(LotMovementViewModel lot1, LotMovementViewModel lot2) {
                Date localDate = DateUtil.parseString(lot1.getExpiryDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR);
                if (localDate != null) {
                    return localDate.compareTo(DateUtil.parseString(lot2.getExpiryDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR));
                } else {
                    return 0;
                }
            }
        });
        inventoryViewModel.setExistingLotMovementViewModelList(lotMovementViewModels);
    }

    public void updateAvgMonthlyConsumption() {
        stockService.immediatelyUpdateAvgMonthlyConsumption();
    }

    public int getCompleteCount() {
        return FluentIterable.from(inventoryViewModelList).filter(new Predicate<InventoryViewModel>() {
            @Override
            public boolean apply(InventoryViewModel inventoryViewModel) {
                return ((PhysicalInventoryViewModel) inventoryViewModel).isDone();
            }
        }).toList().size();
    }
}
