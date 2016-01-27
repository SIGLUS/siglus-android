package org.openlmis.core.view.holder;

import android.app.AlertDialog;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.utils.SingleTextWatcher;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;
import org.openlmis.core.view.widget.InputFilterMinMax;

import lombok.Getter;
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
        populateRequestApprovedAmount(entry, status);
    }

    private void populateAdjustmentTheoreticalIcon(final RequisitionFormItemViewModel itemViewModel) {
        if (!LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_requisition_theoretical)) {
            adjustTheoreticalIcon.setVisibility(View.GONE);
            return;
        }

        if (itemViewModel.getAdjustmentViewModels() == null || itemViewModel.getAdjustmentViewModels().size() == 0) {
            adjustTheoreticalIcon.setVisibility(View.GONE);
        } else {
            adjustTheoreticalIcon.setVisibility(View.VISIBLE);
            adjustTheoreticalIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(context)
                            .setMessage(Html.fromHtml(itemViewModel.getFormattedKitAdjustmentMessage()))
                            .setPositiveButton(R.string.btn_ok, null)
                            .show();
                }
            });
        }
    }

    private void populateRequestApprovedAmount(RequisitionFormItemViewModel entry, RnRForm.STATUS status) {
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
        private RnRForm.STATUS status;

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
