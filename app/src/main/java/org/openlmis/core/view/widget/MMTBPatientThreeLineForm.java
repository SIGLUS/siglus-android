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

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openlmis.core.R;
import org.openlmis.core.constant.ReportConstants;
import org.openlmis.core.model.RegimenItemThreeLines;
import org.openlmis.core.utils.SimpleTextWatcher;

public class MMTBPatientThreeLineForm extends LinearLayout {

  private final Map<String, String> keyToFieldMap;
  private final List<EditText> patientsPharmacyEdits = new ArrayList<>();
  private final List<EditText> patientsTotalEdits = new ArrayList<>();
  private final Map<String, RegimenItemThreeLines> keyToDataMap = new HashMap<>();
  private LinearLayout llAgeRange;
  private LayoutInflater layoutInflater;

  public MMTBPatientThreeLineForm(Context context) {
    this(context, null);
  }

  public MMTBPatientThreeLineForm(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MMTBPatientThreeLineForm(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    Map<String, String> map = new HashMap<>();
    map.put(ReportConstants.KEY_MMTB_THREE_LINE_1, context.getString(R.string.mmtb_three_line_1));
    map.put(ReportConstants.KEY_MMTB_THREE_LINE_2, context.getString(R.string.mmtb_three_line_2));
    map.put(ReportConstants.KEY_MMTB_THREE_LINE_3, context.getString(R.string.mmtb_three_line_3));
    keyToFieldMap = Collections.unmodifiableMap(map);
    initView();
  }

  public boolean isCompleted() {
    for (int i = 0; i < patientsTotalEdits.size(); i++) {
      EditText patientTotalText = patientsTotalEdits.get(i);
      if (TextUtils.isEmpty(patientTotalText.getText().toString())) {
        patientTotalText.setError(getContext().getString(R.string.hint_error_input));
        patientTotalText.requestFocus();
        return false;
      }
      if (patientsPharmacyEdits.size() > 0) {
        EditText editPharmacyText = patientsPharmacyEdits.get(i);
        if (TextUtils.isEmpty(editPharmacyText.getText().toString())) {
          editPharmacyText.setError(getContext().getString(R.string.hint_error_input));
          editPharmacyText.requestFocus();
          return false;
        }
      }
    }
    return true;
  }

  private void initView() {
    setOrientation(LinearLayout.VERTICAL);
    layoutInflater = LayoutInflater.from(getContext());
    layoutInflater.inflate(R.layout.layout_mmtb_requisition_user_quantification, this, true);
    llAgeRange = findViewById(R.id.mmtb_requisition_age_range_container);
  }

  public void setData(List<RegimenItemThreeLines> data) {
    for (RegimenItemThreeLines item : data) {
      keyToDataMap.put(item.getRegimeTypes(), item);
    }
    llAgeRange.removeAllViews();
    addViewItem(keyToDataMap.get(ReportConstants.KEY_MMTB_THREE_LINE_1));
    addViewItem(keyToDataMap.get(ReportConstants.KEY_MMTB_THREE_LINE_2));
    addViewItem(keyToDataMap.get(ReportConstants.KEY_MMTB_THREE_LINE_3));
  }

  private void addViewItem(RegimenItemThreeLines itemThreeLines) {
    if (itemThreeLines == null) {
      return;
    }
    View viewItem = layoutInflater.inflate(R.layout.layout_mmtb_requisition_three_line_item, llAgeRange, false);
    TextView tvNameText = viewItem.findViewById(R.id.tv_title);
    EditText patientsTotalEdit = viewItem.findViewById(R.id.therapeutic_total);
    EditText patientsPharmacyEdit = viewItem.findViewById(R.id.therapeutic_pharmacy);
    tvNameText.setText(keyToFieldMap.get(itemThreeLines.getRegimeTypes()));
    Long patientsAmount = itemThreeLines.getPatientsAmount();
    if (patientsAmount != null) {
      patientsTotalEdit.setText(String.valueOf(patientsAmount));
    }
    patientsTotalEdit.addTextChangedListener(
        new EditTextWatcher(itemThreeLines, RegimenItemThreeLines.CountType.PATIENTS_AMOUNT));
    patientsTotalEdits.add(patientsTotalEdit);

    Long pharmacyAmount = itemThreeLines.getPharmacyAmount();
    if (pharmacyAmount != null) {
      patientsPharmacyEdit.setText(String.valueOf(pharmacyAmount));
    }
    patientsPharmacyEdit
        .addTextChangedListener(new EditTextWatcher(itemThreeLines, RegimenItemThreeLines.CountType.PHARMACY_AMOUNT));
    patientsPharmacyEdits.add(patientsPharmacyEdit);

    llAgeRange.addView(viewItem);
  }

  private static class EditTextWatcher extends SimpleTextWatcher {

    private final RegimenItemThreeLines item;
    private final RegimenItemThreeLines.CountType type;

    public EditTextWatcher(RegimenItemThreeLines item, RegimenItemThreeLines.CountType counttype) {
      this.item = item;
      this.type = counttype;
    }

    @Override
    public void afterTextChanged(Editable editable) {
      Long count = null;
      try {
        count = Long.parseLong(editable.toString());
      } catch (NumberFormatException ignored) {
        // do nothing
      }
      if (RegimenItemThreeLines.CountType.PATIENTS_AMOUNT == type) {
        item.setPatientsAmount(count);
      } else if (RegimenItemThreeLines.CountType.PHARMACY_AMOUNT == type) {
        item.setPharmacyAmount(count);
      }
    }
  }
}
