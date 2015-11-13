/*
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

package org.openlmis.core.view.activity;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;

import com.google.inject.Binder;
import com.google.inject.Module;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.RequisitionBuilder;
import org.openlmis.core.presenter.RequisitionPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowListView;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.robolectric.Shadows.shadowOf;

@RunWith(LMISTestRunner.class)
public class RequisitionActivityTest {

    RequisitionActivity requisitionActivity;
    RequisitionPresenter presenter;
    private List<RequisitionFormItemViewModel> formItemList;

    @Before
    public void setup() {
        presenter = spy(new RequisitionPresenter());

        formItemList = new ArrayList<>();
        formItemList.add(RequisitionBuilder.buildFakeRequisitionViewModel());

        doReturn(true).when(presenter).formIsEditable();
        doReturn("123").when(presenter).getConsultationNumbers();
        doReturn(formItemList).when(presenter).getRequisitionViewModelList();
        doNothing().when(presenter).loadRequisitionFormList(anyLong());
        doNothing().when(presenter).setConsultationNumbers(anyString());

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(RequisitionPresenter.class).toInstance(presenter);
            }
        });
        requisitionActivity = Robolectric.buildActivity(RequisitionActivity.class).create().get();
        requisitionActivity.refreshRequisitionForm();
    }

    @Test
    public void shouldShowErrorOnRequestAmountWhenInputInvalid() {
        requisitionActivity.highLightRequestAmount();
        View item = getFirstItemInForm();
        EditText etRequestAmount = (EditText) item.findViewById(R.id.et_request_amount);

        assertThat(etRequestAmount).isNotNull();
        assertThat(etRequestAmount.getText().toString()).isEqualTo("0");

        formItemList.get(0).setRequestAmount("");
        presenter.processRequisition("123");

        assertThat(etRequestAmount.getError().toString()).isEqualTo(requisitionActivity.getString(R.string.hint_error_input));
    }

    @Test
    public void shouldShowErrorOnApprovedAmountWhenInputInvalid() {
        requisitionActivity.highLightApprovedAmount();
        View item = getFirstItemInForm();
        EditText etApprovedAmount = (EditText) item.findViewById(R.id.et_approved_amount);

        assertThat(etApprovedAmount).isNotNull();
        assertThat(etApprovedAmount.getText().toString()).isEqualTo("0");

        formItemList.get(0).setApprovedAmount("");
        presenter.processRequisition("123");

        assertThat(etApprovedAmount.getError().toString()).isEqualTo(requisitionActivity.getString(R.string.hint_error_input));
    }

    @Test
    public void shouldGetIntentToRequisitionActivity() {
        long formId = 100L;
        Intent intent = RequisitionActivity.getIntentToMe(requisitionActivity, formId);

        assertThat(intent).isNotNull();
        assertThat(intent.getLongExtra(Constants.PARAM_FORM_ID, 0L)).isEqualTo(formId);
    }

    @Test
    public void shouldShowAlertDialogWhenPressedBackWithDataChanges() {
        requisitionActivity.hasDataChanged = true;

        requisitionActivity.onBackPressed();

        DialogFragment fragment = (DialogFragment)(requisitionActivity.getFragmentManager().findFragmentByTag("back_confirm_dialog"));

        assertThat(fragment).isNotNull();

        AlertDialog dialog = (AlertDialog) fragment.getDialog();
        ShadowAlertDialog shadowAlertDialog = shadowOf(dialog);

        assertThat(dialog).isNotNull();

        String alertMessage = requisitionActivity.getString(R.string.msg_mmia_onback_confirm);
        assertThat(shadowAlertDialog.getMessage()).isEqualTo(alertMessage);
    }

    @Test
    public void shouldGoToHomePageWhenMethodCalled() {
        requisitionActivity.backToHomePage();
        assertThat(requisitionActivity.isFinishing()).isTrue();
    }

    private View getFirstItemInForm() {
        requisitionActivity.refreshRequisitionForm();
        ShadowListView shadowListView = shadowOf(requisitionActivity.requisitionForm);
        shadowListView.populateItems();

        return requisitionActivity.requisitionForm.getChildAt(0);
    }
}


