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

    @Ignore
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
        assertThat(regimens.size(), is(19));
    }

    //TODO add adult
    @Test
    public void shouldListRegimeShortCodeAdults() throws Exception {
        List<RegimeShortCode> regimeShortCodes = repository.listRegimeShortCode(Regimen.RegimeType.Adults);
        assertThat(regimeShortCodes.size(), is(16));
        assertThat(regimeShortCodes.get(0).getShortCode(), is("ABC+3TC+DTG"));
        assertThat(regimeShortCodes.get(15).getCode(), is("08S19"));
    }

    @Test
    public void shouldListRegimeShortCodePaediatrics() throws Exception {
        List<RegimeShortCode> regimeShortCodes = repository.listRegimeShortCode(Regimen.RegimeType.Paediatrics);
        assertThat(regimeShortCodes.size(), is(2));
        assertThat(regimeShortCodes.get(0).getShortCode(), is("ABC 60mg+3TC 30mg"));
        assertThat(regimeShortCodes.get(1).getCode(), is("08S01B"));
    }
}