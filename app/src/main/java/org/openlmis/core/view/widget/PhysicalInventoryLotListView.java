package org.openlmis.core.view.widget;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.view.adapter.LotInfoReviewListAdapter;
import org.openlmis.core.view.viewmodel.BaseStockMovementViewModel;
import org.openlmis.core.view.viewmodel.PhysicalInventoryViewModel;

import roboguice.inject.InjectView;

public class PhysicalInventoryLotListView extends BaseLotListView {
    @InjectView(R.id.btn_done)
    TextView btnDone;

    @InjectView(R.id.btn_edit)
    TextView btnEdit;

    @InjectView(R.id.vg_edit_lot_area)
    ViewGroup vgEditLotArea;

    @InjectView(R.id.vg_lot_info_review)
    ViewGroup vgLotInfoReview;

    @InjectView(R.id.rv_lot_info_review)
    RecyclerView rvLotInfoReview;

    public PhysicalInventoryLotListView(Context context) {
        super(context);
    }

    public PhysicalInventoryLotListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        btnDone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateLotList()) {
                    markDone(true);
                }
            }
        });
        btnEdit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                markDone(false);
            }
        });
    }

    @Override
    public void initLotListView(BaseStockMovementViewModel viewModel) {
        super.initLotListView(viewModel);
        markDone(((PhysicalInventoryViewModel) viewModel).isDone());
    }

    private void initLotInfoReviewList() {
        LotInfoReviewListAdapter adapter = new LotInfoReviewListAdapter(viewModel);
        rvLotInfoReview.setLayoutManager(new LinearLayoutManager(context));
        rvLotInfoReview.setAdapter(adapter);
    }

    public void markDone(boolean done) {
        ((PhysicalInventoryViewModel) viewModel).setDone(done);
        vgEditLotArea.setVisibility(done ? GONE : VISIBLE);
        vgLotInfoReview.setVisibility(done ? VISIBLE : GONE);
        if (done) {
            initLotInfoReviewList();
        }
    }

    public boolean validateLotList() {
        int position1 = existingLotMovementAdapter.validateLotNonEmptyQuantity();
        if (position1 >= 0) {
            existingLotListView.scrollToPosition(position1);
            return false;
        }
        int position2 = newLotMovementAdapter.validateLotPositiveQuantity();
        if (position2 >= 0) {
            newLotListView.scrollToPosition(position2);
            return false;
        }
        return true;
    }

    @Override
    protected void inflateLayout(Context context) {
        inflate(context, R.layout.view_lot_list_physical_inventory, this);
    }
}
