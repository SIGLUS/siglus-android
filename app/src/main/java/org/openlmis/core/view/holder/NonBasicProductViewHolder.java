package org.openlmis.core.view.holder;

import androidx.annotation.NonNull;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.NonBasicProductsViewModel;

import roboguice.inject.InjectView;

public class NonBasicProductViewHolder extends BaseViewHolder {

    @InjectView(R.id.checkbox)
    private CheckBox cbIsChecked;

    @InjectView(R.id.tv_product_name)
    private TextView tvProductName;

    @InjectView(R.id.tv_product_unit)
    private TextView tvProductUnit;

    public NonBasicProductViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(final NonBasicProductsViewModel viewModel) {
        cbIsChecked.setChecked(viewModel.isChecked());
        tvProductName.setText(viewModel.getStyledProductName());
        tvProductUnit.setText(viewModel.getProductType());
    }

    @NonNull
    private CompoundButton.OnCheckedChangeListener setCheckedProductListener(final NonBasicProductsViewModel viewModel) {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                viewModel.setChecked(isChecked);
            }
        };
    }

    public void putOnChangedListener(NonBasicProductsViewModel viewModel) {
        cbIsChecked.setOnCheckedChangeListener(setCheckedProductListener(viewModel));
    }
}
