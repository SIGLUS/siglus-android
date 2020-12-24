package org.openlmis.core.service;

import android.os.Build;
import android.support.annotation.RequiresApi;
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
        HashMap<String,HashMap<String,List<StockMovementItem>>> stockCardIdWithDeleteAndKeepMap = checkDuplicateDataAllWithoutSignature();

        filterStockCardIds.addAll(stockCardIdWithDeleteAndKeepMap.keySet());
        List<StockCard> checkedStockCards  = stockRepository.queryCheckedStockCards(filterStockCardIds);
        List<StockCard> deletedStockCards = new ArrayList<>();
        List<StockCard> lastTwoMovementAndLotSOHWrong = checkTheLastTwoMovementAndLotSOH(checkedStockCards);
        deletedStockCards.addAll(lastTwoMovementAndLotSOHWrong);
        saveFullyDeletedInfo(deletedStockCards);
        List<String> productCodes = getCodeFromStockCard(deletedStockCards);
        if (!productCodes.isEmpty()){
            sharedPreferenceMgr.setDeletedProduct(productCodes);
        }
        HashMap<String,HashMap<String,List<StockMovementItem>>> stockCardIdWithDeleteMap = checkDuplicateDataNotAffectCalculate();
        return deletedStockCards;
    }

    public void dirtyDataMonthlyCheck() {
        DateTime recordLastMonthlyCheckPeriod = SharedPreferenceMgr.getInstance().getLatestMonthlyCheckDirtyDataTime();
        Period period = Period.of(today());
        if (recordLastMonthlyCheckPeriod.isBefore(period.getBegin())) {
            Observable.create((Observable.OnSubscribe<Void>) subscriber ->
                    scanAllStockMovements())
                    .subscribeOn(Schedulers.io());
        }
    }

    public List<StockCard> scanAllStockMovements() {
        sharedPreferenceMgr.updateLatestMonthlyCheckDirtyDataTime();
        HashMap<String,HashMap<String,List<StockMovementItem>>> stockCardIdWithDeleteAndKeepMap = checkDuplicateDataAllWithoutSignature();
        filterStockCardIds.addAll(stockCardIdWithDeleteAndKeepMap.keySet());
        List<StockCard> checkedStockCards  = stockRepository.queryCheckedStockCards(filterStockCardIds);
        List<StockCard> deletedStockCards = checkAllMovementAndLotSOHAndSaveToDB(checkedStockCards);
        List<String> productCodes = getCodeFromStockCard(deletedStockCards);
        if (!productCodes.isEmpty()) {
            sharedPreferenceMgr.setDeletedProduct(productCodes);
        }
        return deletedStockCards;
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
        Map<String,List<StockMovementItem>> keepStockMovementItemsMap = sharedPreferenceMgr.getKeepMovementItemsMap();
        List<String> productCodes = sharedPreferenceMgr.getDeletedProduct();
        if(deletedStockMovementItems.size() > 0) {
            stockMovementRepository.deleteStockMovementItems(deletedStockMovementItems);
            lotRepository.deleteLotMovementItems(deletedStockMovementItems);
            sharedPreferenceMgr.setDeletedMovementItems(new ArrayList<>());
        }
        if (keepStockMovementItemsMap.size() > 0) {
            lotRepository.resetLotMovementItems(keepStockMovementItemsMap);
            stockRepository.resetQuantityOnHand(keepStockMovementItemsMap);
            stockMovementRepository.resetKeepItemToNotSynced(keepStockMovementItemsMap);
            sharedPreferenceMgr.setKeepMovementItemsMap(new HashMap<>());
        }
        if (productCodes.size() > 0) {
            dirtyDataRepository.deleteDirtyDataByProductCode(productCodes);
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

    private DirtyDataItemInfo convertStockMovementItemsToStockMovementEntriesForSave(final String facilityId,
                                                                                     List<StockMovementItem> stockMovementItems,
                                                                                     String productCode,
                                                                                     boolean fullyDelete) {
        List<StockMovementEntry> movementEntries = FluentIterable.from(stockMovementItems).transform(stockMovementItem -> {
            if (stockMovementItem.getStockCard().getProduct() != null) {
                return new StockMovementEntry(stockMovementItem, facilityId);
            } else {
                return null;
            }
        }).toList();

        Gson gson = new GsonBuilder().create();
        Type type = new TypeToken<List<StockMovementEntry>>() {
        }.getType();
        gson.toJson(movementEntries, type);
        return new DirtyDataItemInfo(productCode, false, gson.toJson(movementEntries, type), fullyDelete);
    }


    private HashMap<String,HashMap<String,List<StockMovementItem>>> checkDuplicateDataAllWithoutSignature() {
        HashMap<String,HashMap<String,List<StockMovementItem>>> stockCardIdWithDeleteAndKeepMap = new HashMap<>();
        Map<Integer,List<StockMovementItem>> stockMovementItems = stockMovementRepository.queryNoSignatureStockCardsMovements();
        HashMap<String,List<StockMovementItem>> keepMovementMap = new HashMap<>();
        for (Map.Entry map : stockMovementItems.entrySet()) {
            HashMap<String,List<StockMovementItem>> deleteAndKeepMap = new HashMap<>();
            List<StockMovementItem> deleteList = new ArrayList<>();
            List<StockMovementItem> keepList = new ArrayList<>();
            for(int i=0;i<stockMovementItems.get(map.getKey()).size();i++) {
                if (i == stockMovementItems.get(map.getKey()).size()-1) {
                    keepList.add(stockMovementItems.get(map.getKey()).get(i));
                }else {
                    deleteList.add(stockMovementItems.get(map.getKey()).get(i));
                }
            }
            keepMovementMap.put(map.getKey().toString(),keepList);
            sharedPreferenceMgr.setDeletedMovementItems(deleteList);
            sharedPreferenceMgr.setKeepMovementItemsMap(keepMovementMap);
            deleteAndKeepMap.put("delete",deleteList);
            deleteAndKeepMap.put("keep",keepList);
            stockCardIdWithDeleteAndKeepMap.put(map.getKey().toString(),deleteAndKeepMap);
        }
        return stockCardIdWithDeleteAndKeepMap;
    }

    private HashMap<String,HashMap<String,List<StockMovementItem>>> checkDuplicateDataNotAffectCalculate() {
        HashMap<String,HashMap<String,List<StockMovementItem>>> stockCardIdWithDeleteMap = new HashMap<>();
        Map<Integer,List<StockMovementItem>> stockMovementItems = stockMovementRepository.
                queryHavingSignatureAndDuplicatedDirtyDataNoAffectCalculatedStockCardsMovements(filterStockCardIds);

        for(Map.Entry map : stockMovementItems.entrySet()) {
            HashMap<String,List<StockMovementItem>> deleteMap = new HashMap<>();
            List<StockMovementItem> deleteList = new ArrayList<>();
            for (int i=0; i<stockMovementItems.get(map.getKey()).size();i++) {
                if (i < stockMovementItems.get(map.getKey()).size()-2) {
                    deleteList.add(stockMovementItems.get(map.getKey()).get(i));
                }
            }
            sharedPreferenceMgr.setDeletedMovementItems(deleteList);
            deleteMap.put("delete",deleteList);
            stockCardIdWithDeleteMap.put(map.getKey().toString(),deleteMap);
        }
        return stockCardIdWithDeleteMap;
    }
    private List<StockCard> checkTheLastTwoMovementAndLotSOH(List<StockCard> stockCards) {
        List<StockCard> deleted = new ArrayList<>();
        Log.d("performance", "check The Last TwoMovement");
        HashMap<Integer, List<StockMovementItem>> stockMovementItemsMap = getLastStockMovementMap();
        Log.d("performance", "check The Last TwoMovement1");
        List<String> cardIdsLotOnHandLessZero = stockRepository.cardIdsIfLotOnHandLessZero();
        Log.d("performance", "getLastStockMovementMap");
        for (StockCard stockCard : stockCards) {
            List<StockMovementItem> stockMovementItems = stockMovementItemsMap.get((int) stockCard.getId());
            if (!isCorrectOnHand(stockCard, cardIdsLotOnHandLessZero)) {
                deleted.add(stockCard);
            } else if (stockMovementItems != null && stockMovementItems.size() == CHECK_NEWEST_TWO) {
                StockMovementItem movementItemOne = stockMovementItems.get(0);
                StockMovementItem movementItemTwo = stockMovementItems.get(1);
                StockMovementItem currentStockMovement = getCurrentStockMovementItem(movementItemOne, movementItemTwo);
                StockMovementItem preStockMovement = movementItemOne != currentStockMovement ? movementItemOne : movementItemTwo;
                if (!isCorrectMovement(preStockMovement, currentStockMovement)
                        || !isCorrectSOHBetweenMovementAndStockCard(stockCard, currentStockMovement)) {
                    deleted.add(stockCard);
                }
            }
        }
        Log.d("performance", "check The Last TwoMovement");
        Log.d("dirty","daily" + deleted.toString());

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
        Log.d("TwoStockMovements","1");
        List<StockMovementItem> stockMovements = stockMovementRepository.listLastTwoStockMovements();
        Log.d("TwoStockMovements","2");
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
        Log.d("dirty","month"+deleted.toString());
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
