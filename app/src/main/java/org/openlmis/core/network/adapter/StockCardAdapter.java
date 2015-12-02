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

        return stockCard;
    }
}
