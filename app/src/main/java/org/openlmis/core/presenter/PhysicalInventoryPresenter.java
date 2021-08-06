package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.DraftInventory;
import org.openlmis.core.model.Inventory;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.model.service.StockService;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.viewmodel.PhysicalInventoryViewModel;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

public class PhysicalInventoryPresenter extends InventoryPresenter {
    @Inject
    StockService stockService;

    @Inject
    StockMovementRepository movementRepository;

    @Override
    public Observable<List<InventoryViewModel>> loadInventory() {
        return Observable.create((Observable.OnSubscribe<List<InventoryViewModel>>) subscriber -> {
            try {
                List<StockCard> validStockCardsForPhysicalInventory = getValidStockCardsForPhysicalInventory();
                inventoryViewModelList.addAll(convertStockCardsToStockCardViewModels(validStockCardsForPhysicalInventory));
                restoreDraftInventory();
                subscriber.onNext(inventoryViewModelList);
                subscriber.onCompleted();
            } catch (LMISException e) {
                subscriber.onError(e);
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }


    private List<InventoryViewModel> convertStockCardsToStockCardViewModels(List<StockCard> validStockCardsForPhysicalInventory) {
        Map<String,List<Map<String,String>>> lotInfoMap = stockRepository.getLotsAndLotOnHandInfo();
        Map<String,String> lotOnHands = stockRepository.lotOnHands();
        return FluentIterable.from(validStockCardsForPhysicalInventory).transform(stockCard -> {
            addLotInfoToStockCard(stockCard,lotInfoMap);
            InventoryViewModel inventoryViewModel = new PhysicalInventoryViewModel(stockCard,lotOnHands);
            setExistingLotViewModels(inventoryViewModel);
            return inventoryViewModel;
        }).toList();
    }

    private void addLotInfoToStockCard(StockCard stockCard, Map<String,List<Map<String,String>>> lotInfoMap){
        List<LotOnHand> lotOnHands = new ArrayList<>();
        List<Map<String,String>> lotInfoList = lotInfoMap.get(String.valueOf(stockCard.getId()));
        if (lotInfoList != null) {
            for (Map<String,String> lotInfo : lotInfoList){
                Lot lot = new Lot();
                LotOnHand lotOnHand = new LotOnHand();
                lotOnHand.setQuantityOnHand(Long.valueOf(lotInfo.get("quantityOnHand")));
                lot.setLotNumber(lotInfo.get("lotNumber"));
                lot.setExpirationDate(DateUtil.parseString(lotInfo.get("expirationDate"),DateUtil.DB_DATE_FORMAT));
                lotOnHand.setLot(lot);
                lotOnHands.add(lotOnHand);
            }
        }

        stockCard.setLotOnHandListWrapper(lotOnHands);
    }

    protected List<StockCard> getValidStockCardsForPhysicalInventory() throws LMISException {
        return from(stockRepository.list()).filter(stockCard -> {
            //TODO the result of filter will apply to inventory list
            return !stockCard.getProduct().isKit() && stockCard.getProduct().isActive() && !stockCard.getProduct().isArchived();
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

    protected StockMovementItem calculateAdjustment(InventoryViewModel model, StockCard stockCard) {
        long inventory = stockCard.getStockOnHand();
        long stockOnHand = model.getStockOnHand();
        StockMovementItem item = new StockMovementItem();
        item.setSignature(model.getSignature());
        item.setMovementDate(DateUtil.getCurrentDate());
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
        return Observable.create(subscriber -> {
            try {
                inventoryRepository.clearDraft();
                for (InventoryViewModel model : inventoryViewModelList) {
                    inventoryRepository.createDraft(new DraftInventory((PhysicalInventoryViewModel) model));
                }
                subscriber.onNext(null);
                subscriber.onCompleted();
            } catch (LMISException e) {
                subscriber.onError(e);
                new LMISException(e, "saveDraftInventoryObservable").reportToFabric();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


    public Observable<Object> doInventory(final String sign) {
        return Observable.create(subscriber -> {
            try {
                final Date latestStockMovementCreatedTime = movementRepository.getLatestStockMovementCreatedTime();
                if (DateUtil.getCurrentDate().before(latestStockMovementCreatedTime)) {
                    throw new LMISException(LMISApp.getContext().getString(R.string.msg_invalid_stock_movement));
                }
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
                sharedPreferenceMgr.setLatestPhysicInventoryTime(DateUtil.formatDate(DateUtil.getCurrentDate(), DateUtil.DATE_TIME_FORMAT));
                saveInventoryDate();

                subscriber.onNext(null);
                subscriber.onCompleted();
            } catch (LMISException e) {
                subscriber.onError(e);
                new LMISException(e, "doInventory").reportToFabric();
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
        }).toSortedList((lot1, lot2) -> {
            Date localDate = DateUtil.parseString(lot1.getExpiryDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR);
            if (localDate != null) {
                return localDate.compareTo(DateUtil.parseString(lot2.getExpiryDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR));
            } else {
                return 0;
            }
        });
        inventoryViewModel.setExistingLotMovementViewModelList(lotMovementViewModels);
    }

    public int getCompleteCount() {
        return FluentIterable.from(inventoryViewModelList)
                .filter(inventoryViewModel -> ((PhysicalInventoryViewModel) inventoryViewModel).isDone())
                .toList().size();
    }
}
