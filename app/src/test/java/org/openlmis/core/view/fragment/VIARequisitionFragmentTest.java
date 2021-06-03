package org.openlmis.core.view.fragment;/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */


import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.google.inject.Binder;
import com.google.inject.Module;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.builder.RequisitionBuilder;
import org.openlmis.core.presenter.VIARequisitionPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.activity.VIARequisitionActivity;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;
import org.openlmis.core.view.viewmodel.ViaKitsViewModel;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowToast;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import androidx.appcompat.app.AlertDialog;
import roboguice.RoboGuice;

import static android.os.Looper.getMainLooper;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(LMISTestRunner.class)
public class VIARequisitionFragmentTest {

    VIARequisitionFragment viaRequisitionFragment;
    VIARequisitionPresenter presenter;
    private List<RequisitionFormItemViewModel> formItemList;
    private Program program;
    private RnRForm form;

    @Before
    public void setup() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        DateTimeZone.setDefault(DateTimeZone.UTC);
        presenter = mock(VIARequisitionPresenter.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(VIARequisitionPresenter.class).toInstance(presenter);
            }
        });
        program = new Program();
        program.setProgramCode("ESS_MEDS");
        program.setProgramName("ESS_MEDS");

        form = RnRForm.init(program, DateUtil.today());
        form.setPeriodBegin(Date.valueOf("2015-04-21"));
        form.setPeriodEnd(Date.valueOf("2015-05-20"));

        formItemList = new ArrayList<>();
        formItemList.add(RequisitionBuilder.buildFakeRequisitionViewModel());
        when(presenter.getRequisitionFormItemViewModels()).thenReturn(formItemList);
        when(presenter.getViaKitsViewModel()).thenReturn(new ViaKitsViewModel());
        when(presenter.getRnRForm()).thenReturn(form);
        when(presenter.getRnrFormStatus()).thenReturn(RnRForm.STATUS.DRAFT);

        SharedPreferenceMgr.getInstance().setShouldSyncLastYearStockCardData(false);
        viaRequisitionFragment = getVIARequisitionFragmentFromActivityWithIntent();
    }

    private VIARequisitionFragment getVIARequisitionFragmentFromActivityWithIntent() {
        Intent intent = new Intent();
        intent.putExtra(Constants.PARAM_FORM_ID, 1L);
        VIARequisitionActivity viaRequisitionActivity = Robolectric.buildActivity(VIARequisitionActivity.class, intent).create().visible().get();
        return (VIARequisitionFragment) viaRequisitionActivity.getFragmentManager().findFragmentById(R.id.fragment_requisition);
    }

    @Test
    public void shouldShowRequisitionPeriodOnTitle() {
        viaRequisitionFragment.refreshRequisitionForm(viaRequisitionFragment.presenter.getRnRForm());

        assertThat(viaRequisitionFragment.getActivity().getTitle()).isEqualTo("Requisition - 21 Apr to 20 May");
    }

    @Test
    @Ignore
    public void shouldSetEmergencyViewWhenRnrIsEmergency() {
        ((LMISTestApp) RuntimeEnvironment.application).setCurrentTimeMillis(DateUtil.parseString("2015-04-21 17:30:00 UTC", DateUtil.DATE_TIME_FORMAT).getTime());
        RnRForm rnRForm = viaRequisitionFragment.presenter.getRnRForm();
        rnRForm.setEmergency(true);

        viaRequisitionFragment.refreshRequisitionForm(rnRForm);

        viaRequisitionFragment.consultationView.findViewById(R.id.et_external_consultations_performed).performClick();

        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo("This information is not used when creating an emergency requisition");
        assertThat(((TextView) viaRequisitionFragment.kitView.findViewById(R.id.et_via_kit_received_hf)).getText().toString()).isEqualTo(StringUtils.EMPTY);
        assertThat(((TextView) viaRequisitionFragment.consultationView.findViewById(R.id.via_rnr_header)).getText()).isEqualTo("Emergency requisition balancete");
        assertThat(viaRequisitionFragment.getActivity().getTitle().toString()).isEqualTo("Emergency requisition - 21 Apr");
    }

    @Test
    public void shouldSetHistoryViewWhenRnrIsAuthorized() {
        RnRForm rnRForm = viaRequisitionFragment.presenter.getRnRForm();
        rnRForm.setStatus(RnRForm.STATUS.AUTHORIZED);

        viaRequisitionFragment.refreshRequisitionForm(rnRForm);
        assertThat(View.GONE).isEqualTo(viaRequisitionFragment.actionPanelView.getVisibility());

        rnRForm.setEmergency(true);
        viaRequisitionFragment.refreshRequisitionForm(rnRForm);
        assertThat(View.GONE).isEqualTo(viaRequisitionFragment.actionPanelView.getVisibility());
        assertFalse(viaRequisitionFragment.vgContainer.findViewById(R.id.et_external_consultations_performed).hasOnClickListeners());
        assertFalse(viaRequisitionFragment.kitView.findViewById(R.id.et_via_kit_opened_chw).hasOnClickListeners());
    }

    @Test
    public void shouldGetIntentToRequisitionActivity() {
        long formId = 100L;
        Intent intent = VIARequisitionActivity.getIntentToMe(viaRequisitionFragment.getActivity(), formId);

        assertThat(intent).isNotNull();
        assertThat(intent.getLongExtra(Constants.PARAM_FORM_ID, 0L)).isEqualTo(formId);
    }

    @Test
    public void shouldShowAlertDialogWhenPressedBack() {
        when(presenter.isDraft()).thenReturn(true);
        viaRequisitionFragment.onBackPressed();

        shadowOf(getMainLooper()).idle();

        DialogFragment fragment = (DialogFragment) (viaRequisitionFragment.getActivity().getFragmentManager().findFragmentByTag("back_confirm_dialog"));

        assertThat(fragment).isNotNull();

        AlertDialog dialog = (AlertDialog) fragment.getDialog();

        assertThat(dialog).isNotNull();
    }

    @Test
    public void shouldNotRemoveRnrFormWhenGoBack() throws LMISException {
        viaRequisitionFragment.onBackPressed();
        verify(presenter, never()).deleteDraft();
    }

    @Test
    public void shouldShowSubmitSignatureDialog() {
        when(presenter.isDraftOrDraftMissed()).thenReturn(true);

        viaRequisitionFragment.showSignDialog();

        shadowOf(getMainLooper()).idle();

        DialogFragment fragment = (DialogFragment) (viaRequisitionFragment.getActivity().getFragmentManager().findFragmentByTag("signature_dialog"));

        assertThat(fragment).isNotNull();

        Dialog dialog = fragment.getDialog();

        assertThat(dialog).isNotNull();

        String alertMessage = viaRequisitionFragment.getString(R.string.msg_via_submit_signature);
        assertThat(fragment.getArguments().getString("title")).isEqualTo(alertMessage);
    }

    @Test
    public void shouldShowApproveSignatureDialog() {
        when(presenter.isDraftOrDraftMissed()).thenReturn(false);

        viaRequisitionFragment.showSignDialog();

        shadowOf(getMainLooper()).idle();

        DialogFragment fragment = (DialogFragment) (viaRequisitionFragment.getActivity().getFragmentManager().findFragmentByTag("signature_dialog"));

        assertThat(fragment).isNotNull();

        Dialog dialog = fragment.getDialog();

        assertThat(dialog).isNotNull();

        String alertMessage = viaRequisitionFragment.getString(R.string.msg_approve_signature_via);
        assertThat(fragment.getArguments().getString("title")).isEqualTo(alertMessage);
    }

    @Test
    public void shouldMessageNotifyDialog() {
        viaRequisitionFragment.showMessageNotifyDialog();

        shadowOf(getMainLooper()).idle();

        DialogFragment fragment = (DialogFragment) (viaRequisitionFragment.getActivity().getFragmentManager().findFragmentByTag("showMessageNotifyDialog"));

        assertThat(fragment).isNotNull();

        AlertDialog dialog = (AlertDialog) fragment.getDialog();

        assertThat(dialog).isNotNull();
    }

    @Test
    public void shouldShowTheCannotInitFormToastWhenTheAllStockMovementsAreNotSyncDown() {
        reset(presenter);
        when(presenter.getRnrFormStatus()).thenReturn(RnRForm.STATUS.DRAFT);
        SharedPreferenceMgr.getInstance().setShouldSyncLastYearStockCardData(true);
        viaRequisitionFragment = getVIARequisitionFragmentFromActivityWithIntent();

        String msg = viaRequisitionFragment.getString(R.string.msg_stock_movement_is_not_ready);
        assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo(msg);
        verify(presenter, never()).loadData(anyLong(), any(Date.class));
    }
}


