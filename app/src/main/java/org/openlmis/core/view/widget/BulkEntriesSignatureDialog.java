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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.openlmis.core.R;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.view.fragment.BaseDialogFragment;
import roboguice.inject.InjectView;

public class BulkEntriesSignatureDialog extends BaseDialogFragment {

  @Getter
  @Setter
  SignatureDialog.DialogDelegate delegate;

  @InjectView(R.id.et_process_date)
  private TextView etProcessDate;

  @InjectView(R.id.et_signature)
  private EditText etSignature;

  @InjectView(R.id.ly_signature)
  private TextInputLayout lySignature;

  @InjectView(R.id.btn_cancel)
  public TextView btnCancel;

  @InjectView(R.id.btn_done)
  public Button btnSign;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.dialog_bulk_entries_signature, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initUI();
  }

  @Override
  public void onStart() {
    super.onStart();
    setDialogAttributes();
    Bundle arguments = getArguments();
    if (arguments != null) {
      etProcessDate.setText(arguments.getString("Date"));
    }
  }

  public static Bundle getBundleToMe(String date) {
    Bundle bundle = new Bundle();
    bundle.putString("Date", date);
    return bundle;
  }

  public void show(@NonNull FragmentManager manager) {
    if (manager.findFragmentByTag("signature_dialog") != null) {
      return;
    }
    super.show(manager, "signature_dialog");
  }

  private void initUI() {
    btnCancel.setOnClickListener(getSingleClickButtonListener());
    btnSign.setOnClickListener(getSingleClickButtonListener());
    etSignature.setFilters(TextStyleUtil.getSignatureLimitation());
  }

  private SingleClickButtonListener getSingleClickButtonListener() {
    return new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View v) {
        switch (v.getId()) {
          case R.id.btn_done:
            onDone();
            break;
          case R.id.btn_cancel:
            onCancel();
            break;
          default:
            break;
        }
      }
    };
  }

  private void onDone() {
    if (!hasDelegate()) {
      return;
    }

    String signature = etSignature.getText().toString().trim();
    if (checkSignature(signature)) {
      btnSign.setEnabled(false);
      btnCancel.setEnabled(false);
      delegate.onSign(signature);
      dismiss();
    } else {
      lySignature.setError(getString(R.string.hint_signature_error_message));
    }
  }

  private void onCancel() {
    dismiss();
    if (hasDelegate()) {
      delegate.onCancel();
    }
  }

  private void setDialogAttributes() {
    WindowManager.LayoutParams params = new WindowManager.LayoutParams();
    Window window = Objects.requireNonNull(getDialog()).getWindow();
    if (window != null) {
      params.copyFrom(getDialog().getWindow().getAttributes());
    }
    params.width = (int) (getDialog().getContext().getResources().getDisplayMetrics().widthPixels * 0.8);
    getDialog().getWindow().setAttributes(params);
  }

  private boolean hasDelegate() {
    return delegate != null;
  }

  protected boolean checkSignature(String signature) {
    return signature.length() >= 3 && signature.length() <= 5 && signature.matches("\\D+");
  }
}
