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

package org.openlmis.core.service;

import static org.openlmis.core.utils.DateUtil.calculateDateOffsetToNow;

import android.text.TextUtils;
import android.util.Log;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import org.apache.commons.collections.CollectionUtils;
import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;
import org.openlmis.core.LMISApp;
import org.openlmis.core.event.DeleteDirtyDataEvent;
import org.openlmis.core.event.InitialDirtyDataCheckEvent;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.DirtyDataItemInfo;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.CmmRepository;
import org.openlmis.core.model.repository.DirtyDataRepository;
import org.openlmis.core.model.repository.LotRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.model.repository.StockMovementRepository.SortClass;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.network.model.DirtyDataEntry;
import org.openlmis.core.network.model.LotOnHandEntry;
import org.openlmis.core.network.model.StockMovementEntry;
import org.openlmis.core.utils.ToastUtil;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import org.roboguice.shaded.goole.common.collect.ImmutableList;
import roboguice.RoboGuice;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


@Singleton
public class DirtyDataManager {

  private static final String TAG = DirtyDataManager.class.getSimpleName();

  // the newest two already checked.
  private static final int CHECK_NEWEST_TWO = 2;

  private static final boolean DEBUG_ALL_MOVEMENT = false;
  final Map<String, String> lotsOnHands = new HashMap<>();
  @Inject
  StockMovementRepository stockMovementRepository;
  @Inject
  StockRepository stockRepository;
  @Inject
  DirtyDataRepository dirtyDataRepository;
  @Inject
  RnrFormRepository rnrFormRepository;
  @Inject
  CmmRepository cmmRepository;
  @Inject
  LotRepository lotRepository;
  @Inject
  SharedPreferenceMgr sharedPreferenceMgr;
  Observer<Object> deleteDirtyDataSubscribe = new Observer<Object>() {
    @Override
    public void onCompleted() {
      // do nothing
    }

    @Override
    public void onError(Throwable e) {
      EventBus.getDefault().post(DeleteDirtyDataEvent.FINISH);
    }

    @Override
    public void onNext(Object o) {
      EventBus.getDefault().post(DeleteDirtyDataEvent.FINISH);
    }
  };
  private boolean isSyncedMonthCheck = false;

  public DirtyDataManager() {
    RoboGuice.getInjector(LMISApp.getContext()).injectMembersWithoutViews(this);
  }

  public List<StockCard> checkAndGetDirtyData() {
    return checkAndGetDirtyData(stockRepository.list(), stockRepository.lotOnHands());
  }

  public List<StockCard> checkAndGetDirtyData(
      List<StockCard> inputStockCards,
      Map<String, String> inputLotsOnHands
  ) {
    updateStockCardIdToLotOnHandsMap(inputLotsOnHands);
    return doCheckAndGetDirtyData(inputStockCards);
  }

  private List<StockCard> doCheckAndGetDirtyData(List<StockCard> stockCards) {
    List<StockCard> deletedStockCards = new ArrayList<>();
    if (sharedPreferenceMgr.shouldInitialDataCheck()) {
      return deletedStockCards;
    }
    sharedPreferenceMgr.setCheckDataDate(LMISApp.getInstance().getCurrentTimeMillis());
    List<StockCard> lastTwoMovementAndLotSOHWrong = checkTheLastTwoMovementAndLotSOH(stockCards);
    deletedStockCards.addAll(lastTwoMovementAndLotSOHWrong);
    saveFullyDeletedInfo(deletedStockCards);
    Set<String> productCodes = getCodeFromStockCard(deletedStockCards);
    if (!productCodes.isEmpty()) {
      sharedPreferenceMgr.setDeletedProduct(productCodes);
    } else {
      clearDeletedProductFromSP();
    }
    return deletedStockCards;
  }

  @SuppressWarnings("squid:S1905")
  public void dirtyDataMonthlyCheck() {
    Observable.create((OnSubscribe<Void>) subscriber -> {
      try {
        scanAllStockMovements();
      } catch (LMISException e) {
        e.reportToFabric();
        subscriber.onError(e);
      }
      subscriber.onCompleted();
    }).observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe(new Subscriber<Void>() {
          @Override
          public void onCompleted() {
            EventBus.getDefault().post(new InitialDirtyDataCheckEvent(false,
                !sharedPreferenceMgr.getDeletedProduct().isEmpty()));
            Log.i("dirty data", "monthly check finished");
          }

          @Override
          public void onError(Throwable e) {
            ToastUtil.show(e.getMessage());
          }

          @Override
          public void onNext(Void aVoid) {
            // do nothing
          }
        });
  }

  public void initialDirtyDataCheck() throws LMISException {
    if (sharedPreferenceMgr.shouldInitialDataCheck()
        && !sharedPreferenceMgr.shouldSyncLastYearStockData()
        && !sharedPreferenceMgr.isSyncingLastYearStockCards()) {
      sharedPreferenceMgr.setKeyIsInitialDirtyDataChecking(true);
      EventBus.getDefault().post(new InitialDirtyDataCheckEvent(true, false));
      Log.i("check", "start");
      Set<String> deleteProducts = checkSoh();
      Log.i("check", "end");
      saveToSharePreferenceMgr(deleteProducts);
      sharedPreferenceMgr.setKeyIsInitialDirtyDataChecking(false);
      sharedPreferenceMgr.setShouldInitialDirtyDataCheck(false);
      dirtyDataMonthlyCheck();
    }
  }

  private void saveToSharePreferenceMgr(Set<String> deleteProducts) {
    if (!deleteProducts.isEmpty()) {
      sharedPreferenceMgr.setDeletedProduct(deleteProducts);
    } else {
      clearDeletedProductFromSP();
    }
  }

  /**
   * It will check `StockCard` only 1. stockOnHand < 0 2. stockOnHand != the sum of matched lots stockOnHand
   * {@code org.openlmis.core.model.StockCard#calculateSOHFromLots(java.util.Map)}
   *
   * @return productCodes
   */
  private Set<String> checkSoh() throws LMISException {
    initialStockCardIdToLotOnHandsMap();
    List<StockCard> checkedStockCards = stockRepository.queryCheckedStockCards();
    Set<String> deleteStockCardIds = new HashSet<>();
    List<String> cardIdsLotOnHandLessZero = stockRepository.cardIdsIfLotOnHandLessZero();
    for (StockCard stockCard : checkedStockCards) {
      if (!isPositiveOnHand(stockCard, cardIdsLotOnHandLessZero)
          || stockCard.calculateSOHFromLots(lotsOnHands) != stockCard.getStockOnHand()) {
        deleteStockCardIds.add(String.valueOf(stockCard.getId()));
        // report the dirty data to AppCentre
        reportDirtyDataByStockOnHandError(stockCard);
      }
    }
    if (!deleteStockCardIds.isEmpty()) {
      Map<String, List<StockMovementItem>> idToStockItemsForDelete = stockMovementRepository
          .queryStockMovement(deleteStockCardIds, null, null);

      saveFullyDeletedInfoByStockCardIds(deleteStockCardIds);

      return covertMapFromStockIdToProductCode(idToStockItemsForDelete);
    }
    return new HashSet<>();
  }

  private void saveFullyDeletedInfoByStockCardIds(Set<String> deleteStockCardIds) throws LMISException {
    ImmutableList<Long> stockCardIds = FluentIterable
        .from(deleteStockCardIds)
        .filter(id -> id != null)
        .transform(Long::valueOf)
        .toList();
    saveFullyDeletedInfo(stockRepository.listStockCardsByIds(stockCardIds));
  }

  private void scanAllStockMovements() throws LMISException {
    if (sharedPreferenceMgr.shouldSyncLastYearStockData()
        || sharedPreferenceMgr.isSyncingLastYearStockCards()
        || sharedPreferenceMgr.shouldInitialDataCheck()
        || isSyncedMonthCheck) {
      return;
    }
    DateTime recordLastDirtyDataCheck = SharedPreferenceMgr.getInstance().getLatestMonthlyCheckDirtyDataTime();
    if (calculateDateOffsetToNow(recordLastDirtyDataCheck) >= 30) {
      isSyncedMonthCheck = true;
      sharedPreferenceMgr.setCheckDataDate(LMISApp.getInstance().getCurrentTimeMillis());
      Set<String> deleteProducts = checkAllMovementAndLotSOHAndSaveToDB();
      saveToSharePreferenceMgr(deleteProducts);
      isSyncedMonthCheck = false;
      sharedPreferenceMgr.updateLatestMonthlyCheckDirtyDataTime();
    }
  }

  private Set<String> getCodeFromStockCard(List<StockCard> stockCardList) {
    return FluentIterable.from(stockCardList).transform(new Function<StockCard, String>() {
      @Nullable
      @Override
      public String apply(@Nullable StockCard stockCard) {
        return Objects.requireNonNull(stockCard).getProduct().getCode();
      }
    }).toSet();
  }

  private void saveFullyDeletedInfo(List<StockCard> deletedStockCards) {
    Map<String, List<StockMovementItem>> codeToStockItems = new HashMap<>();
    Map<String, List<LotOnHand>> codeToLotOnHands = new HashMap<>();
    Map<String, Long> codeToStockOnHand = new HashMap<>();
    for (StockCard stockCard : deletedStockCards) {
      List<StockMovementItem> stockMovementItems = null;
      List<LotOnHand> lotOnHands = null;
      try {
        long id = stockCard.getId();

        stockMovementItems = stockMovementRepository.queryMovementByStockCardId(id);
        lotOnHands = stockRepository.getLotOnHandByStockCard(id);
      } catch (LMISException e) {
        Log.w(TAG, e);
        e.reportToFabric();
      }

      String code = stockCard.getProduct().getCode();
      codeToStockItems.put(code, stockMovementItems);
      codeToLotOnHands.put(code, lotOnHands);
      codeToStockOnHand.put(code, stockCard.getStockOnHand());
    }
    saveDeletedMovementToDB(codeToStockItems, codeToLotOnHands, codeToStockOnHand, true);
  }

  private void saveDeletedMovementToDB(
      Map<String, List<StockMovementItem>> items,
      Map<String, List<LotOnHand>> codeToLotOnHands,
      Map<String, Long> codeToStockOnHand,
      boolean fullyDelete
  ) {
    if (items.isEmpty()) {
      return;
    }
    final String facilityCode = sharedPreferenceMgr.getUserFacilityCode();
    if (TextUtils.isEmpty(facilityCode)) {
      return;
    }
    List<DirtyDataItemInfo> dirtyDataItemInfos = new ArrayList<>();
    for (Entry<String, List<StockMovementItem>> entry : items.entrySet()) {
      String key = entry.getKey();

      dirtyDataItemInfos.add(
          convertStockMovementItemsToStockMovementEntriesForSave(
              entry.getValue(), key, fullyDelete,
              codeToLotOnHands.get(key),
              codeToStockOnHand.get(key)
          )
      );
    }
    dirtyDataRepository.saveAll(dirtyDataItemInfos);
  }

  public void deleteAndReset() {
    EventBus.getDefault().post(DeleteDirtyDataEvent.START);
    getDeleteDirtyDataObservable()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(deleteDirtyDataSubscribe);
  }

  private Observable<Object> getDeleteDirtyDataObservable() {
    return Observable.create(subscriber -> {
      List<StockMovementItem> deletedStockMovementItems = sharedPreferenceMgr.getDeletedMovementItems();
      Map<String, List<StockMovementItem>> keepStockMovementItemsMap = sharedPreferenceMgr.getKeepMovementItemsMap();
      List<String> productCodes = sharedPreferenceMgr.getDeletedProduct();
      if (!deletedStockMovementItems.isEmpty() || !keepStockMovementItemsMap.isEmpty() || !productCodes.isEmpty()) {
        dirtyDataRepository.deleteDraftForDirtyData();
      }
      if (!deletedStockMovementItems.isEmpty()) {
        stockMovementRepository.deleteStockMovementItems(deletedStockMovementItems);
        lotRepository.deleteLotMovementItems(deletedStockMovementItems);
        sharedPreferenceMgr.setDeletedMovementItems(new ArrayList<>());
      }
      if (!keepStockMovementItemsMap.isEmpty()) {
        List<LotMovementItem> lotMovementItems = lotRepository
            .resetKeepLotMovementItems(keepStockMovementItemsMap);
        stockRepository.resetKeepLotsOnHand(lotMovementItems, keepStockMovementItemsMap);
        stockMovementRepository.resetKeepItemToNotSynced(keepStockMovementItemsMap);
        sharedPreferenceMgr.setKeepMovementItemsMap(new HashMap<>());
      }
      if (!productCodes.isEmpty()) {
        stockRepository.deleteStockMovementsForDirtyData(productCodes);
        try {
          stockRepository.insertNewInventory(productCodes);
        } catch (LMISException e) {
          Log.w(TAG, e);
          e.reportToFabric();
        }
        stockRepository.resetStockCard(productCodes);
        stockRepository.resetLotsOnHand(productCodes);
        cmmRepository.resetCmm(productCodes);
        rnrFormRepository.deleteRnrFormDirtyData(productCodes);
        clearDeletedProductFromSP();
      }
      subscriber.onNext(null);
      subscriber.onCompleted();
    });
  }

  private void clearDeletedProductFromSP() {
    sharedPreferenceMgr.setDeletedProduct(new HashSet<>());
  }

  private DirtyDataItemInfo convertStockMovementItemsToStockMovementEntriesForSave(
      List<StockMovementItem> stockMovementItems, String productCode, boolean fullyDelete,
      List<LotOnHand> lotOnHands, Long stockOnHand) {
    Product product = Product.builder().code(productCode).build();
    List<StockMovementEntry> movementEntries = FluentIterable.from(stockMovementItems)
        .transform(stockMovementItem -> {
          Objects.requireNonNull(stockMovementItem).getStockCard().setProduct(product);
          return stockMovementItem;
        })
        .transform(StockMovementEntry::new)
        .toList();

    List<LotOnHandEntry> lotOnHandEntries = FluentIterable.from(lotOnHands)
        .filter(lotOnHand -> lotOnHand != null)
        .transform(LotOnHandEntry::new)
        .toList();

    DirtyDataEntry dirtyDataEntry = new DirtyDataEntry(movementEntries, lotOnHandEntries);

    return new DirtyDataItemInfo(
        productCode, false, new GsonBuilder().create().toJson(dirtyDataEntry), fullyDelete, stockOnHand
    );
  }

  /**
   * It will check `StockCard` and `StockMovements` 1. stockOnHand < 0 2. stockOnHand != the sum of matched lots
   * stockOnHand 3. last two stock_movements data is incorrect stock_movements.A.stockOnHand + movementQuantity !=
   * stock_movements.B.stockOnHand
   *
   * @param stockCards stock list
   * @return dirty data - map of `StockCard` to `StockMovementItem` list
   */
  private List<StockCard> checkTheLastTwoMovementAndLotSOH(
      List<StockCard> stockCards
  ) {
    List<StockCard> deleted = new ArrayList<>();
    HashMap<Long, List<StockMovementItem>> stockMovementItemsMap = getLastStockMovementMap();
    List<String> cardIdsLotOnHandLessZero = stockRepository.cardIdsIfLotOnHandLessZero();
    Map<String, List<StockMovementItem>> keepMovementItemsMap = sharedPreferenceMgr.getKeepMovementItemsMap();
    Set<String> keepStockCardIds =
        keepMovementItemsMap.isEmpty() ? new HashSet<>() : keepMovementItemsMap.keySet();
    for (StockCard stockCard : stockCards) {
      if (keepStockCardIds.contains(String.valueOf(stockCard.getId()))) {
        continue;
      }
      List<StockMovementItem> stockMovementItems = stockMovementItemsMap.get(stockCard.getId());
      if (!isPositiveOnHand(stockCard, cardIdsLotOnHandLessZero)) {
        deleted.add(stockCard);
        // report the dirty data to AppCentre
        reportDirtyDataByStockOnHandError(stockCard);
      } else if (stockMovementItems != null && stockMovementItems.size() == CHECK_NEWEST_TWO) {
        SortClass sort = new SortClass();
        Collections.sort(stockMovementItems, sort);
        StockMovementItem currentStockMovement = stockMovementItems.get(1);
        StockMovementItem preStockMovement = stockMovementItems.get(0);
        if (!isCorrectMovements(preStockMovement, currentStockMovement)
            || !isCorrectSOHBetweenMovementAndStockCard(stockCard, currentStockMovement)) {
          deleted.add(stockCard);
          // report the dirty data to AppCentre
          reportDirtyDataByStockMovementError(stockCard);
        }
      }
    }
    Log.i("dirty", "daily" + deleted);

    return deleted;
  }

  private HashMap<Long, List<StockMovementItem>> getLastStockMovementMap() {
    List<StockMovementItem> stockMovements = stockMovementRepository.listLastTwoStockMovements();
    HashMap<Long, List<StockMovementItem>> stockMovementItemsMap = new HashMap<>();
    for (StockMovementItem item : stockMovements) {
      long id = item.getStockCard().getId();
      if (!stockMovementItemsMap.containsKey(id)) {
        List<StockMovementItem> list = new ArrayList<>();
        list.add(item);

        stockMovementItemsMap.put(id, list);
      } else {
        List<StockMovementItem> stockMovementItems = stockMovementItemsMap.get(id);
        if (stockMovementItems != null) {
          stockMovementItems.add(item);
        }

      }
    }
    return stockMovementItemsMap;
  }

  /**
   * It will check `StockCard` and `StockMovements` 1. stockOnHand < 0 2. stockOnHand != the sum of matched lots
   * stockOnHand 3. last two stock_movements data is incorrect stock_movements.A.stockOnHand + movementQuantity !=
   * stock_movements.B.stockOnHand
   *
   * @return productCodes
   */
  @SuppressWarnings({"squid:S3776", "squid:S135"})
  private Set<String> checkAllMovementAndLotSOHAndSaveToDB() throws LMISException {
    List<StockCard> checkedStockCards = stockRepository.queryCheckedStockCards();
    initialStockCardIdToLotOnHandsMap();
    Map<String, List<StockMovementItem>> idToStockItemForDelete = new HashMap<>();
    List<String> cardIdsLotOnHandLessZero = stockRepository.cardIdsIfLotOnHandLessZero();
    for (StockCard stockCard : checkedStockCards) {
      try {
        List<StockMovementItem> stockMovementItems = stockMovementRepository
            .queryMovementByStockCardId(stockCard.getId());
        if (CollectionUtils.isEmpty(stockMovementItems)) {
          continue;
        }
        if (isCorrectStockOnHand(cardIdsLotOnHandLessZero, stockCard, stockMovementItems)) {
          idToStockItemForDelete.put(String.valueOf(stockCard.getId()), stockMovementItems);
          // report the dirty data to AppCentre
          reportDirtyDataByStockOnHandError(stockCard);
          continue;
        }
        if (stockMovementItems.size() >= 2) {
          for (int i = 0; i <= stockMovementItems.size() - 2; i++) {
            if (!isCorrectMovements(stockMovementItems.get(i), stockMovementItems.get(i + 1))) {
              debugLog(stockMovementItems.get(i), stockMovementItems.get(i + 1), stockCard);
              idToStockItemForDelete.put(String.valueOf(stockCard.getId()), stockMovementItems);
              // report the dirty data to AppCentre
              reportDirtyDataByStockMovementError(stockCard);
              break;
            }
          }
        }
      } catch (LMISException e) {
        Log.w(TAG, e);
        e.reportToFabric();
      }
    }

    saveFullyDeletedInfoByStockCardIds(idToStockItemForDelete.keySet());

    return covertMapFromStockIdToProductCode(idToStockItemForDelete);
  }

  private void initialStockCardIdToLotOnHandsMap() {
    updateStockCardIdToLotOnHandsMap(stockRepository.lotOnHands());
  }

  private void updateStockCardIdToLotOnHandsMap(Map<String, String> inputLotsOnHands) {
    lotsOnHands.clear();
    lotsOnHands.putAll(inputLotsOnHands);
  }

  private void reportDirtyDataByStockMovementError(
      StockCard stockCard
  ) {
    reportDirtyData("StockMovement", stockCard);
  }

  private void reportDirtyDataByStockOnHandError(
      StockCard stockCard
  ) {
    reportDirtyData("StockOnHand", stockCard);
  }

  private void reportDirtyData(
      String type,
      StockCard stockCard
  ) {
    Product product = stockCard.getProduct();
    // product code
    String productCode = product == null ? "" : product.getCode();
    // report - most 125 characters in error message, so we don't report movement items
    new LMISException("dirty data, type=" + type
        + "\nproductCode=" + productCode
        + "\nSOH=" + stockCard.getStockOnHand()
        + ", calculatedSOH=" + stockCard.calculateSOHFromLots(lotsOnHands))
        .reportToFabric();
  }

  private Set<String> covertMapFromStockIdToProductCode(
      Map<String, List<StockMovementItem>> idToStockItemForDelete
  ) {
    if (!idToStockItemForDelete.isEmpty()) {
      Set<String> stockCardIds = idToStockItemForDelete.keySet();

      Map<String, String> stockCardIdToCode = stockMovementRepository
          .queryStockCardIdAndProductCode(stockCardIds);
      Map<String, List<StockMovementItem>> codeToStockItems = new HashMap<>();
      for (Map.Entry<String, List<StockMovementItem>> entry : idToStockItemForDelete.entrySet()) {
        codeToStockItems.put(stockCardIdToCode.get(entry.getKey()), entry.getValue());
      }

      return codeToStockItems.keySet();
    }
    return new HashSet<>();
  }

  private boolean isCorrectStockOnHand(List<String> cardIdsLotOnHandLessZero, StockCard stockCard,
      List<StockMovementItem> stockMovementItems) {
    return !isPositiveOnHand(stockCard, cardIdsLotOnHandLessZero)
        || !isCorrectSOHBetweenMovementAndStockCard(stockCard,
        stockMovementItems.get(stockMovementItems.size() - 1));
  }

  private void debugLog(StockMovementItem previousMovement, StockMovementItem currentMovement,
      StockCard stockCard) {
    if (!DEBUG_ALL_MOVEMENT) {
      return;
    }
    Log.e(TAG, stockCard.getProduct().getCode()
        + "(" + stockCard.calculateSOHFromLots(lotsOnHands) + ")"
        + ":previous id=" + previousMovement.getId()
        + ",current id =" + currentMovement.getId()
        + ";previousSOH = " + previousMovement.getStockOnHand()
        + (currentMovement.isNegativeMovement() ? ",-" : ",+")
        + ",movementQuantity=" + currentMovement.getMovementQuantity()
        + ",currentSOH=" + currentMovement.getStockOnHand());
  }

  private boolean isPositiveOnHand(StockCard stockCard, List<String> cardIds) {
    return stockCard.getStockOnHand() >= 0 && !cardIds.contains(String.valueOf(stockCard.getId()));
  }

  /**
   * Check if the `StockCard`.stockOnHand == last one `StockMovementItem`.stockOnHand
   *
   * @param stockCard      product information - stockOnHand etc.
   * @param newestMovement last one stockMovementItem
   * @return `StockCard`.stockOnHand == last one `StockMovementItem`.stockOnHand
   */
  private boolean isCorrectSOHBetweenMovementAndStockCard(StockCard stockCard,
      StockMovementItem newestMovement) {
    return stockCard.calculateSOHFromLots(lotsOnHands) == newestMovement.getStockOnHand();
  }

  /**
   * Check if the last two `StockMovementItem`.stockOnHand is valid eg. `StockMovementItem`A is the last one, and
   * `StockMovementItem`B is the penult two. `StockMovementItem`A.stockOnHand == `StockMovementItem`B.stockOnHand +
   * `StockMovementItem`B.movementQuantity
   *
   * @param previousMovement the penult StockMovementItem
   * @param newestMovement   last one StockMovementItem
   * @return A.stockOnHand == B.stockOnHand + B.movementQuantity
   */
  private boolean isCorrectMovements(StockMovementItem previousMovement,
      StockMovementItem newestMovement) {
    return checkFormula(newestMovement, newestMovement.getStockOnHand(),
        previousMovement.getStockOnHand(), newestMovement.getMovementQuantity());
  }

  private boolean checkFormula(StockMovementItem newestMovement, Long currentSOH,
      Long previousSOH, Long currentQuantity) {
    if (newestMovement.isNegativeMovement()) {
      return currentSOH == previousSOH - currentQuantity;
    } else if (newestMovement.isPositiveMovement()) {
      return currentSOH == previousSOH + currentQuantity;
    } else {
      return (currentSOH == previousSOH - currentQuantity)
          || (currentSOH == previousSOH + currentQuantity);
    }
  }

}
