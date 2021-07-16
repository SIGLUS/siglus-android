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

import android.text.Editable;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.textfield.TextInputLayout;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.utils.SingleTextWatcher;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.view.viewmodel.AddDrugsToViaInventoryViewModel;
import roboguice.inject.InjectView;

public class AddDrugsToVIAViewHolder extends BaseViewHolder {

  @InjectView(R.id.tv_product_name)
  TextView productName;

  @InjectView(R.id.tv_short_code)
  TextView tvShortCode;

  @InjectView(R.id.touchArea_checkbox)
  LinearLayout taCheckbox;

  @InjectView(R.id.checkbox)
  CheckBox checkBox;

  @InjectView(R.id.action_panel)
  View actionPanel;

  @InjectView(R.id.tx_quantity)
  EditText txQuantity;

  @InjectView(R.id.action_divider)
  View actionDivider;

  @InjectView(R.id.ly_quantity)
  TextInputLayout lyQuantity;

  public AddDrugsToVIAViewHolder(View itemView) {
    super(itemView);
    txQuantity.setHint(R.string.label_hint_amount_requisition);
    taCheckbox.setOnClickListener(v -> triggerCheckbox());
  }

  public void populate(String queryKeyWord, final AddDrugsToViaInventoryViewModel viewModel) {
    setItemViewListener(viewModel);
    checkBox.setChecked(viewModel.isChecked());

    productName
        .setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord,
            TextStyleUtil.formatStyledProductNameForAddProductPage(viewModel.getProduct())));
    tvShortCode
        .setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, viewModel.getStyledUnit()));

    populateEditPanel(viewModel.getQuantity());
    if (viewModel.isValid()) {
      lyQuantity.setErrorEnabled(false);
    } else {
      lyQuantity.setError(context.getResources().getString(R.string.msg_inventory_check_failed));
    }

  }

  protected void setItemViewListener(final AddDrugsToViaInventoryViewModel viewModel) {

    final EditTextWatcher textWatcher = new EditTextWatcher(viewModel);
    txQuantity.removeTextChangedListener(textWatcher);

    checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
      if (isChecked) {
        showEditPanel(View.VISIBLE);
      } else {
        showEditPanel(View.GONE);
        populateEditPanel(StringUtils.EMPTY);

        viewModel.setQuantity(StringUtils.EMPTY);
      }
      viewModel.setChecked(isChecked);
    });

    txQuantity.addTextChangedListener(textWatcher);
  }

  protected void populateEditPanel(String quantity) {
    txQuantity.setText(quantity);
  }

  protected void showEditPanel(int visible) {
    actionDivider.setVisibility(visible);
    actionPanel.setVisibility(visible);
  }

  private void triggerCheckbox() {
    checkBox.setChecked(!checkBox.isChecked());
  }

  @SuppressWarnings("squid:S2160")
  static class EditTextWatcher extends SingleTextWatcher {

    private final AddDrugsToViaInventoryViewModel viewModel;

    public EditTextWatcher(AddDrugsToViaInventoryViewModel viewModel) {
      this.viewModel = viewModel;
    }

    @Override
    public void afterTextChanged(Editable editable) {
      viewModel.setQuantity(editable.toString());
    }
  }
}
