package org.openlmis.core.view.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.openlmis.core.R;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.presenter.NewStockMovementPresenter;
import org.openlmis.core.view.adapter.LotMovementAdapter;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;

import roboguice.RoboGuice;
import roboguice.inject.InjectView;

public class LotListView extends LinearLayout {
    protected Context context;

    @InjectView(R.id.alert_add_positive_lot_amount)
    ViewGroup alertAddPositiveLotAmount;

    @InjectView(R.id.alert_soonest_expire)
    ViewGroup alertSoonestExpire;

    @InjectView(R.id.action_add_new_lot)
    View actionAddNewLot;

    @InjectView(R.id.lot_list)
    private RecyclerView newLotMovementRecycleView;

    @InjectView(R.id.rv_existing_lot_list)
    private RecyclerView existingLotListView;

    @InjectView(R.id.ly_lot_list)
    private ViewGroup lyLotList;

    private NewStockMovementPresenter newStockMovementPresenter;
    private StockMovementViewModel stockMovementViewModel;
    private MovementReasonManager.MovementType movementType;
    private LotMovementAdapter newLotMovementAdapter;
    private LotMovementAdapter existingLotMovementAdapter;

    public LotListView(Context context) {
        super(context);
    }

    public LotListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        inflate(context, R.layout.view_lot_list, this);
        RoboGuice.injectMembers(getContext(), this);
        RoboGuice.getInjector(getContext()).injectViewMembers(this);
    }

    public void initLotListView(NewStockMovementPresenter presenter, MovementReasonManager.MovementType movementType) {
        this.newStockMovementPresenter = presenter;
        this.stockMovementViewModel = presenter.getStockMovementViewModel();
        this.movementType = movementType;

    }

    public void initExistingLotListView() {
        existingLotListView.setLayoutManager(new LinearLayoutManager(getContext()));
        existingLotMovementAdapter = new LotMovementAdapter(stockMovementViewModel.getExistingLotMovementViewModelList());
        existingLotListView.setAdapter(existingLotMovementAdapter);
        existingLotMovementAdapter.setMovementChangeListener(getMovementChangedListener());
    }

    @NonNull
    private LotMovementAdapter.MovementChangedListener getMovementChangedListener() {
        return new LotMovementAdapter.MovementChangedListener() {
            @Override
            public void movementChange() {
                updateAddPositiveLotAmountAlert();
                updateSoonestToExpireNotIssuedBanner();
            }
        };
    }

    private void updateAddPositiveLotAmountAlert() {
        if (!this.stockMovementViewModel.movementQuantitiesExist()) {
            alertAddPositiveLotAmount.setVisibility(View.VISIBLE);
        } else {
            alertAddPositiveLotAmount.setVisibility(View.GONE);
        }
    }

    public void initNewLotListView() {
        newLotMovementRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
        newLotMovementAdapter = new LotMovementAdapter(stockMovementViewModel.getNewLotMovementViewModelList(), newStockMovementPresenter.getStockCard().getProduct().getProductNameWithCodeAndStrength());
        newLotMovementAdapter.setMovementChangeListener(getMovementChangedListener());
        newLotMovementRecycleView.setAdapter(newLotMovementAdapter);
    }

    private void updateSoonestToExpireNotIssuedBanner() {
        alertSoonestExpire.setVisibility(movementType == MovementReasonManager.MovementType.ISSUE && !stockMovementViewModel.validateSoonestToExpireLotsIssued() ? View.VISIBLE : View.GONE);
    }

    public void setActionAddNewLotVisibility(int visibility) {
        actionAddNewLot.setVisibility(visibility);
    }

    public void setActionAddNewLotListener(OnClickListener addNewLotOnClickListener) {
        actionAddNewLot.setOnClickListener(addNewLotOnClickListener);
    }

    public void setActionAddNewEnabled(boolean actionAddNewEnabled) {
        actionAddNewLot.setEnabled(actionAddNewEnabled);
    }

    public void refreshNewLotList() {
        newLotMovementAdapter.notifyDataSetChanged();
    }

    public void setLotListVisibility(int visibility) {
        lyLotList.setVisibility(visibility);
    }

    public void initLotErrorBanner() {
        if (stockMovementViewModel.hasLotDataChanged()) {
            updateAddPositiveLotAmountAlert();
        }
    }

    public void notifyDataChanged() {
        existingLotMovementAdapter.notifyDataSetChanged();
        newLotMovementAdapter.notifyDataSetChanged();
    }

    public void setAlertAddPositiveLotAmountVisibility(int visibility) {
        alertAddPositiveLotAmount.setVisibility(visibility);
    }

    public boolean validateLotList() {
        int position1 = existingLotMovementAdapter.validateExisting(movementType);
        if (position1 >= 0) {
            existingLotListView.scrollToPosition(position1);
            return true;
        }
        int position2 = newLotMovementAdapter.validateAll();
        if (position2 >= 0) {
            newLotMovementRecycleView.scrollToPosition(position2);
            return true;
        }
        return false;
    }

    public void addNewLot(LotMovementViewModel lotMovementViewModel) {
        stockMovementViewModel.getNewLotMovementViewModelList().add(lotMovementViewModel);
        updateAddPositiveLotAmountAlert();
        refreshNewLotList();
    }
}
