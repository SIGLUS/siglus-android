package org.openlmis.core.view.holder;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.LotMovementAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.widget.AddLotDialogFragment;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.InjectView;

public abstract class InventoryWithLotViewHolder extends BaseViewHolder {
    @InjectView(R.id.product_name)
    TextView tvProductName;

    @InjectView(R.id.product_unit)
    TextView tvProductUnit;

    @InjectView(R.id.tx_add_new_lot)
    TextView txAddNewLot;

    @InjectView(R.id.rv_new_lot_list)
    RecyclerView newLotListView;

    @InjectView(R.id.rv_existing_lot_list)
    RecyclerView existingLotListView;

    protected InventoryViewModel viewModel;

    LotMovementAdapter newLotMovementAdapter;
    LotMovementAdapter existingLotMovementAdapter;

    private AddLotDialogFragment addLotDialogFragment;
    private AddLotDialogFragment.AddLotListener addLotWithoutNumberListener = new AddLotDialogFragment.AddLotListener() {
        @Override
        public void addLot(String expiryDate) {
            txAddNewLot.setEnabled(true);
            String lotNumber = LotMovementViewModel.generateLotNumberForProductWithoutLot(viewModel.getFnm(), expiryDate);
            if (getLotNumbers().contains(lotNumber)) {
                ToastUtil.show(LMISApp.getContext().getText(R.string.error_lot_already_exists));
            } else {
                addNewLot(new LotMovementViewModel(lotNumber,expiryDate, MovementReasonManager.MovementType.PHYSICAL_INVENTORY));
            }
        }
    };

    public void populate(InventoryViewModel inventoryViewModel) {
        this.viewModel = inventoryViewModel;
        setItemViewListener();
        initExistingLotListView();
        initLotListRecyclerView();
    }

    public InventoryWithLotViewHolder(View itemView) {
        super(itemView);
    }

    protected void setItemViewListener() {
        txAddNewLot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddNewLotDialog(txAddNewLot);
            }
        });
    }

    protected void initExistingLotListView() {
        existingLotMovementAdapter = new LotMovementAdapter(viewModel.getExistingLotMovementViewModelList());
        existingLotListView.setLayoutManager(new LinearLayoutManager(context));
        existingLotListView.setAdapter(existingLotMovementAdapter);
    }

    protected void initLotListRecyclerView() {
        newLotMovementAdapter = new LotMovementAdapter(viewModel.getNewLotMovementViewModelList(), viewModel.getProduct().getProductNameWithCodeAndStrength());
        newLotListView.setLayoutManager(new LinearLayoutManager(context));
        newLotListView.setAdapter(newLotMovementAdapter);
    }

    protected void showAddNewLotDialog(final TextView txAddNewLot) {
        txAddNewLot.setEnabled(false);
        addLotDialogFragment = new AddLotDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.PARAM_STOCK_NAME, viewModel.getProduct().getFormattedProductName());
        addLotDialogFragment.setArguments(bundle);
        addLotDialogFragment.setListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_complete:
                        if (addLotDialogFragment.validate() && !addLotDialogFragment.hasIdenticalLot(getLotNumbers())) {
                            addNewLot(new LotMovementViewModel(addLotDialogFragment.getLotNumber(), addLotDialogFragment.getExpiryDate(), MovementReasonManager.MovementType.PHYSICAL_INVENTORY));
                            addLotDialogFragment.dismiss();
                        }
                        txAddNewLot.setEnabled(true);
                        break;
                    case R.id.btn_cancel:
                        addLotDialogFragment.dismiss();
                        txAddNewLot.setEnabled(true);
                        break;
                }
            }
        });
        addLotDialogFragment.setAddLotWithoutNumberListener(addLotWithoutNumberListener);
        addLotDialogFragment.show(((Activity) context).getFragmentManager(), "add_new_lot");
    }

    protected void addNewLot(LotMovementViewModel lotMovementViewModel) {
        this.viewModel.addLotMovementViewModel(lotMovementViewModel);
        newLotMovementAdapter.notifyDataSetChanged();
    }

    private List<String> getLotNumbers() {
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
}
