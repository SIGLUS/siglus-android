package org.openlmis.core.network.adapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.inject.Inject;

import org.openlmis.core.LMISApp;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;

import java.util.List;

import roboguice.RoboGuice;

public class StockCardAdapter implements JsonDeserializer<StockCard> {

    private final Gson gson;

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

        setupMovementStockOnHand(stockCard, wrapper);
        setupStockCardExpireDates(stockCard, wrapper);

        return stockCard;
    }

    public void setupStockCardExpireDates(StockCard stockCard, List<StockMovementItem> wrapper) {
        if (wrapper.size() > 0) {
            stockCard.setExpireDates(wrapper.get(0).getExpireDates());
        }
    }

    public void setupMovementStockOnHand(StockCard stockCard, List<StockMovementItem> wrapper) {
        long stockOnHand = stockCard.getStockOnHand();
        for (StockMovementItem item : wrapper) {
            item.setStockCard(stockCard);
            item.setStockOnHand(stockOnHand);
            stockOnHand = item.calculateStockMovementStockOnHand(stockOnHand);
        }
    }


}
