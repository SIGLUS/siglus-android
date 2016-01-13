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

    public static StockCard saveStockCardWithOneMovement(StockRepository stockRepository) throws LMISException, ParseException {
        StockCard stockCard = new StockCard();
        stockCard.setStockOnHand(90L);
        stockRepository.save(stockCard);

        StockMovementItem stockMovementItem = new StockMovementItem();
        stockMovementItem.setStockCard(stockCard);
        stockMovementItem.setMovementQuantity(10L);
        stockMovementItem.setStockOnHand(100L);
        stockMovementItem.setMovementType(StockMovementItem.MovementType.RECEIVE);
        stockMovementItem.setDocumentNumber("XXX123456");
        stockMovementItem.setReason("some reason");
        stockMovementItem.setMovementDate(DateUtil.parseString("2015-11-11", "yyyy-MM-dd"));

        stockCard.setStockOnHand(stockMovementItem.getStockOnHand());
        stockRepository.addStockMovementAndUpdateStockCard(stockMovementItem);
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

    public StockCardBuilder setStockCardId(long stockCardId) {
        stockCard.setId(stockCardId);
        return this;
    }

    public StockCardBuilder setCreateDate(Date date) {
        stockCard.setCreatedAt(date);
        return this;
    }
}
