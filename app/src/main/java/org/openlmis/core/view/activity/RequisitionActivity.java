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

import android.os.Bundle;

import com.google.gson.Gson;
import com.google.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.repository.VIAReposotory;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.presenter.RequisitionPresenter;
import org.openlmis.core.view.widget.FormView;

import java.util.ArrayList;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;


@ContentView(R.layout.activity_requisition)
public class RequisitionActivity extends BaseActivity implements RequisitionPresenter.RequisitionView{

    @Inject
    RequisitionPresenter presenter;

    @InjectView(R.id.requisition_form)
    FormView requisitionForm;

    @Inject
    VIAReposotory viaReposotory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requisitionForm.loadForm("form");

        requisitionForm.setFormViewListener(new FormView.FormViewCallback() {
            @Override
            public void onStartLoading() {
                startLoading();
            }

            @Override
            public void onStopLoading() {
                stopLoading();
            }

            @Override
            public void onError(LMISException e) {

            }

            @Override
            public String fillFormData() {
                return getRnrFormData();
            }
        });
    }

    public String getRnrFormData(){
        try {
            RnRForm rnRForm = viaReposotory.initVIA();
            RnrFormItem item = rnRForm.getRnrFormItemList().iterator().next();

            ArrayList<ArrayList<String>> dataMap = new ArrayList<>();

            for (int i=0;i<100;i++){
                ArrayList<String> values = new ArrayList<>();


                long received = item.getReceived();
                long total = item.getInitialAmount() + received - item.getIssued();
                long inventory = item.getInventory();

                values.add(item.getProduct().getCode());
                values.add(item.getProduct().getPrimaryName());

                values.add(String.valueOf(item.getInitialAmount()));
                values.add(String.valueOf(received));
                values.add(String.valueOf(item.getIssued()));
                values.add(String.valueOf(total));
                values.add("-");
                values.add(String.valueOf(inventory));
                values.add(String.valueOf(item.getAdjustment() - total));
                values.add(String.valueOf(received * 2 - inventory));

                dataMap.add(values);
            }

            return new Gson().toJson(dataMap);

        } catch (LMISException e){
            e.printStackTrace();
        }

        return StringUtils.EMPTY;
    }

    @Override
    public Presenter getPresenter() {
        return presenter;
    }
}
