package org.openlmis.core.model.repository;

import static junit.framework.Assert.assertNull;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.inject.AbstractModule;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.ProgramDataForm.Status;
import org.openlmis.core.model.ProgramDataFormItem;
import org.openlmis.core.model.ProgramDataFormSignature;
import org.openlmis.core.model.Signature;
import org.openlmis.core.model.builder.ProgramDataColumnBuilder;
import org.openlmis.core.model.builder.ProgramDataFormBuilder;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
@SuppressWarnings("PMD")
public class ProgramDataFormRepositoryTest extends LMISRepositoryUnitTest {

  private ProgramRepository mockProgramRepository;
  private ProgramDataFormRepository programDataFormRepository;
  private Program programRapidTest;

  @Before
  public void setup() throws LMISException {
    mockProgramRepository = mock(ProgramRepository.class);

    programDataFormRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(ProgramDataFormRepository.class);

    programRapidTest = new Program("RapidTest", "Rapid Test", null, false, null, null);
    programRapidTest.setId(1L);
  }

}