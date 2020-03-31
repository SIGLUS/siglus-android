package org.openlmis.core.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.DirtyDataItemInfo;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.DirtyDataRepository;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.model.StockMovementEntry;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


@Singleton
public class DirtyDataManager {

    protected LMISRestApi lmisRestApi;

    @Inject
    StockMovementRepository stockMovementRepository;
    @Inject
    StockRepository stockRepository;

    @Inject
    DirtyDataRepository dirtyDataRepository;

    public DirtyDataManager() {
        lmisRestApi = LMISApp.getInstance().getRestApi();
    }

    public List<StockCard> correctData() {
        List<StockCard> stockCards = stockRepository.list();
        List<StockCard> deletedStockCards = new ArrayList<>();
        List<Long> deletedStockCardIds = new ArrayList<>();
        for (StockCard stockCard : stockCards) {
            try {
                List<StockMovementItem> stockMovementItems = stockMovementRepository.listLastTwoStockMovements(stockCard.getId());
                if (stockMovementItems != null && stockMovementItems.size() == 2) {
                    if (!isCorrectMovement(stockMovementItems.get(0), stockMovementItems.get(1))) {
                        deletedStockCards.add(stockCard);
                        deletedStockCardIds.add(stockCard.getId());
                    }
                }
            } catch (LMISException e) {
                e.printStackTrace();
            }
        }

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


        //TODO delete stock card movement here..
        return deletedStockCards;
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

    private boolean isCorrectMovement(StockMovementItem previousMovement, StockMovementItem newestMovement) {
        Long previousSOH = previousMovement.getStockOnHand();
        Long currentSOH = newestMovement.getStockOnHand();
        if (newestMovement.isNegativeMovement()) {
            return currentSOH == previousSOH - newestMovement.getMovementQuantity();
        } else {
            return currentSOH == previousSOH + newestMovement.getMovementQuantity();
        }
    }
}
