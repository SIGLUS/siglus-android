package org.openlmis.core.view.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import org.openlmis.core.R;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.view.adapter.LotMovementAdapter;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;

import java.util.List;

import roboguice.inject.InjectView;

public class NewMovementLotListView extends BaseLotListView {

    @InjectView(R.id.alert_add_positive_lot_amount)
    ViewGroup alertAddPositiveLotAmount;

    @InjectView(R.id.alert_soonest_expire)
    ViewGroup alertSoonestExpire;

    private StockMovementViewModel viewModel;
    private MovementReasonManager.MovementType movementType;

    public NewMovementLotListView(Context context) {
        super(context);
    }

    public NewMovementLotListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initLotListView(StockMovementViewModel viewModel, MovementReasonManager.MovementType movementType) {
        this.viewModel = viewModel;
        this.movementType = movementType;

        if (MovementReasonManager.MovementType.RECEIVE.equals(movementType)
                || MovementReasonManager.MovementType.POSITIVE_ADJUST.equals(movementType)) {
            setActionAddNewLotVisibility(View.VISIBLE);
            setActionAddNewLotListener(getAddNewLotOnClickListener());
        } else {
            setActionAddNewLotVisibility(GONE);
        }
        initExistingLotListView();
        initNewLotListView();
        initLotErrorBanner();
    }

    public void initNewLotListView() {
        super.initNewLotListView();
        newLotMovementAdapter.setMovementChangeListener(getMovementChangedListener());
    }

    public void initExistingLotListView() {
        super.initExistingLotListView();
        existingLotMovementAdapter.setMovementChangeListener(getMovementChangedListener());
    }

    public void addNewLot(LotMovementViewModel lotMovementViewModel) {
        super.addNewLot(lotMovementViewModel);
        updateAddPositiveLotAmountAlert();
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
        if (!this.viewModel.movementQuantitiesExist()) {
            alertAddPositiveLotAmount.setVisibility(View.VISIBLE);
        } else {
            alertAddPositiveLotAmount.setVisibility(View.GONE);
        }
    }

    private void updateSoonestToExpireNotIssuedBanner() {
        alertSoonestExpire.setVisibility(movementType == MovementReasonManager.MovementType.ISSUE && !viewModel.validateSoonestToExpireLotsIssued() ? View.VISIBLE : View.GONE);
    }

    public void setActionAddNewLotVisibility(int visibility) {
        lyAddNewLot.setVisibility(visibility);
    }

    public void setActionAddNewLotListener(OnClickListener addNewLotOnClickListener) {
        lyAddNewLot.setOnClickListener(addNewLotOnClickListener);
    }

    public void initLotErrorBanner() {
        if (viewModel.hasLotDataChanged()) {
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
        int position1 = existingLotMovementAdapter.validateExisting();
        if (position1 >= 0) {
            existingLotListView.scrollToPosition(position1);
            return true;
        }
        int position2 = newLotMovementAdapter.validateAll();
        if (position2 >= 0) {
            newLotListView.scrollToPosition(position2);
            return true;
        }
        return false;
    }

    @Override
    protected String getProductNameWithCodeAndStrength() {
        return viewModel.getStockCard().getProduct().getProductNameWithCodeAndStrength();
    }

    @Override
    protected String getProductCode() {
        return viewModel.getStockCard().getProduct().getCode();
    }

    @Override
    protected List<LotMovementViewModel> getExistingLotMovementViewModelList() {
        return viewModel.getExistingLotMovementViewModelList();
    }

    @Override
    protected List<LotMovementViewModel> getNewLotMovementViewModelList() {
        return viewModel.getNewLotMovementViewModelList();
    }

    @Override
    protected String getFormattedProductName() {
        return viewModel.getStockCard().getProduct().getFormattedProductName();
    }

    @Override
    protected MovementReasonManager.MovementType getMovementType() {
        return movementType;
    }
}
