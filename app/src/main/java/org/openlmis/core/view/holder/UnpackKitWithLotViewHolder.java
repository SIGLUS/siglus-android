package org.openlmis.core.view.holder;

import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.lang.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.view.adapter.LotMovementAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.UnpackKitInventoryViewModel;
import org.openlmis.core.view.widget.MovementChangeLotListView;

import roboguice.inject.InjectView;
import rx.functions.Action1;

public class UnpackKitWithLotViewHolder extends BaseViewHolder{
    @InjectView(R.id.tv_kit_expected_quantity)
    TextView tvKitExpectedQuantity;

    @InjectView(R.id.tv_alert_quantity_message)
    TextView tvQuantityMessage;

    @InjectView(R.id.tv_confirm_no_stock)
    TextView tvConfirmNoStock;

    @InjectView(R.id.tv_confirm_has_stock)
    TextView tvConfirmHasStock;

    @InjectView(R.id.vg_soh_pop)
    ViewGroup vg_soh_pop;

    @InjectView(R.id.tv_product_name)
    TextView tvProductName;

    @InjectView(R.id.tv_product_unit)
    TextView tvProductUnit;

    @InjectView(R.id.view_lot_list)
    MovementChangeLotListView lotListView;

    protected InventoryViewModel viewModel;

    public UnpackKitWithLotViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(final InventoryViewModel viewModel, Action1 setConfirmNoStockReceivedAction) {
        this.viewModel = viewModel;
        lotListView.initLotListView(viewModel, getMovementChangedListener());

        initViewHolderStyle(viewModel);
        setConfirmStockClickListener((UnpackKitInventoryViewModel) viewModel, setConfirmNoStockReceivedAction);

        validateIfShouldShowUpEmptyLotWarning(viewModel);
        updatePop(viewModel);
    }

    private LotMovementAdapter.MovementChangedListener getMovementChangedListener() {
        return new LotMovementAdapter.MovementChangedListener() {
            @Override
            public void movementChange() {
                updatePop(viewModel);
            }
        };
    }

    private void validateIfShouldShowUpEmptyLotWarning(InventoryViewModel inventoryViewModel) {
        if (((UnpackKitInventoryViewModel) inventoryViewModel).shouldShowEmptyLotWarning()) {
            vg_soh_pop.setVisibility(View.VISIBLE);
            vg_soh_pop.setBackgroundResource(R.drawable.inventory_pop);

            tvConfirmHasStock.setVisibility(View.GONE);
            tvConfirmNoStock.setVisibility(View.VISIBLE);

            tvQuantityMessage.setText(LMISApp.getContext().getResources().getString(R.string.message_no_stock_amount_change));
            tvQuantityMessage.setTextColor(context.getResources().getColor(R.color.color_black));
            tvKitExpectedQuantity.setTextColor(this.context.getResources().getColor(R.color.color_red));
        }

        if (((UnpackKitInventoryViewModel) inventoryViewModel).isConfirmedNoStockReceived()) {
            vg_soh_pop.setVisibility(View.VISIBLE);
            vg_soh_pop.setBackgroundResource(R.drawable.inventory_pop);

            tvConfirmHasStock.setVisibility(View.VISIBLE);
            tvConfirmNoStock.setVisibility(View.GONE);

            tvQuantityMessage.setText(LMISApp.getContext().getResources().getString(R.string.message_no_stock_received));
            tvQuantityMessage.setTextColor(context.getResources().getColor(R.color.color_black));
            tvKitExpectedQuantity.setTextColor(LMISApp.getContext().getResources().getColor(R.color.color_black));
            lotListView.setVisibility(View.GONE);
        }
    }

    protected void updatePop(InventoryViewModel viewModel) {
        long totalQuantity = viewModel.getLotListQuantityTotalAmount();
        long kitExpectQuantity = viewModel.getKitExpectQuantity();

        if (((UnpackKitInventoryViewModel) viewModel).hasLotChanged()) {
            if (totalQuantity == kitExpectQuantity) {
                initViewHolderStyle(viewModel);
            } else {
                vg_soh_pop.setVisibility(View.VISIBLE);
                vg_soh_pop.setBackgroundResource(R.drawable.inventory_pop_warning);
                tvConfirmHasStock.setVisibility(View.GONE);
                tvConfirmNoStock.setVisibility(View.GONE);
                tvKitExpectedQuantity.setTextColor(LMISApp.getContext().getResources().getColor(R.color.color_black));
                tvQuantityMessage.setTextColor(context.getResources().getColor(R.color.color_warning_text_unpack_kit_pop));
                if (totalQuantity > kitExpectQuantity) {
                    tvQuantityMessage.setText(Html.fromHtml(context.getString(R.string.label_unpack_kit_quantity_more_than_expected)));
                } else {
                    tvQuantityMessage.setText(Html.fromHtml(context.getString(R.string.label_unpack_kit_quantity_less_than_expected)));
                }
            }
        } else {
            initViewHolderStyle(viewModel);
            validateIfShouldShowUpEmptyLotWarning(viewModel);
        }
    }

    private void initViewHolderStyle(InventoryViewModel inventoryViewModel) {
        lotListView.setVisibility(View.VISIBLE);
        vg_soh_pop.setVisibility(View.GONE);
        tvKitExpectedQuantity.setTextColor(LMISApp.getContext().getResources().getColor(R.color.color_black));
        tvProductName.setText(TextStyleUtil.getHighlightQueryKeyWord(StringUtils.EMPTY, inventoryViewModel.getStyledName()));
        tvProductUnit.setText(TextStyleUtil.getHighlightQueryKeyWord(StringUtils.EMPTY, inventoryViewModel.getStyledUnit()));
        tvKitExpectedQuantity.setText(this.context.getResources().getString(R.string.text_quantity_expected,
                Long.toString(inventoryViewModel.getKitExpectQuantity())));
    }

    private void setConfirmStockClickListener(final UnpackKitInventoryViewModel inventoryViewModel, final Action1 setConfirmNoStockReceivedAction) {
        tvConfirmNoStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvConfirmNoStock.setVisibility(View.GONE);
                tvConfirmHasStock.setVisibility(View.VISIBLE);
                tvQuantityMessage.setText(LMISApp.getContext().getResources().getString(R.string.message_no_stock_received));
                tvKitExpectedQuantity.setTextColor(LMISApp.getContext().getResources().getColor(R.color.color_black));
                lotListView.setVisibility(View.GONE);
                setConfirmNoStockReceivedAction.call(inventoryViewModel);
            }
        });
        tvConfirmHasStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvConfirmHasStock.setVisibility(View.GONE);
                tvConfirmNoStock.setVisibility(View.VISIBLE);
                tvQuantityMessage.setText(LMISApp.getContext().getResources().getString(R.string.message_no_stock_amount_change));
                tvKitExpectedQuantity.setTextColor(LMISApp.getContext().getResources().getColor(R.color.color_red));
                lotListView.setVisibility(View.VISIBLE);
                inventoryViewModel.setConfirmedNoStockReceived(false);
                inventoryViewModel.setShouldShowEmptyLotWarning(true);
            }
        });
    }
}
