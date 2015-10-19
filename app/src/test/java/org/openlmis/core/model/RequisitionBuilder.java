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

package org.openlmis.core.model;

import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;

public class RequisitionBuilder {

    public static RequisitionFormItemViewModel buildFakeRequisitionViewModel(){
        RequisitionFormItemViewModel viewModel = new RequisitionFormItemViewModel();
        String text = String.valueOf(0);
        viewModel.setFmn(text);
        viewModel.setProductName(text);
        viewModel.setInitAmount(text);
        viewModel.setReceived(text);
        viewModel.setIssued(text);
        viewModel.setTheoretical(text);
        viewModel.setTotal(text);
        viewModel.setInventory(text);
        viewModel.setDifferent(text);
        viewModel.setTotalRequest(text);
        viewModel.setRequestAmount(text);
        viewModel.setApprovedAmount(text);

        return viewModel;
    }
}
