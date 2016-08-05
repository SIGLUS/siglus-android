package org.openlmis.core.network.adapter;

import com.google.gson.JsonParser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.JsonFileReader;

import java.text.SimpleDateFormat;

import static junit.framework.Assert.assertEquals;
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
        String movementDateString = new SimpleDateFormat(DateUtil.DB_DATE_FORMAT).format(stockMovementItem.getMovementDate());
        assertEquals("2015-10-10", movementDateString);
        assertThat(stockMovementItem.getMovementType()).isEqualTo(MovementReasonManager.MovementType.NEGATIVE_ADJUST);
        assertThat(stockMovementItem.isSynced()).isEqualTo(true);
    }
}