package org.openlmis.core.view.widget;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.view.adapter.BulkInitialInventoryLotMovementAdapter;
import org.openlmis.core.view.adapter.LotInfoReviewListAdapter;
import org.openlmis.core.view.holder.BulkInitialInventoryWithLotViewHolder;
import org.openlmis.core.view.viewmodel.BulkInitialInventoryViewModel;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import roboguice.inject.InjectView;

public class BulkInitialInventoryLotListView extends BaseLotListView {
    @InjectView(R.id.btn_done)
    ViewGroup btnDone;

    @InjectView(R.id.btn_edit)
    TextView btnEdit;

    @InjectView(R.id.vg_edit_lot_area)
    ViewGroup vgEditLotArea;

    @InjectView(R.id.vg_lot_info_review)
    ViewGroup vgLotInfoReview;

    @InjectView(R.id.rv_lot_info_review)
    RecyclerView rvLotInfoReview;

    private BulkInitialInventoryWithLotViewHolder.InventoryItemStatusChangeListener statusChangeListener;
    public BulkInitialInventoryLotListView(Context context) {
        super(context);
    }

    public BulkInitialInventoryLotListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        btnDone.setOnClickListener(v -> {
            if (validateLotList()) {
                markDone(true);
            }
        });
        btnEdit.setOnClickListener(v -> markDone(false));
    }

    public void initLotListView(InventoryViewModel viewModel, BulkInitialInventoryWithLotViewHolder.InventoryItemStatusChangeListener statusChangeListener) {
        this.statusChangeListener = statusChangeListener;
        super.initLotListView(viewModel);
        markDone(((BulkInitialInventoryViewModel) viewModel).isDone());
    }

    @Override
    public void initExistingLotListView() {
        existingLotListView.setLayoutManager(new LinearLayoutManager(getContext()));
        existingLotMovementAdapter = new BulkInitialInventoryLotMovementAdapter(viewModel.getExistingLotMovementViewModelList());
        existingLotListView.setAdapter(existingLotMovementAdapter);
    }

    @Override
    public void initNewLotListView() {
        newLotListView.setLayoutManager(new LinearLayoutManager(getContext()));
        newLotMovementAdapter = new BulkInitialInventoryLotMovementAdapter(viewModel.getNewLotMovementViewModelList(), viewModel.getProduct().getProductNameWithCodeAndStrength());
        newLotListView.setAdapter(newLotMovementAdapter);
    }

    private void initLotInfoReviewList() {
        LotInfoReviewListAdapter adapter = new LotInfoReviewListAdapter(viewModel);
        rvLotInfoReview.setLayoutManager(new LinearLayoutManager(context));
        rvLotInfoReview.setAdapter(adapter);
    }


    public void markDone(boolean done) {
        ((BulkInitialInventoryViewModel) viewModel).setDone(done);
        vgEditLotArea.setVisibility(done ? GONE : VISIBLE);
        vgLotInfoReview.setVisibility(done ? VISIBLE : GONE);
        statusChangeListener.onStatusChange(done);
        if (done) {
            initLotInfoReviewList();
        }
    }

    public boolean validateLotList() {
        int position1 = ((BulkInitialInventoryLotMovementAdapter) existingLotMovementAdapter).validateLotNonEmptyQuantity();
        if (position1 >= 0) {
            existingLotListView.scrollToPosition(position1);
            return false;
        }
        int position2 = ((BulkInitialInventoryLotMovementAdapter) newLotMovementAdapter).validateLotPositiveQuantity();
        if (position2 >= 0) {
            newLotListView.scrollToPosition(position2);
            return false;
        }
        return true;
    }

    @Override
    protected void inflateLayout(Context context) {
        inflate(context, R.layout.view_lot_list_bulkinitial_inventory, this);
    }
}
