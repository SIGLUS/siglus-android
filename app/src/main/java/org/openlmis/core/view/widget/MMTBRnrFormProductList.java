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
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.SimpleTextWatcher;
import org.openlmis.core.utils.ViewUtil;

public class MMTBRnrFormProductList extends LinearLayout {

  private final LayoutInflater layoutInflater;

  @Getter
  private final RnrFormHorizontalScrollView rnrItemsHorizontalScrollView;

  private final ViewGroup leftViewGroup;

  @Getter
  private final ViewGroup rightViewGroup;

  @Getter
  private View leftHeaderView;

  @Getter
  private ViewGroup rightHeaderView;

  private final List<Pair<EditText, EditTextWatcher>> editTexts = new ArrayList<>();

  public MMTBRnrFormProductList(Context context) {
    this(context, null);
  }

  public MMTBRnrFormProductList(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MMTBRnrFormProductList(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    layoutInflater = LayoutInflater.from(getContext());
    View container = layoutInflater.inflate(R.layout.view_mmia_rnr_form, this, true);
    rnrItemsHorizontalScrollView = container.findViewById(R.id.vg_right_scrollview);
    leftViewGroup = container.findViewById(R.id.rnr_from_list_product_name);
    rightViewGroup = container.findViewById(R.id.rnr_from_list);
  }

  public void setData(List<RnrFormItem> list) {
    leftViewGroup.removeAllViews();
    rightViewGroup.removeAllViews();
    editTexts.clear();
    generateHeaderView();
    generateItemView(list);
  }

  public boolean isCompleted() {
    for (Pair<EditText, EditTextWatcher> editText : editTexts) {
      if (TextUtils.isEmpty(editText.first.getText().toString()) || !isValidate(editText.first)) {
        editText.first.setError(getContext().getString(R.string.hint_error_input));
        editText.first.requestFocus();
        return false;
      }
    }
    return true;
  }

  private boolean isValidate(EditText editText) {
    if (editText.getId() != R.id.et_adjustment) {
      return true;
    }
    Long text;
    try {
      text = Long.valueOf(editText.getText().toString());
    } catch (NumberFormatException e) {
      text = null;
    }
    return text != null;
  }

  private void generateHeaderView() {
    leftHeaderView = generateLeftView(null, true);
    rightHeaderView = generateRightView(null, true, false);
    post(() -> {
      setRightItemWidth(rightHeaderView);
      ViewUtil.syncViewHeight(leftHeaderView, rightHeaderView);
      MarginLayoutParams marginLayoutParams = (MarginLayoutParams) getLayoutParams();
      marginLayoutParams.topMargin = rightHeaderView.getLayoutParams().height;
      setLayoutParams(marginLayoutParams);
    });
  }

  private void generateItemView(List<RnrFormItem> rnrFormItemList) {
    boolean isFirstlyFillRequisition = isFirstlyFillRequisition(rnrFormItemList);
    for (RnrFormItem item : rnrFormItemList) {
      View leftView = generateLeftView(item, false);
      ViewGroup rightView = generateRightView(item, false, isFirstlyFillRequisition);
      leftViewGroup.addView(leftView);
      rightViewGroup.addView(rightView);
      post(() -> {
        setRightItemWidth(rightView);
        ViewUtil.syncViewHeight(leftView, rightView);
      });
    }
  }

  private void setRightItemWidth(final ViewGroup rightView) {
    int rightWidth = rnrItemsHorizontalScrollView.getWidth();
    if (rightViewGroup.getWidth() < rightWidth) {
      int childCount = rightView.getChildCount();
      int rightViewWidth = getRightViewWidth(rightWidth, childCount);
      for (int i = 0; i < childCount; i++) {
        rightView.getChildAt(i).getLayoutParams().width = rightViewWidth;
      }
      rightView.getChildAt(0).getLayoutParams().width =
          rightViewWidth + getRightViewRemainderWidth(rightWidth, childCount);
    }
  }

  private View generateLeftView(RnrFormItem item, boolean isHeaderView) {
    View view = layoutInflater.inflate(R.layout.item_rnr_from_product_name, this, false);
    TextView tvPrimaryName = view.findViewById(R.id.tv_primary_name);
    TextView textView = view.findViewById(R.id.mmia_product_barcode_column_code);

    if (isHeaderView) {
      tvPrimaryName.setText(R.string.label_rnrfrom_left_header);
      tvPrimaryName.setGravity(Gravity.CENTER);
      textView.setText(R.string.label_fnm);
      view.setBackgroundResource(R.color.color_mmia_info_name);
    } else {
      Product product = item.getProduct();
      tvPrimaryName.setText(product.getPrimaryName());
      textView.setText(product.getCode());
      view.setBackgroundResource(R.color.color_mmtb_product_item);
    }
    return view;
  }

  private ViewGroup generateRightView(RnrFormItem item, boolean isHeaderView, boolean isFirstlyFillRequisition) {
    ViewGroup view = (ViewGroup) layoutInflater.inflate(R.layout.item_mmtb_product, this, false);
    TextView tvIssuedUnit = view.findViewById(R.id.tv_issued_unit);
    EditText etInitialAmount = view.findViewById(R.id.et_initial_amount);
    TextView tvReceived = view.findViewById(R.id.tv_received);
    EditText etIssued = view.findViewById(R.id.et_issued);
    EditText etAdjustment = view.findViewById(R.id.et_adjustment);
    EditText etInventory = view.findViewById(R.id.et_inventory);
    TextView tvValidate = view.findViewById(R.id.tv_validate);
    if (isHeaderView) {
      tvIssuedUnit.setText(R.string.label_issued_unit);
      etInitialAmount.setText(R.string.label_initial_amount);
      etInitialAmount.setSingleLine(false);
      tvReceived.setText(R.string.label_received_mmia);
      etIssued.setText(R.string.label_issued_mmia);
      etIssued.setEnabled(false);
      etAdjustment.setText(R.string.label_adjustment);
      etAdjustment.setEnabled(false);
      etAdjustment.setSingleLine(false);
      etInventory.setText(R.string.label_inventory);
      etInventory.setEnabled(false);
      tvValidate.setText(R.string.label_validate);
      view.setBackgroundResource(R.color.color_mmia_info_name);
    } else {
      tvIssuedUnit.setText(item.getProduct().getStrength());
      boolean isArchived = item.getProduct().isArchived();
      tvReceived.setText(getValue(isArchived, item.getReceived()));
      editTexts.add(configEditText(item, etInitialAmount, getValue(isArchived, item.getInitialAmount())));

      boolean isInitialAmountEditable = false;
      boolean isInitialAmountNull = Boolean.TRUE.equals(item.getIsCustomAmount());
      if (isInitialAmountNull && (item.getForm().getStatus() == null || item.getForm().isDraft())) {
        if (isFirstlyFillRequisition) {
          isInitialAmountEditable = true;
        } else {
          etInitialAmount.setText("0");
        }
      }
      etInitialAmount.setEnabled(isInitialAmountEditable);

      editTexts.add(configEditText(item, etIssued, getValue(isArchived, item.getIssued())));
      editTexts.add(configEditText(item, etAdjustment, getValue(isArchived, item.getAdjustment())));
      editTexts.add(configEditText(item, etInventory, getValue(isArchived, item.getInventory())));
      try {
        if (!(TextUtils.isEmpty(item.getValidate()) || isArchived)) {
          tvValidate.setText(DateUtil.convertDate(item.getValidate(), DateUtil.SIMPLE_DATE_FORMAT,
              DateUtil.DB_DATE_FORMAT));
        }
      } catch (Exception e) {
        new LMISException(e, "MMTBRnrForm.addRightView").reportToFabric();
      }
    }
    return view;
  }

  private Pair<EditText, EditTextWatcher> configEditText(RnrFormItem item, EditText text, String value) {
    text.setText(value);
    text.setEnabled(true);
    EditTextWatcher textWatcher = new EditTextWatcher(item, text);
    text.addTextChangedListener(textWatcher);
    return new Pair<>(text, textWatcher);
  }

  private String getValue(boolean isArchived, Long value) {
    if (isArchived) {
      return String.valueOf(0);
    }
    return value == null ? "" : String.valueOf(value.longValue());
  }

  private int getRightViewWidth(int rightWidth, int childCount) {
    return (rightWidth - (childCount - 1) * getDividerWidth()) / childCount;
  }

  private int getRightViewRemainderWidth(int rightWidth, int childCount) {
    return (rightWidth - (childCount - 1) * getDividerWidth()) % childCount;
  }

  private int getDividerWidth() {
    return (int) getResources().getDimension(R.dimen.divider);
  }

  public void removeListenerOnDestroyView() {
    for (Pair<EditText, EditTextWatcher> editText : editTexts) {
      if (editText.first != null) {
        editText.first.clearFocus();
        if (editText.second != null) {
          editText.first.removeTextChangedListener(editText.second);
        }
      }
    }
  }

  private boolean isFirstlyFillRequisition(List<RnrFormItem> formItems) {
    for (RnrFormItem item : formItems) {
      if (item.getInitialAmount() != null) {
        return false;
      }
    }
    return true;
  }

  private static class EditTextWatcher extends SimpleTextWatcher {

    private final RnrFormItem item;
    private final EditText editText;

    public EditTextWatcher(RnrFormItem item, EditText editText) {
      this.item = item;
      this.editText = editText;
    }

    @Override
    public void afterTextChanged(Editable etText) {
      switch (editText.getId()) {
        case R.id.et_initial_amount:
          item.setInitialAmount(getEditValue(etText));
          break;
        case R.id.et_inventory:
          item.setInventory(getEditValue(etText));
          break;
        case R.id.et_issued:
          item.setIssued(getEditValue(etText));
          break;
        case R.id.et_adjustment:
          item.setAdjustment(getEditValue(etText));
          break;
        default:
          // do nothing
      }
    }

    private Long getEditValue(Editable etText) {
      Long editTextValue;
      try {
        editTextValue = Long.valueOf(etText.toString());
      } catch (NumberFormatException e) {
        editTextValue = null;
      }
      return editTextValue;
    }
  }

}
