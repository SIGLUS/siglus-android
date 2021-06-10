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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.ProgramDataForm.Status;
import org.openlmis.core.model.ProgramDataFormBasicItem;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.SimpleTextWatcher;
import org.openlmis.core.view.widget.CleanableEditText;
import org.openlmis.core.view.widget.RapidTestProductInfoView;

public class RapidTestTopProductInfoAdapter extends RapidTestProductInfoView.Adapter {

  /**
   * all EditText pass validate flag
   */
  public static final int ALL_COMPLETE = -1;

  private static final int CHECK_TYPE_INVENTORY = 1;

  private static final int CHECK_TYPE_STOCK = 1;

  /**
   * inventory EditText Cache
   */
  private final List<CleanableEditText> inventoryEditTexts;

  /**
   * stock EditText Cache
   */
  private final List<CleanableEditText> stockEditTexts;

  private final List<ProgramDataFormBasicItem> productInfos;

  private int lastNotCompleteType = -1;

  public RapidTestTopProductInfoAdapter(List<ProgramDataFormBasicItem> productInfos) {
    this.productInfos = productInfos;
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
    TextView tvIssue = itemView.findViewById(R.id.tv_issue);
    TextView tvValidate = itemView.findViewById(R.id.tv_expire);
    final ProgramDataFormBasicItem formBasicItem = productInfos.get(position);
    tvProductName.setText(formBasicItem.getProduct().getPrimaryName());
    tvReceived.setText(String.valueOf(formBasicItem.getReceived()));
    tvIssue.setText(String.valueOf(formBasicItem.getIssued()));
    TextView tvAdjustment = itemView.findViewById(R.id.tv_adjustment);
    tvAdjustment.setText(String.valueOf(formBasicItem.getAdjustment()));
    //config etStock
    CleanableEditText etStock = itemView.findViewById(R.id.et_stock);
    etStock.setText(getValue(formBasicItem.getInitialAmount()));
    etStock.setEnabled(Boolean.TRUE.equals(formBasicItem.getIsCustomAmount()) && (
        formBasicItem.getForm().getStatus() == null
            || formBasicItem.getForm().getStatus() == Status.DRAFT));
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
    etInventory.setEnabled(formBasicItem.getForm().getStatus() == null
        || formBasicItem.getForm().getStatus() == Status.DRAFT);
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
        tvValidate
            .setText(DateUtil.convertDate(formBasicItem.getValidate(), "dd/MM/yyyy", "MMM yyyy"));
      }
    } catch (ParseException e) {
      new LMISException(e, "RapidTestRnrForm.addView").reportToFabric();
    }
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
    for (CleanableEditText editText : inventoryEditTexts) {
      editText.clearTextChangedListeners();
    }
    for (CleanableEditText editText : stockEditTexts) {
      editText.clearTextChangedListeners();
    }
    inventoryEditTexts.clear();
    stockEditTexts.clear();
  }

  public int getNotCompletePosition() {
    for (int i = 0; i < inventoryEditTexts.size(); i++) {
      final CleanableEditText item = inventoryEditTexts.get(i);
      if (TextUtils.isEmpty(item.getText().toString())) {
        lastNotCompleteType = CHECK_TYPE_INVENTORY;
        return i;
      }
    }
    for (int i = 0; i < stockEditTexts.size(); i++) {
      final CleanableEditText item = stockEditTexts.get(i);
      if (TextUtils.isEmpty(item.getText().toString())) {
        lastNotCompleteType = CHECK_TYPE_STOCK;
        return i;
      }
    }
    return ALL_COMPLETE;
  }

  public void showError(int position) {
    if (position < 0) {
      return;
    }
    final CleanableEditText editText =
        lastNotCompleteType == CHECK_TYPE_INVENTORY ? inventoryEditTexts.get(position)
            : stockEditTexts.get(position);
    editText.setError(LMISApp.getContext().getString(R.string.hint_error_input));
    editText.requestFocus();
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
    if (position >= 0 && position < inventoryEditTexts.size()) {
      inventoryEditTexts.get(position).clearFocus();
    }
    if (position >= 0 && position < stockEditTexts.size()) {
      stockEditTexts.get(position).clearFocus();
    }
  }

  @Override
  public int getItemCount() {
    return productInfos == null ? 0 : productInfos.size();
  }
}
