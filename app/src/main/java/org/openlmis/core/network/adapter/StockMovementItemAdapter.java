package org.openlmis.core.network.adapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.MovementReasonNotFoundException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.utils.DateUtil;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class StockMovementItemAdapter implements JsonDeserializer<StockMovementItem> {

    private final Gson gson;

    public StockMovementItemAdapter() {
        gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new DateAdapter()).setDateFormat(DateFormat.LONG)
                .create();
    }

    @Override
    public StockMovementItem deserialize(JsonElement json, Type typeOfT,
                                         JsonDeserializationContext context) throws JsonParseException {

        StockMovementItem stockMovementItem = gson.fromJson(json, StockMovementItemResponse.class).convertToStockMovementItem();

        String reason = json.getAsJsonObject().get("reason").getAsString();
        try {
            MovementReasonManager.MovementReason movementReason = MovementReasonManager.getInstance().queryByCode(reason);
            stockMovementItem.setMovementType(movementReason.getMovementType());
            stockMovementItem.setReason(movementReason.getCode());
        } catch (MovementReasonNotFoundException e) {
            new LMISException(e,"StockMovementItemAdapter,deserialize").reportToFabric();
            e.printStackTrace();
        }

        Date createdDate = new Date(json.getAsJsonObject().get("createdDate").getAsLong());

        stockMovementItem.setCreatedTime(createdDate);
        stockMovementItem.setSynced(true);
        return stockMovementItem;
    }

    class StockMovementItemResponse extends StockMovementItem {
        Map<String, String> extensions;
        String occurred;
        List<LotMovementItemResponse> lotMovementItems;

        public StockMovementItem convertToStockMovementItem() {
            final StockMovementItem movementItem = this;
            if (extensions != null) {
                this.setExpireDates(extensions.get("expirationdates"));
                this.setSignature(extensions.get("signature"));
                try {
                    this.setStockOnHand(Long.parseLong(extensions.get("soh")));
                } catch (NumberFormatException e) {
                    new LMISException(e).reportToFabric();
                    e.printStackTrace();
                }
            }
            if (occurred != null) {
                this.setMovementDate(DateUtil.parseString(occurred, DateUtil.DB_DATE_FORMAT));
            }

            if (lotMovementItems != null) {
                List<LotMovementItem> lotMovementItemsWrapper = FluentIterable.from(this.lotMovementItems).transform(new Function<LotMovementItemResponse, LotMovementItem>() {
                    @Override
                    public LotMovementItem apply(LotMovementItemResponse lotMovementItemResponse) {
                        LotMovementItem lotMovementItem = lotMovementItemResponse.convertToLotMovementItem();
                        lotMovementItem.setStockMovementItem(movementItem);
                        return lotMovementItem;
                    }
                }).toList();
                this.setLotMovementItemListWrapper(lotMovementItemsWrapper);
            }
            return movementItem;
        }
    }

    class LotMovementItemResponse extends LotMovementItem {
        Map<String, String> extensions;
        String lotNumber;

        public LotMovementItem convertToLotMovementItem() {
            LotMovementItem lotMovementItem = this;
            if (extensions != null) {
                try {
                    this.setStockOnHand(Long.parseLong(extensions.get("soh")));
                } catch (NumberFormatException e) {
                    new LMISException(e).reportToFabric();
                    e.printStackTrace();
                }
            }
            Lot lot = new Lot();
            lot.setLotNumber(lotNumber);
            lotMovementItem.setLot(lot);
            lotMovementItem.setMovementQuantity(lotMovementItem.getMovementQuantity());
            return lotMovementItem;
        }
    }
}
