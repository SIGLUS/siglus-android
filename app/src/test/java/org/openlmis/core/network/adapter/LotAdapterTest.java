package org.openlmis.core.network.adapter;

import com.google.gson.JsonParser;

import org.junit.Test;
import org.openlmis.core.model.Lot;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.JsonFileReader;

import static org.junit.Assert.assertEquals;

public class LotAdapterTest {
    @Test
    public void shouldCreateLotFromJson() throws Exception {
        LotAdapter lotAdapter = new LotAdapter();

        String json = JsonFileReader.readJson(getClass(), "Lot.json");

        Lot lot = lotAdapter.deserialize(new JsonParser().parse(json), null, null);

        assertEquals("6MK07", lot.getLotNumber());
        assertEquals(DateUtil.parseString("2019-10-30", DateUtil.DB_DATE_FORMAT), lot.getExpirationDate());
    }
}