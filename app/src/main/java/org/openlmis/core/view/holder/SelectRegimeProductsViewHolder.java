package org.openlmis.core.view.holder;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.RegimeProductViewModel;

import roboguice.inject.InjectView;


public class SelectRegimeProductsViewHolder extends BaseViewHolder {

    @InjectView(R.id.tv_product_name)
    TextView productName;

    @InjectView(R.id.tv_short_code)
    TextView tvShortCode;

    @InjectView(R.id.touchArea_checkbox)
    LinearLayout taCheckbox;

    @InjectView(R.id.checkbox)
    CheckBox checkBox;

    public SelectRegimeProductsViewHolder(View itemView) {
        super(itemView);
        taCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerCheckbox();
            }
        });
    }

    public void populate(final RegimeProductViewModel viewModel) {
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                viewModel.setChecked(isChecked);
            }
        });
        checkBox.setChecked(viewModel.isChecked());
        productName.setText(viewModel.getEntireName());
        tvShortCode.setText(viewModel.getShortCode());
    }

    private void triggerCheckbox() {
        if (checkBox.isChecked()) {
            checkBox.setChecked(false);
        } else {
            checkBox.setChecked(true);
        }
    }
}
