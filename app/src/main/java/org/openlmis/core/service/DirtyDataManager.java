package org.openlmis.core.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.network.LMISRestApi;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class DirtyDataManager {

    protected LMISRestApi lmisRestApi;

    @Inject
    StockMovementRepository stockMovementRepository;
    @Inject
    StockRepository stockRepository;

    public DirtyDataManager() {
        lmisRestApi = LMISApp.getInstance().getRestApi();
    }

    public List<StockCard> correctData() {
        List<StockCard> stockCards = stockRepository.list();
        List<StockCard> deletedStockCards = new ArrayList<>();
        for (StockCard stockCard : stockCards) {
            try {
                List<StockMovementItem> stockMovementItems = stockMovementRepository.listLastTwoStockMovements(stockCard.getId());
                if (stockMovementItems != null && stockMovementItems.size() == 2) {
                    if (!isCorrectMovement(stockMovementItems.get(0), stockMovementItems.get(1))) {
                        //TODO delete stock card here..
                        deletedStockCards.add(stockCard);
                    }
                }
            } catch (LMISException e) {
                e.printStackTrace();
            }
        }
        return deletedStockCards;
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
