package org.openlmis.core.network.adapter;

import com.google.gson.JsonParser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.utils.JsonFileReader;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(LMISTestRunner.class)
public class StockMovementItemAdapterTest {
    @Test
    public void shouldCreateStockMovementFromJSON() throws Exception {

        StockMovementItemAdapter stockMovementItemAdapter = new StockMovementItemAdapter();

        String json = JsonFileReader.readJson(getClass(), "StockMovementItem.json");

        StockMovementItem stockMovementItem = stockMovementItemAdapter.deserialize(new JsonParser().parse(json), null, null);

        assertThat(stockMovementItem).isNotNull();
        assertThat(stockMovementItem.getDocumentNumber()).isEqualTo("referenceNumber3");
        assertThat(stockMovementItem.getMovementQuantity()).isEqualTo(20);
        assertThat(stockMovementItem.getSignature()).isEqualTo("signature");
        assertThat(stockMovementItem.getReason()).isEqualTo("LOANS_DEPOSIT");
        assertThat(stockMovementItem.getMovementType()).isEqualTo(StockMovementItem.MovementType.NEGATIVE_ADJUST);
    }
}