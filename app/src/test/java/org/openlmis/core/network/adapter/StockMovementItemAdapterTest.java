package org.openlmis.core.network.adapter;

import com.google.gson.JsonParser;

import org.junit.Assert;
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
import static org.hamcrest.core.Is.is;

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

    @Test
    public void shouldCreateStockMovementWithLotMovementsFromJSON() throws Exception {
        StockMovementItemAdapter stockMovementItemAdapter = new StockMovementItemAdapter();

        String json = JsonFileReader.readJson(getClass(), "StockMovementItemWithLotItems.json");

        StockMovementItem stockMovementItem = stockMovementItemAdapter.deserialize(new JsonParser().parse(json), null, null);

        Assert.assertThat(stockMovementItem.getLotMovementItemListWrapper().size(), is(1));
        Assert.assertThat(stockMovementItem.getLotMovementItemListWrapper().get(0).getMovementQuantity(), is(5L));
        Assert.assertThat(stockMovementItem.getLotMovementItemListWrapper().get(0).getStockOnHand(), is(5L));
        Assert.assertThat(stockMovementItem.getLotMovementItemListWrapper().get(0).getLot().getLotNumber(), is("TEST-A"));
    }
}