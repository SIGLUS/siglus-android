package org.openlmis.core.view.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import org.openlmis.core.R;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;

import java.util.List;

import lombok.Setter;

public class InitialInventoryLotListView extends BaseLotListView {
    @Setter
    private InventoryViewModel viewModel;
    @Setter
    private UpdateCheckBoxListener updateCheckBoxListener;

    public InitialInventoryLotListView(Context context) {
        super(context);
    }

    public InitialInventoryLotListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected String getProductNameWithCodeAndStrength() {
        return viewModel.getProduct().getProductNameWithCodeAndStrength();
    }

    @Override
    protected String getProductCode() {
        return viewModel.getProduct().getCode();
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
        return viewModel.getProduct().getFormattedProductName();
    }

    @Override
    protected MovementReasonManager.MovementType getMovementType() {
        return MovementReasonManager.MovementType.PHYSICAL_INVENTORY;
    }

    @NonNull
    @Override
    protected OnClickListener getAddNewLotDialogOnClickListener() {
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
                        updateCheckBoxListener.updateCheckBox();
                        lyAddNewLot.setEnabled(true);
                        break;
                }
            }
        };
    }

    public interface UpdateCheckBoxListener {
        void updateCheckBox();
    }
}
