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

import android.view.View;
import android.widget.EditText;

import com.google.inject.Binder;
import com.google.inject.Module;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.factory.RequisitionDataFactory;
import org.openlmis.core.presenter.RequisitionPresenter;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowListView;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
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
        formItemList.add(RequisitionDataFactory.buildFakeRequisitionViewModel());

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

        assertThat(etRequestAmount, is(notNullValue()));
        assertThat(etRequestAmount.getText().toString(), is("0"));

        formItemList.get(0).setRequestAmount("");
        presenter.processRequisition("123");

        assertThat(etRequestAmount.getError().toString(), is(RuntimeEnvironment.application.getResources().getString(R.string.hint_error_input)));
    }

    @Test
    public void shouldShowErrorOnApprovedAmountWhenInputInvalid() {
        requisitionActivity.highLightApprovedAmount();
        View item = getFirstItemInForm();
        EditText etApprovedAmount = (EditText) item.findViewById(R.id.et_approved_amount);

        assertThat(etApprovedAmount, is(notNullValue()));
        assertThat(etApprovedAmount.getText().toString(), is("0"));

        formItemList.get(0).setApprovedAmount("");
        presenter.processRequisition("123");

        assertThat(etApprovedAmount.getError().toString(), is(RuntimeEnvironment.application.getResources().getString(R.string.hint_error_input)));
    }

    private View getFirstItemInForm() {
        requisitionActivity.refreshRequisitionForm();
        ShadowListView shadowListView = shadowOf(requisitionActivity.requisitionForm);
        shadowListView.populateItems();

        return requisitionActivity.requisitionForm.getChildAt(0);
    }
}


