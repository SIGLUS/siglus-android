package org.openlmis.core.model.repository;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.persistence.GenericDao;
import org.robolectric.RuntimeEnvironment;

import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class ProgramDataFormRepositoryTest extends LMISRepositoryUnitTest {

  @Mock
  GenericDao<ProgramDataForm> genericDaoMock;

  private ProgramDataFormRepository programDataFormRepository;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    programDataFormRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
            .getInstance(ProgramDataFormRepository.class);
    programDataFormRepository.genericDao = genericDaoMock;
  }
  @Test
  public void shouldQueryProgramData() throws Exception {
    // when
    programDataFormRepository.list();

    // then
   verify(genericDaoMock).queryForAll();
  }

}