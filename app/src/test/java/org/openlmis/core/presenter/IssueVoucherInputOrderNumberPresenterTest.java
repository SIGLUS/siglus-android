package org.openlmis.core.presenter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.inject.AbstractModule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.repository.PodRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.utils.Constants;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;
import rx.Observable;
import rx.observers.TestSubscriber;

@RunWith(LMISTestRunner.class)
public class IssueVoucherInputOrderNumberPresenterTest {

  private ProgramRepository mockProgramRepository;

  private PodRepository mockPodRepository;

  private IssueVoucherInputOrderNumberPresenter presenter;

  @Before
  public void setUp() throws Exception {
    mockProgramRepository = mock(ProgramRepository.class);
    mockPodRepository = mock(PodRepository.class);

    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(ProgramRepository.class).toInstance(mockProgramRepository);
        bind(PodRepository.class).toInstance(mockPodRepository);
      }
    });
    presenter = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(IssueVoucherInputOrderNumberPresenter.class);
  }

  @Test
  public void shouldLoadPrograms() throws LMISException {
    // given
    Program program1 = Program.builder().programCode("TR").programName("Testes Rápidos Diag.").build();
    Program program2 = Program.builder().programCode("T").programName("TARV").build();
    Program program3 = Program.builder().programCode("VC").programName("Via Clássica").build();
    List<Program> programs = new ArrayList<>();
    programs.add(program1);
    programs.add(program2);
    programs.add(program3);
    when(mockProgramRepository.queryProgramWithoutML()).thenReturn(programs);

    // when
    TestSubscriber<List<Program>> subscriber = new TestSubscriber<>();
    Observable<List<Program>> observable = presenter.loadData();
    observable.subscribe(subscriber);
    subscriber.awaitTerminalEvent(1, TimeUnit.SECONDS);
    subscriber.assertNoErrors();
    List<Program> resultPrograms = subscriber.getOnNextEvents().get(0);

    // then
    assertEquals(3, resultPrograms.size());

  }

  @Test
  public void isOrderNumberExistedShouldBeTrue() {
    // given
    Pod pod = Pod.builder().orderCode("ORDER-123456789").build();
    presenter.setExistingPods(Collections.singletonList(pod));

    // when
    boolean result = presenter.isOrderNumberExisted("ORDER-123456789");

    // then
    assertTrue(result);
  }

  @Test
  public void testIsProgramAvailable() {
    // given
    Pod pod = new Pod();
    pod.setLocal(true);
    presenter.getProgramCodeToIssueVoucher().put(Constants.Program.VIA_PROGRAM.getCode(), pod);
    Program program = new Program();
    program.setProgramCode(Constants.Program.VIA_PROGRAM.getCode());

    // then
    assertNotNull(presenter.getSameProgramIssueVoucher(program));
  }
}