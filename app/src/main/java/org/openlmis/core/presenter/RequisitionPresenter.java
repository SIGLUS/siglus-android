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

package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.repository.VIAReposotory;
import org.openlmis.core.view.View;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;
import org.roboguice.shaded.goole.common.base.Function;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

import java.util.ArrayList;
import java.util.List;


public class RequisitionPresenter implements Presenter{

    @Inject
    VIAReposotory viaReposotory;

    private RnRForm rnRForm;

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void attachView(View v) throws ViewNotMatchException {

    }

    public RnRForm loadRnrForm(){
        try {
            rnRForm = viaReposotory.initVIA();
            return rnRForm;
        } catch (LMISException e){
            e.printStackTrace();
        }
        return null;
    }


    public List<RequisitionFormItemViewModel> getRequisitionViewModelList() {
        if (rnRForm == null) {
            loadRnrForm();
        }

        List<RnrFormItem> tmpList = new ArrayList<>();
        RnrFormItem item =rnRForm.getRnrFormItemList().iterator().next();
        for (int i=0;i<100;i++){
            tmpList.add(item);
        }

        return from(tmpList).transform(new Function<RnrFormItem, RequisitionFormItemViewModel>() {
            @Override
            public RequisitionFormItemViewModel apply(RnrFormItem item) {
                return new RequisitionFormItemViewModel(item);
            }
        }).toList();
    }


    public interface RequisitionView extends View {

    }

}
