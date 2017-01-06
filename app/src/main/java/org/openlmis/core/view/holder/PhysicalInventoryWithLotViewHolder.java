package org.openlmis.core.view.holder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.view.viewmodel.PhysicalInventoryViewModel;
import org.openlmis.core.view.widget.PhysicalInventoryLotListView;

import roboguice.inject.InjectView;

public class PhysicalInventoryWithLotViewHolder extends BaseViewHolder {
    @InjectView(R.id.tv_product_name)
    TextView tvProductName;

    @InjectView(R.id.tv_product_unit)
    TextView tvProductUnit;

    @InjectView(R.id.ic_done)
    View icDone;

    @InjectView(R.id.tv_inventory_item_soh)
    TextView tvSOH;

    @InjectView(R.id.vg_inventory_item_soh)
    ViewGroup vgStockOnHand;

    @InjectView(R.id.view_lot_list)
    PhysicalInventoryLotListView lotListView;

    protected PhysicalInventoryViewModel viewModel;

    public PhysicalInventoryWithLotViewHolder(View itemView) {
        super(itemView);
    }

    public void populate(final PhysicalInventoryViewModel viewModel, final String queryKeyWord) {
        this.viewModel = viewModel;
        lotListView.initLotListView(viewModel, new InventoryItemStatusChangeListener() {
            @Override
            public void onStatusChange(boolean done) {
                updateTitle(done, queryKeyWord);
            }
        });
        updateTitle(viewModel.isDone(), queryKeyWord);
    }

    private void highlightQueryKeyWord(PhysicalInventoryViewModel inventoryViewModel, String queryKeyWord, boolean done) {
        if (done) {
            tvProductName.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, inventoryViewModel.getGreenName()));
            tvProductName.setTextSize(context.getResources().getDimension(R.dimen.font_size_regular));
            tvProductUnit.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, inventoryViewModel.getGreenUnit()));
            tvProductUnit.setTextSize(context.getResources().getDimension(R.dimen.font_size_small));
        } else {
            tvProductName.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, inventoryViewModel.getStyledName()));
            tvProductName.setTextSize(context.getResources().getDimension(R.dimen.font_size_normal));
            tvProductUnit.setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, inventoryViewModel.getStyledUnit()));
            tvProductUnit.setTextSize(context.getResources().getDimension(R.dimen.font_size_regular));
        }
    }

    private void updateTitle(boolean done, String queryKeyWord) {
        icDone.setVisibility(done ? View.VISIBLE : View.GONE);
        highlightQueryKeyWord(viewModel, queryKeyWord, done);
        vgStockOnHand.setVisibility(done ? View.VISIBLE : View.GONE);
        if (done) {
            tvSOH.setText(String.valueOf(viewModel.getLotListQuantityTotalAmount()));
        }
    }

    public interface InventoryItemStatusChangeListener {
        void onStatusChange(boolean done);
    }
}
