package org.openlmis.core.view.activity;

import android.app.Dialog;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.presenter.MMIARequisitionPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import org.openlmis.core.view.widget.MMIAInfoList;
import org.openlmis.core.view.widget.MMIARegimeList;
import org.openlmis.core.view.widget.MMIARnrForm;
import org.openlmis.core.view.widget.SignatureDialog;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowToast;

import java.sql.Date;
import java.util.ArrayList;

import roboguice.RoboGuice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class MMIARequisitionActivityTest {

    private MMIARequisitionPresenter mmiaFormPresenter;
    private MMIARequisitionActivity mmiaRequisitionActivity;
    private Program program;
    private RnRForm form;
    private MMIARegimeList regimeListView;
    private MMIAInfoList mmiaInfoListView;
    private MMIARnrForm rnrFormList;

    @Before
    public void setUp() throws Exception{
        mmiaFormPresenter = mock(MMIARequisitionPresenter.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(MMIARequisitionPresenter.class).toInstance(mmiaFormPresenter);
            }
        });

        Intent intent = new Intent();
        intent.putExtra(Constants.PARAM_FORM_ID, 3);
        mmiaRequisitionActivity = Robolectric.buildActivity(MMIARequisitionActivity.class).withIntent(intent).create().get();

        regimeListView = mock(MMIARegimeList.class);
        mmiaInfoListView = mock(MMIAInfoList.class);
        rnrFormList = mock(MMIARnrForm.class);

        mmiaRequisitionActivity.regimeListView = regimeListView;
        mmiaRequisitionActivity.mmiaInfoListView = mmiaInfoListView;
        mmiaRequisitionActivity.rnrFormList = rnrFormList;

        EditText patientTotalView = mock(EditText.class);
        when(mmiaInfoListView.getPatientTotalView()).thenReturn(patientTotalView);
        
        program = new Program();
        program.setProgramCode("MMIA");
        program.setProgramName("MMIA");
        form = RnRForm.init(program, DateUtil.today());
        form.setId(1L);
        form.setComments("");
        when(mmiaFormPresenter.getRnrForm(anyInt())).thenReturn(form);
    }

    @Test
    public void shouldInitRegimeAndMMIAListView() {
        ArrayList<RegimenItem> regimenItems = new ArrayList<>();
        ArrayList<BaseInfoItem> baseInfoItems = new ArrayList<>();
        form.setBaseInfoItemListWrapper(baseInfoItems);
        form.setRegimenItemListWrapper(regimenItems);

        mmiaRequisitionActivity.initView(form);

        verify(rnrFormList).initView(any(ArrayList.class));
        verify(regimeListView).initView(regimenItems, mmiaRequisitionActivity.tvRegimeTotal);
        verify(mmiaInfoListView).initView(baseInfoItems);

    }

    @Test
    public void shouldShowErrorMessageWhenMethodCalled() {
        mmiaRequisitionActivity.showErrorMessage("Hello message");

        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo("Hello message");
    }

    @Test
    public void shouldSaveCompleteWhenMethodCalled() {
        mmiaRequisitionActivity.completeSuccess();

        String successMessage = mmiaRequisitionActivity.getString(R.string.msg_mmia_submit_tip);
        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo(successMessage);

        assertThat(mmiaRequisitionActivity.isFinishing()).isTrue();
    }

    @Test
    public void shouldShowValidationAlertWhenMethodCalled() {
        mmiaRequisitionActivity.showValidationAlert();

        SimpleDialogFragment fragment = (SimpleDialogFragment) mmiaRequisitionActivity.getFragmentManager().findFragmentByTag("not_match_dialog");

        assertThat(fragment).isNotNull();

        final Dialog dialog = fragment.getDialog();
        assertThat(dialog).isNotNull();
    }

    @Test
    public void shouldRemoveRnrFormWhenPositiveButtonClicked() {
        mmiaRequisitionActivity.positiveClick(MMIARequisitionActivity.TAG_BACK_PRESSED);

        verify(mmiaFormPresenter).removeRnrForm();
    }

    @Test
    public void shouldNotRemoveRnrFormWhenGoBack() {
        mmiaRequisitionActivity.onBackPressed();
        verify(mmiaFormPresenter,never()).removeRnrForm();
    }

    @Test
    public void shouldShowSignDialogWhenShowSignDialogCalled() {
        mmiaRequisitionActivity.showSignDialog();

        SignatureDialog signatureDialog = (SignatureDialog) mmiaRequisitionActivity.getFragmentManager().findFragmentByTag("signature_dialog");

        assertThat(signatureDialog).isNotNull();
    }

    @Test
    public void shouldAuthorizeFormWhenValidSign() throws Exception {
        mmiaRequisitionActivity.signatureDialogDelegate.onSign("valid");
        verify(mmiaFormPresenter).authoriseForm("valid");
    }

    @Test
    public void shouldShowSaveAndCompleteButtonWhenFormIsEditable() {
        when(mmiaFormPresenter.formIsEditable()).thenReturn(true);

        mmiaRequisitionActivity.initView(form);

        assertThat(mmiaRequisitionActivity.btnSave.getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(mmiaRequisitionActivity.btnComplete.getVisibility()).isEqualTo(View.VISIBLE);
    }

    @Test
    public void shouldNotShowSaveAndCompleteButtonWhenFormIsNotEditable() {
        when(mmiaFormPresenter.formIsEditable()).thenReturn(false);

        mmiaRequisitionActivity.initView(form);

        assertThat(mmiaRequisitionActivity.btnSave.getVisibility()).isEqualTo(View.GONE);
        assertThat(mmiaRequisitionActivity.btnComplete.getVisibility()).isEqualTo(View.GONE);
    }

    @Test
    public void shouldSetTitleWithPeriodWhenToggleOn() throws Exception {
        form.setPeriodBegin(Date.valueOf("2015-04-21"));
        form.setPeriodEnd(Date.valueOf("2015-05-20"));

        ((LMISTestApp) RuntimeEnvironment.application).setFeatureToggle(true);

        mmiaRequisitionActivity.initView(form);

        assertThat(mmiaRequisitionActivity.getTitle()).isEqualTo("MMIA - 21 Apr to 20 May");
    }

    @Test
    public void shouldSetTitleDependsOnFormIsEditableWhenToggleOff() throws Exception {
        form.setPeriodBegin(Date.valueOf("2015-04-21"));
        form.setPeriodEnd(Date.valueOf("2015-05-20"));

        ((LMISTestApp) RuntimeEnvironment.application).setFeatureToggle(false);
        when(mmiaFormPresenter.getRnrForm(form.getId())).thenReturn(form);

        when(mmiaFormPresenter.formIsEditable()).thenReturn(true);
        mmiaRequisitionActivity.initView(form);
        assertThat(mmiaRequisitionActivity.getTitle()).isEqualTo("MMIA");


        when(mmiaFormPresenter.formIsEditable()).thenReturn(false);
        when(mmiaFormPresenter.getRnrForm(1L)).thenReturn(form);
        mmiaRequisitionActivity.initView(form);
        assertThat(mmiaRequisitionActivity.getTitle()).isEqualTo("21 Apr 2015  to  20 May 2015");
    }

    @Test
    public void shouldDehighlightWhenTotalMatches() {
        when(regimeListView.getTotal()).thenReturn(20L);
        when(mmiaInfoListView.getTotal()).thenReturn(20L);

        mmiaRequisitionActivity.regimeListView = regimeListView;
        mmiaRequisitionActivity.mmiaInfoListView = mmiaInfoListView;

        mmiaRequisitionActivity.initView(form);

        verify(regimeListView).deHighLightTotal();
        verify(mmiaInfoListView).deHighLightTotal();
        assertThat(mmiaRequisitionActivity.tvMismatch.getVisibility()).isEqualTo(View.INVISIBLE);
    }

    @Test
    public void shouldHighlightWhenTotalNotMatches() {
        when(regimeListView.getTotal()).thenReturn(20L);
        when(mmiaInfoListView.getTotal()).thenReturn(40L);

        mmiaRequisitionActivity.regimeListView = regimeListView;
        mmiaRequisitionActivity.mmiaInfoListView = mmiaInfoListView;

        mmiaRequisitionActivity.initView(form);

        verify(regimeListView).highLightTotal();
        verify(mmiaInfoListView).highLightTotal();
        assertThat(mmiaRequisitionActivity.tvMismatch.getVisibility()).isEqualTo(View.VISIBLE);
    }
}