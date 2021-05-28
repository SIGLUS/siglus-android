package org.openlmis.core.view.adapter;

import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.ProgramDataFormBasicItem;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.SimpleTextWatcher;
import org.openlmis.core.view.widget.CleanableEditText;
import org.openlmis.core.view.widget.RapidTestProductInfoView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

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
        return LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rapid_test_from, parent, false);
    }

    @Override
    public void onUpdateView(View itemView, int position) {
        TextView tvProductName = itemView.findViewById(R.id.tv_name);
        CleanableEditText etStock = itemView.findViewById(R.id.et_stock);
        CleanableEditText etInventory = itemView.findViewById(R.id.et_inventory);
        TextView tvReceived = itemView.findViewById(R.id.tv_received);
        TextView tvIssue = itemView.findViewById(R.id.tv_issue);
        TextView tvAdjustment = itemView.findViewById(R.id.tv_adjustment);
        TextView tvValidate = itemView.findViewById(R.id.tv_expire);
        final ProgramDataFormBasicItem formBasicItem = productInfos.get(position);
        tvProductName.setText(formBasicItem.getProduct().getPrimaryName());
        tvReceived.setText(String.valueOf(formBasicItem.getReceived()));
        tvIssue.setText(String.valueOf(formBasicItem.getIssued()));
        tvAdjustment.setText(String.valueOf(formBasicItem.getAdjustment()));

        //config etStock
        etStock.setText(getValue(formBasicItem.getInitialAmount()));
        etStock.setEnabled(Boolean.TRUE.equals(formBasicItem.getIsCustomAmount()) && (formBasicItem.getForm().getStatus() == null || formBasicItem.getForm().getStatus() == ProgramDataForm.STATUS.DRAFT));
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
        etInventory.setText(getValue(formBasicItem.getInventory()));
        etInventory.setEnabled(formBasicItem.getForm().getStatus() == null || formBasicItem.getForm().getStatus() == ProgramDataForm.STATUS.DRAFT);
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
        if (position < 0) return;
        final CleanableEditText editText = lastNotCompleteType == CHECK_TYPE_INVENTORY ? inventoryEditTexts.get(position) : stockEditTexts.get(position);
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
