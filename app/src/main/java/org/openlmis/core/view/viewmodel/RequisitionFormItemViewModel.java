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

package org.openlmis.core.view.viewmodel;

import android.text.TextUtils;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.model.RnrFormItem;

import java.util.List;

import lombok.Data;

@Data
public class RequisitionFormItemViewModel {

    private String fmn;
    private String productName;
    private String initAmount;
    private String received;
    private String issued;
    private String theoretical;
    private String total;
    private String inventory;
    private String different;
    private String totalRequest;
    private String requestAmount;
    private String approvedAmount;
    private String adjustedTotalRequest;
    private List<RnRFormItemAdjustmentViewModel> adjustmentViewModels;
    private RnrFormItem item;

    public RequisitionFormItemViewModel() {

    }

    public RequisitionFormItemViewModel(RnrFormItem item) {
        this.item = item;

        this.fmn = (item.getProduct().getCode());
        this.productName = item.getProduct().getPrimaryName();

        long issued = item.getIssued();
        long received = item.getReceived();
        long theoretical = item.getInitialAmount() + received - item.getIssued();
        long inventory = item.getInventory();
        long different = inventory - theoretical;
        this.initAmount = String.valueOf(item.getInitialAmount());
        this.received = String.valueOf(received);
        this.issued = String.valueOf(issued);
        this.theoretical = String.valueOf(theoretical);
        this.total = "-";
        this.inventory = String.valueOf(inventory);
        this.different = String.valueOf(different);
        this.totalRequest = String.valueOf(item.getCalculatedOrderQuantity());
        this.adjustedTotalRequest = totalRequest;
        inflateTotalAmount();
    }

    private void inflateTotalAmount() {
        this.requestAmount = (null == item.getRequestAmount()) ? this.adjustedTotalRequest : String.valueOf(item.getRequestAmount());
        this.approvedAmount = (null == item.getApprovedAmount()) ? this.adjustedTotalRequest : String.valueOf(item.getApprovedAmount());
    }

    public void setAdjustmentViewModels(List<RnRFormItemAdjustmentViewModel> viewModels) {
        this.adjustmentViewModels = viewModels;
        adjustTheoreticalByKitProductAmount();
    }

    public RnrFormItem toRnrFormItem() {
        if (!TextUtils.isEmpty(requestAmount)) {
            item.setRequestAmount(Long.valueOf(this.requestAmount));
        }
        if (!TextUtils.isEmpty(approvedAmount)) {
            item.setApprovedAmount(Long.valueOf(approvedAmount));
        }
        item.setCalculatedOrderQuantity(Long.valueOf(this.adjustedTotalRequest));
        return item;
    }

    private void adjustTheoreticalByKitProductAmount() {
        long adjustAmount = calculateAdjustAmount();
        long theoreticalTotalRequest = Long.valueOf(this.totalRequest);
        if (adjustAmount <= theoreticalTotalRequest) {
            theoreticalTotalRequest = theoreticalTotalRequest - adjustAmount;
        } else {
            theoreticalTotalRequest = 0;
        }
        this.adjustedTotalRequest = String.valueOf(theoreticalTotalRequest);

        inflateTotalAmount();
    }

    private long calculateAdjustAmount() {
        long adjustAmount = 0;
        for (RnRFormItemAdjustmentViewModel adjustInfo : adjustmentViewModels) {
            adjustAmount += adjustInfo.getQuantity() * adjustInfo.getKitStockOnHand();
        }
        return adjustAmount;
    }

    public String getFormattedKitAdjustmentMessage() {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append(LMISApp.getContext().getString(R.string.label_adjustment_dialog_header));
        for (RnRFormItemAdjustmentViewModel adjustmentViewModel : adjustmentViewModels) {
            messageBuilder.append(adjustmentViewModel.formatAdjustmentContentForProduct(productName));
        }
        messageBuilder.append(LMISApp.getContext().getString(R.string.label_adjustment_dialog_adjust_amount, calculateAdjustAmount(), productName));
        messageBuilder.append(LMISApp.getContext().getString(R.string.label_adjustment_dialog_initial_amount, totalRequest));
        messageBuilder.append(LMISApp.getContext().getString(R.string.label_adjustment_dialog_adjusted_amount, adjustedTotalRequest));
        return messageBuilder.toString();
    }
}
