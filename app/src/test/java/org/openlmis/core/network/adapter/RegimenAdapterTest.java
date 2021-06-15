package org.openlmis.core.network.adapter;

import com.google.gson.JsonParser;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Regimen.RegimeType;
import org.openlmis.core.network.model.SyncDownRegimensResponse;
import org.openlmis.core.utils.JsonFileReader;

import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(LMISTestRunner.class)
public class RegimenAdapterTest {

    @Test
    public void shouldDeserializeRegimenSuccess() {
        // given
        RegimenAdapter regimenAdapter = new RegimenAdapter();
        String json = JsonFileReader.readJson(getClass(), "fetchRegimenResponse.json");

        // when
        SyncDownRegimensResponse syncDownRegimensResponse = regimenAdapter.deserialize(new JsonParser().parse(json),null,null);

        // then
        assertThat(syncDownRegimensResponse.getRegimenList().get(0).getCode(), Matchers.is("RG MMC"));
        assertThat(syncDownRegimensResponse.getRegimenList().get(0).getName(),Matchers.is("Regime Genérico MMC"));
        assertThat(syncDownRegimensResponse.getRegimenList().get(1).getType(),Matchers.is(RegimeType.Adults));
        assertThat(syncDownRegimensResponse.getRegimenList().get(2).getType(),Matchers.is(RegimeType.Paediatrics));
    }

}