package org.openlmis.core.model.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
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
        repository.create(customRegime);

        List<Regimen> regimens = repository.listDefaultRegime();
        assertThat(regimens.size(), is(18));
    }
}