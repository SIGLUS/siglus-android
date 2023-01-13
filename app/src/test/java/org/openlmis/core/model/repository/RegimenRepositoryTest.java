package org.openlmis.core.model.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.inject.AbstractModule;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.utils.Constants;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class RegimenRepositoryTest {

  private RegimenRepository repository;
  private ProgramRepository mockProgramRepository;

  @Before
  public void setUp() throws Exception {
    mockProgramRepository = mock(ProgramRepository.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(ProgramRepository.class).toInstance(mockProgramRepository);
      }
    });
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
    // given
    Program program = new Program();
    program.setProgramCode(Constants.VIA_PROGRAM_CODE);
    when(mockProgramRepository.queryByCode(anyString())).thenReturn(program);
    Regimen customRegime = new Regimen();
    customRegime.setName("customName");
    customRegime.setCustom(true);
    customRegime.setActive(true);
    customRegime.setProgram(program);
    repository.create(customRegime);

    Regimen customRegime2 = new Regimen();
    customRegime2.setName("default");
    customRegime2.setCustom(false);
    customRegime2.setActive(true);
    customRegime2.setProgram(program);
    repository.create(customRegime2);

    List<Regimen> regimens = repository.listDefaultRegime(Constants.VIA_PROGRAM_CODE);
    assertThat(regimens.size(), is(1));
  }

}