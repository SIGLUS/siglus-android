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
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.google.inject.Binder;
import com.google.inject.Module;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.builder.RequisitionBuilder;
import org.openlmis.core.presenter.VIARequisitionPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.activity.VIARequisitionActivity;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowListView;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(LMISTestRunner.class)
public class VIARequisitionFragmentTest {

    org.openlmis.core.view.fragment.VIARequisitionFragment VIARequisitionFragment;
    VIARequisitionPresenter presenter;
    private List<RequisitionFormItemViewModel> formItemList;
    private Program program;
    private RnRForm form;

    @Before
    public void setup() throws Exception {
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
        when(presenter.getRnRForm()).thenReturn(form);

        VIARequisitionFragment = getVIARequisitionFragmentFromActivityWithIntent();
    }

    private VIARequisitionFragment getVIARequisitionFragmentFromActivityWithIntent() {
        Intent intent = new Intent();
        intent.putExtra(Constants.PARAM_FORM_ID, 1L);
        VIARequisitionActivity viaRequisitionActivity = Robolectric.buildActivity(VIARequisitionActivity.class).withIntent(intent).create().start().visible().get();
        return (VIARequisitionFragment)viaRequisitionActivity.getFragmentManager().findFragmentById(R.id.fragment_requisition);
    }

    @Test
    public void shouldShowRequisitionPeriodOnTitleWhenToggleOn() {
        ((LMISTestApp) RuntimeEnvironment.application).setFeatureToggle(true);

        VIARequisitionFragment.refreshRequisitionForm(VIARequisitionFragment.presenter.getRnRForm());

        assertThat(VIARequisitionFragment.getActivity().getTitle()).isEqualTo("Requisition - 21 Apr to 20 May");
    }

    @Test
    public void shouldShowOnlyPeriodWhenToggleOff() {
        ((LMISTestApp) RuntimeEnvironment.application).setFeatureToggle(false);
        VIARequisitionFragment = getVIARequisitionFragmentFromActivityWithIntent();

        VIARequisitionFragment.refreshRequisitionForm(VIARequisitionFragment.presenter.getRnRForm());

        assertThat(VIARequisitionFragment.getActivity().getTitle()).isEqualTo("21 Apr 2015  to  20 May 2015");
    }

    @Test
    public void shouldGetIntentToRequisitionActivity() {
        long formId = 100L;
        Intent intent = VIARequisitionActivity.getIntentToMe(VIARequisitionFragment.getActivity(), formId);

        assertThat(intent).isNotNull();
        assertThat(intent.getLongExtra(Constants.PARAM_FORM_ID, 0L)).isEqualTo(formId);
    }

    @Test
    public void shouldShowAlertDialogWhenPressedBackWithDataChanges() {
        VIARequisitionFragment.hasDataChanged = true;

        VIARequisitionFragment.onBackPressed();

        DialogFragment fragment = (DialogFragment) (VIARequisitionFragment.getActivity().getFragmentManager().findFragmentByTag("back_confirm_dialog"));

        assertThat(fragment).isNotNull();

        AlertDialog dialog = (AlertDialog) fragment.getDialog();

        assertThat(dialog).isNotNull();
    }

    @Test
    public void shouldGoToHomePageWhenMethodCalled() {
        VIARequisitionFragment.backToHomePage();
        assertThat(VIARequisitionFragment.getActivity().isFinishing()).isTrue();
    }

    @Test
    public void shouldNotRemoveRnrFormWhenGoBack() throws LMISException {
        VIARequisitionFragment.onBackPressed();
        verify(presenter, never()).removeRnrForm();
    }

    @Test
    public void shouldShowSubmitSignatureDialog() {
        VIARequisitionFragment.showSignDialog(true);

        DialogFragment fragment = (DialogFragment) (VIARequisitionFragment.getActivity().getFragmentManager().findFragmentByTag("signature_dialog"));

        assertThat(fragment).isNotNull();

        Dialog dialog = fragment.getDialog();

        assertThat(dialog).isNotNull();

        String alertMessage = VIARequisitionFragment.getString(R.string.msg_via_submit_signature);
        assertThat(fragment.getArguments().getString("title")).isEqualTo(alertMessage);
    }

    @Test
    public void shouldShowApproveSignatureDialog() {
        VIARequisitionFragment.showSignDialog(false);

        DialogFragment fragment = (DialogFragment) (VIARequisitionFragment.getActivity().getFragmentManager().findFragmentByTag("signature_dialog"));

        assertThat(fragment).isNotNull();

        Dialog dialog = fragment.getDialog();

        assertThat(dialog).isNotNull();

        String alertMessage = VIARequisitionFragment.getString(R.string.msg_via_approve_signature);
        assertThat(fragment.getArguments().getString("title")).isEqualTo(alertMessage);
    }

    @Test
    public void shouldMessageNotifyDialog() {
        VIARequisitionFragment.showMessageNotifyDialog();

        DialogFragment fragment = (DialogFragment) (VIARequisitionFragment.getActivity().getFragmentManager().findFragmentByTag("showMessageNotifyDialog"));

        assertThat(fragment).isNotNull();

        AlertDialog dialog = (AlertDialog) fragment.getDialog();

        assertThat(dialog).isNotNull();
    }

    private View getFirstItemInForm() {
        VIARequisitionFragment.refreshRequisitionForm(form);
        ShadowListView shadowListView = shadowOf(VIARequisitionFragment.requisitionForm);
        shadowListView.populateItems();

        return VIARequisitionFragment.requisitionForm.getChildAt(0);
    }
}


