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
import org.openlmis.core.model.repository.MMIARepository;
import org.openlmis.core.view.View;

import java.sql.SQLException;

public class MMIAFormPresenter implements Presenter {

    RnRForm form;
    MIMIAFormView view;

    @Inject
    MMIARepository mmiaRepository;

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void attachView(View v) throws ViewNotMatchException {
        if (v instanceof MIMIAFormView) {
            this.view = (MIMIAFormView) v;
        } else {
            throw new ViewNotMatchException(MIMIAFormView.class.getName());
        }
    }

    public RnRForm getRnrForm() {
        if (form == null) {
            form = initMIMIA();
        }
        return form;
    }

    private RnRForm initMIMIA() {
        try {
            form = mmiaRepository.initMIMIA();
        } catch (LMISException e) {
            view.showErrorMessage(e.getMessage());
        }
        return form;
    }

    public void saveForm() throws SQLException {

        if (validate(form)) {
            try {
                form.setStatus(RnRForm.STATUS.AUTHORIZED);
                mmiaRepository.save(form);
            } catch (LMISException e) {
                view.showErrorMessage(e.getMessage());
            }
        } else {
            view.showValidationAlert();
        }
    }

    private boolean validate(RnRForm form) {
        return form.getRegimenItemListAmount(form.getRegimenItemListWrapper()) == mmiaRepository.getTotalPatients(form);
    }

    public interface MIMIAFormView extends View {
        void showValidationAlert();

        void showErrorMessage(String msg);
    }
}
