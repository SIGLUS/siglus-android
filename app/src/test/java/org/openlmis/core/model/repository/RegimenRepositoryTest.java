package org.openlmis.core.model.repository;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.RegimeShortCode;
import org.openlmis.core.model.Regimen;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(LMISTestRunner.class)
public class RegimenRepositoryTest {

    private RegimenRepository repository;

    @Before
    public void setUp() throws Exception {
        repository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(RegimenRepository.class);
    }

    @Test
    public void shouldQueryByNameSuccessful() throws Exception {
        Regimen regimen = new Regimen();
        String regimeName = "regimeName";
        regimen.setType(Regimen.RegimeType.Adults);
        regimen.setName(regimeName);
        repository.create(regimen);

        Regimen actualRegime = repository.getByNameAndCategory(regimeName, Regimen.RegimeType.Adults);
        assertThat(actualRegime.getName(), is(regimeName));
    }

    @Test
    public void shouldListDefaultRegime() throws Exception {
        Regimen customRegime = new Regimen();
        customRegime.setName("customName");
        customRegime.setCustom(true);
        repository.create(customRegime);

        Regimen customRegime2 = new Regimen();
        customRegime2.setName("default");
        customRegime.setCustom(false);
        repository.create(customRegime2);

        List<Regimen> regimens = repository.listDefaultRegime();
        assertThat(regimens.size(), is(1));
    }


    @Test
    public void shouldListRegimeShortCodeAdults() throws Exception {
        List<RegimeShortCode> regimeShortCodes = repository.listRegimeShortCode(Regimen.RegimeType.Adults);
        assertThat(regimeShortCodes.size(), is(13));
        assertThat(regimeShortCodes.get(0).getShortCode(), is("ABC+3TC+EFV"));
        assertThat(regimeShortCodes.get(12).getCode(), is("AdultPlus13"));
    }

    @Test
    public void shouldListRegimeShortCodePaediatrics() throws Exception {
        List<RegimeShortCode> regimeShortCodes = repository.listRegimeShortCode(Regimen.RegimeType.Paediatrics);
        assertThat(regimeShortCodes.size(), is(1));
        assertThat(regimeShortCodes.get(0).getShortCode(), is("AZT+3TC+ABC (2FDC+ABC Baby)"));
//        assertThat(regimeShortCodes.get(1).getCode(), is("08S01B"));
    }
}