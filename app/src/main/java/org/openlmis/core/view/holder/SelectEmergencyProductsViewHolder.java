package org.openlmis.core.view.holder;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.SelectEmergencyProductAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import roboguice.inject.InjectView;


public class SelectEmergencyProductsViewHolder extends BaseViewHolder {

    @InjectView(R.id.product_name)
    TextView productName;

    @InjectView(R.id.tv_short_code)
    TextView tvShortCode;

    @InjectView(R.id.touchArea_checkbox)
    LinearLayout taCheckbox;

    @InjectView(R.id.checkbox)
    CheckBox checkBox;

    public SelectEmergencyProductsViewHolder(View itemView) {
        super(itemView);
        taCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerCheckbox();
            }
        });
    }

    public void populate(final SelectEmergencyProductAdapter selectEmergencyProductAdapter, final InventoryViewModel viewModel) {
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && selectEmergencyProductAdapter.isAllowedSelect()) {
                    ToastUtil.show(R.string.msg_out_limit_of_selected_emergency_products);
                    checkBox.setChecked(false);
                    return;
                }
                viewModel.setChecked(isChecked);
            }
        });
        checkBox.setChecked(viewModel.isChecked());
        productName.setText(viewModel.getStyledName());
        tvShortCode.setText(viewModel.getStyledUnit());
    }

    private void triggerCheckbox() {
        if (checkBox.isChecked()) {
            checkBox.setChecked(false);
        } else {
            checkBox.setChecked(true);
        }
    }
}
