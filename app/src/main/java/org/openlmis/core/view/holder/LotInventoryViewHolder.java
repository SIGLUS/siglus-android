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

public abstract class LotInventoryViewHolder extends BaseViewHolder {
    @InjectView(R.id.tx_add_new_lot)
    protected TextView txAddNewLot;

    @InjectView(R.id.rv_new_lot_list)
    protected RecyclerView newLotListRecyclerView;

    @InjectView(R.id.rv_existing_lot_list)
    protected RecyclerView existingLotListRecyclerView;

    private AddLotDialogFragment addLotDialogFragment;

    protected LotMovementAdapter lotMovementAdapter;
    protected LotMovementAdapter existingLotMovementAdapter;

    public LotInventoryViewHolder(View itemView) {
        super(itemView);
    }

    protected void populate(final InventoryViewModel viewModel) {
        setItemViewListener(viewModel);
        initExistingLotListView(viewModel);
        initLotListRecyclerView(viewModel);
    }

    protected void setItemViewListener(final InventoryViewModel viewModel) {
        txAddNewLot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddNewLotDialog(viewModel);
            }
        });
    }

    protected void showAddNewLotDialog(final InventoryViewModel viewModel) {
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

    protected void addLotView(LotMovementViewModel lotMovementViewModel, InventoryViewModel viewModel) {
        viewModel.addLotMovementViewModel(lotMovementViewModel);
        refreshLotList();
    }

    private void refreshLotList() {
        lotMovementAdapter.notifyDataSetChanged();
    }

    protected List<String> getLotNumbers(InventoryViewModel viewModel) {
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

    protected void initExistingLotListView(final InventoryViewModel viewModel) {
        existingLotMovementAdapter = new LotMovementAdapter(viewModel.getExistingLotMovementViewModelList());
        existingLotListRecyclerView.setLayoutManager(new NestedRecyclerViewLinearLayoutManager(context));
        existingLotListRecyclerView.setAdapter(existingLotMovementAdapter);
    }

    protected void initLotListRecyclerView(final InventoryViewModel viewModel) {
        lotMovementAdapter = new LotMovementAdapter(viewModel.getLotMovementViewModelList(), viewModel.getProduct().getProductNameWithCodeAndStrength());
        newLotListRecyclerView.setLayoutManager(new NestedRecyclerViewLinearLayoutManager(context));
        newLotListRecyclerView.setAdapter(lotMovementAdapter);
    }

}
