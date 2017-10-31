package org.openlmis.core.view.holder;

import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.model.PTVProgramStockInformation;
import org.openlmis.core.model.ServiceDispensation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import roboguice.inject.InjectView;

public class PTVProgramViewHolder extends BaseViewHolder {

    public static final int DEFAULT_QUANTITY = 0;
    @InjectView(R.id.tv_drug_name)
    TextView tvDrugName;

    @InjectView(R.id.et_product_quantity_1)
    EditText etProductQuantity1;

    @InjectView(R.id.et_product_quantity_2)
    EditText etProductQuantity2;

    @InjectView(R.id.et_product_quantity_3)
    EditText etProductQuantity3;

    @InjectView(R.id.et_product_quantity_4)
    EditText etProductQuantity4;

    @InjectView(R.id.et_product_quantity_5)
    EditText etProductQuantity5;

    @InjectView(R.id.et_product_quantity_6)
    EditText etProductQuantity6;

    @InjectView(R.id.et_product_quantity_7)
    EditText etProductQuantity7;

    @InjectView(R.id.et_product_quantity_8)
    EditText etProductQuantity8;

    @InjectView(R.id.tv_initial_stock)
    TextView tvInitialStock;

    @InjectView(R.id.tv_total)
    TextView tvTotal;

    @InjectView(R.id.tv_entry_quantity)
    TextView tvEntryQuantity;

    @InjectView(R.id.et_adjustments_quantity)
    EditText etAdjustmentsQuantity;

    @InjectView(R.id.tv_final_stock)
    TextView tvFinalStock;

    @InjectView(R.id.et_requisition_quantity)
    EditText etRequisitionQuantity;

    private long total;
    private long finalStock;
    private EditText[] productQuantities;
    private PTVProgramStockInformation ptvProgramStockInformation;

    public PTVProgramViewHolder(View itemView, boolean isCompleted) {
        super(itemView);
        productQuantities = new EditText[]{etProductQuantity1, etProductQuantity2, etProductQuantity3, etProductQuantity4, etProductQuantity5, etProductQuantity6, etProductQuantity7, etProductQuantity8};
        if(isCompleted){
            for(EditText productQuantity : productQuantities){
                disableView(productQuantity);
            }
            disableView(etAdjustmentsQuantity);
            disableView(etRequisitionQuantity);

        }
    }

    private void disableView(View view) {
        view.setClickable(false);
        view.setFocusable(false);
    }

    public void populate(PTVProgramStockInformation ptvProgramStockInformation) {
        this.ptvProgramStockInformation = ptvProgramStockInformation;
        total = DEFAULT_QUANTITY;
        tvDrugName.setText(ptvProgramStockInformation.getProduct().getPrimaryName());
        setProductQuantities(ptvProgramStockInformation);
        calculateTotal();
        tvInitialStock.setText(String.valueOf(ptvProgramStockInformation.getInitialStock()));
        tvEntryQuantity.setText(String.valueOf(ptvProgramStockInformation.getEntries()));
        etAdjustmentsQuantity.setText(String.valueOf(ptvProgramStockInformation.getLossesAndAdjustments()));
        etRequisitionQuantity.setText(String.valueOf(ptvProgramStockInformation.getRequisition()));
        tvTotal.setText(String.valueOf(total));
        calculateFinalStock();
    }

    @NonNull
    private TextWatcher productQuantitiesWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateTotal();
                calculateFinalStock();
                updateServiceDispensations();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }

    private void updateServiceDispensations() {
        List<ServiceDispensation> serviceDispensations = new ArrayList<>(ptvProgramStockInformation.getServiceDispensations());
        for (int i = 0; i < serviceDispensations.size(); i ++) {
            String quantityValue = productQuantities[i].getText().toString();
            if (!quantityValue.isEmpty()) {
                serviceDispensations.get(i).setQuantity(Long.valueOf(quantityValue));
            }
        }
        String adjustmentsValue = etAdjustmentsQuantity.getText().toString();
        if (!adjustmentsValue.isEmpty()) {
            ptvProgramStockInformation.setLossesAndAdjustments(Long.valueOf(adjustmentsValue));
        }
        String requisitionValue = etRequisitionQuantity.getText().toString();
        if (!requisitionValue.isEmpty()) {
            ptvProgramStockInformation.setRequisition(Long.valueOf(requisitionValue));
        }
    }


    public void setWatchersForEditableListeners() {
        List<EditText> textViewsListened = new ArrayList<>();
        textViewsListened.addAll(Arrays.asList(productQuantities));
        textViewsListened.add(etRequisitionQuantity);
        textViewsListened.add(etAdjustmentsQuantity);
        for (EditText editText : textViewsListened) {
            editText.addTextChangedListener(productQuantitiesWatcher());
        }
    }

    private void calculateTotal() {
        total = DEFAULT_QUANTITY;
        for (EditText editText : productQuantities) {
            String quantity = editText.getText().toString();
            if (!quantity.isEmpty()) {
                total += Long.valueOf(quantity);
            }
        }
        tvTotal.setText(String.valueOf(total));
    }

    private void calculateFinalStock() {
        String lossesAndAdjustmentsValue = etAdjustmentsQuantity.getText().toString();
        long lossesAndAdjustmentsQuantity = DEFAULT_QUANTITY;
        if (!lossesAndAdjustmentsValue.isEmpty()) {
            lossesAndAdjustmentsQuantity = Long.valueOf(lossesAndAdjustmentsValue);
        }
        finalStock = ptvProgramStockInformation.getInitialStock() + ptvProgramStockInformation.getEntries() - total - lossesAndAdjustmentsQuantity;
        tvFinalStock.setText(String.valueOf(finalStock));
    }

    private void setProductQuantities(PTVProgramStockInformation ptvProgramStockInformation) {
        List<ServiceDispensation> serviceDispensations = new ArrayList<>(ptvProgramStockInformation.getServiceDispensations());
        for (int i = 0; i < serviceDispensations.size(); i++) {
            switch (i) {
                case 0:
                    etProductQuantity1.setText(String.valueOf(serviceDispensations.get(i).getQuantity()));
                    break;
                case 1:
                    etProductQuantity2.setText(String.valueOf(serviceDispensations.get(i).getQuantity()));
                    break;
                case 2:
                    etProductQuantity3.setText(String.valueOf(serviceDispensations.get(i).getQuantity()));
                    break;
                case 3:
                    etProductQuantity4.setText(String.valueOf(serviceDispensations.get(i).getQuantity()));
                    break;
                case 4:
                    etProductQuantity5.setText(String.valueOf(serviceDispensations.get(i).getQuantity()));
                    break;
                case 5:
                    etProductQuantity6.setText(String.valueOf(serviceDispensations.get(i).getQuantity()));
                    break;
                case 6:
                    etProductQuantity7.setText(String.valueOf(serviceDispensations.get(i).getQuantity()));
                    break;
                case 7:
                    etProductQuantity8.setText(String.valueOf(serviceDispensations.get(i).getQuantity()));
                    break;
                default:
                    break;
            }
        }
    }

}
