package org.openlmis.core.view.holder;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.NestedRecyclerViewLinearLayoutManager;
import org.openlmis.core.view.adapter.LotMovementAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.widget.AddLotDialogFragment;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.InjectView;

public abstract class AddLotViewHolder extends BaseViewHolder {
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

    LotMovementAdapter newLotMovementAdapter;
    LotMovementAdapter existingLotMovementAdapter;

    private AddLotDialogFragment addLotDialogFragment;

    public void populate(InventoryViewModel viewModel) {
        setItemViewListener(viewModel);
        initExistingLotListView(viewModel);
        initLotListRecyclerView(viewModel);
    }

    public AddLotViewHolder(View itemView) {
        super(itemView);
    }

    protected void setItemViewListener(final InventoryViewModel viewModel) {
        txAddNewLot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddNewLotDialog(viewModel, txAddNewLot);
            }
        });
    }

    protected void initExistingLotListView(final InventoryViewModel viewModel) {
        existingLotMovementAdapter = new LotMovementAdapter(viewModel.getExistingLotMovementViewModelList());
        existingLotListView.setLayoutManager(new NestedRecyclerViewLinearLayoutManager(context));
        existingLotListView.setAdapter(existingLotMovementAdapter);
    }

    protected void initLotListRecyclerView(final InventoryViewModel viewModel) {
        newLotMovementAdapter = new LotMovementAdapter(viewModel.getLotMovementViewModelList(), viewModel.getProduct().getProductNameWithCodeAndStrength());
        newLotListView.setLayoutManager(new NestedRecyclerViewLinearLayoutManager(context));
        newLotListView.setAdapter(newLotMovementAdapter);
    }

    protected void showAddNewLotDialog(final InventoryViewModel viewModel, final TextView txAddNewLot) {
        txAddNewLot.setEnabled(false);
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
                        txAddNewLot.setEnabled(true);
                        break;
                    case R.id.btn_cancel:
                        addLotDialogFragment.dismiss();
                        txAddNewLot.setEnabled(true);
                        break;
                }
            }
        });
        addLotDialogFragment.show(((Activity) context).getFragmentManager(), "add_new_lot");
    }

    protected void addLotView(LotMovementViewModel lotMovementViewModel, InventoryViewModel viewModel) {
        viewModel.addLotMovementViewModel(lotMovementViewModel);
        newLotMovementAdapter.notifyDataSetChanged();
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
