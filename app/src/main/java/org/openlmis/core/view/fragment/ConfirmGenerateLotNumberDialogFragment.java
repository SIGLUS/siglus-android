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

package org.openlmis.core.view.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import org.openlmis.core.R;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import roboguice.inject.InjectView;

public class ConfirmGenerateLotNumberDialogFragment extends BaseDialogFragment {

  @InjectView(R.id.tv_generate_lot_number_msg)
  TextView msgGenerateLotNumber;

  @InjectView(R.id.btn_confirm_generate)
  Button btnConfirm;

  @InjectView(R.id.btn_cancel)
  Button btnCancel;

  private SingleClickButtonListener positiveClickListener;

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    if (getArguments() != null) {
      String message = getArguments().getString(Constants.PARAM_MSG_CONFIRM_GENERATE_LOT_NUMBER);
      if (message != null) {
        msgGenerateLotNumber.setText(Html.fromHtml(message));
      }
    }

    btnConfirm.setOnClickListener(positiveClickListener);
    btnCancel.setOnClickListener(v -> ConfirmGenerateLotNumberDialogFragment.this.dismiss());
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.dialog_confirm_generate_lot_number, container);
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Dialog dialog = super.onCreateDialog(savedInstanceState);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    return dialog;
  }

  public void setPositiveClickListener(SingleClickButtonListener listener) {
    positiveClickListener = listener;
  }
}
