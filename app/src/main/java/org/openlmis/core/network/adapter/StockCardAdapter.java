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
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.LotRepository;
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
    private LotRepository lotRepository;

    @Inject
    public StockCardAdapter() {
        RoboGuice.getInjector(LMISApp.getContext()).injectMembersWithoutViews(this);
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(StockMovementItem.class, new StockMovementItemAdapter())
                .registerTypeAdapter(Product.class, new ProductAdapter())
                .registerTypeAdapter(Lot.class, new LotAdapter())
                .create();
    }

    @Override
    public StockCard deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        StockCard stockCard = gson.fromJson(json, StockCard.class);
        try {
            setupStockCard(stockCard);

            setupLotOnHandList(stockCard);

            setupProductAndStockCardOfMovementItems(stockCard);
        } catch (LMISException e) {
            new LMISException(e,"StockCardAdapter.deserialize").reportToFabric();
        }
        return stockCard;
    }

    private void setupStockCard(StockCard stockCard) throws LMISException {
        stockCard.setProduct(productRepository.getByCode(stockCard.getProduct().getCode()));
        updateStockCardIdIfStockCardAlreadyExist(stockCard);
        setupStockCardExpireDates(stockCard);
    }

    private void setupLotOnHandList(StockCard stockCard) throws LMISException {
        for (LotOnHand lotOnHand : stockCard.getLotOnHandListWrapper()) {
            lotOnHand.getLot().setProduct(stockCard.getProduct());
            lotOnHand.setStockCard(stockCard);
            updateLotOnHandIdAndLotIfLotAlreadyExist(lotOnHand);
        }
    }

    private void updateLotOnHandIdAndLotIfLotAlreadyExist(LotOnHand lotOnHand) throws LMISException {
        Product product = lotOnHand.getLot().getProduct();
        Lot existingLot = lotRepository.getLotByLotNumberAndProductId(lotOnHand.getLot().getLotNumber(), product.getId());
        if (existingLot != null) {
            lotOnHand.setId(lotRepository.getLotOnHandByLot(existingLot).getId());
            lotOnHand.setLot(existingLot);
        }
    }

    private void updateStockCardIdIfStockCardAlreadyExist(StockCard stockCard) throws LMISException {
        StockCard stockCardInDB = stockRepository.queryStockCardByProductId(stockCard.getProduct().getId());
        if (stockCardInDB != null) {
            stockCard.setId(stockCardInDB.getId());
        }
    }

    public void setupStockCardExpireDates(StockCard stockCard) {
        List<StockMovementItem> wrapper = stockCard.getStockMovementItemsWrapper();
        int size = wrapper.size();
        if (size > 0) {
            stockCard.setExpireDates(wrapper.get(size - 1).getExpireDates());
        }
    }

    public void setupProductAndStockCardOfMovementItems(StockCard stockCard) {
        for (StockMovementItem stockMovementItem : stockCard.getStockMovementItemsWrapper()) {
            stockMovementItem.setStockCard(stockCard);
            for (LotMovementItem lotMovementItem : stockMovementItem.getLotMovementItemListWrapper()) {
                lotMovementItem.getLot().setProduct(stockCard.getProduct());
            }
        }
    }

}
