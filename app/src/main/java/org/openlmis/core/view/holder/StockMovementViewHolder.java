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

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.googleAnalytics.TrackerActions;
import org.openlmis.core.googleAnalytics.TrackerCategories;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.SingleTextWatcher;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.StockMovementAdapter;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;
import org.openlmis.core.view.widget.InputFilterMinMax;
import org.openlmis.core.view.widget.MovementTypeDialog;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roboguice.inject.InjectView;


public class StockMovementViewHolder extends BaseViewHolder {

    @InjectView(R.id.tx_date)
    TextView txMovementDate;

    @InjectView(R.id.tx_reason)
    TextView txReason;

    @InjectView(R.id.et_document_no)
    EditText etDocumentNo;

    @InjectView(R.id.et_received)
    EditText etReceived;

    @InjectView(R.id.et_negative_adjustment)
    EditText etNegativeAdjustment;

    @InjectView(R.id.et_positive_adjustment)
    EditText etPositiveAdjustment;

    @InjectView(R.id.et_issued)
    EditText etIssued;

    @InjectView(R.id.tx_stock_on_hand)
    TextView txStockExistence;

    @InjectView(R.id.tx_signature)
    TextView txSignature;

    private Drawable editTextBackground;
    private StockMovementAdapter.MovementChangedListener movementChangeListener;
    private Map<StockMovementItem.MovementType, EditText> movementViewMap;

    public StockMovementViewHolder(View itemView, StockMovementAdapter.MovementChangedListener movementChangeListener) {
        super(itemView);
        this.movementChangeListener = movementChangeListener;

        editTextBackground = new EditText(context).getBackground();

        InputFilter[] filters = new InputFilter[]{new InputFilterMinMax(Integer.MAX_VALUE)};
        etReceived.setFilters(filters);
        etNegativeAdjustment.setFilters(filters);
        etPositiveAdjustment.setFilters(filters);
        etIssued.setFilters(filters);

        initStockViewMap();
    }

    private void initStockViewMap() {
        movementViewMap = new HashMap<>();
        movementViewMap.put(StockMovementItem.MovementType.ISSUE, etIssued);
        movementViewMap.put(StockMovementItem.MovementType.RECEIVE, etReceived);
        movementViewMap.put(StockMovementItem.MovementType.NEGATIVE_ADJUST, etNegativeAdjustment);
        movementViewMap.put(StockMovementItem.MovementType.POSITIVE_ADJUST, etPositiveAdjustment);
    }

    public void populate(final StockMovementViewModel model, StockCard stockCard) {
        removeTextChangeListeners(model, stockCard.getStockOnHand());

        disableLine();
        hideUnderline();

        txMovementDate.setText(model.getMovementDate());
        etDocumentNo.setText(model.getDocumentNo());
        etReceived.setText(model.getReceived());
        etNegativeAdjustment.setText(model.getNegativeAdjustment());
        etPositiveAdjustment.setText(model.getPositiveAdjustment());
        etIssued.setText(model.getIssued());
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

        addTextChangedListeners(model, stockCard.getStockOnHand());
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

    private void resetStockEditText(StockMovementItem.MovementType type) {
        for (Map.Entry<StockMovementItem.MovementType, EditText> movementView : movementViewMap.entrySet()) {
            if (movementView.getKey().equals(type)) {
                enableAndUnderlineEditText(movementView.getValue());
            } else {
                disableAndRemoveUnderlineEditText(movementView.getValue());
            }
        }
    }

    public void highLightAndShowBottomBtn() {
        itemView.setBackgroundResource(R.color.color_primary_50);

        if (movementChangeListener != null) {
            movementChangeListener.movementChange();
        }
    }

    private void enableAndUnderlineEditText(EditText editText) {
        editText.setEnabled(true);
        editText.setBackground(editTextBackground);
    }

    private void disableAndRemoveUnderlineEditText(EditText editText) {
        editText.setText(StringUtils.EMPTY);
        editText.setEnabled(false);
        editText.setBackground(null);
    }

    private void removeTextChangeListeners(StockMovementViewModel model, long currentStockOnHand) {

        etReceived.removeTextChangedListener(new EditTextWatcher(etReceived, model, currentStockOnHand));

        etNegativeAdjustment.removeTextChangedListener(new EditTextWatcher(etNegativeAdjustment, model, currentStockOnHand));

        etPositiveAdjustment.removeTextChangedListener(new EditTextWatcher(etPositiveAdjustment, model, currentStockOnHand));

        etIssued.removeTextChangedListener(new EditTextWatcher(etIssued, model, currentStockOnHand));

        etDocumentNo.removeTextChangedListener(new EditTextWatcher(etDocumentNo, model, currentStockOnHand));
    }

    private void addClickListeners(final StockMovementViewModel model, final Date previousMovementDate) {
        txReason.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (model.isDraft()) {
                    new MovementTypeDialog(context, new MovementSelectListener(model)).show();
                    trackStockMovementEvent(TrackerActions.SelectReason);
                }
            }
        });

        txMovementDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (model.isDraft()) {
                    showDatePickerDialog(model, previousMovementDate);
                    trackStockMovementEvent(TrackerActions.SelectMovementDate);
                }
            }
        });
    }

    private void trackStockMovementEvent(TrackerActions action) {
        LMISApp.getInstance().trackEvent(TrackerCategories.StockMovement, action);
    }

    private void showDatePickerDialog(final StockMovementViewModel model, final Date previousMovementDate) {
        final Calendar today = GregorianCalendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(context, DatePickerDialog.BUTTON_NEUTRAL,
                new MovementDateListener(model, previousMovementDate),
                today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

        dialog.show();
    }

    private void addTextChangedListeners(StockMovementViewModel model, long currentStockOnHand) {
        EditTextWatcher watcher = new EditTextWatcher(etReceived, model, currentStockOnHand);
        etReceived.addTextChangedListener(watcher);

        EditTextWatcher watcher1 = new EditTextWatcher(etNegativeAdjustment, model, currentStockOnHand);
        etNegativeAdjustment.addTextChangedListener(watcher1);

        EditTextWatcher watcher2 = new EditTextWatcher(etPositiveAdjustment, model, currentStockOnHand);
        etPositiveAdjustment.addTextChangedListener(watcher2);

        EditTextWatcher watcher3 = new EditTextWatcher(etIssued, model, currentStockOnHand);
        etIssued.addTextChangedListener(watcher3);

        EditTextWatcher watcher4 = new EditTextWatcher(etDocumentNo, model, currentStockOnHand);
        etDocumentNo.addTextChangedListener(watcher4);
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
        txMovementDate.setEnabled(false);
        txReason.setEnabled(false);
    }

    public void resetLine() {
        txMovementDate.setText(StringUtils.EMPTY);
        txReason.setText(StringUtils.EMPTY);
        txStockExistence.setText(StringUtils.EMPTY);

        etDocumentNo.setText(StringUtils.EMPTY);
        etReceived.setText(StringUtils.EMPTY);
        etNegativeAdjustment.setText(StringUtils.EMPTY);
        etPositiveAdjustment.setText(StringUtils.EMPTY);
        etIssued.setText(StringUtils.EMPTY);
        disableLine();
        hideUnderline();
    }

    protected Date getPreviousMovementDate(StockCard stockCard) {
        List<StockMovementItem> stockMovements = stockCard.getStockMovementItemsWrapper();
        if (stockMovements != null) {
            if (!stockMovements.isEmpty()) {
                Collections.sort(stockMovements, new Comparator<StockMovementItem>() {
                    public int compare(StockMovementItem item1, StockMovementItem item2) {
                        return item1.getMovementDate().compareTo(item2.getMovementDate());
                    }
                });
                return stockMovements.get(stockMovements.size() - 1).getMovementDate();
            }
        }
        return null;
    }

    class EditTextWatcher extends SingleTextWatcher {

        private final View view;
        private final long currentStockOnHand;
        private final StockMovementViewModel model;

        public EditTextWatcher(View view, StockMovementViewModel model, long currentStockOnHand) {
            this.view = view;
            this.currentStockOnHand = currentStockOnHand;
            this.model = model;
        }

        @Override
        public void afterTextChanged(Editable editable) {
            setValue(view, model, currentStockOnHand);
        }

        private void setValue(View v, StockMovementViewModel model, long currentStockOnHand) {
            String text = ((TextView) v).getText().toString();

            if (v != etDocumentNo) {
                updateStockExistence(v, model, currentStockOnHand, text);
            }

            if (v == etReceived) {
                model.setReceived(etReceived.getText().toString());
            } else if (v == etIssued) {
                model.setIssued(etIssued.getText().toString());
            } else if (v == etPositiveAdjustment) {
                model.setPositiveAdjustment(etPositiveAdjustment.getText().toString());
            } else if (v == etNegativeAdjustment) {
                model.setNegativeAdjustment(etNegativeAdjustment.getText().toString());
            } else if (v == etDocumentNo) {
                model.setDocumentNo(etDocumentNo.getText().toString());
            }
        }

        private void updateStockExistence(View v, StockMovementViewModel model, long currentStockOnHand, String text) {
            long number = 0;
            if (!StringUtils.isEmpty(text)) {
                number = Long.parseLong(text);
            }

            String stockExistence = "";
            if (v == etReceived || v == etPositiveAdjustment) {
                stockExistence = String.valueOf(currentStockOnHand + number);
            } else if (v == etIssued || v == etNegativeAdjustment) {
                stockExistence = String.valueOf(currentStockOnHand - number);
            }
            txStockExistence.setText(stockExistence);
            model.setStockExistence(stockExistence);
        }
    }

    class MovementSelectListener implements MovementTypeDialog.OnMovementSelectListener {

        private StockMovementViewModel model;

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
                String movementDate = DateUtil.formatDate(new Date());
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

        private Date previousMovementDate;
        private StockMovementViewModel model;

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
            Calendar today = GregorianCalendar.getInstance();

            return previousMovementDate == null || !previousMovementDate.after(chosenDate) && !chosenDate.after(today.getTime());
        }
    }

}
