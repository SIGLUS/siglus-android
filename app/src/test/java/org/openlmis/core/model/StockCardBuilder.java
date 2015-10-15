package org.openlmis.core.model;

public class StockCardBuilder {

    private StockCard stockCard;

    public StockCardBuilder() {
        stockCard = new StockCard();
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
