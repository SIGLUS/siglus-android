package org.openlmis.core.view.widget;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.adapter.LotMovementAdapter;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;
import roboguice.inject.InjectView;

public abstract class BaseLotListView extends FrameLayout {
    protected Context context;

    @InjectView(R.id.ly_add_new_lot)
    View txAddNewLot;

    @InjectView(R.id.rv_new_lot_list)
    private RecyclerView newLotListView;

    @InjectView(R.id.rv_existing_lot_list)
    private RecyclerView existingLotListView;

    @InjectView(R.id.ly_lot_list)
    private ViewGroup lyLotList;

    protected AddLotDialogFragment addLotDialogFragment;

    private LotMovementAdapter newLotMovementAdapter;
    private LotMovementAdapter existingLotMovementAdapter;

    public BaseLotListView(Context context) {
        super(context);
    }

    public BaseLotListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        inflate(context, R.layout.view_lot_list, this);
        RoboGuice.injectMembers(getContext(), this);
        RoboGuice.getInjector(getContext()).injectViewMembers(this);
    }

    public void initLotListView() {
        initExistingLotListView();
        initNewLotListView();
        setActionAddNewLotVisibility(View.VISIBLE);
        setActionAddNewLotListener(getAddNewLotOnClickListener());
    }

    public void initExistingLotListView() {
        existingLotListView.setLayoutManager(new LinearLayoutManager(getContext()));
        existingLotMovementAdapter = new LotMovementAdapter(getExistingLotMovementViewModelList());
        existingLotListView.setAdapter(existingLotMovementAdapter);
    }

    public void initNewLotListView() {
        newLotListView.setLayoutManager(new LinearLayoutManager(getContext()));
        newLotMovementAdapter = new LotMovementAdapter(getNewLotMovementViewModelList(), getProductNameWithCodeAndStrength());
        newLotListView.setAdapter(newLotMovementAdapter);
    }

    protected abstract String getProductNameWithCodeAndStrength();


    public void setActionAddNewLotVisibility(int visibility) {
        txAddNewLot.setVisibility(visibility);
    }

    public void setActionAddNewLotListener(OnClickListener addNewLotOnClickListener) {
        txAddNewLot.setOnClickListener(addNewLotOnClickListener);
    }

    public void setActionAddNewEnabled(boolean actionAddNewEnabled) {
        txAddNewLot.setEnabled(actionAddNewEnabled);
    }

    public void refreshNewLotList() {
        newLotMovementAdapter.notifyDataSetChanged();
    }

    public void addNewLot(LotMovementViewModel lotMovementViewModel) {
        getNewLotMovementViewModelList().add(lotMovementViewModel);
        refreshNewLotList();
    }

    public AddLotDialogFragment.AddLotListener getAddLotWithoutNumberListener() {
        return new AddLotDialogFragment.AddLotListener() {
            @Override
            public void addLot(String expiryDate) {
                txAddNewLot.setEnabled(true);
                String lotNumber = LotMovementViewModel.generateLotNumberForProductWithoutLot(getProductCode(), expiryDate);
                if (getLotNumbers().contains(lotNumber)) {
                    ToastUtil.show(LMISApp.getContext().getString(R.string.error_lot_already_exists));
                } else {
                    addNewLot(new LotMovementViewModel(lotNumber, expiryDate, MovementReasonManager.MovementType.PHYSICAL_INVENTORY));
                }
            }
        };
    }

    protected abstract String getProductCode();

    @NonNull
    public List<String> getLotNumbers() {
        final List<String> existingLots = new ArrayList<>();
        existingLots.addAll(FluentIterable.from(getNewLotMovementViewModelList()).transform(new Function<LotMovementViewModel, String>() {
            @Override
            public String apply(LotMovementViewModel lotMovementViewModel) {
                return lotMovementViewModel.getLotNumber();
            }
        }).toList());
        existingLots.addAll(FluentIterable.from((getExistingLotMovementViewModelList())).transform(new Function<LotMovementViewModel, String>() {
            @Override
            public String apply(LotMovementViewModel lotMovementViewModel) {
                return lotMovementViewModel.getLotNumber();
            }
        }).toList());
        return existingLots;
    }

    protected abstract List<LotMovementViewModel> getExistingLotMovementViewModelList();

    protected abstract List<LotMovementViewModel> getNewLotMovementViewModelList();

    @NonNull
    public View.OnClickListener getAddNewLotOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddLotDialogFragment();
            }
        };
    }

    public void showAddLotDialogFragment() {
        setActionAddNewEnabled(false);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.PARAM_STOCK_NAME, getFormattedProductName());
        addLotDialogFragment = new AddLotDialogFragment();
        addLotDialogFragment.setArguments(bundle);
        addLotDialogFragment.setListener(getAddNewLotDialogOnClickListener());
        addLotDialogFragment.setAddLotWithoutNumberListener(getAddLotWithoutNumberListener());
        addLotDialogFragment.show(((Activity) context).getFragmentManager(), "add_new_lot");
    }

    protected abstract String getFormattedProductName();

    @NonNull
    protected View.OnClickListener getAddNewLotDialogOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_complete:
                        if (addLotDialogFragment.validate() && !addLotDialogFragment.hasIdenticalLot(getLotNumbers())) {
                            addNewLot(new LotMovementViewModel(addLotDialogFragment.getLotNumber(), addLotDialogFragment.getExpiryDate(), getMovementType()));
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

    protected abstract MovementReasonManager.MovementType getMovementType();
}
