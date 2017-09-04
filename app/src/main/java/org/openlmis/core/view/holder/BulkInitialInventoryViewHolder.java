package org.openlmis.core.view.holder;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.BulkLotAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.widget.AddBulkLotDialogFragment;
import org.openlmis.core.view.widget.AddLotDialogFragment;
import org.openlmis.core.view.widget.BaseLotListView;
import org.openlmis.core.view.widget.SingleClickButtonListener;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import roboguice.inject.InjectView;

public class BulkInitialInventoryViewHolder extends BaseViewHolder {
    public static final String ADD_LOT = "add_new_lot";
    public static final String QUANTITY_ZERO = "0";

    @InjectView(R.id.tv_product_name)
    TextView productName;

    @InjectView(R.id.tv_product_unit)
    TextView productUnit;

    @InjectView(R.id.tv_sho_amount)
    TextView tvSOHAmount;

    @InjectView(R.id.btn_add_new_lot)
    TextView btnAddNewLot;

    @InjectView(R.id.btn_no_stock)
    TextView btnNoStock;

    @InjectView(R.id.rv_lots)
    RecyclerView rvLots;

    @InjectView(R.id.ll_lot_information)
    LinearLayout llLotInformation;

    @InjectView(R.id.ll_soh_information)
    LinearLayout llSOHInformation;

    @Getter
    @Setter
    private InventoryViewModel viewModel;

    protected AddBulkLotDialogFragment addBulkLotDialogFragment;
    protected AddLotDialogFragment addLotDialogFragment;
    protected BulkLotAdapter bulkLotAdapter;
    protected int lotQuantity;
    protected int lotQuantityBeforeSum;

    public BulkInitialInventoryViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(final InventoryViewModel inventoryViewModel, String queryKeyWord) {
        this.viewModel = inventoryViewModel;
        rvLots.setLayoutManager(new LinearLayoutManager(context));
        bulkLotAdapter = new BulkLotAdapter(inventoryViewModel, deleteLotListener(), this);
        rvLots.setAdapter(bulkLotAdapter);

        productName.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, viewModel.getStyledName()));
        productUnit.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, viewModel.getStyleType()));

        showLotInformation();

        btnAddNewLot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNewLotDialog();
            }
        });

        btnNoStock.setOnClickListener(noStockListener());
    }

    public void showNewLotDialog() {
        if (!AddBulkLotDialogFragment.IS_OCCUPIED) {
            AddBulkLotDialogFragment.IS_OCCUPIED = true;
            Bundle bundle = new Bundle();
            bundle.putString(Constants.PARAM_STOCK_NAME, viewModel.getProduct().getFormattedProductName());
            addBulkLotDialogFragment = new AddBulkLotDialogFragment();
            addBulkLotDialogFragment.setArguments(bundle);
            addBulkLotDialogFragment.setListener(getAddNewLotDialogOnClickListener());
            addBulkLotDialogFragment.setOnDismissListener(getOnAddNewLotDialogDismissListener());
            addBulkLotDialogFragment.setAddLotWithoutNumberListener(getAddLotWithoutNumberListener());
            addBulkLotDialogFragment.show(((Activity) context).getFragmentManager(), ADD_LOT);
        }
    }

    @NonNull
    protected SingleClickButtonListener getAddNewLotDialogOnClickListener() {
        return new SingleClickButtonListener() {
            @Override
            public void onSingleClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_complete:
                        if (addBulkLotDialogFragment.validate() && !addBulkLotDialogFragment.hasIdenticalLot(getLotNumbers())) {
                            AddBulkLotDialogFragment.IS_OCCUPIED = false;
                            addNewLot(new LotMovementViewModel(addBulkLotDialogFragment.getLotNumber(), addBulkLotDialogFragment.getExpiryDate(), viewModel.getMovementType(), addBulkLotDialogFragment.getQuantity()));
                            addBulkLotDialogFragment.dismiss();
                        }
                        break;
                    case R.id.btn_cancel:
                        AddBulkLotDialogFragment.IS_OCCUPIED = false;
                        addBulkLotDialogFragment.dismiss();
                        break;
                }
            }
        };
    }

    @NonNull
    public List<String> getLotNumbers() {
        final List<String> existingLots = new ArrayList<>();
        existingLots.addAll(FluentIterable.from(viewModel.getNewLotMovementViewModelList()).transform(new Function<LotMovementViewModel, String>() {
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

    public void addNewLot(LotMovementViewModel lotMovementViewModel) {
        viewModel.getNewLotMovementViewModelList().add(lotMovementViewModel);
        sumLotQuantities();
        showLotInformation();
        bulkLotAdapter.notifyDataSetChanged();
    }

    public void removeLot(int position) {
        viewModel.getNewLotMovementViewModelList().remove(position);
        showLotInformation();
        sumLotQuantities();
        bulkLotAdapter.notifyDataSetChanged();
    }

    public void sumLotQuantities() {
        int totalQuantityLot = 0;
        for (LotMovementViewModel lot : viewModel.getNewLotMovementViewModelList()) {
            totalQuantityLot += Integer.parseInt(lot.getQuantity());
        }
        tvSOHAmount.setText(String.valueOf(totalQuantityLot));
    }

    public View.OnClickListener deleteLotListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeLot(Integer.parseInt(v.getTag().toString()));
            }
        };
    }

    public void showLotInformation() {
        if (viewModel.getNewLotMovementViewModelList().isEmpty()) {
            llLotInformation.setVisibility(View.INVISIBLE);
            llSOHInformation.setVisibility(View.INVISIBLE);
            btnNoStock.setVisibility(View.VISIBLE);
        }
        else {
            llLotInformation.setVisibility(View.VISIBLE);
            llSOHInformation.setVisibility(View.VISIBLE);
            btnNoStock.setVisibility(View.INVISIBLE);
        }
    }

    @NonNull
    public BaseLotListView.OnDismissListener getOnAddNewLotDialogDismissListener() {
        return new BaseLotListView.OnDismissListener() {
            @Override
            public void onDismissAction() {
                setActionAddNewEnabled(true);
            }
        };
    }

    public void setActionAddNewEnabled(boolean actionAddNewEnabled) {
        btnAddNewLot.setEnabled(actionAddNewEnabled);
    }

    public AddLotDialogFragment.AddLotWithoutNumberListener getAddLotWithoutNumberListener() {
        return new AddLotDialogFragment.AddLotWithoutNumberListener() {
            @Override
            public void addLotWithoutNumber(String expiryDate) {
                btnAddNewLot.setEnabled(true);
                String lotNumber = LotMovementViewModel.generateLotNumberForProductWithoutLot(viewModel.getProduct().getCode(), expiryDate);
                if (getLotNumbers().contains(lotNumber)) {
                    ToastUtil.show(LMISApp.getContext().getString(R.string.error_lot_without_number_already_exists));
                } else {
                    addNewLot(new LotMovementViewModel(lotNumber, expiryDate, MovementReasonManager.MovementType.PHYSICAL_INVENTORY));
                }
            }
        };
    }

    public View.OnClickListener noStockListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llSOHInformation.setVisibility(View.VISIBLE);
                tvSOHAmount.setText(QUANTITY_ZERO);
                btnNoStock.setVisibility(View.INVISIBLE);
            }
        };
    }
}
