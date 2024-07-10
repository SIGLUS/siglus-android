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

import static org.openlmis.core.manager.MovementReasonManager.INVENTORY;
import static org.openlmis.core.utils.DateUtil.calculateDateOffsetToNow;

import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.DirtyDataItemInfo;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.CmmRepository;
import org.openlmis.core.model.repository.DirtyDataRepository;
import org.openlmis.core.model.repository.LotRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.network.model.StockMovementEntry;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.ToastUtil;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import roboguice.RoboGuice;
import rx.Observable;
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
    lotsOnHands.putAll(inputLotsOnHands);
    return doCheckAndGetDirtyData(inputStockCards);
  }

  private List<StockCard> doCheckAndGetDirtyData(List<StockCard> stockCards) {
    List<StockCard> dirtyDataOfStockCards = new ArrayList<>();
    if (sharedPreferenceMgr.shouldInitialDataCheck()) {
      return dirtyDataOfStockCards;
    }
    // update the check data date
    sharedPreferenceMgr.setCheckDataDate(LMISApp.getInstance().getCurrentTimeMillis());
    // check and cache the dirty data
    List<String> cardIdsLotOnHandLessZero = stockRepository.cardIdsIfLotOnHandLessZero();
    HashMap<StockCard, List<StockMovementItem>> dirtyDataOfStockCardToLastTwoStockMovementItems =
        checkTheLastTwoMovementAndLotSOH(stockCards, cardIdsLotOnHandLessZero);
//    correctAndSaveDirtyData(dirtyDataOfStockCardToLastTwoStockMovementItems,
//        cardIdsLotOnHandLessZero);
    // save wrong product codes
    dirtyDataOfStockCards.addAll(dirtyDataOfStockCardToLastTwoStockMovementItems.keySet());
    Set<String> productCodes = getCodeFromStockCard(dirtyDataOfStockCards);
    if (!productCodes.isEmpty()) {
      sharedPreferenceMgr.setDeletedProduct(productCodes);
    }

    return dirtyDataOfStockCards;
  }

  /**
   * correct dirty data when `StockCard`.stockOnHand < 0
   * 1. set `StockCard`.stockOnHand = 0
   * 2. create or update last one `StockMovementItem`
   *
   * @param stockCard stockCard
   * @param lastTwoStockMovementItems last two stock movement items
   * @throws LMISException db error
   */
  private void correctStockCardWhenStockOnHandIsLessThan0(
      StockCard stockCard,
      @Nullable List<StockMovementItem> lastTwoStockMovementItems
  ) throws LMISException {
    // update latestStockMovementItem and stockCard
    StockMovementItem latestStockMovementItem;

    if (lastTwoStockMovementItems != null && !lastTwoStockMovementItems.isEmpty()) {
      int dirtyDataOfStockMovementItemsSize = lastTwoStockMovementItems.size();
      if (dirtyDataOfStockMovementItemsSize == 1) {
        latestStockMovementItem = correctLastOneMovementItemWhenOnlyOneItem(lastTwoStockMovementItems);
      } else {
        latestStockMovementItem = correctLastOneMovementItemWhenHaveLeastTwoItems(lastTwoStockMovementItems);
      }
    } else {
      latestStockMovementItem = createNewInventoryStockMovementItem(stockCard);
    }
    // save latestStockMovementItem into db
    stockMovementRepository.createOrUpdateStockMovementAndLotMovementItems(latestStockMovementItem);
    stockCard.setStockOnHand(latestStockMovementItem.getStockOnHand());
    // save stockCard into db
    stockRepository.createOrUpdate(stockCard);
  }

  @NonNull
  private StockMovementItem correctLastOneMovementItemWhenHaveLeastTwoItems(
      @NonNull List<StockMovementItem> lastTwoStockMovementItems) {
    StockMovementItem latestStockMovementItem;
    StockMovementItem previousStockMovementItem = lastTwoStockMovementItems.get(0);
    latestStockMovementItem = lastTwoStockMovementItems.get(1);

    if (!isCorrectMovements(previousStockMovementItem, latestStockMovementItem)) {
      if (latestStockMovementItem.isNegativeMovement()) {
        long diff = previousStockMovementItem.getStockOnHand()
            - latestStockMovementItem.getMovementQuantity();
        if (diff >= 0) {
          latestStockMovementItem.setStockOnHand(diff);
        } else {
          latestStockMovementItem.setStockOnHand(0L);
        }
      } else if (latestStockMovementItem.isPositiveMovement()) {
        long diff = previousStockMovementItem.getStockOnHand()
            + latestStockMovementItem.getMovementQuantity();
        if (diff >= 0) {
          latestStockMovementItem.setStockOnHand(diff);
        } else {
          latestStockMovementItem.setStockOnHand(0L);
        }
      }
    }

    long stockOnHand = latestStockMovementItem.getStockOnHand();
    if (stockOnHand < 0) {
      latestStockMovementItem.setStockOnHand(0L);
    }
    return latestStockMovementItem;
  }

  @NonNull
  private static StockMovementItem correctLastOneMovementItemWhenOnlyOneItem(
      @NonNull List<StockMovementItem> lastTwoStockMovementItems) {
    StockMovementItem latestStockMovementItem;
    latestStockMovementItem = lastTwoStockMovementItems.get(0);

    long stockOnHand = latestStockMovementItem.getStockOnHand();
    if (stockOnHand < 0) {
      latestStockMovementItem.setStockOnHand(0L);
    }
    return latestStockMovementItem;
  }

  @NonNull
  private StockMovementItem createNewInventoryStockMovementItem(StockCard stockCard) {
    StockMovementItem newStockMovementItem = new StockMovementItem();
    Date now = DateUtil.getCurrentDate();
    newStockMovementItem.setCreatedTime(now);
    newStockMovementItem.setStockOnHand(0);
    newStockMovementItem.setMovementDate(now);
    newStockMovementItem.setMovementType(
        MovementReasonManager.MovementType.PHYSICAL_INVENTORY
    );
    newStockMovementItem.setReason(INVENTORY);
    newStockMovementItem.setRequested(null);
    newStockMovementItem.setSignature(null);
    newStockMovementItem.setSynced(false);
    newStockMovementItem.setCreatedAt(now);
    newStockMovementItem.setUpdatedAt(now);
    newStockMovementItem.setStockCard(stockCard);
    newStockMovementItem.setMovementQuantity(0);

    return newStockMovementItem;
  }

  @SuppressWarnings("squid:S1905")
  public void dirtyDataMonthlyCheck() {
    Observable.create((Observable.OnSubscribe<Void>) subscriber -> {
          scanAllStockMovements();
          subscriber.onCompleted();
        }).observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe(new Subscriber<Void>() {
          @Override
          public void onCompleted() {
            EventBus.getDefault().post(new InitialDirtyDataCheckEvent(false,
                !sharedPreferenceMgr.getDeletedProduct().isEmpty()));
            Log.d("dirty data", "monthly check finished");
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

  public void initialDirtyDataCheck() {
    if (sharedPreferenceMgr.shouldInitialDataCheck()
        && !sharedPreferenceMgr.shouldSyncLastYearStockData()
        && !sharedPreferenceMgr.isSyncingLastYearStockCards()) {
      sharedPreferenceMgr.setKeyIsInitialDirtyDataChecking(true);
      EventBus.getDefault().post(new InitialDirtyDataCheckEvent(true, false));
      Log.d("check", "start");
      Set<String> deleteProducts = checkSoh();
      Log.d("check", "end");
      saveToSharePreferenceMgr(deleteProducts);
      sharedPreferenceMgr.setKeyIsInitialDirtyDataChecking(false);
      sharedPreferenceMgr.setShouldInitialDirtyDataCheck(false);
      dirtyDataMonthlyCheck();
    }
  }

  private void saveToSharePreferenceMgr(Set<String> deleteProducts) {
    if (!deleteProducts.isEmpty()) {
      sharedPreferenceMgr.setDeletedProduct(deleteProducts);
    }
  }

  /**
   * It will check `StockCard` only 1. stockOnHand < 0 2. stockOnHand != the sum of matched lots
   * stockOnHand {@code org.openlmis.core.model.StockCard#calculateSOHFromLots(java.util.Map)}
   *
   * @return productCodes
   */
  private Set<String> checkSoh() {
    lotsOnHands.putAll(stockRepository.lotOnHands());
    List<StockCard> checkedStockCards = stockRepository.queryCheckedStockCards();
    Set<String> deleteStockCardIds = new HashSet<>();
    List<String> cardIdsLotOnHandLessZero = stockRepository.cardIdsIfLotOnHandLessZero();
    for (StockCard stockCard : checkedStockCards) {
      if (!isPositiveOnHand(stockCard, cardIdsLotOnHandLessZero)
          || stockCard.calculateSOHFromLots(lotsOnHands) != stockCard.getStockOnHand()) {
        deleteStockCardIds.add(String.valueOf(stockCard.getId()));
      }
    }
    if (!deleteStockCardIds.isEmpty()) {
      Map<String, List<StockMovementItem>> idToStockItemsForDelete = stockMovementRepository
          .queryStockMovement(deleteStockCardIds, null, null);
      return covertMapFromStockIdToProductCode(idToStockItemsForDelete);
    }
    return new HashSet<>();
  }

  private void scanAllStockMovements() {
    if (sharedPreferenceMgr.shouldSyncLastYearStockData()
        || sharedPreferenceMgr.isSyncingLastYearStockCards()
        || sharedPreferenceMgr.shouldInitialDataCheck()
        || isSyncedMonthCheck) {
      return;
    }
    DateTime recordLastDirtyDataCheck = SharedPreferenceMgr.getInstance()
        .getLatestMonthlyCheckDirtyDataTime();
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

  private void saveFullyDeletedInfo(
      HashMap<StockCard, List<StockMovementItem>> wrongStockCardToLastTwoStockMovementItems
  ) {
    Map<String, List<StockMovementItem>> codeToStockItems = new HashMap<>();
    for (StockCard stockCard : wrongStockCardToLastTwoStockMovementItems.keySet()) {
      if (stockCard != null) {
        codeToStockItems.put(
            stockCard.getProduct().getCode(),
            wrongStockCardToLastTwoStockMovementItems.get(stockCard)
        );
      }
    }
    saveDeletedMovementToDB(codeToStockItems, true);
  }

  private void saveDeletedMovementToDB(Map<String, List<StockMovementItem>> items,
      boolean fullyDelete) {
    if (items.isEmpty()) {
      return;
    }
    final String facilityCode = sharedPreferenceMgr.getUserFacilityCode();
    if (TextUtils.isEmpty(facilityCode)) {
      return;
    }
    List<DirtyDataItemInfo> dirtyDataItemInfos = new ArrayList<>();
    for (Map.Entry<String, List<StockMovementItem>> entry : items.entrySet()) {
      dirtyDataItemInfos.add(
          convertStockMovementItemsToStockMovementEntriesForSave(entry.getValue(),
              entry.getKey(), fullyDelete));
    }
    dirtyDataRepository.saveAll(dirtyDataItemInfos);
  }

  public void correctDirtyData() {
    EventBus.getDefault().post(DeleteDirtyDataEvent.START);
    getCorrectDirtyDataObservable().subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(deleteDirtyDataSubscribe);
  }

  private Observable<Void> getCorrectDirtyDataObservable() {
    return Observable.create(subscriber -> {
        try {
          List<String> productCodes = sharedPreferenceMgr.getDeletedProduct();
          if (!productCodes.isEmpty()) {

            correctAndSaveDirtyData(
                stockRepository.queryStockCardsByProductCodes(productCodes),
                stockRepository.cardIdsIfLotOnHandLessZero()
            );
            // TODO: to be deleted
            stockRepository.deleteStockMovementsForDirtyData(productCodes);
            try {
              stockRepository.insertNewInventory(productCodes);
            } catch (LMISException e) {
              Log.w(TAG, e);
            }
            stockRepository.resetStockCard(productCodes);
            stockRepository.resetLotsOnHand(productCodes);
            cmmRepository.resetCmm(productCodes);
            rnrFormRepository.deleteRnrFormDirtyData(productCodes);
            sharedPreferenceMgr.setDeletedProduct(new HashSet<>());
          }
        } catch (LMISException e) {
          e.reportToFabric();
          subscriber.onError(e);
        }
      subscriber.onNext(null);
      subscriber.onCompleted();
    });
  }

  /**
   * correct data by
   * 1. adjust the `StockCard`.stockOnHand as 0 if the values is less than 0
   * 2. adjust the `StockCard`.stockOnHand value and last one `StockMovementItem`.stockOnHand and
   * quantity if the value is not valid in ${@code isCorrectSOHBetweenMovementAndStockCard} or
   * ${@code isCorrectMovements}
   *
   * @param stockCards dirty data of stock cards
   * @param cardIdsLotOnHandLessZero `StockCard` ids which `StockCard`.stockOnHand < 0
   */
  private void correctAndSaveDirtyData(
      List<StockCard> stockCards,
      List<String> cardIdsLotOnHandLessZero
  ) throws LMISException {
    HashMap<Long, List<StockMovementItem>> stockMovementItemsMap = getLastStockMovementMap();

    for (StockCard stockCard : stockCards) {
      if (stockCard == null) {
        continue;
      }
      // 1. `StockCard`.stockOnHand < 0
      if (!isPositiveOnHand(stockCard, cardIdsLotOnHandLessZero)) {
        correctStockCardWhenStockOnHandIsLessThan0(stockCard, stockMovementItemsMap.get(stockCard.getId()));
      }
      // 2. `StockCard`.stockOnHand != last one `StockMovementItem`.stockOnHand

      // 3. last one `StockMovementItem`.stockOnHand !=
      // penult `StockMovementItem`.stockOnHand + `StockMovementItem`.movementQuantity

    }
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
      if (!deletedStockMovementItems.isEmpty() || !keepStockMovementItemsMap.isEmpty()
          || !productCodes.isEmpty()) {
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
        }
        stockRepository.resetStockCard(productCodes);
        stockRepository.resetLotsOnHand(productCodes);
        cmmRepository.resetCmm(productCodes);
        rnrFormRepository.deleteRnrFormDirtyData(productCodes);
        sharedPreferenceMgr.setDeletedProduct(new HashSet<>());
      }
      subscriber.onNext(null);
      subscriber.onCompleted();
    });
  }

  private DirtyDataItemInfo convertStockMovementItemsToStockMovementEntriesForSave(
      List<StockMovementItem> stockMovementItems, String productCode, boolean fullyDelete) {
    Product product = Product.builder().code(productCode).build();
    List<StockMovementEntry> movementEntries = FluentIterable.from(stockMovementItems)
        .transform(stockMovementItem -> {
          Objects.requireNonNull(stockMovementItem).getStockCard().setProduct(product);
          return stockMovementItem;
        })
        .transform(StockMovementEntry::new)
        .toList();

    Gson gson = new GsonBuilder().create();
    Type type = new TypeToken<List<StockMovementEntry>>() {
    }.getType();
    gson.toJson(movementEntries, type);
    return new DirtyDataItemInfo(productCode, false, gson.toJson(movementEntries, type),
        fullyDelete);
  }

  /**
   * It will check `StockCard` and `StockMovements` 1. stockOnHand < 0 2. stockOnHand != the sum of
   * matched lots stockOnHand 3. last two stock_movements data is incorrect
   * stock_movements.A.stockOnHand + movementQuantity != stock_movements.B.stockOnHand
   *
   * @param stockCards               stock list
   * @param cardIdsLotOnHandLessZero stockCardIds which `StockCard`.stockOnHand is less than 0
   * @return dirty data - map of `StockCard` to `StockMovementItem` list
   */
  private HashMap<StockCard, List<StockMovementItem>> checkTheLastTwoMovementAndLotSOH(
      List<StockCard> stockCards, List<String> cardIdsLotOnHandLessZero
  ) {
    HashMap<StockCard, List<StockMovementItem>> stockCardToStockMovementItems = new HashMap<>();
    HashMap<Long, List<StockMovementItem>> stockMovementItemsMap = getLastStockMovementMap();
    Map<String, List<StockMovementItem>> keepMovementItemsMap = sharedPreferenceMgr.getKeepMovementItemsMap();
    Set<String> keepStockCardIds =
        keepMovementItemsMap.isEmpty() ? new HashSet<>() : keepMovementItemsMap.keySet();
    for (StockCard stockCard : stockCards) {
      if (keepStockCardIds.contains(String.valueOf(stockCard.getId()))) {
        continue;
      }
      List<StockMovementItem> stockMovementItems = stockMovementItemsMap.get(stockCard.getId());
      if (!isPositiveOnHand(stockCard, cardIdsLotOnHandLessZero)) {
        stockCardToStockMovementItems.put(stockCard, stockMovementItems);
      } else if (stockMovementItems != null && stockMovementItems.size() == CHECK_NEWEST_TWO) {
        StockMovementRepository.SortClass sort = new StockMovementRepository.SortClass();
        Collections.sort(stockMovementItems, sort);
        StockMovementItem currentStockMovement = stockMovementItems.get(1);
        StockMovementItem preStockMovement = stockMovementItems.get(0);
        if (!isCorrectMovements(preStockMovement, currentStockMovement)
            || !isCorrectSOHBetweenMovementAndStockCard(stockCard, currentStockMovement)) {
          stockCardToStockMovementItems.put(stockCard, stockMovementItems);
        }
      }
    }
    Log.d("performance", "check The Last TwoMovement");
    Log.d("dirty", "daily" + stockCardToStockMovementItems);

    return stockCardToStockMovementItems;
  }

  private HashMap<Long, List<StockMovementItem>> getLastStockMovementMap() {
    Log.d("TwoStockMovements", "1");
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
   * It will check `StockCard` and `StockMovements` 1. stockOnHand < 0 2. stockOnHand != the sum of
   * matched lots stockOnHand 3. last two stock_movements data is incorrect
   * stock_movements.A.stockOnHand + movementQuantity != stock_movements.B.stockOnHand
   *
   * @return productCodes
   */
  @SuppressWarnings({"squid:S3776", "squid:S135"})
  private Set<String> checkAllMovementAndLotSOHAndSaveToDB() {
    List<StockCard> checkedStockCards = stockRepository.queryCheckedStockCards();
    lotsOnHands.putAll(stockRepository.lotOnHands());
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
          continue;
        }
        if (stockMovementItems.size() >= 2) {
          for (int i = 0; i <= stockMovementItems.size() - 2; i++) {
            if (!isCorrectMovements(stockMovementItems.get(i), stockMovementItems.get(i + 1))) {
              debugLog(stockMovementItems.get(i), stockMovementItems.get(i + 1), stockCard);
              idToStockItemForDelete.put(String.valueOf(stockCard.getId()), stockMovementItems);
              break;
            }
          }
        }
      } catch (LMISException e) {
        Log.w(TAG, e);
      }
    }
    return covertMapFromStockIdToProductCode(idToStockItemForDelete);
  }

  private Set<String> covertMapFromStockIdToProductCode(
      Map<String, List<StockMovementItem>> idToStockItemForDelete) {
    if (!idToStockItemForDelete.isEmpty()) {
      Set<String> stockCardIds = idToStockItemForDelete.keySet();

      Map<String, String> stockCardIdToCode = stockMovementRepository
          .queryStockCardIdAndProductCode(stockCardIds);
      Map<String, List<StockMovementItem>> codeToStockItems = new HashMap<>();
      for (Map.Entry<String, List<StockMovementItem>> entry : idToStockItemForDelete.entrySet()) {
        codeToStockItems.put(stockCardIdToCode.get(entry.getKey()), entry.getValue());
      }
      saveDeletedMovementToDB(codeToStockItems, true);
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
   * Check if the last two `StockMovementItem`.stockOnHand is valid eg. `StockMovementItem`A is the
   * last one, and `StockMovementItem`B is the penult two. `StockMovementItem`A.stockOnHand ==
   * `StockMovementItem`B.stockOnHand + `StockMovementItem`B.movementQuantity
   *
   * @param previousMovement the penult StockMovementItem
   * @param newestMovement   last one StockMovementItem
   * @return `StockMovementItem`A.stockOnHand == `StockMovementItem`B.stockOnHand +
   * `StockMovementItem`B.movementQuantity
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
