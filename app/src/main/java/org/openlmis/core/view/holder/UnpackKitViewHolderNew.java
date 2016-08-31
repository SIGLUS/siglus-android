package org.openlmis.core.view.holder;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.NestedRecyclerViewLinearLayoutManager;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.view.adapter.LotMovementAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.widget.AddLotDialogFragment;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.InjectView;

public class UnpackKitViewHolderNew extends BaseViewHolder {
    TextView tvProductName;
    TextView tvKitExpectedQuantity;
    TextView tvQuantityMessage;
    TextView tvConfirmNoStock;
    TextView tvConfirmHasStock;
    TextView tvProductUnit;
    ViewGroup vg_soh_pop;

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
    private AddLotDialogFragment addLotDialogFragment;

    public UnpackKitViewHolderNew(View itemView) {
        super(itemView);
        tvProductName = (TextView) itemView.findViewById(R.id.product_name);
        tvProductUnit = (TextView) itemView.findViewById(R.id.product_unit);
        vg_soh_pop = (ViewGroup) itemView.findViewById(R.id.vg_soh_pop);
        tvKitExpectedQuantity = (TextView) itemView.findViewById(R.id.kit_expected_quantity);
        tvQuantityMessage = (TextView) itemView.findViewById(R.id.tv_alert_quantity_message);
        tvConfirmNoStock = (TextView) itemView.findViewById(R.id.tv_confirm_no_stock);
        tvConfirmHasStock = (TextView) itemView.findViewById(R.id.tv_confirm_has_stock);
    }

    public void populate(final InventoryViewModel inventoryViewModel) {
        setItemViewListener(inventoryViewModel);
        initViewHolderStyle(inventoryViewModel);

        initExistingLotListView(inventoryViewModel);
        initLotListRecyclerView(inventoryViewModel);

        setConfirmStockClickListener(inventoryViewModel);

        validateIfShouldShowUpEmptyLotWarning(inventoryViewModel);
        updatePop(inventoryViewModel);
    }

    private void validateIfShouldShowUpEmptyLotWarning(InventoryViewModel inventoryViewModel) {
        if (inventoryViewModel.shouldShowEmptyLotWarning()) {
            vg_soh_pop.setVisibility(View.VISIBLE);
            vg_soh_pop.setBackgroundResource(R.drawable.inventory_pop);
            tvConfirmHasStock.setVisibility(View.GONE);
            tvConfirmNoStock.setVisibility(View.VISIBLE);
            tvQuantityMessage.setText(LMISApp.getContext().getResources().getString(R.string.message_no_stock_amount_change));
            tvQuantityMessage.setTextColor(context.getResources().getColor(R.color.color_black));
            tvKitExpectedQuantity.setTextColor(this.context.getResources().getColor(R.color.color_red));
        }

        if (inventoryViewModel.hasConfirmedNoStockReceived()) {
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

        if (viewModel.hasLotChanged()) {
            if (totalQuantity > kitExpectQuantity) {
                vg_soh_pop.setVisibility(View.VISIBLE);
                vg_soh_pop.setBackgroundResource(R.drawable.inventory_pop_warning);
                tvConfirmHasStock.setVisibility(View.GONE);
                tvConfirmNoStock.setVisibility(View.GONE);
                tvQuantityMessage.setText(Html.fromHtml(context.getString(R.string.label_unpack_kit_quantity_more_than_expected)));
                tvQuantityMessage.setTextColor(context.getResources().getColor(R.color.color_warning_text_unpack_kit_pop));
                tvKitExpectedQuantity.setTextColor(LMISApp.getContext().getResources().getColor(R.color.color_black));
            } else if (totalQuantity < kitExpectQuantity) {
                vg_soh_pop.setVisibility(View.VISIBLE);
                vg_soh_pop.setBackgroundResource(R.drawable.inventory_pop_warning);
                tvConfirmHasStock.setVisibility(View.GONE);
                tvConfirmNoStock.setVisibility(View.GONE);
                tvQuantityMessage.setText(Html.fromHtml(context.getString(R.string.label_unpack_kit_quantity_less_than_expected)));
                tvQuantityMessage.setTextColor(context.getResources().getColor(R.color.color_warning_text_unpack_kit_pop));
                tvKitExpectedQuantity.setTextColor(LMISApp.getContext().getResources().getColor(R.color.color_black));
            } else {
                initViewHolderStyle(viewModel);
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

    private void setConfirmStockClickListener(final InventoryViewModel inventoryViewModel) {
        tvConfirmNoStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvConfirmNoStock.setVisibility(View.GONE);
                tvConfirmHasStock.setVisibility(View.VISIBLE);
                tvQuantityMessage.setText(LMISApp.getContext().getResources().getString(R.string.message_no_stock_received));
                tvKitExpectedQuantity.setTextColor(LMISApp.getContext().getResources().getColor(R.color.color_black));
                lotListContainer.setVisibility(View.GONE);
                inventoryViewModel.setHasConfirmedNoStockReceived(true);
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
                inventoryViewModel.setHasConfirmedNoStockReceived(false);
                inventoryViewModel.setShouldShowEmptyLotWarning(true);
            }
        });
    }

    protected void setItemViewListener(final InventoryViewModel viewModel) {
        txAddNewLot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddNewLotDialog(viewModel);
            }
        });
    }

    private void initExistingLotListView(final InventoryViewModel viewModel) {
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

    private void initLotListRecyclerView(final InventoryViewModel viewModel) {
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

    private void addLotView(LotMovementViewModel lotMovementViewModel, InventoryViewModel viewModel) {
        viewModel.addLotMovementViewModel(lotMovementViewModel);
        refreshLotList();
    }

    private void refreshLotList() {
        lotMovementAdapter.notifyDataSetChanged();
    }

    private void showAddNewLotDialog(final InventoryViewModel viewModel) {
        addLotDialogFragment = new AddLotDialogFragment();
        addLotDialogFragment.setListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_complete:
                        if (addLotDialogFragment.validate() && !addLotDialogFragment.hasIdenticalLot(getLotNumbers(viewModel))) {
                            addLotView(new LotMovementViewModel(addLotDialogFragment.getLotNumber(), addLotDialogFragment.getExpiryDate(), MovementReasonManager.MovementType.PHYSICAL_INVENTORY), viewModel);
                            addLotDialogFragment.dismiss();
                        }
                        break;
                    case R.id.btn_cancel:
                        addLotDialogFragment.dismiss();
                        break;
                }
            }
        });
        addLotDialogFragment.show(((Activity) context).getFragmentManager(), "add_new_lot");
    }

    private List<String> getLotNumbers(InventoryViewModel viewModel) {
        final List<String> existingLots = new ArrayList<>();
        existingLots.addAll(FluentIterable.from(viewModel.getLotMovementViewModelList()).transform(new Function<LotMovementViewModel, String>() {
            @Override
            public String apply(LotMovementViewModel lotMovementViewModel) {
                return lotMovementViewModel.getLotNumber();
            }
        }).toList());
        existingLots.addAll(FluentIterable.from(viewModel.getExistingLotMovementViewModelList()).transform(new Function<LotMovementViewModel, String>() {
            @Override
            public String apply(LotMovementViewModel lotMovementViewModel) {
                return lotMovementViewModel.getLotNumber();
            }
        }).toList());
        return existingLots;
    }
}
