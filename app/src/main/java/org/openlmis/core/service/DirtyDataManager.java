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

import static org.openlmis.core.utils.DateUtil.today;

import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.DirtyDataItemInfo;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.CmmRepository;
import org.openlmis.core.model.repository.DirtyDataRepository;
import org.openlmis.core.model.repository.LotRepository;
import org.openlmis.core.model.repository.ProgramDataFormRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.network.model.StockMovementEntry;
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
  private static final String DELETE_PRODUCT = "deleteProduct";
  private static final String DELETE_ITEMS = "deleteItems";
  private static final String KEEP_ITEMS_MAP = "KeepItemsMap";

  // the newest two already checked.
  private static final int CHECK_NEWEST_TWO = 2;

  private static final boolean DEBUG_ALL_MOVEMENT = false;

  @Inject
  StockMovementRepository stockMovementRepository;
  @Inject
  StockRepository stockRepository;

  @Inject
  DirtyDataRepository dirtyDataRepository;
  @Inject
  RnrFormRepository rnrFormRepository;
  @Inject
  ProgramDataFormRepository programDataFormRepository;
  @Inject
  CmmRepository cmmRepository;
  @Inject
  LotRepository lotRepository;

  @Inject
  SharedPreferenceMgr sharedPreferenceMgr;

  private boolean isSyncedMonthCheck = false;

  final Map<String, String> lotsOnHands = new HashMap<>();

  Observer<Object> deleteDirtyDataSubscribe = new Observer<Object>() {
    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onNext(Object o) {

    }
  };

  public DirtyDataManager() {
    RoboGuice.getInjector(LMISApp.getContext()).injectMembersWithoutViews(this);
  }

  public List<StockCard> correctData() {
    lotsOnHands.putAll(stockRepository.lotOnHands());
    return doCorrectDirtyData(stockRepository.list());
  }

  public List<StockCard> correctDataForStockCardOverView(List<StockCard> stockCards,
      Map<String, String> inputLotsOnHands) {
    lotsOnHands.putAll(inputLotsOnHands);
    return doCorrectDirtyData(stockCards);
  }

  private List<StockCard> doCorrectDirtyData(List<StockCard> stockCards) {
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
    }
    return deletedStockCards;
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
      Set<String> filterStockCardIds = new HashSet<>();
      Map<String, Object> duplicateMap = checkDuplicateDataAllWithoutSignature(filterStockCardIds);
      Set<String> deleteProducts = checkSoh(filterStockCardIds);
      List<StockMovementItem> duplicatedNotAffectCalculate = checkDuplicateDataNotAffectCalculate(filterStockCardIds);
      saveToSharePreferenceMgr(duplicateMap, deleteProducts, duplicatedNotAffectCalculate);
      sharedPreferenceMgr.setIsInitialDataCheck(false);
    }
  }

  private void saveToSharePreferenceMgr(Map<String, Object> duplicateMap,
      Set<String> deleteProducts, List<StockMovementItem> duplicatedNotAffectCalculate) {
    if (duplicateMap.containsKey(DELETE_ITEMS)) {
      List<StockMovementItem> deletedMovementItems = (List<StockMovementItem>) duplicateMap.get(DELETE_ITEMS);
      if (!deletedMovementItems.isEmpty()) {
        sharedPreferenceMgr.setDeletedMovementItems(deletedMovementItems);
      }
    }
    if (duplicateMap.containsKey(KEEP_ITEMS_MAP)) {
      HashMap<String, List<StockMovementItem>> keepMaps =
          (HashMap<String, List<StockMovementItem>>) duplicateMap.get(KEEP_ITEMS_MAP);
      if (keepMaps.size() > 0) {
        sharedPreferenceMgr.setKeepMovementItemsMap(keepMaps);
      }
    }
    if (duplicateMap.containsKey(DELETE_PRODUCT)) {
      Set<String> deleteForDuplicated = (Set<String>) duplicateMap.get(DELETE_PRODUCT);
      if (!deleteProducts.isEmpty()) {
        sharedPreferenceMgr.setDeletedProduct(deleteForDuplicated);
      }
    }
    if (!deleteProducts.isEmpty()) {
      sharedPreferenceMgr.setDeletedProduct(deleteProducts);
    }
    if (!duplicatedNotAffectCalculate.isEmpty()) {
      sharedPreferenceMgr.setDeletedMovementItems(duplicatedNotAffectCalculate);
    }
  }

  private Set<String> checkSoh(Set<String> filterStockCardIds) {
    lotsOnHands.putAll(stockRepository.lotOnHands());
    List<StockCard> checkedStockCards = stockRepository.queryCheckedStockCards(filterStockCardIds);
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
          .queryStockMovement(deleteStockCardIds);
      return covertMapFromStockIdToProductCode(filterStockCardIds, idToStockItemsForDelete);
    }
    return new HashSet<>();
  }

  public void scanAllStockMovements() {
    if (sharedPreferenceMgr.shouldSyncLastYearStockData()
        || sharedPreferenceMgr.isSyncingLastYearStockCards()
        || sharedPreferenceMgr.shouldInitialDataCheck()
        || isSyncedMonthCheck) {
      return;
    }
    DateTime recordLastDirtyDataCheck = SharedPreferenceMgr.getInstance()
        .getLatestMonthlyCheckDirtyDataTime();
    Period period = Period.of(today());
    if (recordLastDirtyDataCheck.isBefore(period.getBegin())) {
      isSyncedMonthCheck = true;
      sharedPreferenceMgr.setCheckDataDate(LMISApp.getInstance().getCurrentTimeMillis());
      Set<String> filterStockCardIds = new HashSet<>();
      Map<String, Object> duplicateMap = checkDuplicateDataAllWithoutSignature(filterStockCardIds);
      Set<String> deleteProducts = checkAllMovementAndLotSOHAndSaveToDB(filterStockCardIds);
      List<StockMovementItem> duplicatedNotAffectCalculate = checkDuplicateDataNotAffectCalculate(filterStockCardIds);
      saveToSharePreferenceMgr(duplicateMap, deleteProducts, duplicatedNotAffectCalculate);
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
    for (StockCard stockCard : deletedStockCards) {
      List<StockMovementItem> stockMovementItems = null;
      try {
        stockMovementItems = stockMovementRepository.queryMovementByStockCardId(stockCard.getId());
      } catch (LMISException e) {
        Log.w(TAG, e);
      }
      codeToStockItems.put(stockCard.getProduct().getCode(), stockMovementItems);
    }
    saveDeletedMovementToDB(codeToStockItems, true);
  }

  private void saveDeletedMovementToDB(Map<String, List<StockMovementItem>> items, boolean fullyDelete) {
    if (items.size() == 0) {
      return;
    }
    final String facilityCode = sharedPreferenceMgr.getUserFacilityCode();
    if (TextUtils.isEmpty(facilityCode)) {
      return;
    }
    List<DirtyDataItemInfo> dirtyDataItemInfos = new ArrayList<>();
    for (Map.Entry<String, List<StockMovementItem>> entry : items.entrySet()) {
      dirtyDataItemInfos.add(convertStockMovementItemsToStockMovementEntriesForSave(entry.getValue(),
          entry.getKey(), fullyDelete));
    }
    dirtyDataRepository.saveAll(dirtyDataItemInfos);
  }

  public void deleteAndReset() {
    getDeleteDirtyDataObservable()
        .subscribeOn(AndroidSchedulers.mainThread())
        .observeOn(Schedulers.io())
        .subscribe(deleteDirtyDataSubscribe);
  }

  private Observable<Object> getDeleteDirtyDataObservable() {
    return Observable.create(subscriber -> {
      List<StockMovementItem> deletedStockMovementItems = sharedPreferenceMgr.getDeletedMovementItems();
      Map<String, List<StockMovementItem>> keepStockMovementItemsMap = sharedPreferenceMgr.getKeepMovementItemsMap();
      List<String> productCodes = sharedPreferenceMgr.getDeletedProduct();
      if (!deletedStockMovementItems.isEmpty() || !keepStockMovementItemsMap.isEmpty() || !productCodes.isEmpty()) {
        dirtyDataRepository.deleteDraftForDirtyData();
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
    return new DirtyDataItemInfo(productCode, false, gson.toJson(movementEntries, type), fullyDelete);
  }

  private Map<String, Object> checkDuplicateDataAllWithoutSignature(Set<String> filterStockCards) {
    Map<String, List<StockMovementItem>> stockMovementItems = stockMovementRepository
        .queryNoSignatureStockCardsMovements();
    if (stockMovementItems.keySet().isEmpty()) {
      return new HashMap<>();
    }
    Map<String, String> stockCardIdToCode = stockMovementRepository
        .queryStockCardIdAndProductCode(stockMovementItems.keySet());
    HashMap<String, List<StockMovementItem>> keepMovementMap = new HashMap<>();
    List<StockMovementItem> deleteMovementList = new ArrayList<>();
    List<DirtyDataItemInfo> dirtyDataItemInfos = new ArrayList<>();
    Set<String> deleteProductList = new HashSet<>();
    for (Map.Entry<String, List<StockMovementItem>> map : stockMovementItems.entrySet()) {
      List<StockMovementItem> items = map.getValue();
      int count = items.size();
      StockMovementItem keepMovementItem = items.get(count - 1);
      if (keepMovementItem.getReason().equalsIgnoreCase(MovementReasonManager.INVENTORY)) {
        deleteMovementList.addAll(items.subList(0, count - 1));
        keepMovementMap.put(map.getKey(), Arrays.asList(keepMovementItem));
      } else {
        String code = stockCardIdToCode
            .get(String.valueOf(keepMovementItem.getStockCard().getId()));
        deleteProductList.add(code);
      }
      dirtyDataItemInfos.add(
          convertStockMovementItemsToStockMovementEntriesForSave(items,
              stockCardIdToCode.get(map.getKey()), true));
    }
    filterStockCards.addAll(stockMovementItems.keySet());
    Map<String, Object> mapToStockItems = new HashMap<>();
    mapToStockItems.put(DELETE_ITEMS, deleteMovementList);
    mapToStockItems.put(KEEP_ITEMS_MAP, keepMovementMap);
    mapToStockItems.put(DELETE_PRODUCT, deleteProductList);
    dirtyDataRepository.saveAll(dirtyDataItemInfos);
    return mapToStockItems;
  }

  private List<StockMovementItem> checkDuplicateDataNotAffectCalculate(Set<String> filterStockCardIds) {
    Map<String, List<StockMovementItem>> stockMovementItems = stockMovementRepository
        .queryDirtyDataNoAffectCalculatedStockCardsMovements(filterStockCardIds);
    if (stockMovementItems.keySet().isEmpty()) {
      return new ArrayList<>();
    }
    Map<String, String> stockCardIdToCode = stockMovementRepository
        .queryStockCardIdAndProductCode(stockMovementItems.keySet());
    List<StockMovementItem> deleteList = new ArrayList<>();
    List<DirtyDataItemInfo> dirtyDataItemInfos = new ArrayList<>();
    for (Map.Entry<String, List<StockMovementItem>> map : stockMovementItems.entrySet()) {
      List<StockMovementItem> items = map.getValue();
      List<StockMovementItem> deleteItems = items.subList(0, items.size() - 2);
      dirtyDataItemInfos.add(
          convertStockMovementItemsToStockMovementEntriesForSave(items,
              stockCardIdToCode.get(map.getKey()), false));
      deleteList.addAll(deleteItems);
    }
    dirtyDataRepository.saveAll(dirtyDataItemInfos);
    return deleteList;
  }

  private List<StockCard> checkTheLastTwoMovementAndLotSOH(List<StockCard> stockCards) {
    List<StockCard> deleted = new ArrayList<>();
    HashMap<Integer, List<StockMovementItem>> stockMovementItemsMap = getLastStockMovementMap();
    List<String> cardIdsLotOnHandLessZero = stockRepository.cardIdsIfLotOnHandLessZero();
    Map<String, List<StockMovementItem>> keepMovementItemsMap = sharedPreferenceMgr.getKeepMovementItemsMap();
    Set<String> keepStockCardIds = keepMovementItemsMap.size() == 0 ? new HashSet<>() : keepMovementItemsMap.keySet();
    for (StockCard stockCard : stockCards) {
      if (keepStockCardIds.contains(String.valueOf(stockCard.getId()))) {
        continue;
      }
      List<StockMovementItem> stockMovementItems = stockMovementItemsMap
          .get((int) stockCard.getId());
      if (!isPositiveOnHand(stockCard, cardIdsLotOnHandLessZero)) {
        deleted.add(stockCard);
      } else if (stockMovementItems != null && stockMovementItems.size() == CHECK_NEWEST_TWO) {
        StockMovementRepository.SortClass sort = new StockMovementRepository.SortClass();
        Collections.sort(stockMovementItems, sort);
        StockMovementItem currentStockMovement = stockMovementItems.get(1);
        StockMovementItem preStockMovement = stockMovementItems.get(0);
        if (!isCorrectMovement(preStockMovement, currentStockMovement)
            || !isCorrectSOHBetweenMovementAndStockCard(stockCard, currentStockMovement)) {
          deleted.add(stockCard);
        }
      }
    }
    Log.d("performance", "check The Last TwoMovement");
    Log.d("dirty", "daily" + deleted.toString());

    return deleted;
  }

  private HashMap<Integer, List<StockMovementItem>> getLastStockMovementMap() {
    Log.d("TwoStockMovements", "1");
    List<StockMovementItem> stockMovements = stockMovementRepository.listLastTwoStockMovements();
    HashMap<Integer, List<StockMovementItem>> stockMovementItemsMap = new HashMap<>();
    for (StockMovementItem item : stockMovements) {
      long id = item.getStockCard().getId();
      if (!stockMovementItemsMap.containsKey((int) id)) {
        List<StockMovementItem> list = new ArrayList<>();
        list.add(item);

        stockMovementItemsMap.put((int) id, list);
      } else {
        stockMovementItemsMap.get((int) id).add(item);

      }
    }
    return stockMovementItemsMap;
  }

  @SuppressWarnings({"squid:S3776", "squid:S135"})
  private Set<String> checkAllMovementAndLotSOHAndSaveToDB(Set<String> filterStockCardIds) {
    List<StockCard> checkedStockCards = stockRepository.queryCheckedStockCards(filterStockCardIds);
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
    return covertMapFromStockIdToProductCode(filterStockCardIds, idToStockItemForDelete);
  }

  private Set<String> covertMapFromStockIdToProductCode(Set<String> filterStockCardIds,
      Map<String, List<StockMovementItem>> idToStockItemForDelete) {
    if (idToStockItemForDelete.size() > 0) {
      Set<String> stockCardIds = idToStockItemForDelete.keySet();
      filterStockCardIds.addAll(stockCardIds);

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

  private boolean isCorrectSOHBetweenMovementAndStockCard(StockCard stockCard,
      StockMovementItem newestMovement) {
    return stockCard.calculateSOHFromLots(lotsOnHands) == newestMovement.getStockOnHand();
  }

  private boolean isCorrectMovement(StockMovementItem previousMovement,
      StockMovementItem newestMovement) {
    return checkFormula(newestMovement, newestMovement.getStockOnHand(),
        previousMovement.getStockOnHand(), newestMovement.getMovementQuantity());
  }

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
