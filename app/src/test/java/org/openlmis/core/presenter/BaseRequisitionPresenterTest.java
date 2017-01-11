package org.openlmis.core.presenter;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.robolectric.RuntimeEnvironment;

import roboguice.RoboGuice;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(LMISTestRunner.class)
public class BaseRequisitionPresenterTest {

    private BaseRequisitionPresenter presenter;

    private RnrFormRepository mockRnrFormRepository;

    @Before
    public void setUp() throws Exception {
        mockRnrFormRepository = mock(RnrFormRepository.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        MMIARequisitionPresenter.MMIARequisitionView mock = mock(MMIARequisitionPresenter.MMIARequisitionView.class);
        presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(MMIARequisitionPresenter.class);
        presenter.attachView(mock);
    }

    @Test
    public void shouldDeleteDraft() throws Exception {
        presenter.isHistoryForm = false;
        presenter.deleteDraft();
        verify(mockRnrFormRepository).removeRnrForm(any(RnRForm.class));
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(RnrFormRepository.class).toInstance(mockRnrFormRepository);
        }
    }
}