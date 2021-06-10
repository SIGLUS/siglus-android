package org.openlmis.core.presenter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.inject.AbstractModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.repository.MMIARepository;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class BaseRequisitionPresenterTest {

  private BaseRequisitionPresenter presenter;

  private MMIARepository mockRnrFormRepository;

  @Before
  public void setUp() throws Exception {
    mockRnrFormRepository = mock(MMIARepository.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
    MMIARequisitionPresenter.MMIARequisitionView mock = mock(
        MMIARequisitionPresenter.MMIARequisitionView.class);
    presenter = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(MMIARequisitionPresenter.class);
    presenter.attachView(mock);
  }

  @Test
  public void shouldDeleteDraft() throws Exception {
    presenter.isHistoryForm = false;
    presenter.rnRForm = new RnRForm();
    presenter.deleteDraft();
    verify(mockRnrFormRepository).removeRnrForm(presenter.rnRForm);
  }

  public class MyTestModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(MMIARepository.class).toInstance(mockRnrFormRepository);
    }
  }
}