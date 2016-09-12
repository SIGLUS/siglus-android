package org.openlmis.core.view.holder;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.manager.NestedRecyclerViewLinearLayoutManager;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.view.adapter.LotMovementAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.UnpackKitInventoryViewModel;

import roboguice.inject.InjectView;

public class UnpackKitViewHolderNew extends AddLotViewHolder {
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

    @InjectView(R.id.product_name)
    TextView tvProductName;

    @InjectView(R.id.product_unit)
    TextView tvProductUnit;

    @InjectView(R.id.tx_add_new_lot)
    private TextView txAddNewLot;

    @InjectView(R.id.rv_add_lot)
    private RecyclerView lotListRecyclerView;

    @InjectView(R.id.existing_lot_list)
    private RecyclerView existingLotListView;

    @InjectView(R.id.lot_list_container)
    private LinearLayout lotListContainer;

    private LotMovementAdapter lotMovementAdapter;
    private LotMovementAdapter existingLotMovementAdapter;

    public UnpackKitViewHolderNew(View itemView) {
        super(itemView);
    }

    public void populate(final InventoryViewModel inventoryViewModel) {
        setItemViewListener(inventoryViewModel);
        initViewHolderStyle(inventoryViewModel);

        initExistingLotListView(inventoryViewModel);
        initLotListRecyclerView(inventoryViewModel);

        setConfirmStockClickListener((UnpackKitInventoryViewModel) inventoryViewModel);

        validateIfShouldShowUpEmptyLotWarning(inventoryViewModel);
        updatePop(inventoryViewModel);
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
            lotListContainer.setVisibility(View.GONE);
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
        lotListContainer.setVisibility(View.VISIBLE);
        vg_soh_pop.setVisibility(View.GONE);
        tvKitExpectedQuantity.setTextColor(LMISApp.getContext().getResources().getColor(R.color.color_black));
        tvProductName.setText(TextStyleUtil.getHighlightQueryKeyWord(StringUtils.EMPTY, inventoryViewModel.getStyledName()));
        tvProductUnit.setText(TextStyleUtil.getHighlightQueryKeyWord(StringUtils.EMPTY, inventoryViewModel.getStyledUnit()));
        tvKitExpectedQuantity.setText(this.context.getResources().getString(R.string.text_quantity_expected,
                Long.toString(inventoryViewModel.getKitExpectQuantity())));
    }

    private void setConfirmStockClickListener(final UnpackKitInventoryViewModel inventoryViewModel) {
        tvConfirmNoStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvConfirmNoStock.setVisibility(View.GONE);
                tvConfirmHasStock.setVisibility(View.VISIBLE);
                tvQuantityMessage.setText(LMISApp.getContext().getResources().getString(R.string.message_no_stock_received));
                tvKitExpectedQuantity.setTextColor(LMISApp.getContext().getResources().getColor(R.color.color_black));
                lotListContainer.setVisibility(View.GONE);
                inventoryViewModel.setConfirmedNoStockReceived(true);
            }
        });
        tvConfirmHasStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvConfirmHasStock.setVisibility(View.GONE);
                tvConfirmNoStock.setVisibility(View.VISIBLE);
                tvQuantityMessage.setText(LMISApp.getContext().getResources().getString(R.string.message_no_stock_amount_change));
                tvKitExpectedQuantity.setTextColor(LMISApp.getContext().getResources().getColor(R.color.color_red));
                lotListContainer.setVisibility(View.VISIBLE);
                inventoryViewModel.setConfirmedNoStockReceived(false);
                inventoryViewModel.setShouldShowEmptyLotWarning(true);
            }
        });
    }

    @Override
    protected void setItemViewListener(final InventoryViewModel viewModel) {
        txAddNewLot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddNewLotDialog(viewModel);
            }
        });
    }

    @Override
    protected void initExistingLotListView(final InventoryViewModel viewModel) {
        existingLotMovementAdapter = new LotMovementAdapter(viewModel.getExistingLotMovementViewModelList());
        existingLotMovementAdapter.setMovementChangeListener(new LotMovementAdapter.MovementChangedListener() {
            @Override
            public void movementChange() {
                updatePop(viewModel);
            }
        });
        existingLotListView.setLayoutManager(new NestedRecyclerViewLinearLayoutManager(context));
        existingLotListView.setAdapter(existingLotMovementAdapter);
    }

    @Override
    protected void initLotListRecyclerView(final InventoryViewModel viewModel) {
        lotMovementAdapter = new LotMovementAdapter(viewModel.getLotMovementViewModelList(), viewModel.getProduct().getProductNameWithCodeAndStrength());
        lotMovementAdapter.setMovementChangeListener(new LotMovementAdapter.MovementChangedListener() {
            @Override
            public void movementChange() {
                updatePop(viewModel);
            }
        });
        lotListRecyclerView.setLayoutManager(new NestedRecyclerViewLinearLayoutManager(context));
        lotListRecyclerView.setAdapter(lotMovementAdapter);
    }

    @Override
    void refreshLotList() {
        lotMovementAdapter.notifyDataSetChanged();
    }

}
