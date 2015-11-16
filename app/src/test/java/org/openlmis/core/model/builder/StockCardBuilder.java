package org.openlmis.core.model.builder;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.DateUtil;

import java.text.ParseException;
import java.util.Date;

public class StockCardBuilder {

    private StockCard stockCard;

    public StockCardBuilder() {
        stockCard = new StockCard();
    }

    public static StockCard buildStockCardWithOneMovement(StockRepository stockRepository) throws LMISException, ParseException {

        StockMovementItem stockMovementItem = new StockMovementItem();

        StockCard stockCard = new StockCard();
        stockRepository.save(stockCard);

        stockMovementItem.setStockCard(stockCard);
        stockMovementItem.setMovementQuantity(10L);
        stockMovementItem.setStockOnHand(100L);
        stockMovementItem.setMovementType(StockMovementItem.MovementType.RECEIVE);
        stockMovementItem.setDocumentNumber("XXX123456");
        stockMovementItem.setReason("some reason");
        stockMovementItem.setMovementDate(DateUtil.parseString("2015-11-11", "yyyy-MM-dd"));

        stockRepository.saveStockItem(stockMovementItem);
        stockCard.setStockOnHand(100L);
        stockRepository.update(stockCard);
        stockRepository.refresh(stockCard);

        return stockCard;
    }

    public StockCardBuilder setExpireDates(String expireDates) {
        stockCard.setExpireDates(expireDates);
        return this;
    }

    public StockCardBuilder setProduct(Product product) {
        stockCard.setProduct(product);
        return this;
    }

    public StockCardBuilder setStockOnHand(long stockOnHand) {
        stockCard.setStockOnHand(stockOnHand);
        return this;
    }

    public StockCard build() {
        return stockCard;
    }

    public static StockCard buildStockCard() {
        return new StockCardBuilder()
                .setExpireDates("2020-10-01")
                .setProduct(ProductBuilder.buildAdultProduct())
                .setStockOnHand(200)
                .build();
    }
}
