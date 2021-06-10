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

package org.openlmis.core.view.widget;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.google.android.material.textfield.TextInputLayout;
import lombok.Getter;
import org.openlmis.core.R;
import roboguice.inject.InjectView;

public class AddBulkLotDialogFragment extends AddLotDialogFragment {

  public static boolean IS_OCCUPIED = false;

  @InjectView(R.id.et_soh_amount)
  private EditText etSOHAmount;

  @InjectView(R.id.ly_soh_amount)
  private TextInputLayout lySohAmount;
  @Getter
  private String quantity;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.dialog_add_bulk_lot, container, false);
  }

  @Override
  public boolean validate() {
    return validateAmount() && super.validate();
  }

  private boolean validateAmount() {
    quantity = etSOHAmount.getText().toString();
    if (quantity.isEmpty()) {
      lySohAmount.setError(getString(R.string.amount_field_cannot_be_empty));
      return Boolean.FALSE;
    } else if (Integer.parseInt(quantity) <= 0) {
      lySohAmount.setError(getString(R.string.amount_cannot_be_less_or_equal_to_zero));
      return Boolean.FALSE;
    }
    return Boolean.TRUE;
  }

}
