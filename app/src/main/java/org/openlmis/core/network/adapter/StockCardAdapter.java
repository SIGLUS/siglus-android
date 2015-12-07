package org.openlmis.core.network.adapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.inject.Inject;

import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.StockRepository;

import java.util.List;

import roboguice.RoboGuice;

public class StockCardAdapter implements JsonDeserializer<StockCard> {

    private final Gson gson;
    @Inject
    private ProductRepository productRepository;

    @Inject
    private StockRepository stockRepository;

    @Inject
    public StockCardAdapter() {
        RoboGuice.getInjector(LMISApp.getContext()).injectMembersWithoutViews(this);
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(StockMovementItem.class, new StockMovementItemAdapter())
                .registerTypeAdapter(Product.class, new ProductsAdapter())
                .create();
    }

    @Override
    public StockCard deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        StockCard stockCard = gson.fromJson(json, StockCard.class);

        List<StockMovementItem> wrapper = stockCard.getStockMovementItemsWrapper();
        if (wrapper == null) {
            return stockCard;
        }
        //the product is synced before stock card
        //stock cards will be synced at the first time,
        //if synced more than once, will be ignored and won't insert to local db
        try {
            stockCard.setProduct(productRepository.getByCode(stockCard.getProduct().getCode()));

            StockCard stockCardInDB = stockRepository.queryStockCardByProductId(stockCard.getProduct().getId());
            if (stockCardInDB != null) {
                stockCard.setId(stockCardInDB.getId());
            }
        } catch (LMISException e) {
            e.reportToFabric();
        }

        setupMovementStockOnHand(stockCard, wrapper);
        setupStockCardExpireDates(stockCard, wrapper);

        return stockCard;
    }

    public void setupStockCardExpireDates(StockCard stockCard, List<StockMovementItem> wrapper) {
        int size = wrapper.size();
        if (size > 0) {
            stockCard.setExpireDates(wrapper.get(size - 1).getExpireDates());
        }
    }

    public void setupMovementStockOnHand(StockCard stockCard, List<StockMovementItem> wrapper) {
        long stockOnHand = stockCard.getStockOnHand();
        for (int i = wrapper.size() - 1; i >= 0; i--) {
            StockMovementItem item = wrapper.get(i);

            item.setStockCard(stockCard);
            item.setStockOnHand(stockOnHand);
            stockOnHand = item.calculateStockMovementStockOnHand(stockOnHand);
        }
    }


}
