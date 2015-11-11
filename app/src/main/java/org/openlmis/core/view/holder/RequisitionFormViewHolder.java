package org.openlmis.core.view.holder;

import android.text.Editable;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.utils.SimpleTextWatcher;
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
    TextView theoretical;
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
        theoretical.setText(entry.getTheoretical());
        total.setText(entry.getTotal());
        inventory.setText(entry.getInventory());
        different.setText(entry.getDifferent());
        totalRequest.setText(entry.getTotalRequest());


        MyTextWatcher mySimpleTextWatcher = new MyTextWatcher(entry, status);
        requestAmount.removeTextChangedListener(mySimpleTextWatcher);
        approvedAmount.removeTextChangedListener(mySimpleTextWatcher);

        requestAmount.setText(entry.getRequestAmount());
        requestAmount.setError(null);
        approvedAmount.setText(entry.getApprovedAmount());


        if (status == RnRForm.STATUS.SUBMITTED) {
            requestAmount.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
            approvedAmount.setBackgroundColor(context.getResources().getColor(R.color.white));

            requestAmount.setEnabled(false);
            approvedAmount.setEnabled(true);
            approvedAmount.addTextChangedListener(mySimpleTextWatcher);

        } else if (status == RnRForm.STATUS.DRAFT) {
            requestAmount.setBackgroundColor(context.getResources().getColor(R.color.white));
            approvedAmount.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));

            requestAmount.setEnabled(true);
            approvedAmount.setEnabled(false);
            requestAmount.addTextChangedListener(mySimpleTextWatcher);
        }
    }

    class MyTextWatcher extends SimpleTextWatcher {

        private final RequisitionFormItemViewModel entry;
        private RnRForm.STATUS status;

        public MyTextWatcher(RequisitionFormItemViewModel entry, RnRForm.STATUS status) {
            this.entry = entry;
            this.status = status;
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            return true;
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
