package org.openlmis.core.network.adapter;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.repository.RegimenRepository;
import org.openlmis.core.utils.JsonFileReader;
import org.robolectric.RuntimeEnvironment;

import roboguice.RoboGuice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class RegimenItemAdapterTest {

    private RegimenRepository regimenRepository;

    @Before
    public void setUp() throws Exception {
        regimenRepository = mock(RegimenRepository.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
    }


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

        assertNotNull(json);
        assertThat(json.getAsJsonObject().get("categoryName").getAsString(), is("Adults"));
    }

    @Test
    public void shouldDeSerializeRegimenItem() throws Exception {
        when(regimenRepository.getByCode("020")).thenReturn(null);

        String json = JsonFileReader.readJson(getClass(), "RegimenItemResponse.json");

        RegimenItemAdapter regimenItemAdapter = new RegimenItemAdapter();

        RegimenItem deserialize = regimenItemAdapter.deserialize(new JsonParser().parse(json), null, null);

        Regimen regimen = deserialize.getRegimen();
        assertThat(regimen.getType(), is(Regimen.RegimeType.Adults));
        assertThat(regimen.getCode(), is("020"));
        assertThat(regimen.getName(), is("d4T 30+3TC+EFV"));

        verify(regimenRepository).create(regimen);
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(RegimenRepository.class).toInstance(regimenRepository);
        }
    }
}