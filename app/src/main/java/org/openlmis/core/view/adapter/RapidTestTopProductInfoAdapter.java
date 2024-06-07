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

package org.openlmis.core.view.adapter;

import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.SimpleTextWatcher;
import org.openlmis.core.view.widget.CleanableEditText;
import org.openlmis.core.view.widget.RapidTestProductInfoView;

public class RapidTestTopProductInfoAdapter extends RapidTestProductInfoView.Adapter {

  /**
   * stock EditText Cache
   */
  private final List<CleanableEditText> issueEditTexts;

  /**
   * stock EditText Cache
   */
  private final List<CleanableEditText> adjustmentEditTexts;

  /**
   * inventory EditText Cache
   */
  private final List<CleanableEditText> inventoryEditTexts;

  /**
   * stock EditText Cache
   */
  private final List<CleanableEditText> stockEditTexts;

  private final List<RnrFormItem> productInfos;

  private CleanableEditText lastNotCompleteEditText;

  public RapidTestTopProductInfoAdapter(List<RnrFormItem> productInfos) {
    this.productInfos = productInfos;
    this.issueEditTexts = new ArrayList<>();
    this.adjustmentEditTexts = new ArrayList<>();
    this.inventoryEditTexts = new ArrayList<>();
    this.stockEditTexts = new ArrayList<>();
  }

  @Override
  public View onCreateView(ViewGroup parent, int position) {
    return LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_rapid_test_from, parent, false);
  }

  @Override
  public void onUpdateView(View itemView, int position) {
    TextView tvProductName = itemView.findViewById(R.id.tv_name);
    TextView tvReceived = itemView.findViewById(R.id.tv_received);
    TextView tvValidate = itemView.findViewById(R.id.tv_expire);
    final RnrFormItem formBasicItem = productInfos.get(position);
    tvProductName.setText(formBasicItem.getProduct().getPrimaryName());
    tvReceived.setText(String.valueOf(formBasicItem.getReceived()));

    boolean isDraftOrUnknownStatus = isDraftOrUnknownStatus(formBasicItem);
    // config etIssue
    CleanableEditText etIssue = itemView.findViewById(R.id.et_issue);
    etIssue.setText(getValue(formBasicItem.getIssued()));
    etIssue.setEnabled(isDraftOrUnknownStatus);
    etIssue.addTextChangedListener(new SimpleTextWatcher() {
      @Override
      public void afterTextChanged(Editable s) {
        formBasicItem.setIssued(getEditValue(s));
        super.afterTextChanged(s);
      }
    });
    issueEditTexts.add(etIssue);
    // config etAdjustment
    CleanableEditText etAdjustment = itemView.findViewById(R.id.et_adjustment);
    etAdjustment.setText(getValue(formBasicItem.getAdjustment()));
    etAdjustment.setEnabled(isDraftOrUnknownStatus);
    etAdjustment.addTextChangedListener(new SimpleTextWatcher() {
      @Override
      public void afterTextChanged(Editable s) {
        formBasicItem.setAdjustment(getEditValue(s));
        super.afterTextChanged(s);
      }
    });
    adjustmentEditTexts.add(etAdjustment);
    //config etStock
    CleanableEditText etStock = itemView.findViewById(R.id.et_stock);
    etStock.setText(getValue(formBasicItem.getInitialAmount()));
    etStock.setEnabled(Boolean.TRUE.equals(formBasicItem.getIsCustomAmount()) && isDraftOrUnknownStatus);
    if (Boolean.TRUE.equals(formBasicItem.getIsCustomAmount())) {
      etStock.addTextChangedListener(new SimpleTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
          formBasicItem.setInitialAmount(getEditValue(s));
          super.afterTextChanged(s);
        }
      });
      stockEditTexts.add(etStock);
    }
    //config etInventory
    CleanableEditText etInventory = itemView.findViewById(R.id.et_inventory);
    etInventory.setText(getValue(formBasicItem.getInventory()));
    etInventory.setEnabled(isDraftOrUnknownStatus);
    etInventory.addTextChangedListener(new SimpleTextWatcher() {
      @Override
      public void afterTextChanged(Editable s) {
        formBasicItem.setInventory(getEditValue(s));
        super.afterTextChanged(s);
      }
    });
    inventoryEditTexts.add(etInventory);
    try {
      if (!(TextUtils.isEmpty(formBasicItem.getValidate()))) {
        tvValidate.setText(DateUtil.convertDate(formBasicItem.getValidate(), "dd/MM/yyyy", "MMM yyyy"));
      }
    } catch (Exception e) {
      new LMISException(e, "RapidTestRnrForm.addView").reportToFabric();
    }
  }

  private boolean isDraftOrUnknownStatus(RnrFormItem formBasicItem) {
    return formBasicItem.getForm().getStatus() == null || formBasicItem.getForm().isDraft();
  }

  @Override
  protected void onNotifyDataChangeCalled() {
    clearEditText();
  }

  @Override
  protected void onDetachFromWindow() {
    clearEditText();
  }

  private void clearEditText() {
    clearEditTexts(issueEditTexts);
    clearEditTexts(adjustmentEditTexts);
    clearEditTexts(inventoryEditTexts);
    clearEditTexts(stockEditTexts);
  }

  private void clearEditTexts(List<CleanableEditText> editTexts) {
    for (CleanableEditText editText : editTexts) {
      editText.clearTextChangedListeners();
    }
    editTexts.clear();
  }

  public int getNotCompletePosition() {
    Integer emptyIssuePosition = getNotCompletePositionFromEditTexts(issueEditTexts);
    if (emptyIssuePosition != null) {
      return emptyIssuePosition;
    }

    Integer emptyAdjustmentPosition = getNotCompletePositionFromEditTexts(adjustmentEditTexts);
    if (emptyAdjustmentPosition != null) {
      return emptyAdjustmentPosition;
    }

    Integer emptyStockPosition = getNotCompletePositionFromEditTexts(stockEditTexts);
    if (emptyStockPosition != null) {
      return emptyStockPosition;
    }

    Integer emptyInventoryPosition = getNotCompletePositionFromEditTexts(inventoryEditTexts);
    if (emptyInventoryPosition != null) {
      return emptyInventoryPosition;
    }

    return -1;
  }

  @Nullable
  private Integer getNotCompletePositionFromEditTexts(
      List<CleanableEditText> editTexts
  ) {
    for (int i = 0; i < editTexts.size(); i++) {
      CleanableEditText cleanableEditText = editTexts.get(i);
      Editable text = cleanableEditText.getText();
      if (text != null && TextUtils.isEmpty(text.toString())) {
        lastNotCompleteEditText = cleanableEditText;
        return i;
      }
    }
    return null;
  }

  public void showError() {
    if (lastNotCompleteEditText != null) {
      lastNotCompleteEditText.setError(LMISApp.getContext().getString(R.string.hint_error_input));
      lastNotCompleteEditText.requestFocus();
    }
  }

  private String getValue(Long value) {
    return value == null ? "" : String.valueOf(value.longValue());
  }

  private Long getEditValue(Editable etText) {
    Long editText;
    try {
      editText = Long.valueOf(etText.toString());
    } catch (NumberFormatException e) {
      editText = null;
    }
    return editText;
  }

  public void clearFocusByPosition(int position) {
    clearFocusFromEditTextsByPosition(position, issueEditTexts);
    clearFocusFromEditTextsByPosition(position, adjustmentEditTexts);
    clearFocusFromEditTextsByPosition(position, inventoryEditTexts);
    clearFocusFromEditTextsByPosition(position, stockEditTexts);
  }

  private void clearFocusFromEditTextsByPosition(
      int position, List<CleanableEditText> editTexts
  ) {
    if (position >= 0 && position < editTexts.size()) {
      editTexts.get(position).clearFocus();
    }
  }

  @Override
  public int getItemCount() {
    return productInfos == null ? 0 : productInfos.size();
  }

}
