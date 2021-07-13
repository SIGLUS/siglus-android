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

package org.openlmis.core.view.holder;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.googleanalytics.TrackerActions;
import org.openlmis.core.googleanalytics.TrackerCategories;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;
import org.openlmis.core.view.widget.InputFilterMinMax;
import org.openlmis.core.view.widget.MovementTypeDialog;
import roboguice.inject.InjectView;

public class StockMovementViewHolder extends BaseViewHolder {

  @InjectView(R.id.tx_date)
  TextView txMovementDate;

  @InjectView(R.id.tx_reason)
  TextView txReason;

  @InjectView(R.id.et_document_number)
  EditText etDocumentNo;

  @InjectView(R.id.et_received)
  EditText etReceived;

  @InjectView(R.id.et_negative_adjustment)
  EditText etNegativeAdjustment;

  @InjectView(R.id.et_positive_adjustment)
  EditText etPositiveAdjustment;

  @InjectView(R.id.et_issued)
  EditText etIssued;

  @InjectView(R.id.et_requested)
  EditText etRequested;

  @InjectView(R.id.tx_stock_on_hand)
  TextView txStockExistence;

  @InjectView(R.id.tx_signature)
  TextView txSignature;

  private Map<MovementReasonManager.MovementType, List<EditText>> movementViewMap;

  public StockMovementViewHolder(View itemView) {
    super(itemView);
    InputFilter[] filters = new InputFilter[]{new InputFilterMinMax(Integer.MAX_VALUE)};
    etReceived.setFilters(filters);
    etNegativeAdjustment.setFilters(filters);
    etPositiveAdjustment.setFilters(filters);
    etIssued.setFilters(filters);
    etRequested.setFilters(filters);

    initStockViewMap();
  }

  private void initStockViewMap() {
    movementViewMap = new HashMap<>();
    movementViewMap.put(MovementReasonManager.MovementType.ISSUE, asList(etIssued, etRequested));
    movementViewMap.put(MovementReasonManager.MovementType.RECEIVE, singletonList(etReceived));
    movementViewMap.put(MovementReasonManager.MovementType.NEGATIVE_ADJUST, singletonList(etNegativeAdjustment));
    movementViewMap.put(MovementReasonManager.MovementType.POSITIVE_ADJUST, singletonList(etPositiveAdjustment));
  }

  public void populate(final StockMovementViewModel model, StockCard stockCard) {
    removeTextChangeListeners(model, stockCard.calculateSOHFromLots());

    disableLine();
    hideUnderline();

    txMovementDate.setText(model.getMovementDate());
    etDocumentNo.setText(model.getDocumentNo());
    etReceived.setText(model.getReceived());
    etNegativeAdjustment.setText(model.getNegativeAdjustment());
    etPositiveAdjustment.setText(model.getPositiveAdjustment());
    etIssued.setText(model.getIssued());
    etRequested.setText(model.getRequested());
    txStockExistence.setText(model.getStockExistence());
    txSignature.setText(model.getSignature());
    if (model.getReason() != null) {
      txReason.setText(model.getReason().getDescription());
    } else {
      txReason.setText(StringUtils.EMPTY);
    }

    setItemViewTextColor(model);

    if (model.isDraft()) {
      setInitialDraftStyle(model);
    } else {
      itemView.setBackgroundColor(Color.TRANSPARENT);
    }

    addClickListeners(model, getPreviousMovementDate(stockCard));

    addTextChangedListeners(model, stockCard.calculateSOHFromLots());
  }

  private void setInitialDraftStyle(final StockMovementViewModel model) {
    txMovementDate.setEnabled(true);
    txReason.setEnabled(true);
    setEditableQuantityField(model);
    if (!TextUtils.isEmpty(model.getMovementDate())) {
      highLightAndShowBottomBtn();
    }
  }

  private void setEditableQuantityField(StockMovementViewModel model) {
    if (model.getReason() != null) {
      enableAndUnderlineEditText(etDocumentNo);
      resetStockEditText(model.getReason().getMovementType());
    }
  }

  private void resetStockEditText(MovementReasonManager.MovementType type) {
    for (Map.Entry<MovementReasonManager.MovementType, List<EditText>> movementView : movementViewMap.entrySet()) {
      if (movementView.getKey().equals(type)) {
        for (Object view : movementView.getValue()) {
          enableAndUnderlineEditText((EditText) view);
        }
      } else {
        for (Object view : movementView.getValue()) {
          disableAndRemoveUnderlineEditText((EditText) view);
        }
      }
    }
  }

  public void highLightAndShowBottomBtn() {
    itemView.setBackgroundResource(R.color.color_primary_50);
  }

  private void enableAndUnderlineEditText(EditText editText) {
    editText.setEnabled(true);
    editText.setBackground(new EditText(context).getBackground());
  }

  private void disableAndRemoveUnderlineEditText(EditText editText) {
    editText.setText(StringUtils.EMPTY);
    editText.setEnabled(false);
    editText.setBackground(null);
  }

  private void removeTextChangeListeners(StockMovementViewModel model, long currentStockOnHand) {
    etReceived.removeTextChangedListener(
        new StockMovementViewHolderEditTextWatcher(this, etReceived, model, currentStockOnHand));
    etNegativeAdjustment.removeTextChangedListener(
        new StockMovementViewHolderEditTextWatcher(this, etNegativeAdjustment, model,
            currentStockOnHand));
    etPositiveAdjustment.removeTextChangedListener(
        new StockMovementViewHolderEditTextWatcher(this, etPositiveAdjustment, model,
            currentStockOnHand));

    etIssued.removeTextChangedListener(
        new StockMovementViewHolderEditTextWatcher(this, etIssued, model, currentStockOnHand));
    etRequested.removeTextChangedListener(
        new StockMovementViewHolderEditTextWatcher(this, etRequested, model, currentStockOnHand));

    etDocumentNo.removeTextChangedListener(
        new StockMovementViewHolderEditTextWatcher(this, etDocumentNo, model, currentStockOnHand));
  }

  private void addClickListeners(final StockMovementViewModel model,
      final Date previousMovementDate) {
    txReason.setOnClickListener(v -> {
      if (model.isDraft()) {
        new MovementTypeDialog(context, new MovementSelectListener(model)).show();
        trackStockMovementEvent(TrackerActions.SELECT_REASON);
      }
    });

    txMovementDate.setOnClickListener(v -> {
      if (model.isDraft()) {
        showDatePickerDialog(model, previousMovementDate);
        trackStockMovementEvent(TrackerActions.SELECT_MOVEMENT_DATE);
      }
    });
  }

  private void trackStockMovementEvent(TrackerActions action) {
    LMISApp.getInstance().trackEvent(TrackerCategories.STOCK_MOVEMENT, action);
  }

  private void showDatePickerDialog(final StockMovementViewModel model,
      final Date previousMovementDate) {
    final Calendar today = DateUtil.getCurrentCalendar();

    DatePickerDialog dialog = new DatePickerDialog(context, DialogInterface.BUTTON_NEUTRAL,
        new MovementDateListener(model, previousMovementDate),
        today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
    dialog.show();
  }

  private void addTextChangedListeners(StockMovementViewModel model, long currentStockOnHand) {
    etReceived.addTextChangedListener(
        new StockMovementViewHolderEditTextWatcher(this, etReceived, model, currentStockOnHand));
    etNegativeAdjustment.addTextChangedListener(
        new StockMovementViewHolderEditTextWatcher(this, etNegativeAdjustment, model,
            currentStockOnHand));
    etPositiveAdjustment.addTextChangedListener(
        new StockMovementViewHolderEditTextWatcher(this, etPositiveAdjustment, model,
            currentStockOnHand));
    etIssued.addTextChangedListener(
        new StockMovementViewHolderEditTextWatcher(this, etIssued, model, currentStockOnHand));
    etDocumentNo.addTextChangedListener(
        new StockMovementViewHolderEditTextWatcher(this, etDocumentNo, model, currentStockOnHand));
    etRequested.addTextChangedListener(
        new StockMovementViewHolderEditTextWatcher(this, etRequested, model, currentStockOnHand));
  }

  private void setItemViewTextColor(StockMovementViewModel model) {
    if (model.isIssuedReason()) {
      setRowFontColor(context.getResources().getColor(R.color.color_black));
    } else {
      setRowFontColor(context.getResources().getColor(R.color.color_red));
    }
  }

  private void setRowFontColor(int color) {
    txMovementDate.setTextColor(color);
    txReason.setTextColor(color);
    etDocumentNo.setTextColor(color);
    etReceived.setTextColor(color);
    etPositiveAdjustment.setTextColor(color);
    etNegativeAdjustment.setTextColor(color);
    txStockExistence.setTextColor(color);
    txSignature.setTextColor(color);
  }

  private void hideUnderline() {
    etDocumentNo.setBackground(null);
    etIssued.setBackground(null);
    etRequested.setBackground(null);
    etNegativeAdjustment.setBackground(null);
    etPositiveAdjustment.setBackground(null);
    etReceived.setBackground(null);
  }

  private void disableLine() {
    etDocumentNo.setEnabled(false);
    etReceived.setEnabled(false);
    etNegativeAdjustment.setEnabled(false);
    etPositiveAdjustment.setEnabled(false);
    etIssued.setEnabled(false);
    etRequested.setEnabled(false);
    txMovementDate.setEnabled(false);
    txReason.setEnabled(false);
  }

  protected Date getPreviousMovementDate(StockCard stockCard) {
    List<StockMovementItem> stockMovements = stockCard.getStockMovementItemsWrapper();
    if (stockMovements != null && !stockMovements.isEmpty()) {
      Collections.sort(stockMovements,
          (item1, item2) -> item1.getMovementDate().compareTo(item2.getMovementDate()));
      return stockMovements.get(stockMovements.size() - 1).getMovementDate();
    }
    return null;
  }

  class MovementSelectListener implements MovementTypeDialog.OnMovementSelectListener {

    private final StockMovementViewModel model;

    public MovementSelectListener(StockMovementViewModel model) {
      this.model = model;
    }

    @Override
    public void onComplete(MovementReasonManager.MovementReason reason) {
      txReason.setText(reason.getDescription());
      model.setReason(reason);

      setMovementDate();

      clearQuantityAndDocumentNoField();
      setEditableQuantityField(model);
      highLightAndShowBottomBtn();
      setItemViewTextColor(model);
    }

    private void setMovementDate() {
      if (StringUtils.EMPTY.equals(txMovementDate.getText().toString())) {
        String movementDate = DateUtil.formatDate(DateUtil.getCurrentDate());
        txMovementDate.setText(movementDate);
        model.setMovementDate(movementDate);
      }
    }
  }

  private void clearQuantityAndDocumentNoField() {
    etDocumentNo.setText(StringUtils.EMPTY);
    etReceived.setText(StringUtils.EMPTY);
    etNegativeAdjustment.setText(StringUtils.EMPTY);
    etPositiveAdjustment.setText(StringUtils.EMPTY);
    etIssued.setText(StringUtils.EMPTY);
  }

  class MovementDateListener implements DatePickerDialog.OnDateSetListener {

    private final Date previousMovementDate;
    private final StockMovementViewModel model;

    public MovementDateListener(StockMovementViewModel model, Date previousMovementDate) {
      this.previousMovementDate = previousMovementDate;
      this.model = model;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

      Date chosenDate = new GregorianCalendar(year, monthOfYear, dayOfMonth).getTime();
      if (validateStockMovementDate(previousMovementDate, chosenDate)) {
        txMovementDate.setText(DateUtil.formatDate(chosenDate));
        model.setMovementDate(DateUtil.formatDate(chosenDate));
        highLightAndShowBottomBtn();
      } else {
        ToastUtil.show(R.string.msg_invalid_stock_movement_date);
      }
    }

    private boolean validateStockMovementDate(Date previousMovementDate, Date chosenDate) {
      Calendar today = DateUtil.getCurrentCalendar();

      return previousMovementDate == null || !previousMovementDate.after(chosenDate) && !chosenDate
          .after(today.getTime());
    }
  }

}
