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
import android.widget.LinearLayout;
import android.widget.TextView;
import org.openlmis.core.R;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.SelectEmergencyProductAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import roboguice.inject.InjectView;

public class SelectEmergencyProductsViewHolder extends BaseViewHolder {

  @InjectView(R.id.tv_product_name)
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

  public void populate(final SelectEmergencyProductAdapter selectEmergencyProductAdapter,
      String queryKeyWord, final InventoryViewModel viewModel) {
    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked == viewModel.isChecked()) {
          return;
        }

        if (isChecked && selectEmergencyProductAdapter.isReachLimit()) {
          ToastUtil.showForLongTime(R.string.msg_out_limit_of_selected_emergency_products);
          checkBox.setChecked(false);
          return;
        }
        viewModel.setChecked(isChecked);
      }
    });
    checkBox.setChecked(viewModel.isChecked());

    productName
        .setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, viewModel.getStyledName()));
    tvShortCode
        .setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, viewModel.getStyledUnit()));
  }

  private void triggerCheckbox() {
    checkBox.setChecked(!checkBox.isChecked());
  }
}
