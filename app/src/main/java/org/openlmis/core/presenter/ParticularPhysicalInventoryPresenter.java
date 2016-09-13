package org.openlmis.core.presenter;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.StockCard;
import org.roboguice.shaded.goole.common.base.Predicate;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.List;

public class ParticularPhysicalInventoryPresenter extends PhysicalInventoryPresenter {
    @Override
    protected List<StockCard> getValidStockCardsForPhysicalInventory() throws LMISException {
        return FluentIterable.from(stockRepository.list()).filter(new Predicate<StockCard>() {
            @Override
            public boolean apply(StockCard stockCard) {
                return !stockCard.getProduct().isKit() && stockCard.getStockOnHand() > 0;
            }
        }).toList();
    }
}
