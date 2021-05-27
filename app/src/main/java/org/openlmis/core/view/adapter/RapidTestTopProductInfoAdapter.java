package org.openlmis.core.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.ProgramDataFormBasicItem;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.SimpleTextWatcher;
import org.openlmis.core.view.widget.CleanableEditText;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class RapidTestTopProductInfoAdapter extends RecyclerView.Adapter<RapidTestTopProductInfoAdapter.RapidTestTopProductInfoViewHolder> {

    private final List<ProgramDataFormBasicItem> productInfos;

    private final List<CleanableEditText> editTexts;


    public RapidTestTopProductInfoAdapter(List<ProgramDataFormBasicItem> productInfos) {
        this.productInfos = productInfos;
        this.editTexts = new ArrayList<>();
    }

    @Override
    public RapidTestTopProductInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rapid_test_from, parent, false);
        final RapidTestTopProductInfoViewHolder rapidTestTopProductInfoViewHolder = new RapidTestTopProductInfoViewHolder(inflate);
        rapidTestTopProductInfoViewHolder.setIsRecyclable(false);
        return rapidTestTopProductInfoViewHolder;
    }

    @Override
    public void onBindViewHolder(RapidTestTopProductInfoViewHolder holder, int position) {
        final ProgramDataFormBasicItem formBasicItem = productInfos.get(position);
        holder.tvProductName.setText(formBasicItem.getProduct().getPrimaryName());
        holder.tvReceived.setText(String.valueOf(formBasicItem.getReceived()));
        holder.tvIssue.setText(String.valueOf(formBasicItem.getIssued()));
        holder.tvAdjustment.setText(String.valueOf(formBasicItem.getAdjustment()));

        //config etStock
        holder.etStock.setText(getValue(formBasicItem.getInitialAmount()));
        holder.etStock.setEnabled(Boolean.TRUE.equals(formBasicItem.getIsCustomAmount()) && (formBasicItem.getForm().getStatus() == null || formBasicItem.getForm().getStatus() == ProgramDataForm.STATUS.DRAFT));
        if (Boolean.TRUE.equals(formBasicItem.getIsCustomAmount())) {
            holder.etStock.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    formBasicItem.setInitialAmount(getEditValue(s));
                    super.afterTextChanged(s);
                }
            });
            editTexts.add(holder.etStock);
        }

        //config etInventory
        holder.etInventory.setText(getValue(formBasicItem.getInventory()));
        holder.etInventory.setEnabled(formBasicItem.getForm().getStatus() == null || formBasicItem.getForm().getStatus() == ProgramDataForm.STATUS.DRAFT);
        holder.etInventory.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                formBasicItem.setInventory(getEditValue(s));
                super.afterTextChanged(s);
            }
        });
        editTexts.add(holder.etInventory);

        try {
            if (!(TextUtils.isEmpty(formBasicItem.getValidate()))) {
                holder.tvValidate.setText(DateUtil.convertDate(formBasicItem.getValidate(), "dd/MM/yyyy", "MMM yyyy"));
            }
        } catch (ParseException e) {
            new LMISException(e, "RapidTestRnrForm.addView").reportToFabric();
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        for (CleanableEditText editText : editTexts) {
            editText.clearTextChangedListeners();
        }
        editTexts.clear();
        super.onDetachedFromRecyclerView(recyclerView);
    }

    public boolean isComplete() {
        for (EditText item : editTexts) {
            if (TextUtils.isEmpty(item.getText().toString())) {
                item.setError(LMISApp.getContext().getString(R.string.hint_error_input));
                item.requestFocus();
                return false;
            }
        }
        return true;
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

    @Override
    public int getItemCount() {
        return productInfos == null ? 0 : productInfos.size();
    }

    protected static class RapidTestTopProductInfoViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvProductName;
        private final CleanableEditText etStock;
        private final CleanableEditText etInventory;
        private final TextView tvReceived;
        private final TextView tvIssue;
        private final TextView tvAdjustment;
        private final TextView tvValidate;

        public RapidTestTopProductInfoViewHolder(View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_name);
            etStock = itemView.findViewById(R.id.et_stock);
            tvReceived = itemView.findViewById(R.id.tv_received);
            tvIssue = itemView.findViewById(R.id.tv_issue);
            tvAdjustment = itemView.findViewById(R.id.tv_adjustment);
            tvValidate = itemView.findViewById(R.id.tv_expire);
            etInventory = itemView.findViewById(R.id.et_inventory);
        }
    }
}
