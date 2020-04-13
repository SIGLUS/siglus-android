package org.openlmis.core.service;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.DirtyDataItemInfo;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.CmmRepository;
import org.openlmis.core.model.repository.DirtyDataRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.model.StockMovementEntry;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;


@Singleton
public class DirtyDataManager {

    private static final String TAG = DirtyDataManager.class.getSimpleName();

    protected LMISRestApi lmisRestApi;

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
    SharedPreferenceMgr sharedPreferenceMgr;

    public DirtyDataManager() {
        lmisRestApi = LMISApp.getInstance().getRestApi();
    }

    public List<StockCard> correctData() {
        List<StockCard> stockCards = stockRepository.list();

        List<StockCard> deletedStockCards = getWrongStockCard(stockCards);
        saveDeletedInfoToDB(deletedStockCards);

        List<String> productCodes = FluentIterable.from(deletedStockCards).transform(new Function<StockCard, String>() {
            @Nullable
            @Override
            public String apply(@Nullable StockCard stockCard) {
                return stockCard.getProduct().getCode();
            }
        }).toList();
        deleteAndReset(productCodes);
        sharedPreferenceMgr.setDeletedProduct(productCodes);
        return deletedStockCards;
    }

    private void saveDeletedInfoToDB(List<StockCard> deletedStockCards) {
        for (StockCard stockCard : deletedStockCards) {
            List<StockMovementItem> stockMovementItems = null;
            try {
                stockMovementItems = stockMovementRepository.queryMovementByStockCardId(stockCard.getId());
            } catch (LMISException e) {
                e.printStackTrace();
            }
            final String facilityId = UserInfoMgr.getInstance().getUser().getFacilityId();
            DirtyDataItemInfo dirtyDataItems = convertStockMovementItemsToStockMovementEntriesForSave(
                    facilityId,
                    stockMovementItems,
                    stockCard.getProduct().getCode());

            dirtyDataRepository.save(dirtyDataItems);
        }
    }

    private void deleteAndReset(List<String> productCodes) {
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
        }
    }

    private DirtyDataItemInfo convertStockMovementItemsToStockMovementEntriesForSave(final String facilityId,
                                                                                     List<StockMovementItem> stockMovementItems,
                                                                                     String productCode) {
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
        return new DirtyDataItemInfo(productCode, false, gson.toJson(movementEntries, type));
    }

    private List<StockCard> getWrongStockCard(List<StockCard> stockCards) {
        List<StockCard> deleted = new ArrayList<>();
        for (StockCard stockCard : stockCards) {
            try {
                List<StockMovementItem> stockMovementItems = stockMovementRepository.listLastTwoStockMovements(stockCard.getId());
                if (stockMovementItems != null && stockMovementItems.size() == 2) {
                    if (!isCorrectMovement(stockMovementItems.get(0), stockMovementItems.get(1))
                            || !isCorrectLotOnHand(stockCard)) {
                        deleted.add(stockCard);
                    }
                }
            } catch (LMISException e) {
                e.printStackTrace();
            }
        }
        return deleted;
    }

    private boolean isCorrectLotOnHand(StockCard stockCard) {
        List<LotOnHand> lotOnHands = stockCard.getLotOnHandListWrapper();
        if (CollectionUtils.isEmpty(lotOnHands)) {
            return true;
        }
        return FluentIterable.from(lotOnHands).allMatch(lotOnHand ->
                lotOnHand != null && lotOnHand.getQuantityOnHand() >= 0);
    }

    private boolean isCorrectMovement(StockMovementItem previousMovement, StockMovementItem newestMovement) {
        Long previousSOH = previousMovement.getStockOnHand();
        Long currentSOH = newestMovement.getStockCard().calculateSOHFromLots();
        if (newestMovement.isNegativeMovement()) {
            return currentSOH == previousSOH - newestMovement.getMovementQuantity();
        } else if (newestMovement.isPositiveMovement()) {
            return currentSOH == previousSOH + newestMovement.getMovementQuantity();
        } else {
            return true;
        }
    }
}
