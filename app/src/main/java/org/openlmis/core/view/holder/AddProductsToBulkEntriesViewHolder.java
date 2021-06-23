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
import org.openlmis.core.view.viewmodel.ProductsToBulkEntriesViewModel;
import roboguice.inject.InjectView;

public class AddProductsToBulkEntriesViewHolder extends BaseViewHolder {

  @InjectView(R.id.checkbox)
  private CheckBox checkBox;

  @InjectView(R.id.tv_product_name)
  private TextView productName;

  @InjectView(R.id.tv_product_type)
  private TextView productType;

  public AddProductsToBulkEntriesViewHolder(View itemView) {
    super(itemView);
  }


  public void populate(final ProductsToBulkEntriesViewModel viewModel, String queryKeyWord) {
    checkBox.setChecked(viewModel.isChecked());
    productName.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord,viewModel.getStyledProductName()));
    productType.setText(viewModel.getProduct().getType());
  }

  @NonNull
  private CompoundButton.OnCheckedChangeListener setCheckedProductListener(
      final ProductsToBulkEntriesViewModel viewModel) {
    return (buttonView, isChecked) -> viewModel.setChecked(isChecked);
  }

  public void putOnChangedListener(ProductsToBulkEntriesViewModel viewModel) {
    checkBox.setOnCheckedChangeListener(setCheckedProductListener(viewModel));
  }


}
