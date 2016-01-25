package org.openlmis.core.view.holder;

import android.app.DatePickerDialog;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.InputFilter;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.SingleTextWatcher;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.widget.InputFilterMinMax;

import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import roboguice.inject.InjectView;


public class InitialInventoryViewHolder extends BaseViewHolder {

    @InjectView(R.id.product_name)
    TextView productName;
    @InjectView(R.id.product_unit)
    TextView productUnit;
    @InjectView(R.id.ly_quantity)
    TextInputLayout lyQuantity;
    @InjectView(R.id.tx_quantity)
    EditText txQuantity;
    @InjectView(R.id.tx_expire_date)
    TextView txExpireDate;
    @InjectView(R.id.action_divider)
    View actionDivider;
    @InjectView(R.id.checkbox)
    CheckBox checkBox;
    @InjectView(R.id.action_panel)
    View actionPanel;
    @InjectView(R.id.action_view_history)
    TextView tvHistoryAction;
    @InjectView(R.id.touchArea_checkbox)
    LinearLayout taCheckbox;

    public InitialInventoryViewHolder(View itemView) {
        super(itemView);
        initView();
    }

    private void initView() {
        txQuantity.setFilters(new InputFilter[]{new InputFilterMinMax(Integer.MAX_VALUE)});
        txQuantity.setHint(R.string.hint_quantity_in_stock);
        taCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerCheckbox();
            }
        });
    }

    private void triggerCheckbox() {
        if (checkBox.isChecked()) {
            checkBox.setChecked(false);
        } else {
            checkBox.setChecked(true);
            txQuantity.requestFocus();
        }
    }

    public void populate(final InventoryViewModel viewModel, String queryKeyWord, ViewHistoryListener listener) {
        setItemViewListener(viewModel);

        checkBox.setChecked(viewModel.isChecked());

        productName.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, viewModel.getStyledName()));
        productUnit.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, viewModel.getStyleType()));

        populateEditPanel(viewModel.getQuantity(), viewModel.optFirstExpiryDate());

        if (viewModel.isValid()) {
            lyQuantity.setErrorEnabled(false);
        } else {
            lyQuantity.setError(context.getResources().getString(R.string.msg_inventory_check_failed));
        }

        initHistoryView(viewModel, listener);
    }

    protected void setItemViewListener(final InventoryViewModel viewModel) {
        txExpireDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(viewModel);
            }
        });

        final EditTextWatcher textWatcher = new EditTextWatcher(viewModel);
        txQuantity.removeTextChangedListener(textWatcher);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    showEditPanel(View.VISIBLE);
                } else {
                    showEditPanel(View.GONE);
                    populateEditPanel(StringUtils.EMPTY, StringUtils.EMPTY);

                    viewModel.setQuantity(StringUtils.EMPTY);
                    viewModel.clearExpiryDates();
                }
                viewModel.setChecked(isChecked);
            }
        });

        txQuantity.addTextChangedListener(textWatcher);
    }

    private void initHistoryView(final InventoryViewModel viewModel, final ViewHistoryListener listener) {
        tvHistoryAction.setVisibility(viewModel.getProduct().isArchived() ? View.VISIBLE : View.GONE);
        tvHistoryAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.viewHistory(viewModel.getStockCard());
                }
            }
        });
    }

    protected void populateEditPanel(String quantity, String expireDate) {
        txQuantity.setText(quantity);
        txExpireDate.setText(expireDate);
    }

    protected void showEditPanel(int visible) {
        actionDivider.setVisibility(visible);
        actionPanel.setVisibility(visible);
    }

    class EditTextWatcher extends SingleTextWatcher {

        private final InventoryViewModel viewModel;

        public EditTextWatcher(InventoryViewModel viewModel) {
            this.viewModel = viewModel;
        }

        @Override
        public void afterTextChanged(Editable editable) {
            viewModel.setQuantity(editable.toString());
        }
    }

    public void showDatePicker(final InventoryViewModel viewModel) {
        final Calendar today = GregorianCalendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(context, DatePickerDialog.BUTTON_NEUTRAL, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                GregorianCalendar date = new GregorianCalendar(year, monthOfYear, dayOfMonth);
                if (today.before(date)) {
                    String dateString = new StringBuilder().append(dayOfMonth).append("/").append(monthOfYear + 1).append("/").append(year).toString();
                    try {
                        txExpireDate.setText(DateUtil.convertDate(dateString, "dd/MM/yyyy", "MMM yyyy"));
                    } catch (ParseException e) {
                        new LMISException(e).reportToFabric();
                    }
                    viewModel.addExpiryDate(dateString, false);
                } else {
                    ToastUtil.show(R.string.msg_invalid_date);
                }
            }
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

        dialog.show();
    }

    public interface ViewHistoryListener {
        void viewHistory(StockCard stockCard);
    }

}
