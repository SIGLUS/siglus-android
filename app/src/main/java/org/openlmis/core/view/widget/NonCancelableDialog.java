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

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import org.openlmis.core.R;
import org.openlmis.core.view.fragment.BaseDialogFragment;

public class NonCancelableDialog extends BaseDialogFragment {

  public static NonCancelableDialog newInstance(int messageId) {
    Bundle bundle = new Bundle();
    bundle.putInt("messageResId", messageId);
    NonCancelableDialog unCancelableDialog = new NonCancelableDialog();
    unCancelableDialog.setArguments(bundle);
    return unCancelableDialog;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View contentView = inflater.inflate(R.layout.dialog_initial_dirty_data_check, container, false);
    TextView textView = contentView.findViewById(R.id.dialog_message);
    textView.setText(requireArguments().getInt("messageResId"));
    return contentView;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    Dialog dialog = super.onCreateDialog(savedInstanceState);
    dialog.setCanceledOnTouchOutside(false);
    setCancelable(false);
    return dialog;
  }

  public void show(FragmentManager manager) {
    if (manager.findFragmentByTag("initial_dirty_data_check_dialog") != null) {
      return;
    }
    super.show(manager, "initial_dirty_data_check_dialog");
  }
}
