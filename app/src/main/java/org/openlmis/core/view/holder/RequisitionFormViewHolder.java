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

package org.openlmis.core.view.holder;

import android.app.Activity;
import android.app.DialogFragment;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import lombok.Getter;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.utils.SingleTextWatcher;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;
import org.openlmis.core.view.widget.InputFilterMinMax;
import roboguice.inject.InjectView;

public class RequisitionFormViewHolder extends BaseViewHolder {

  @InjectView(R.id.tx_FNM)
  TextView productCode;
  @InjectView(R.id.tx_product_name)
  TextView productName;
  @InjectView(R.id.tx_initial_amount)
  TextView initAmount;
  @InjectView(R.id.tx_received)
  TextView received;
  @InjectView(R.id.tx_issued)
  TextView issued;
  @InjectView(R.id.tx_theoretical)
  TextView theoreticalInventory;
  @InjectView(R.id.tx_total)
  TextView total;
  @InjectView(R.id.tx_inventory)
  TextView inventory;
  @InjectView(R.id.tx_different)
  TextView different;
  @InjectView(R.id.tx_total_request)
  TextView totalRequest;
  @InjectView(R.id.et_request_amount)
  EditText requestAmount;
  @InjectView(R.id.et_approved_amount)
  EditText approvedAmount;
  @InjectView(R.id.iv_adjustment_theoratical)
  ImageView adjustTheoreticalIcon;

  @Getter
  boolean hasDataChanged;

  public RequisitionFormViewHolder(View itemView) {
    super(itemView);

    requestAmount.setFilters(new InputFilter[]{new InputFilterMinMax(Integer.MAX_VALUE)});
    approvedAmount.setFilters(new InputFilter[]{new InputFilterMinMax(Integer.MAX_VALUE)});
  }

  public void populate(RequisitionFormItemViewModel entry, RnRForm.STATUS status) {
    productCode.setText(entry.getFmn());
    productName.setText(entry.getProductName());

    initAmount.setText(entry.getInitAmount());
    received.setText(entry.getReceived());
    issued.setText(entry.getIssued());
    theoreticalInventory.setText(entry.getTheoretical());
    total.setText(entry.getTotal());
    inventory.setText(entry.getInventory());
    different.setText(entry.getDifferent());
    totalRequest.setText(entry.getAdjustedTotalRequest());

    populateAdjustmentTheoreticalIcon(entry);

    if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)) {
      fakePopulateRequestApprovedAmount(entry, status);
    } else {
      populateRequestApprovedAmount(entry, status);
    }

  }

  private void populateAdjustmentTheoreticalIcon(final RequisitionFormItemViewModel itemViewModel) {
    if (itemViewModel.getAdjustmentViewModels() == null
        || itemViewModel.getAdjustmentViewModels().size() == 0) {
      adjustTheoreticalIcon.setVisibility(View.GONE);
    } else {
      adjustTheoreticalIcon.setVisibility(View.VISIBLE);
      adjustTheoreticalIcon.setOnClickListener(v -> {
        DialogFragment dialogFragment = SimpleDialogFragment.newInstance(null,
            Html.fromHtml(itemViewModel.getFormattedKitAdjustmentMessage()),
            context.getString(R.string.btn_ok));
        dialogFragment
            .show(((Activity) context).getFragmentManager(), "adjustmentTheoreticalDialog");
      });
    }
  }

  private void populateRequestApprovedAmount(RequisitionFormItemViewModel entry,
      RnRForm.STATUS status) {
    MyTextWatcher mySimpleTextWatcher = new MyTextWatcher(entry, status);
    requestAmount.removeTextChangedListener(mySimpleTextWatcher);
    approvedAmount.removeTextChangedListener(mySimpleTextWatcher);

    requestAmount.setText(entry.getRequestAmount());
    requestAmount.setError(null);
    approvedAmount.setText(entry.getApprovedAmount());

    if (status == RnRForm.STATUS.SUBMITTED) {
      showDisabledAmount(requestAmount);
      showEnabledAmount(approvedAmount);
      approvedAmount.addTextChangedListener(mySimpleTextWatcher);
    } else if (status == RnRForm.STATUS.DRAFT) {
      showEnabledAmount(requestAmount);
      showDisabledAmount(approvedAmount);
      requestAmount.addTextChangedListener(mySimpleTextWatcher);
    }
  }

  private void fakePopulateRequestApprovedAmount(RequisitionFormItemViewModel entry,
      RnRForm.STATUS status) {
    if (status == RnRForm.STATUS.SUBMITTED_MISSED) {
      status = RnRForm.STATUS.SUBMITTED;
    } else if (status == RnRForm.STATUS.DRAFT_MISSED) {
      status = RnRForm.STATUS.DRAFT;
    }

    MyTextWatcher mySimpleTextWatcher = new MyTextWatcher(entry, status);
    requestAmount.removeTextChangedListener(mySimpleTextWatcher);
    approvedAmount.removeTextChangedListener(mySimpleTextWatcher);

    requestAmount.setText(entry.getRequestAmount());
    requestAmount.setError(null);
    approvedAmount.setText(entry.getApprovedAmount());

    if (status == RnRForm.STATUS.SUBMITTED) {
      showDisabledAmount(requestAmount);
      showEnabledAmount(approvedAmount);
      approvedAmount.addTextChangedListener(mySimpleTextWatcher);

    } else if (status == RnRForm.STATUS.DRAFT) {
      requestAmount.setEnabled(true);
      showEnabledAmount(requestAmount);
      showDisabledAmount(approvedAmount);
      requestAmount.addTextChangedListener(mySimpleTextWatcher);
    }
  }

  private void showDisabledAmount(View view) {
    view.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
    view.setEnabled(false);
  }

  private void showEnabledAmount(View view) {
    view.setBackgroundColor(context.getResources().getColor(R.color.color_white));
    view.setEnabled(true);
  }

  class MyTextWatcher extends SingleTextWatcher {

    private final RequisitionFormItemViewModel entry;
    private final RnRForm.STATUS status;

    public MyTextWatcher(RequisitionFormItemViewModel entry, RnRForm.STATUS status) {
      this.entry = entry;
      this.status = status;
    }

    @Override
    public void afterTextChanged(Editable editable) {
      hasDataChanged = true;
      String value = editable.toString();
      if (status == RnRForm.STATUS.SUBMITTED) {
        entry.setApprovedAmount(value);
      } else if (status == RnRForm.STATUS.DRAFT) {
        approvedAmount.setText(value);
        entry.setApprovedAmount(value);
        entry.setRequestAmount(value);
      }
    }
  }
}
