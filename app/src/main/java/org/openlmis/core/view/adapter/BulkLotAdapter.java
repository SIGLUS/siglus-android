package org.openlmis.core.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.view.holder.BaseViewHolder;
import org.openlmis.core.view.holder.BulkInitialInventoryViewHolder;
import org.openlmis.core.view.holder.BulkLotViewHolder;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;

import java.util.List;

import lombok.Getter;

public class BulkLotAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private List<LotMovementViewModel> lots;
    private InventoryViewModel inventoryViewModel;

    @Getter
    private View.OnClickListener removeLotListener;

    private  BulkInitialInventoryViewHolder bulkInitialInventoryViewHolder;

    public BulkLotAdapter(InventoryViewModel inventoryViewModel, View.OnClickListener removeLotListener, BulkInitialInventoryViewHolder bulkInitialInventoryViewHolder) {
        this.lots = inventoryViewModel.getNewLotMovementViewModelList();
        this.inventoryViewModel = inventoryViewModel;
        this.removeLotListener = removeLotListener;
        this.bulkInitialInventoryViewHolder = bulkInitialInventoryViewHolder;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BulkLotViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bulk_lots, parent, false), inventoryViewModel, this);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        final LotMovementViewModel viewModel = lots.get(position);

        ((BulkLotViewHolder) holder).populate(viewModel);
        ((BulkLotViewHolder) holder).btnDeleteLot.setTag(position);
    }

    @Override
    public int getItemCount() {
        return lots.size();
    }

    public void updateSOH(InventoryViewModel inventoryViewModel){
        bulkInitialInventoryViewHolder.setViewModel(inventoryViewModel);
        bulkInitialInventoryViewHolder.sumLotQuantities();
    }

}
