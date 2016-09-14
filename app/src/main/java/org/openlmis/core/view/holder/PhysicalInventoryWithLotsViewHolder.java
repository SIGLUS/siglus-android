package org.openlmis.core.view.holder;

import android.view.View;

import org.openlmis.core.manager.NestedRecyclerViewLinearLayoutManager;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.view.adapter.LotMovementAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

public class PhysicalInventoryWithLotsViewHolder extends AddLotViewHolder {
    public PhysicalInventoryWithLotsViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(final InventoryViewModel inventoryViewModel, String queryKeyWord) {
        setItemViewListener(inventoryViewModel);
        tvProductName.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, inventoryViewModel.getStyledName()));
        tvProductUnit.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, inventoryViewModel.getStyledUnit()));

        initExistingLotListView(inventoryViewModel);
        initLotListRecyclerView(inventoryViewModel);
    }

    @Override
    protected void setItemViewListener(final InventoryViewModel viewModel) {
        txAddNewLot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddNewLotDialog(viewModel, txAddNewLot);
            }
        });
    }

    @Override
    protected void initExistingLotListView(final InventoryViewModel viewModel) {
        existingLotMovementAdapter = new LotMovementAdapter(viewModel.getExistingLotMovementViewModelList());
        existingLotListView.setLayoutManager(new NestedRecyclerViewLinearLayoutManager(context));
        existingLotListView.setAdapter(existingLotMovementAdapter);
    }

    @Override
    protected void initLotListRecyclerView(final InventoryViewModel viewModel) {
        lotMovementAdapter = new LotMovementAdapter(viewModel.getLotMovementViewModelList(), viewModel.getProduct().getProductNameWithCodeAndStrength());
        newLotListView.setLayoutManager(new NestedRecyclerViewLinearLayoutManager(context));
        newLotListView.setAdapter(lotMovementAdapter);
    }

    @Override
    void refreshLotList() {
        lotMovementAdapter.notifyDataSetChanged();
    }

}
