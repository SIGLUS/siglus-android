package org.openlmis.core.network.adapter;

import com.google.gson.JsonElement;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

@RunWith(LMISTestRunner.class)
public class RegimenItemAdapterTest {
    @Test
    public void shouldSerializeRegimenItem() throws Exception {
        RegimenItem regimenItem = new RegimenItem();
        Regimen regimen = new Regimen();
        regimen.setCode("regimenCode");
        regimen.setName("regimenName");
        regimen.setType(Regimen.RegimeType.Adults);

        regimenItem.setAmount(100L);
        regimenItem.setRegimen(regimen);

        RegimenItemAdapter regimenItemAdapter = new RegimenItemAdapter();
        JsonElement json = regimenItemAdapter.serialize(regimenItem, null, null);

        assertThat(json).isNotNull();
        assertThat(json.getAsJsonObject().get("categoryName")).isNotNull();
        assertEquals(json.getAsJsonObject().get("categoryName"), is("Adults"));
    }
}