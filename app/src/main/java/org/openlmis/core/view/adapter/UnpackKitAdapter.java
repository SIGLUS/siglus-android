package org.openlmis.core.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.view.holder.UnpackKitWithLotViewHolder;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.UnpackKitInventoryViewModel;

import java.util.List;

import rx.functions.Action1;

public class UnpackKitAdapter extends InventoryListAdapterWithBottomBtn implements FilterableAdapter {

    private final View.OnClickListener onClickListener;
    private Action1 setConfirmNoStockReceivedAction = new Action1<UnpackKitInventoryViewModel>() {
        @Override
        public void call(UnpackKitInventoryViewModel unpackKitInventoryViewModel) {
            unpackKitInventoryViewModel.setConfirmedNoStockReceived(true);
            unpackKitInventoryViewModel.getNewLotMovementViewModelList().clear();
            UnpackKitAdapter.this.notifyDataSetChanged();
        }
    };

    public UnpackKitAdapter(List<InventoryViewModel> data, View.OnClickListener onClickListener) {
        super(data);
        this.onClickListener = onClickListener;
    }

    @Override
    protected void populate(RecyclerView.ViewHolder viewHolder, int position) {
        final InventoryViewModel viewModel = filteredList.get(position);
        ((UnpackKitWithLotViewHolder) viewHolder).populate(viewModel, setConfirmNoStockReceivedAction);
    }

    @Override
    protected VHFooter onCreateFooterView(ViewGroup parent) {
        VHFooter vhFooter = new VHFooter(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_complete_btn, parent, false));
        vhFooter.itemView.findViewById(R.id.btn_complete).setOnClickListener(onClickListener);
        return vhFooter;
    }

    @Override
    public RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_unpack_kit_with_lots, parent, false);
        return new UnpackKitWithLotViewHolder(view);
    }
}
