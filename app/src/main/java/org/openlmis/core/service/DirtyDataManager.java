package org.openlmis.core.service;

import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.DateTime;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.DirtyDataItemInfo;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.CmmRepository;
import org.openlmis.core.model.repository.DirtyDataRepository;
import org.openlmis.core.model.repository.LotRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.network.model.StockMovementEntry;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import roboguice.RoboGuice;
import rx.Observable;
import rx.schedulers.Schedulers;

import static org.openlmis.core.utils.DateUtil.today;


@Singleton
public class DirtyDataManager {

    private static final String TAG = DirtyDataManager.class.getSimpleName();

    // the newest two already checked.
    private static final int DO_NOT_CHECK_NEWEST_TWO = 3;
    private static final int CHECK_NEWEST_TWO = 2;

    private static final boolean DEBUG_ALL_MOVEMENT = true;

    @Inject
    StockMovementRepository stockMovementRepository;
    @Inject
    StockRepository stockRepository;

    @Inject
    DirtyDataRepository dirtyDataRepository;
    @Inject
    RnrFormRepository rnrFormRepository;
    @Inject
    ProgramRepository programRepository;
    @Inject
    CmmRepository cmmRepository;
    @Inject
    LotRepository lotRepository;

    @Inject
    SharedPreferenceMgr sharedPreferenceMgr;

    final Map<String, String> lotsOnHands = new HashMap<>();

    Set<String> filterStockCardIds = new HashSet<>();

    public DirtyDataManager() {
        RoboGuice.getInjector(LMISApp.getContext()).injectMembersWithoutViews(this);
    }

    public List<StockCard> correctData() {
        lotsOnHands.putAll(stockRepository.lotOnHands());
        return doCorrectDirtyData(stockRepository.list());
    }

    public List<StockCard> correctDataForStockCardOverView(List<StockCard> stockCards, Map<String, String> inputLotsOnHands) {
        lotsOnHands.putAll(inputLotsOnHands);
        return doCorrectDirtyData(stockCards);
    }

    private List<StockCard> doCorrectDirtyData(List<StockCard> stockCards) {
        List<StockCard> deletedStockCards = new ArrayList<>();
        List<StockCard> lastTwoMovementAndLotSOHWrong = checkTheLastTwoMovementAndLotSOH(stockCards);
        deletedStockCards.addAll(lastTwoMovementAndLotSOHWrong);
        saveFullyDeletedInfo(deletedStockCards);
        List<String> productCodes = getCodeFromStockCard(deletedStockCards);
        if (!productCodes.isEmpty()) {
            sharedPreferenceMgr.setDeletedProduct(productCodes);
        }
        return deletedStockCards;
    }

    public void dirtyDataMonthlyCheck() {
        Observable.create((Observable.OnSubscribe<Void>) subscriber ->
                scanAllStockMovements())
                .subscribeOn(Schedulers.io());
    }

    public List<StockCard> scanAllStockMovements() {
        DateTime recordLastDirtyDataCheck = SharedPreferenceMgr.getInstance().getLatestMonthlyCheckDirtyDataTime();
        Period period = Period.of(today());
        if (recordLastDirtyDataCheck.isBefore(period.getBegin())) {
            filterStockCardIds.clear();
            sharedPreferenceMgr.updateLatestMonthlyCheckDirtyDataTime();
            final String facilityId = sharedPreferenceMgr.getUserFacilityId();
            Set<String> duplicatedStockCardIds = checkDuplicateDataAllWithoutSignature(facilityId);
            filterStockCardIds.addAll(duplicatedStockCardIds);
            List<StockCard> checkedStockCards = stockRepository.queryCheckedStockCards(filterStockCardIds);
            List<StockCard> deletedStockCards = checkAllMovementAndLotSOHAndSaveToDB(checkedStockCards);
            List<String> productCodes = getCodeFromStockCard(deletedStockCards);
            if (!productCodes.isEmpty()) {
                sharedPreferenceMgr.setDeletedProduct(productCodes);
            }
            checkDuplicateDataNotAffectCalculate(facilityId);
            return deletedStockCards;
        }
        return new ArrayList<>();
    }

    private List<String> getCodeFromStockCard(List<StockCard> stockCardList) {
        return FluentIterable.from(stockCardList).transform(new Function<StockCard, String>() {
            @Nullable
            @Override
            public String apply(@Nullable StockCard stockCard) {
                return stockCard.getProduct().getCode();
            }
        }).toList();
    }

    private void saveFullyDeletedInfo(List<StockCard> deletedStockCards) {
        for (StockCard stockCard : deletedStockCards) {
            List<StockMovementItem> stockMovementItems = null;
            try {
                stockMovementItems = stockMovementRepository.queryMovementByStockCardId(stockCard.getId());
            } catch (LMISException e) {
                e.printStackTrace();
            }
            saveDeletedMovementToDB(stockMovementItems, stockCard.getProduct().getCode(), true);
        }
    }

    private void saveDeletedMovementToDB(List<StockMovementItem> movementItems, String productCode, boolean fullyDelete) {
        final String facilityId = sharedPreferenceMgr.getUserFacilityId();
        if (TextUtils.isEmpty(facilityId)) {
            return;
        }
        DirtyDataItemInfo dirtyDataItems = convertStockMovementItemsToStockMovementEntriesForSave(
                facilityId, movementItems, productCode, fullyDelete);
        dirtyDataRepository.save(dirtyDataItems);
    }

    public void deleteAndReset() {
        List<StockMovementItem> deletedStockMovementItems = sharedPreferenceMgr.getDeletedMovementItems();
        Map<String, List<StockMovementItem>> keepStockMovementItemsMap = sharedPreferenceMgr.getKeepMovementItemsMap();
        List<String> productCodes = sharedPreferenceMgr.getDeletedProduct();
        if (deletedStockMovementItems.size() > 0
                || keepStockMovementItemsMap.size() > 0
                || productCodes.size() > 0) {
            dirtyDataRepository.deleteDraftForDirtyData();
            if (deletedStockMovementItems.size() > 0) {
                stockMovementRepository.deleteStockMovementItems(deletedStockMovementItems);
                lotRepository.deleteLotMovementItems(deletedStockMovementItems);
                sharedPreferenceMgr.setDeletedMovementItems(new ArrayList<>());
            }
            if (keepStockMovementItemsMap.size() > 0) {
                List<LotMovementItem> lotMovementItems = lotRepository.resetKeepLotMovementItems(keepStockMovementItemsMap);
                stockRepository.resetKeepLotsOnHand(lotMovementItems, keepStockMovementItemsMap);
                stockMovementRepository.resetKeepItemToNotSynced(keepStockMovementItemsMap);
                sharedPreferenceMgr.setKeepMovementItemsMap(new HashMap<>());
            }
            if (productCodes.size() > 0) {
                stockRepository.deleteStockMovementsForDirtyData(productCodes);
                try {
                    stockRepository.insertNewInventory(productCodes);
                } catch (LMISException e) {
                    e.printStackTrace();
                }
                stockRepository.resetStockCard(productCodes);
                stockRepository.resetLotsOnHand(productCodes);
                cmmRepository.resetCmm(productCodes);
                rnrFormRepository.deleteRnrFormDirtyData(productCodes);
                programRepository.deleteProgramDirtyData(productCodes);
                sharedPreferenceMgr.setDeletedProduct(new ArrayList<>());
            }
        }
    }

    private DirtyDataItemInfo convertStockMovementItemsToStockMovementEntriesForSave(final String facilityId,
                                                                                     List<StockMovementItem> stockMovementItems,
                                                                                     String productCode,
                                                                                     boolean fullyDelete) {
        List<StockMovementEntry> movementEntries = FluentIterable.from(stockMovementItems).transform(stockMovementItem -> {
            return new StockMovementEntry(stockMovementItem, facilityId, productCode);
        }).toList();

        Gson gson = new GsonBuilder().create();
        Type type = new TypeToken<List<StockMovementEntry>>() {
        }.getType();
        gson.toJson(movementEntries, type);
        return new DirtyDataItemInfo(productCode, false, gson.toJson(movementEntries, type), fullyDelete);
    }


    private Set<String> checkDuplicateDataAllWithoutSignature(String facilityId) {
        Map<String, List<StockMovementItem>> stockMovementItems = stockMovementRepository.queryNoSignatureStockCardsMovements();
        if (stockMovementItems.keySet().size() == 0) {
            return new HashSet<>();
        }
        Map<String, String> stockCardIdToCode = stockMovementRepository.queryStockCardIdAndProductCode(stockMovementItems.keySet());
        HashMap<String, List<StockMovementItem>> keepMovementMap = new HashMap<>();
        List<StockMovementItem> deleteList = new ArrayList<>();
        List<StockMovementItem> keepList = new ArrayList<>();
        List<DirtyDataItemInfo> dirtyDataItemInfos = new ArrayList<>();
        for (Map.Entry map : stockMovementItems.entrySet()) {
            List<StockMovementItem> items = (List<StockMovementItem>) map.getValue();
            int count = items.size();
            keepList.add(items.get(count - 1));
            deleteList.addAll(items.subList(0, count - 1));
            keepMovementMap.put(map.getKey().toString(), keepList);
            dirtyDataItemInfos.add(
                    convertStockMovementItemsToStockMovementEntriesForSave(facilityId, items, stockCardIdToCode.get(map.getKey()), true));
        }
        sharedPreferenceMgr.setDeletedMovementItems(deleteList);
        sharedPreferenceMgr.setKeepMovementItemsMap(keepMovementMap);
        dirtyDataRepository.saveAll(dirtyDataItemInfos);
        return stockMovementItems.keySet();
    }

    private void checkDuplicateDataNotAffectCalculate(String facilityId) {
        Map<String, List<StockMovementItem>> stockMovementItems = stockMovementRepository.
                queryHavingSignatureAndDuplicatedDirtyDataNoAffectCalculatedStockCardsMovements(filterStockCardIds);
        if (stockMovementItems.keySet().size() == 0) {
            return;
        }
        Map<String, String> stockCardIdToCode = stockMovementRepository.queryStockCardIdAndProductCode(stockMovementItems.keySet());
        List<StockMovementItem> deleteList = new ArrayList<>();
        List<DirtyDataItemInfo> dirtyDataItemInfos = new ArrayList<>();
        for (Map.Entry map : stockMovementItems.entrySet()) {
            List<StockMovementItem> items = (List<StockMovementItem>) map.getValue();
            List<StockMovementItem> deleteItems = items.subList(0, items.size() - 2);
            dirtyDataItemInfos.add(
                    convertStockMovementItemsToStockMovementEntriesForSave(facilityId, items, stockCardIdToCode.get(map.getKey()), false));
            deleteList.addAll(deleteItems);
        }
        dirtyDataRepository.saveAll(dirtyDataItemInfos);
        sharedPreferenceMgr.setDeletedMovementItems(deleteList);
    }

    private List<StockCard> checkTheLastTwoMovementAndLotSOH(List<StockCard> stockCards) {
        List<StockCard> deleted = new ArrayList<>();
        Log.d("performance", "check The Last TwoMovement");
        HashMap<Integer, List<StockMovementItem>> stockMovementItemsMap = getLastStockMovementMap();
        Log.d("performance", "check The Last TwoMovement1");
        List<String> cardIdsLotOnHandLessZero = stockRepository.cardIdsIfLotOnHandLessZero();
        for (StockCard stockCard : stockCards) {
            List<StockMovementItem> stockMovementItems = stockMovementItemsMap.get((int) stockCard.getId());
            if (!isCorrectOnHand(stockCard, cardIdsLotOnHandLessZero)) {
                deleted.add(stockCard);
                filterStockCardIds.add(String.valueOf(stockCard.getId()));
            } else if (stockMovementItems != null && stockMovementItems.size() == CHECK_NEWEST_TWO) {
                StockMovementItem movementItemOne = stockMovementItems.get(0);
                StockMovementItem movementItemTwo = stockMovementItems.get(1);
                StockMovementItem currentStockMovement = getCurrentStockMovementItem(movementItemOne, movementItemTwo);
                StockMovementItem preStockMovement = movementItemOne != currentStockMovement ? movementItemOne : movementItemTwo;
                if (!isCorrectMovement(preStockMovement, currentStockMovement)
                        || !isCorrectSOHBetweenMovementAndStockCard(stockCard, currentStockMovement)) {
                    deleted.add(stockCard);
                    filterStockCardIds.add(String.valueOf(stockCard.getId()));
                }
            }
        }
        Log.d("performance", "check The Last TwoMovement");
        Log.d("dirty", "daily" + deleted.toString());

        return deleted;
    }

    private StockMovementItem getCurrentStockMovementItem(StockMovementItem movementItemOne, StockMovementItem movementItemTwo) {
        StockMovementItem currentStockMovement;
        if (movementItemOne.getMovementDate().after(movementItemTwo.getMovementDate())) {
            currentStockMovement = movementItemOne;
        } else {
            if (movementItemOne.getMovementDate().equals(movementItemTwo.getMovementDate())) {
                currentStockMovement = movementItemOne.getCreatedTime().after(movementItemTwo.getCreatedTime())
                        ? movementItemOne : movementItemTwo;
            } else {
                currentStockMovement = movementItemTwo;
            }
        }
        return currentStockMovement;
    }

    private HashMap<Integer, List<StockMovementItem>> getLastStockMovementMap() {
        Log.d("TwoStockMovements", "1");
        List<StockMovementItem> stockMovements = stockMovementRepository.listLastTwoStockMovements();
        HashMap<Integer, List<StockMovementItem>> stockMovementItemsMap = new HashMap<Integer, List<StockMovementItem>>();
        for (StockMovementItem item : stockMovements) {
            long id = item.getStockCard().getId();
            if (!stockMovementItemsMap.containsKey((int) id)) {
                List<StockMovementItem> list = new ArrayList<StockMovementItem>();
                list.add(item);

                stockMovementItemsMap.put((int) id, list);
            } else {
                stockMovementItemsMap.get((int) id).add(item);

            }
        }
        return stockMovementItemsMap;
    }

    private List<StockCard> checkAllMovementAndLotSOHAndSaveToDB(List<StockCard> stockCards) {
        List<StockCard> deleted = new ArrayList<>();
        List<String> cardIdsLotOnHandLessZero = stockRepository.cardIdsIfLotOnHandLessZero();
        for (StockCard stockCard : stockCards) {
            try {
                List<StockMovementItem> stockMovementItems = stockMovementRepository.queryMovementByStockCardId(stockCard.getId());
                if (CollectionUtils.isEmpty(stockMovementItems)) {
                    continue;
                }
                if (!isCorrectOnHand(stockCard, cardIdsLotOnHandLessZero)) {
                    deleted.add(stockCard);
                    filterStockCardIds.add(String.valueOf(stockCard.getId()));
                    saveDeletedMovementToDB(stockMovementItems, stockCard.getProduct().getCode(), true);
                    continue;
                }
                if (stockMovementItems.size() < DO_NOT_CHECK_NEWEST_TWO) {
                    continue;
                }
                for (int i = 0; i <= stockMovementItems.size() - DO_NOT_CHECK_NEWEST_TWO; i++) {
                    if (!isCorrectMovements(stockMovementItems.get(i), stockMovementItems.get(i + 1))) {
                        debugLog(stockMovementItems.get(i), stockMovementItems.get(i + 1), stockCard);
                        deleted.add(stockCard);
                        filterStockCardIds.add(String.valueOf(stockCard.getId()));
                        saveDeletedMovementToDB(stockMovementItems, stockCard.getProduct().getCode(), true);
                        break;
                    }
                }
            } catch (LMISException e) {
                e.printStackTrace();
            }
        }
        Log.d("dirty", "month" + deleted.toString());
        return deleted;
    }

    private void debugLog(StockMovementItem previousMovement, StockMovementItem currentMovement, StockCard stockCard) {
        if (!DEBUG_ALL_MOVEMENT) return;
        Log.e(TAG, stockCard.getProduct().getCode()
                + "(" + stockCard.calculateSOHFromLots(lotsOnHands) + ")"
                + ":previous id=" + previousMovement.getId()
                + ",current id =" + currentMovement.getId()
                + ";previousSOH = " + previousMovement.getStockOnHand()
                + (currentMovement.isNegativeMovement() ? ",-" : ",+")
                + ",movementQuantity=" + currentMovement.getMovementQuantity()
                + ",currentSOH=" + currentMovement.getStockOnHand());
    }

    private boolean isCorrectOnHand(StockCard stockCard, List<String> cardIds) {
        return stockCard.getStockOnHand() >= 0 && !cardIds.contains(String.valueOf(stockCard.getId()));
    }

    private boolean isCorrectSOHBetweenMovementAndStockCard(StockCard stockCard, StockMovementItem newestMovement) {
        return stockCard.calculateSOHFromLots(lotsOnHands) == newestMovement.getStockOnHand();
    }

    private boolean isCorrectMovement(StockMovementItem previousMovement, StockMovementItem newestMovement) {
        return checkFormula(newestMovement, newestMovement.getStockOnHand(),
                previousMovement.getStockOnHand(), newestMovement.getMovementQuantity());
    }

    private boolean isCorrectMovements(StockMovementItem previousMovement, StockMovementItem newestMovement) {
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
