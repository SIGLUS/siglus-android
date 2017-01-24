package org.openlmis.core.view.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.LotMovementAdapter;
import org.openlmis.core.view.adapter.NewMovementLotMovementAdapter;
import org.openlmis.core.view.viewmodel.BaseStockMovementViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;

import roboguice.inject.InjectView;

public class NewMovementLotListView extends MovementChangeLotListView {

    @InjectView(R.id.alert_add_positive_lot_amount)
    ViewGroup alertAddPositiveLotAmount;

    @InjectView(R.id.alert_soonest_expire)
    ViewGroup alertSoonestExpire;

    public NewMovementLotListView(Context context) {
        super(context);
    }

    public NewMovementLotListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initLotListView(BaseStockMovementViewModel viewModel) {
        this.viewModel = viewModel;
        movementChangedListener = getMovementChangedListener();

        if (MovementReasonManager.MovementType.RECEIVE.equals(viewModel.getMovementType())
                || MovementReasonManager.MovementType.POSITIVE_ADJUST.equals(viewModel.getMovementType())) {
            setActionAddNewLotVisibility(View.VISIBLE);
            setActionAddNewLotListener(getAddNewLotOnClickListener());
        } else {
            setActionAddNewLotVisibility(GONE);
        }
        initExistingLotListView();
        initNewLotListView();
        initLotErrorBanner();
    }

    public void addNewLot(LotMovementViewModel lotMovementViewModel) {
        super.addNewLot(lotMovementViewModel);
        updateAddPositiveLotAmountAlert();
    }

    @NonNull
    protected LotMovementAdapter.MovementChangedListener getMovementChangedListener() {
        return new LotMovementAdapter.MovementChangedListener() {
            @Override
            public void movementChange() {
                updateAddPositiveLotAmountAlert();
                updateSoonestToExpireNotIssuedBanner();
            }
        };
    }

    @Override
    public void initExistingLotListView() {
        existingLotListView.setLayoutManager(new LinearLayoutManager(getContext()));
        existingLotMovementAdapter = new NewMovementLotMovementAdapter(viewModel.getExistingLotMovementViewModelList());
        existingLotListView.setAdapter(existingLotMovementAdapter);
        existingLotMovementAdapter.setMovementChangeListener(movementChangedListener);
    }

    @Override
    public void initNewLotListView() {
        newLotListView.setLayoutManager(new LinearLayoutManager(getContext()));
        newLotMovementAdapter = new NewMovementLotMovementAdapter(viewModel.getNewLotMovementViewModelList(), viewModel.getProduct().getProductNameWithCodeAndStrength());
        newLotListView.setAdapter(newLotMovementAdapter);
        newLotMovementAdapter.setMovementChangeListener(movementChangedListener);
    }

    private void updateAddPositiveLotAmountAlert() {
        if (!((StockMovementViewModel) viewModel).movementQuantitiesExist()) {
            alertAddPositiveLotAmount.setVisibility(View.VISIBLE);
        } else {
            alertAddPositiveLotAmount.setVisibility(View.GONE);
        }
    }

    private void updateSoonestToExpireNotIssuedBanner() {
        alertSoonestExpire.setVisibility(viewModel.getMovementType() == MovementReasonManager.MovementType.ISSUE && !((StockMovementViewModel) viewModel).validateSoonestToExpireLotsIssued() ? View.VISIBLE : View.GONE);
    }

    public void setActionAddNewLotVisibility(int visibility) {
        actionPanel.setVisibility(visibility);
    }

    public void setActionAddNewLotListener(OnClickListener addNewLotOnClickListener) {
        btnAddNewLot.setOnClickListener(addNewLotOnClickListener);
    }

    public void initLotErrorBanner() {
        if (((StockMovementViewModel) viewModel).hasLotDataChanged()) {
            updateAddPositiveLotAmountAlert();
        }
    }

    public void refresh() {
        existingLotMovementAdapter.notifyDataSetChanged();
        newLotMovementAdapter.notifyDataSetChanged();
    }

    public void setAlertAddPositiveLotAmountVisibility(int visibility) {
        alertAddPositiveLotAmount.setVisibility(visibility);
    }

    public boolean validateLotListWithValidValues() {
        int position1 = ((NewMovementLotMovementAdapter) existingLotMovementAdapter).validateLotQuantityNotGreaterThanSOH();
        if (position1 >= 0) {
            existingLotListView.scrollToPosition(position1);
            return false;
        }
        int position2 = ((NewMovementLotMovementAdapter) newLotMovementAdapter).validateLotPositiveQuantity();
        if (position2 >= 0) {
            newLotListView.scrollToPosition(position2);
            return false;
        }
        return true;
    }

    public boolean validate() {
        if (validateLotListNotEmpty() && validateLotListWithValidValues()) {
            return true;
        }
        refresh();
        return false;
    }

    public boolean validateLotListNotEmpty() {
        if (((StockMovementViewModel) viewModel).isLotEmpty()) {
            ToastUtil.show(getResources().getString(R.string.empty_lot_warning));
            return false;
        }
        if (!((StockMovementViewModel) viewModel).movementQuantitiesExist()) {
            setAlertAddPositiveLotAmountVisibility(View.VISIBLE);
            return false;
        }
        return true;
    }
}
