package org.openlmis.core.view.holder;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.utils.SingleTextWatcher;
import org.openlmis.core.view.activity.BaseActivity;
import org.openlmis.core.view.activity.InventoryActivity;
import org.openlmis.core.view.adapter.LotMovementAdapter;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;

import roboguice.inject.InjectView;

public class LotMovementViewHolder extends BaseViewHolder {

    @InjectView(R.id.stock_on_hand_in_lot)
    private TextView txStockOnHandInLot;

    @InjectView(R.id.tv_stock_on_hand_in_lot_tip)
    private TextView tvStockOnHandInLotTip;

    @InjectView(R.id.et_lot_amount)
    private EditText etLotAmount;

    @InjectView(R.id.ly_lot_amount)
    private TextInputLayout lyLotAmount;

    @InjectView(R.id.et_lot_info)
    private EditText etLotInfo;

    @InjectView(R.id.vg_soh_lot)
    private LinearLayout lySOHLot;

    @InjectView(R.id.iv_del)
    private ImageView iconDel;

    public LotMovementViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(final LotMovementViewModel viewModel, final LotMovementAdapter lotMovementAdapter) {
        final EditTextWatcher textWatcher = new EditTextWatcher(viewModel);
        etLotAmount.removeTextChangedListener(textWatcher);
        etLotAmount.addTextChangedListener(textWatcher);

        etLotAmount.setHint(LMISApp.getInstance().getString(R.string.hint_lot_amount));
        etLotInfo.setText(viewModel.getLotNumber() + " - " + viewModel.getExpiryDate());
        etLotInfo.setKeyListener(null);
        etLotAmount.setText(viewModel.getQuantity());

        if (viewModel.isValid() && viewModel.isQuantityValid()) {
            lyLotAmount.setErrorEnabled(false);
        } else {
            etLotAmount.requestFocus();
            if (!viewModel.isValid()) {
                lyLotAmount.setError(context.getResources().getString(R.string.alert_lot_quantity_error));
            }
            if (!viewModel.isQuantityValid()) {
                lyLotAmount.setError(context.getResources().getString(R.string.msg_invalid_quantity));
            }
        }

        etLotInfo.setOnKeyListener(null);
        etLotInfo.setBackground(null);

        txStockOnHandInLot.setText(viewModel.getLotSoh());
        if (isLotNewAdded(viewModel)) {
            tvStockOnHandInLotTip.setText(context.getResources().getString(R.string.label_new_added_lot));
            iconDel.setVisibility(View.VISIBLE);
            iconDel.setOnClickListener(getOnClickListenerForDeleteIcon(viewModel, lotMovementAdapter));
        }
        if (context instanceof InventoryActivity) {
            lySOHLot.setVisibility(View.GONE);
        }
    }

    @NonNull
    private View.OnClickListener getOnClickListenerForDeleteIcon(final LotMovementViewModel viewModel, final LotMovementAdapter lotMovementAdapter) {
        return new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(
                        Html.fromHtml(context.getResources().getString(R.string.msg_remove_new_lot_title)),
                        Html.fromHtml(context.getResources().getString(R.string.msg_remove_new_lot, viewModel.getLotNumber(), viewModel.getExpiryDate(), lotMovementAdapter.getProductName())),
                        context.getResources().getString(R.string.btn_remove_lot),
                        context.getResources().getString(R.string.btn_cancel), "confirm_dialog");
                dialogFragment.show(((BaseActivity) context).getFragmentManager(), "confirm_dialog");
                dialogFragment.setCallBackListener(new SimpleDialogFragment.MsgDialogCallBack() {
                    @Override
                    public void positiveClick(String tag) {
                        lotMovementAdapter.remove(viewModel);
                        if (context instanceof InventoryActivity) {
                            ((InventoryActivity) context).productListRecycleView.getAdapter().notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void negativeClick(String tag) {
                        dialogFragment.dismiss();
                    }
                });
            }
        };
    }

    private boolean isLotNewAdded(LotMovementViewModel viewModel) {
        return StringUtils.isBlank(viewModel.getLotSoh());
    }

    class EditTextWatcher extends SingleTextWatcher {

        private final LotMovementViewModel viewModel;

        public EditTextWatcher(LotMovementViewModel viewModel) {
            this.viewModel = viewModel;
        }

        @Override
        public void afterTextChanged(Editable editable) {
            viewModel.setQuantity(editable.toString());
        }
    }
}
