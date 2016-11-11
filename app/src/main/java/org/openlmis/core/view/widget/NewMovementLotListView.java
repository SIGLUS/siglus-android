package org.openlmis.core.view.widget;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.activity.NewStockMovementActivity;
import org.openlmis.core.view.adapter.LotMovementAdapter;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;
import roboguice.inject.InjectView;

public class NewMovementLotListView extends BaseLotListView {

    @InjectView(R.id.alert_add_positive_lot_amount)
    ViewGroup alertAddPositiveLotAmount;

    @InjectView(R.id.alert_soonest_expire)
    ViewGroup alertSoonestExpire;

    private AddLotDialogFragment addLotDialogFragment;

    private StockMovementViewModel viewModel;
    private MovementReasonManager.MovementType movementType;

    public NewMovementLotListView(Context context) {
        super(context);
    }

    public NewMovementLotListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        inflate(context, R.layout.view_lot_list, this);
        RoboGuice.injectMembers(getContext(), this);
        RoboGuice.getInjector(getContext()).injectViewMembers(this);
    }

    public void initLotListView(StockMovementViewModel viewModel, MovementReasonManager.MovementType movementType) {
        this.viewModel = viewModel;
        this.movementType = movementType;

        if (MovementReasonManager.MovementType.RECEIVE.equals(movementType)
                || MovementReasonManager.MovementType.POSITIVE_ADJUST.equals(movementType)) {
            setActionAddNewLotVisibility(View.VISIBLE);
            setActionAddNewLotListener(getAddNewLotOnClickListener());
        }
        initExistingLotListView();
        initNewLotListView();
        initLotErrorBanner();
    }

    public void initExistingLotListView() {
        super.initExistingLotListView();
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
        if (!this.viewModel.movementQuantitiesExist()) {
            alertAddPositiveLotAmount.setVisibility(View.VISIBLE);
        } else {
            alertAddPositiveLotAmount.setVisibility(View.GONE);
        }
    }

    public void initNewLotListView() {
        super.initNewLotListView();
        newLotMovementAdapter.setMovementChangeListener(getMovementChangedListener());
    }

    @Override
    protected String getProductNameWithCodeAndStrength() {
        return viewModel.getStockCard().getProduct().getProductNameWithCodeAndStrength();
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

    public void refreshNewLotList() {
        newLotMovementAdapter.notifyDataSetChanged();
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

    public void addNewLot(LotMovementViewModel lotMovementViewModel) {
        super.addNewLot(lotMovementViewModel);
        updateAddPositiveLotAmountAlert();
    }

    public AddLotDialogFragment.AddLotListener getAddLotWithoutNumberListener() {
        return new AddLotDialogFragment.AddLotListener() {
            @Override
            public void addLot(String expiryDate) {
                lyAddNewLot.setEnabled(true);
                String lotNumber = LotMovementViewModel.generateLotNumberForProductWithoutLot(viewModel.getStockCard().getProduct().getCode(), expiryDate);
                if (getLotNumbers().contains(lotNumber)) {
                    ToastUtil.show(LMISApp.getContext().getString(R.string.error_lot_already_exists));
                } else {
                    addNewLot(new LotMovementViewModel(lotNumber, expiryDate, MovementReasonManager.MovementType.PHYSICAL_INVENTORY));
                }
            }

        };
    }

    @Override
    protected String getProductCode() {
        return viewModel.getStockCard().getProduct().getCode();
    }

    @NonNull
    public List<String> getLotNumbers() {
        final List<String> existingLots = new ArrayList<>();
        existingLots.addAll(FluentIterable.from(viewModel.getNewLotMovementViewModelList()).transform(new Function<LotMovementViewModel, String>() {
            @Override
            public String apply(LotMovementViewModel lotMovementViewModel) {
                return lotMovementViewModel.getLotNumber();
            }
        }).toList());
        existingLots.addAll(FluentIterable.from((viewModel.getExistingLotMovementViewModelList())).transform(new Function<LotMovementViewModel, String>() {
            @Override
            public String apply(LotMovementViewModel lotMovementViewModel) {
                return lotMovementViewModel.getLotNumber();
            }
        }).toList());
        return existingLots;
    }

    @Override
    protected List<LotMovementViewModel> getExistingLotMovementViewModelList() {
        return viewModel.getExistingLotMovementViewModelList();
    }

    @Override
    protected List<LotMovementViewModel> getNewLotMovementViewModelList() {
        return viewModel.getNewLotMovementViewModelList();
    }

    @NonNull
    public View.OnClickListener getAddNewLotOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setActionAddNewEnabled(false);
                addLotDialogFragment = new AddLotDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putString(Constants.PARAM_STOCK_NAME, ((NewStockMovementActivity) context).getStockName());
                addLotDialogFragment.setArguments(bundle);
                addLotDialogFragment.setListener(getAddNewLotDialogOnClickListener());
                addLotDialogFragment.setAddLotWithoutNumberListener(getAddLotWithoutNumberListener());
                addLotDialogFragment.show(((NewStockMovementActivity) context).getFragmentManager(), "");
            }
        };
    }

    @Override
    protected String getFormattedProductName() {
        return viewModel.getStockCard().getProduct().getFormattedProductName();
    }

    @NonNull
    protected View.OnClickListener getAddNewLotDialogOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_complete:
                        if (addLotDialogFragment.validate() && !addLotDialogFragment.hasIdenticalLot(getLotNumbers())) {
                            addNewLot(new LotMovementViewModel(addLotDialogFragment.getLotNumber(), addLotDialogFragment.getExpiryDate(), movementType));
                            addLotDialogFragment.dismiss();
                        }
                        setActionAddNewEnabled(true);
                        break;
                    case R.id.btn_cancel:
                        addLotDialogFragment.dismiss();
                        setActionAddNewEnabled(true);
                        break;
                }
            }
        };
    }

    @Override
    protected MovementReasonManager.MovementType getMovementType() {
        return movementType;
    }
}
