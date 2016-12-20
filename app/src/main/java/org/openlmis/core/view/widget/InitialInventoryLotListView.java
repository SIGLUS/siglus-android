package org.openlmis.core.view.widget;

import android.content.Context;
import android.support.annotation.NonNull;
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
    protected OnClickListener getAddNewLotDialogOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_complete:
                        updateCheckBoxListener.updateCheckBox(true);
                        if (addLotDialogFragment.validate() && !addLotDialogFragment.hasIdenticalLot(getLotNumbers())) {
                            addNewLot(new LotMovementViewModel(addLotDialogFragment.getLotNumber(), addLotDialogFragment.getExpiryDate(), viewModel.getMovementType()));
                            addLotDialogFragment.dismiss();
                        }
                        setActionAddNewEnabled(true);
                        break;
                    case R.id.btn_cancel:
                        addLotDialogFragment.dismiss();
                        updateCheckBoxListener.updateCheckBox(false);
                        lyAddNewLot.setEnabled(true);
                        break;
                }
            }
        };
    }

    public interface UpdateCheckBoxListener {
        void updateCheckBox(boolean checked);
    }
}
