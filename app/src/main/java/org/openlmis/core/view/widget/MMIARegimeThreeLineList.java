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
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openlmis.core.R;
import org.openlmis.core.model.RegimenItemThreeLines;
import org.openlmis.core.model.RnRForm;

public class MMIARegimeThreeLineList extends LinearLayout {

  private List<RegimenItemThreeLines> dataList;
  Map<String, RegimenItemThreeLines> dataMap;
  private final List<EditText> patientsTotalEdits = new ArrayList<>();
  private final List<EditText> patientsPharmacyEdits = new ArrayList<>();
  private LayoutInflater layoutInflater;
  private TextView mmiaThreeLinePatientsTotal;
  private TextView mmiaThreeLinePharmacyTotal;

  private String attrFirstLine;
  private String attrFirstLineKey;
  private String attrSecondLine;
  private String attrSecondLineKey;
  private String attrThirdLine;
  private String attrThirdLineKey;

  public enum CountType {
    PATIENTSAMOUNT,
    PHARMACYAMOUNT,
  }

  public MMIARegimeThreeLineList(Context context) {
    super(context);
    init();
  }

  public MMIARegimeThreeLineList(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public MMIARegimeThreeLineList(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    setOrientation(LinearLayout.VERTICAL);
    layoutInflater = LayoutInflater.from(getContext());
    attrFirstLine = getString(R.string.mmia_1stline);
    attrFirstLineKey = getString(R.string.key_regime_3lines_1);
    attrSecondLine = getString(R.string.mmia_2ndline);
    attrSecondLineKey = getString(R.string.key_regime_3lines_2);
    attrThirdLine = getString(R.string.mmia_3rdline);
    attrThirdLineKey = getString(R.string.key_regime_3lines_3);

  }

  private String getString(int id) {
    return getContext().getString(id);
  }

  public void initView(TextView total, TextView pharmacyTotal,
      List<RegimenItemThreeLines> dataList) {
    mmiaThreeLinePatientsTotal = total;
    mmiaThreeLinePharmacyTotal = pharmacyTotal;
    dataMap = new HashMap<>();
    this.dataList = dataList;
    initCategoryList();
    addViewItem(dataMap.get(attrFirstLineKey));
    addViewItem(dataMap.get(attrSecondLineKey));
    addViewItem(dataMap.get(attrThirdLineKey));
    mmiaThreeLinePatientsTotal.setText(String.valueOf(getTotal(CountType.PATIENTSAMOUNT)));
    mmiaThreeLinePharmacyTotal.setText(String.valueOf(getTotal(CountType.PHARMACYAMOUNT)));
  }

  private void initCategoryList() {
    for (RegimenItemThreeLines itemThreeLines : dataList) {
      dataMap.put(itemThreeLines.getRegimeTypes(), itemThreeLines);
    }
  }

  private Map<String, String> linesMap() {
    Map<String, String> linesMap = new HashMap<>();
    linesMap.put(attrFirstLineKey, attrFirstLine);
    linesMap.put(attrSecondLineKey, attrSecondLine);
    linesMap.put(attrThirdLineKey, attrThirdLine);
    return linesMap;
  }

  private void addViewItem(RegimenItemThreeLines itemThreeLines) {
    View viewItem = layoutInflater
        .inflate(R.layout.fragment_mmia_requisition_regime_threeline_item, this, false);
    TextView tvNameText = viewItem.findViewById(R.id.tv_title);
    EditText patientsTotalEdit = viewItem.findViewById(R.id.therapeutic_total);
    EditText patientsPharmacyEdit = viewItem.findViewById(R.id.therapeutic_pharmacy);

    Map<String, String> linesMap = linesMap();
    tvNameText.setText(linesMap.get(itemThreeLines.getRegimeTypes()));
    Long patientsAmount = itemThreeLines.getPatientsAmount();
    if (patientsAmount != null) {
      patientsTotalEdit.setText(String.valueOf(patientsAmount));
    }
    patientsTotalEdit
        .addTextChangedListener(new EditTextWatcher(itemThreeLines, CountType.PATIENTSAMOUNT));
    patientsTotalEdits.add(patientsTotalEdit);

    Long pharmacyAmount = itemThreeLines.getPharmacyAmount();
    if (pharmacyAmount != null) {
      patientsPharmacyEdit.setText(String.valueOf(pharmacyAmount));
    }
    patientsPharmacyEdit
        .addTextChangedListener(new EditTextWatcher(itemThreeLines, CountType.PHARMACYAMOUNT));
    patientsPharmacyEdits.add(patientsPharmacyEdit);

    addView(viewItem);
  }

  public boolean hasEmptyField() {
    for (RegimenItemThreeLines item : dataList) {
      if (item.getPatientsAmount() == null || item.getPharmacyAmount() == null) {
        return true;
      }
    }
    return false;
  }

  public boolean isCompleted() {
    boolean completedTotal = true;
    boolean completedPharmacy = true;
    for (EditText editText : patientsTotalEdits) {
      if (TextUtils.isEmpty(editText.getText().toString())) {
        editText.setError(getContext().getString(R.string.hint_error_input));
        editText.requestFocus();
        completedTotal = false;
      }
    }
    for (EditText editText : patientsPharmacyEdits) {
      if (TextUtils.isEmpty(editText.getText().toString())) {
        editText.setError(getContext().getString(R.string.hint_error_input));
        editText.requestFocus();
        completedPharmacy = false;
      }
    }
    return completedTotal && completedPharmacy;
  }

  public void deHighLightTotal() {
    mmiaThreeLinePatientsTotal.setBackground(getResources().getDrawable(R.color.color_page_gray));
    mmiaThreeLinePharmacyTotal.setBackground(getResources().getDrawable(R.color.color_page_gray));
  }

  public List<RegimenItemThreeLines> getDataList() {
    return dataList;
  }

  class EditTextWatcher implements TextWatcher {

    private final RegimenItemThreeLines item;
    private final CountType type;

    public EditTextWatcher(RegimenItemThreeLines item, CountType counttype) {
      this.item = item;
      this.type = counttype;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      // do nothing
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      // do nothing
    }

    @Override
    public void afterTextChanged(Editable editable) {
      long count = 0L;
      try {
        count = Long.parseLong(editable.toString());
      } catch (NumberFormatException ignored) {
        // do nothing
      }
      if (CountType.PATIENTSAMOUNT == type) {
        item.setPatientsAmount(count);
        mmiaThreeLinePatientsTotal.setText(String.valueOf(getTotal(type)));
      } else if (CountType.PHARMACYAMOUNT == type) {
        item.setPharmacyAmount(count);
        mmiaThreeLinePharmacyTotal.setText(String.valueOf(getTotal(type)));
      }
    }
  }

  public long getTotal(CountType counttype) {
    return RnRForm.caculateTotalRegimenTypeAmount(dataList, counttype);
  }
}