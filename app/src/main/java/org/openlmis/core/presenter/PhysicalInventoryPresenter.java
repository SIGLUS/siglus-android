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

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.viewmodel.PhysicalInventoryViewModel;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PhysicalInventoryPresenter extends InventoryPresenter {

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
        sharedPreferenceMgr.setLatestPhysicInventoryTime(
            DateUtil.formatDate(DateUtil.getCurrentDate(), DateUtil.DATE_TIME_FORMAT));
        saveInventoryDate();

        subscriber.onNext(null);
        subscriber.onCompleted();
      } catch (LMISException e) {
        subscriber.onError(e);
        new LMISException(e, "doInventory").reportToFabric();
      }
    }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
  }

  protected List<StockCard> getValidStockCardsForPhysicalInventory() {
    //TODO the result of filter will apply to inventory list
    return from(stockRepository.list())
        .filter(stockCard -> !stockCard.getProduct().isKit()
            && stockCard.getProduct().isActive()
            && !stockCard.getProduct().isArchived())
        .toList();
  }

  protected void restoreDraftInventory() throws LMISException {
    List<DraftInventory> draftList = inventoryRepository.queryAllDraft();

    for (DraftInventory draftInventory : draftList) { // total : N
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
    item.setMovementType(MovementReasonManager.MovementType.PHYSICAL_INVENTORY);
    if (inventory > stockOnHand) {
      item.setReason(MovementReasonManager.INVENTORY_POSITIVE);
    } else if (inventory < stockOnHand) {
      item.setReason(MovementReasonManager.INVENTORY_NEGATIVE);
    } else {
      item.setReason(MovementReasonManager.INVENTORY);
    }
    setLotMovementReason(model);
    item.populateLotAndResetStockOnHandOfLotAccordingPhysicalAdjustment(
        model.getExistingLotMovementViewModelList(), model.getNewLotMovementViewModelList());
    return item;
  }

  private void setLotMovementReason(InventoryViewModel model) {
    for (LotMovementViewModel lotMovementViewModel : model.getExistingLotMovementViewModelList()) {
      long quantity = Long.parseLong(lotMovementViewModel.getQuantity());
      long lotSoh = Long.parseLong(lotMovementViewModel.getLotSoh());
      if (quantity > lotSoh) {
        lotMovementViewModel.setMovementReason(MovementReasonManager.INVENTORY_POSITIVE);
      } else if (quantity < lotSoh) {
        lotMovementViewModel.setMovementReason(MovementReasonManager.INVENTORY_NEGATIVE);
      } else {
        lotMovementViewModel.setMovementReason(MovementReasonManager.INVENTORY);
      }
    }
    for (LotMovementViewModel lotMovementViewModel : model.getNewLotMovementViewModelList()) {
      lotMovementViewModel.setMovementReason(MovementReasonManager.INVENTORY_POSITIVE);
    }
  }

  private List<InventoryViewModel> convertStockCardsToStockCardViewModels(
      List<StockCard> validStockCardsForPhysicalInventory) {
    Map<String, List<Map<String, String>>> lotInfoMap = stockRepository.getLotsAndLotOnHandInfo();
    Map<String, String> lotOnHands = stockRepository.lotOnHands();
    return FluentIterable.from(validStockCardsForPhysicalInventory).transform(stockCard -> {
      addLotInfoToStockCard(stockCard, lotInfoMap);
      InventoryViewModel inventoryViewModel = new PhysicalInventoryViewModel(stockCard, lotOnHands);
      setExistingLotViewModels(inventoryViewModel);
      return inventoryViewModel;
    }).toList();
  }

  private void addLotInfoToStockCard(StockCard stockCard,
      Map<String, List<Map<String, String>>> lotInfoMap) {
    List<LotOnHand> lotOnHands = new ArrayList<>();
    List<Map<String, String>> lotInfoList = lotInfoMap.get(String.valueOf(stockCard.getId()));
    if (lotInfoList != null) {
      for (Map<String, String> lotInfo : lotInfoList) {
        Lot lot = new Lot();
        LotOnHand lotOnHand = new LotOnHand();
        lotOnHand.setQuantityOnHand(Long.valueOf(lotInfo.get("quantityOnHand")));
        lot.setLotNumber(lotInfo.get("lotNumber"));
        lot.setExpirationDate(
            DateUtil.parseString(lotInfo.get("expirationDate"), DateUtil.DB_DATE_FORMAT));
        lotOnHand.setLot(lot);
        lotOnHands.add(lotOnHand);
      }
    }

    stockCard.setLotOnHandListWrapper(lotOnHands);
  }

  private void saveInventoryDate() {
    inventoryRepository.save(new Inventory());
  }

  private void setExistingLotViewModels(InventoryViewModel inventoryViewModel) {
    List<LotMovementViewModel> lotMovementViewModels = FluentIterable
        .from(inventoryViewModel.getStockCard().getNonEmptyLotOnHandList())
        .transform(lotOnHand -> new LotMovementViewModel(lotOnHand.getLot().getLotNumber(),
            DateUtil.formatDate(lotOnHand.getLot().getExpirationDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR),
            lotOnHand.getQuantityOnHand().toString(), MovementReasonManager.MovementType.RECEIVE))
        .toSortedList((lot1, lot2) -> {
          Date localDate = DateUtil
              .parseString(lot1.getExpiryDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR);
          if (localDate != null) {
            return localDate
                .compareTo(DateUtil.parseString(lot2.getExpiryDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR));
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
