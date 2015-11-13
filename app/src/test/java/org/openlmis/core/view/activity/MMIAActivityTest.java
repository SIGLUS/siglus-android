package org.openlmis.core.view.activity;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Intent;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.presenter.MMIAFormPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.widget.SignatureDialog;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowToast;

import roboguice.RoboGuice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@RunWith(LMISTestRunner.class)
public class MMIAActivityTest {

    private MMIAFormPresenter mmiaFormPresenter;
    private MMIAActivity mmiaActivity;

    @Before
    public void setUp() {
        mmiaFormPresenter = mock(MMIAFormPresenter.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(MMIAFormPresenter.class).toInstance(mmiaFormPresenter);
            }
        });

        Intent intent = new Intent();
        intent.putExtra(Constants.PARAM_FORM_ID, 3);
        mmiaActivity = Robolectric.buildActivity(MMIAActivity.class).withIntent(intent).create().get();
    }

    @Test
    public void shouldShowErrorMessageWhenMethodCalled() {
        mmiaActivity.showErrorMessage("Hello message");

        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo("Hello message");
    }

    @Test
    public void shouldSaveCompleteWhenMethodCalled() {
        mmiaActivity.completeSuccess();

        String successMessage = mmiaActivity.getString(R.string.msg_mmia_submit_tip);
        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo(successMessage);

        assertThat(mmiaActivity.isFinishing()).isTrue();
    }

    @Test
    public void shouldShowValidationAlertWhenMethodCalled() {
        mmiaActivity.showValidationAlert();

        DialogFragment fragment = (DialogFragment) mmiaActivity.getFragmentManager().findFragmentByTag("not_match_dialog");

        assertThat(fragment).isNotNull();

        AlertDialog dialog = (AlertDialog) fragment.getDialog();
        ShadowAlertDialog shadowAlertDialog = shadowOf(dialog);

        String dialogMessage = mmiaActivity.getString(R.string.msg_regime_total_and_patient_total_not_match);
        assertThat(shadowAlertDialog.getMessage()).isEqualTo(dialogMessage);
    }

    @Test
    public void shouldRemoveRnrFormWhenPositiveButtonClicked() {
        mmiaActivity.positiveClick(MMIAActivity.TAG_BACK_PRESSED);

        verify(mmiaFormPresenter).removeRnrForm();
    }

    @Test
    public void shouldNotRemoveRnrFormWhenGoBack() {
        mmiaActivity.onBackPressed();
        verify(mmiaFormPresenter,never()).removeRnrForm();
    }

    @Test
    public void shouldShowSignDialogWhenShowSignDialogCalled() {
        mmiaActivity.showSignDialog();

        SignatureDialog signatureDialog = (SignatureDialog) mmiaActivity.getFragmentManager().findFragmentByTag("signature_dialog");

        assertThat(signatureDialog).isNotNull();
    }

    @Test
    public void shouldAuthorizeFormWhenValidSign() throws Exception {
        mmiaActivity.signatureDialogDelegate.onSign("valid");
        verify(mmiaFormPresenter).authoriseForm("valid");
    }
}