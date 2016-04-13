package org.openlmis.core.presenter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.RnRForm;
import org.robolectric.RuntimeEnvironment;

import roboguice.RoboGuice;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@RunWith(LMISTestRunner.class)
public class BaseRequisitionPresenterTest {

    private BaseRequisitionPresenter presenter;

    @Before
    public void setUp() throws Exception {
        MMIARequisitionPresenter.MMIARequisitionView mock = mock(MMIARequisitionPresenter.MMIARequisitionView.class);
        presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(MMIARequisitionPresenter.class);
        presenter.attachView(mock);
    }

    @Test
    public void shouldSetRightSubmittedTypeWhenStatusIsMissed() throws Exception {
        presenter = spy(presenter);
        RnRForm form = new RnRForm();
        doNothing().when(presenter).submitRequisition(form);
        doNothing().when(presenter).authoriseRequisition(form);

        form.setStatus(RnRForm.STATUS.DRAFT_MISSED);

        presenter.processSign("Sign", form);

        assertTrue(form.isMissed());
        assertTrue(form.isSubmitted());

        form.setStatus(RnRForm.STATUS.DRAFT);
        presenter.processSign("Sign", form);
        assertTrue(form.isSubmitted());

        form.setStatus(RnRForm.STATUS.SUBMITTED);
        presenter.processSign("Sign", form);
        assertTrue(form.isAuthorized());
    }
}