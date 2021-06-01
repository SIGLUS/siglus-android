package org.openlmis.core.view.widget;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;

import lombok.Setter;

public class InitialInventoryLotListView extends BaseLotListView {
    @Setter
    private UpdateCheckBoxListener updateCheckBoxListener;

    public InitialInventoryLotListView(Context context) {
        super(context);
    }

    public InitialInventoryLotListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @NonNull
    @Override
    protected SingleClickButtonListener getAddNewLotDialogOnClickListener() {
        return new SingleClickButtonListener() {
            @Override
            public void onSingleClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_complete:
                        if (addLotDialogFragment.validate()
                                && addLotDialogFragment.isAdded()
                                && !addLotDialogFragment.hasIdenticalLot(getLotNumbers())) {
                            addNewLot(new LotMovementViewModel(addLotDialogFragment.getLotNumber(),
                                    addLotDialogFragment.getExpiryDate(),
                                    viewModel.getMovementType()));
                            addLotDialogFragment.dismiss();
                        }
                        break;
                    case R.id.btn_cancel:
                        addLotDialogFragment.dismiss();
                        break;

                }
            }
        };
    }

    @NonNull
    @Override
    public OnDismissListener getOnAddNewLotDialogDismissListener() {
        return () -> {
            setActionAddNewEnabled(true);
            updateCheckBoxListener.updateCheckBox();
        };
    }

    public interface UpdateCheckBoxListener {
        void updateCheckBox();
    }
}
