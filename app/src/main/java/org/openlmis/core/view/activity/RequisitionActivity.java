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

import com.google.inject.Inject;

import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.repository.VIAReposotory;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.presenter.RequisitionPresenter;
import org.openlmis.core.view.widget.RequisitionForm;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;


@ContentView(R.layout.activity_requisition)
public class RequisitionActivity extends BaseActivity implements RequisitionPresenter.RequisitionView{

    @Inject
    RequisitionPresenter presenter;

    @InjectView(R.id.requisition_form)
    RequisitionForm requisitionForm;

    @Inject
    VIAReposotory viaReposotory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            requisitionForm.setData(viaReposotory.initVIA());
        }catch (LMISException e){
            e.printStackTrace();
        }
    }

    @Override
    public Presenter getPresenter() {
        return presenter;
    }
}
