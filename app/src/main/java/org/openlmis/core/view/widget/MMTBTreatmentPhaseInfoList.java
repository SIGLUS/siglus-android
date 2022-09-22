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
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.openlmis.core.R;

public class MMTBTreatmentPhaseInfoList extends LinearLayout {

  private static final String MOCK_ADULT_CATEGORY = "Adult";
  private static final String MOCK_PEDIATRIC_CATEGORY = "Pediatric";

  private static final List<TreatmentPhaseItem> mockData = Collections.unmodifiableList(Arrays.asList(
      new TreatmentPhaseItem("Adult Treatment Phase 1", "", MOCK_ADULT_CATEGORY),
      new TreatmentPhaseItem("Adult Treatment Phase 2", "", MOCK_ADULT_CATEGORY),
      new TreatmentPhaseItem("Adult Treatment Phase 3", "", MOCK_ADULT_CATEGORY),
      new TreatmentPhaseItem("Adult Treatment Phase 4", "", MOCK_ADULT_CATEGORY),
      new TreatmentPhaseItem("Adult Treatment Phase 5", "", MOCK_ADULT_CATEGORY),
      new TreatmentPhaseItem("Adult Treatment Phase 6", "", MOCK_ADULT_CATEGORY),
      new TreatmentPhaseItem("Adult Treatment Phase 7", "", MOCK_ADULT_CATEGORY),
      new TreatmentPhaseItem("Pediatric Treatment Phase 1", "", MOCK_PEDIATRIC_CATEGORY),
      new TreatmentPhaseItem("Pediatric Treatment Phase 2", "", MOCK_PEDIATRIC_CATEGORY),
      new TreatmentPhaseItem("Pediatric Treatment Phase 3", "", MOCK_PEDIATRIC_CATEGORY),
      new TreatmentPhaseItem("Pediatric Treatment Phase 4", "", MOCK_PEDIATRIC_CATEGORY),
      new TreatmentPhaseItem("Pediatric Treatment Phase 5", "", MOCK_PEDIATRIC_CATEGORY),
      new TreatmentPhaseItem("Pediatric Treatment Phase 6", "", MOCK_PEDIATRIC_CATEGORY),
      new TreatmentPhaseItem("Pediatric Treatment Phase 7", "", MOCK_PEDIATRIC_CATEGORY)
  ));

  private RotateTextView rtvAdult;
  private RotateTextView rtvPediatric;
  private final LinearLayout llAdultContainer;
  private final LinearLayout llPediatricContainer;
  private final LayoutInflater layoutInflater;
  private final List<EditText> editTexts = new ArrayList<>();

  public MMTBTreatmentPhaseInfoList(Context context) {
    this(context, null);
  }

  public MMTBTreatmentPhaseInfoList(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MMTBTreatmentPhaseInfoList(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    layoutInflater = LayoutInflater.from(context);
    layoutInflater.inflate(R.layout.layout_mmtb_requisition_treatment_phase_info, this, true);
    llAdultContainer = findViewById(R.id.ll_adult_container);
    llPediatricContainer = findViewById(R.id.ll_pediatric_container);
    rtvAdult = findViewById(R.id.rtv_adult);
    rtvPediatric = findViewById(R.id.rtv_pediatric);
    setData();
  }

  public boolean isCompleted() {
    for (EditText editText : editTexts) {
      if (TextUtils.isEmpty(editText.getText().toString())) {
        editText.setError(getContext().getString(R.string.hint_error_input));
        editText.requestFocus();
        return false;
      }
    }
    return true;
  }

  // TODO set data from form
  private void setData() {
    for (TreatmentPhaseItem phaseItem : mockData) {
      View itemView = layoutInflater.inflate(R.layout.item_mmtb_requisition_treatment_phase, this, false);
      TextView tvTitle = itemView.findViewById(R.id.tv_title);
      tvTitle.setText(phaseItem.name);
      EditText etAmount = itemView.findViewById(R.id.et_treatment_phase_amount);
      etAmount.setText(phaseItem.value);
      etAmount.addTextChangedListener(new EditTextWatcher(phaseItem));
      editTexts.add(etAmount);
      if (MOCK_ADULT_CATEGORY.equals(phaseItem.category)) {
        itemView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.color_e5f2ff));
        etAmount.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_blue_no_border));
        llAdultContainer.addView(itemView);
      } else {
        itemView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.color_cce5ff));
        etAmount.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_light_blue_no_border));
        llPediatricContainer.addView(itemView);
      }
    }
    post(this::updateLeftHeader);
  }

  private void updateLeftHeader() {
    LayoutParams adultParams = (LayoutParams) rtvAdult.getLayoutParams();
    adultParams.height = llAdultContainer.getHeight();
    rtvAdult.setLayoutParams(adultParams);
    LayoutParams childrenParams = (LayoutParams) rtvPediatric.getLayoutParams();
    childrenParams.height = llPediatricContainer.getHeight();
    rtvPediatric.setLayoutParams(childrenParams);
  }

  @AllArgsConstructor
  @Data
  private static class TreatmentPhaseItem {

    private String name;
    private String value;
    private String category;
  }

  private static class EditTextWatcher implements android.text.TextWatcher {

    private final TreatmentPhaseItem item;

    public EditTextWatcher(TreatmentPhaseItem item) {
      this.item = item;
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
      item.setValue(editable.toString());
    }
  }
}
