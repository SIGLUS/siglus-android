package org.openlmis.core.view.holder;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.RegimeProductViewModel;
import roboguice.inject.InjectView;


public class SelectRegimeProductsViewHolder extends BaseViewHolder {

  @InjectView(R.id.tv_short_code)
  TextView tvShortCode;

  @InjectView(R.id.checkbox)
  CheckBox checkBox;

  public SelectRegimeProductsViewHolder(View itemView) {
    super(itemView);
  }

  public void populate(final RegimeProductViewModel viewModel) {
    checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> viewModel.setChecked(isChecked));
    checkBox.setChecked(viewModel.isChecked());
    tvShortCode.setText(viewModel.getShortCode());
  }
}
