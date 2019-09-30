package org.openlmis.core.view.holder;

import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.ptv.PTVViewModel;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.InjectView;

import static org.openlmis.core.utils.Constants.ENTRIES;
import static org.openlmis.core.utils.Constants.FINAL_STOCK;
import static org.openlmis.core.utils.Constants.LOSSES_AND_ADJUSTMENTS;
import static org.openlmis.core.utils.Constants.REQUISITIONS;
import static org.openlmis.core.utils.Constants.TOTAL;

public class PTVProgramViewHolder extends BaseViewHolder {

    @InjectView(R.id.tv_placeholder_item)
    TextView tvPlaceHolderItem;

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

    private EditText[] productQuantities;
    private PTVViewModel ptvViewModel;
    private boolean isCompleted;

    public PTVProgramViewHolder(View itemView, boolean isCompleted) {
        super(itemView);
        productQuantities = new EditText[]{etProductQuantity1, etProductQuantity2, etProductQuantity3, etProductQuantity4, etProductQuantity5};
        this.isCompleted = isCompleted;
    }

    private boolean isNotAService() {
        return ptvViewModel.getPlaceholderItemName().equals(TOTAL) || ptvViewModel.getPlaceholderItemName().equals(ENTRIES) || ptvViewModel.getPlaceholderItemName().equals(LOSSES_AND_ADJUSTMENTS) || ptvViewModel.getPlaceholderItemName().equals(FINAL_STOCK) || ptvViewModel.getPlaceholderItemName().equals(REQUISITIONS);
    }

    private boolean needsBeDisable() {
        return isCompleted || ptvViewModel.getPlaceholderItemName().equals(TOTAL) || ptvViewModel.getPlaceholderItemName().equals(FINAL_STOCK) || ptvViewModel.getPlaceholderItemName().equals(ENTRIES);
    }

    private void disableView(View view) {
        view.setClickable(false);
        view.setFocusable(false);
    }

    public void populate(PTVViewModel ptvViewModel) {
        this.ptvViewModel = ptvViewModel;
        tvPlaceHolderItem.setText(ptvViewModel.getPlaceholderItemName());
        setProductQuantities();
        if (needsBeDisable()) {
            for (EditText productQuantity : productQuantities) {
                disableView(productQuantity);
            }
        }
        if (isNotAService()) {
            tvPlaceHolderItem.setBackgroundColor(context.getResources().getColor(R.color.patient_data_not_editable));
        }
    }

    public void updateProductQuantities() {
        for (int i = 0; i < productQuantities.length; i++) {
            String quantityValue = productQuantities[i].getText().toString();
            if (!quantityValue.isEmpty()) {
                ptvViewModel.setQuantity(i + 1, Long.valueOf(quantityValue));
            }
        }
    }


    public void setWatchersForEditableListeners(TextWatcher textWatcher) {
        List<EditText> textViewsListened = new ArrayList<>();
        for (EditText editText: productQuantities) {
            textViewsListened.add(editText);
        }
        for (EditText editText : textViewsListened) {
            editText.addTextChangedListener(textWatcher);
        }
    }

    private void setProductQuantities() {
        etProductQuantity1.setText(String.valueOf(ptvViewModel.getQuantity1()));
        etProductQuantity2.setText(String.valueOf(ptvViewModel.getQuantity2()));
        etProductQuantity3.setText(String.valueOf(ptvViewModel.getQuantity3()));
        etProductQuantity4.setText(String.valueOf(ptvViewModel.getQuantity4()));
        etProductQuantity5.setText(String.valueOf(ptvViewModel.getQuantity5()));
    }

}
