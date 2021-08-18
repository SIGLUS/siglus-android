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

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import org.openlmis.core.R;
import org.openlmis.core.utils.TextStyleUtil;
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

  public void populate(final NonBasicProductsViewModel viewModel, String queryKeyword) {
    cbIsChecked.setChecked(viewModel.isChecked());
    tvProductName.setText(viewModel.getStyledProductName());
    tvProductUnit.setText(viewModel.getProductType());
    tvProductName.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyword, viewModel.getStyledProductName()));
  }

  @NonNull
  private CompoundButton.OnCheckedChangeListener setCheckedProductListener(NonBasicProductsViewModel viewModel) {
    return (buttonView, isChecked) -> viewModel.setChecked(isChecked);
  }

  public void putOnChangedListener(NonBasicProductsViewModel viewModel) {
    cbIsChecked.setOnCheckedChangeListener(setCheckedProductListener(viewModel));
  }
}
