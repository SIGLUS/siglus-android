package org.openlmis.core.model.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Regimen;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class RegimenRepositoryTest {

  private RegimenRepository repository;

  @Before
  public void setUp() throws Exception {
    repository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(RegimenRepository.class);
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
    customRegime.setActive(true);
    repository.create(customRegime);

    Regimen customRegime2 = new Regimen();
    customRegime2.setName("default");
    customRegime.setCustom(false);
    customRegime2.setActive(true);
    repository.create(customRegime2);

    List<Regimen> regimens = repository.listDefaultRegime();
    assertThat(regimens.size(), is(1));
  }

}