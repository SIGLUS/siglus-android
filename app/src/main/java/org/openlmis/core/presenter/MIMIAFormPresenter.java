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
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.repository.MIMIARepository;
import org.openlmis.core.view.View;

public class MIMIAFormPresenter implements Presenter{

    RnRForm form;
    MIMIAFormView view;

    @Inject
    MIMIARepository mimiaRepository;

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void attachView(View v) throws ViewNotMatchException{
        if (v instanceof MIMIAFormView){
            this.view = (MIMIAFormView)v;
        }else {
            throw new ViewNotMatchException(MIMIAFormView.class.getName());
        }
    }

    public RnRForm initMIMIA(){
        RnRForm form = null;
        try {
            form = mimiaRepository.initMIMIA();
        } catch (LMISException e){
            view.showErrorMessage(e.getMessage());
        }
        return form;
    }

    public void saveForm(){
        if (validate(form)){
            try {
                mimiaRepository.save(form);
            } catch (LMISException e){
                view.showErrorMessage(e.getMessage());
            }
        }
    }

    private boolean validate(RnRForm form){
        long totalRegimenNumber = 0;
        for (RegimenItem item : form.getRegimenItemList()){
            totalRegimenNumber += item.getAmount();
        }
        return totalRegimenNumber != mimiaRepository.getTotalPatients(form);
    }

    public interface MIMIAFormView extends View {
        void showValidationAlert();
        void showErrorMessage(String msg);
    }
}
