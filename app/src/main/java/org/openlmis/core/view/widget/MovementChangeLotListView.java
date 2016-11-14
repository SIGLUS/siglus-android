package org.openlmis.core.view.widget;

import android.content.Context;
import android.util.AttributeSet;

import org.openlmis.core.view.adapter.LotMovementAdapter;
import org.openlmis.core.view.viewmodel.BaseStockMovementViewModel;

public class MovementChangeLotListView extends BaseLotListView{
    LotMovementAdapter.MovementChangedListener movementChangedListener;

    public void initLotListView(BaseStockMovementViewModel viewModel, LotMovementAdapter.MovementChangedListener listener) {
        super.initLotListView(viewModel);
        movementChangedListener = listener;
    }

    public MovementChangeLotListView(Context context) {
        super(context);
    }

    public MovementChangeLotListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void initNewLotListView() {
        super.initNewLotListView();
        newLotMovementAdapter.setMovementChangeListener(movementChangedListener);
    }

    @Override
    public void initExistingLotListView() {
        super.initExistingLotListView();
        existingLotMovementAdapter.setMovementChangeListener(movementChangedListener);
    }
}
