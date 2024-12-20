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
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.BlendModeColorFilterCompat;
import androidx.core.graphics.BlendModeCompat;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.fragment.BaseDialogFragment;
import org.openlmis.core.view.fragment.ConfirmGenerateLotNumberDialogFragment;
import roboguice.inject.InjectView;

public class AddLotDialogFragment extends BaseDialogFragment {

  @InjectView(R.id.ly_lot_number)
  private TextInputLayout lyLotNumber;

  @InjectView(R.id.et_lot_number)
  protected EditText etLotNumber;

  @InjectView(R.id.dp_add_new_lot)
  protected DatePicker datePicker;

  @InjectView(R.id.btn_cancel)
  private TextView btnCancel;

  @InjectView(R.id.btn_complete)
  private Button btnComplete;

  @InjectView(R.id.tv_expiry_date_warning)
  private TextView expiryDateWarning;

  @InjectView(R.id.drug_name)
  private TextView drugName;

  @Getter
  private String lotNumber;

  @Getter
  private String expiryDate;

  @Setter
  private SingleClickButtonListener listener;
  private AddLotWithoutNumberListener addLotWithoutNumberListener;
  private BaseLotListView.OnDismissListener onDismissListener;

  private static final String UNKNOWN_STRING = "Unknown";

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.dialog_add_lot, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    if (getArguments() != null) {
      String drugNameFromArgs = getArguments().getString(Constants.PARAM_STOCK_NAME);
      if (drugNameFromArgs != null) {
        this.drugName.setVisibility(View.VISIBLE);
        this.drugName.setText(drugNameFromArgs);
      }
    }
    btnCancel.setOnClickListener(listener);
    btnComplete.setOnClickListener((v) -> {
      etLotNumber.clearFocus();
      listener.onClick(v);
    });
    this.setCancelable(false);
    setLotEditTextFocusListener();
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Dialog dialog = super.onCreateDialog(savedInstanceState);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    return dialog;
  }

  public boolean validate() {
    clearErrorMessage();
    Date enteredDate = new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(),
        datePicker.getDayOfMonth()).getTime();
    expiryDate = DateUtil.formatDate(enteredDate, DateUtil.DB_DATE_FORMAT);

    if (StringUtils.isBlank(etLotNumber.getText().toString())) {
      showConfirmNoLotNumberDialog();
      return false;
    }

    lotNumber = etLotNumber.getText().toString().trim().toUpperCase() + "-"
        + DateUtil.convertDate(expiryDate, DateUtil.DB_DATE_FORMAT, DateUtil.SIMPLE_DATE_FORMAT);
    return true;
  }

  private void setLotEditTextFocusListener() {
    etLotNumber.setOnFocusChangeListener((view, hasFocus) -> {
      if (!hasFocus) {
        String inputContent = etLotNumber.getText().toString().trim();
        inputContent = removeConsecutiveHyphens(inputContent);
        inputContent = removeHyphenAtEnd(inputContent);
        etLotNumber.setText(inputContent);
      }
    });
  }

  private String removeConsecutiveHyphens(String content) {
    String modifiedContent = content.trim();
    if (!TextUtils.isEmpty(modifiedContent)) {
      modifiedContent = modifiedContent.replaceAll("-{2,}", "-");
    }
    return modifiedContent;
  }

  private String removeHyphenAtEnd(String content) {
    String modifiedContent = content.trim();
    if (!TextUtils.isEmpty(modifiedContent) && modifiedContent.endsWith("-")) {
      modifiedContent = modifiedContent.substring(0, modifiedContent.length() - 1);
    }
    return modifiedContent;
  }

  private void showConfirmNoLotNumberDialog() {
    Bundle bundle = new Bundle();
    bundle.putString(Constants.PARAM_MSG_CONFIRM_GENERATE_LOT_NUMBER,
        getString1(R.string.msg_confirm_empty_lot_number, drugName.getText()));
    final ConfirmGenerateLotNumberDialogFragment confirmDialog = new ConfirmGenerateLotNumberDialogFragment();
    confirmDialog.setArguments(bundle);
    confirmDialog.setPositiveClickListener(new SingleClickButtonListener() {
      @Override
      public void onSingleClick(View v) {
        confirmDialog.dismiss();
        addLotWithoutNumberListener.addLotWithoutNumber(expiryDate);
        AddLotDialogFragment.this.dismiss();
      }
    });
    confirmDialog.show(getParentFragmentManager(), "confirm generate lot number");
  }

  private String getString1(int resId) {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
      if (getContext() != null) {
        return getString(resId);
      } else {
        return UNKNOWN_STRING;
      }
    } else {
      if (isAdded()) {
        return getString(resId);
      } else {
        return UNKNOWN_STRING;
      }
    }
  }

  private String getString1(int resId, Object... formatArgs) {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
      if (getContext() != null) {
        return getString(resId, formatArgs);
      } else {
        return UNKNOWN_STRING;
      }
    } else {
      if (isAdded()) {
        return getString(resId, formatArgs);
      } else {
        return UNKNOWN_STRING;
      }
    }
  }

  private void clearErrorMessage() {
    lyLotNumber.setErrorEnabled(false);
    expiryDateWarning.setVisibility(View.GONE);
  }

  public boolean hasIdenticalLot(List<String> existingLots) {
    if (existingLots != null && existingLots.contains(lotNumber)) {
      lyLotNumber.setError(getString1(R.string.error_lot_already_exists));
      etLotNumber.getBackground().setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
          ContextCompat.getColor(requireContext(), R.color.color_red), BlendModeCompat.SRC_ATOP));
      return true;
    }
    return false;
  }

  public void setAddLotWithoutNumberListener(
      AddLotWithoutNumberListener addLotWithoutNumberListener) {
    this.addLotWithoutNumberListener = addLotWithoutNumberListener;
  }

  public void setOnDismissListener(BaseLotListView.OnDismissListener onDismissListener) {
    this.onDismissListener = onDismissListener;
  }

  public interface AddLotWithoutNumberListener {

    void addLotWithoutNumber(String expiryDate);
  }

  @Override
  public void onDismiss(DialogInterface dialog) {
    if (onDismissListener != null) {
      onDismissListener.onDismissAction();
    }
    super.onDismiss(dialog);
  }
}
