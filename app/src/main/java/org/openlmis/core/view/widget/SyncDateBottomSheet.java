package org.openlmis.core.view.widget;

import android.app.Dialog;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.view.fragment.BaseDialogFragment;

import roboguice.inject.InjectView;

public class SyncDateBottomSheet extends BaseDialogFragment {

    @InjectView(R.id.tx_last_synced_rnrform)
    private TextView txRnrFormSyncTime;
    @InjectView(R.id.tx_last_synced_stockcard)
    private TextView txStockCardSyncTime;

    public static final String RNR_SYNC_TIME = "rnrFormSyncTime";
    public static final String STOCK_SYNC_TIME = "stockCardSyncTime";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_sync_date_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUI();
    }

    @Override
    public void onStart() {
        super.onStart();
        setDialogAttributes();
    }

    private void setDialogAttributes() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.copyFrom(getDialog().getWindow().getAttributes());
        params.width = (int) (getDialog().getContext().getResources().getDisplayMetrics().widthPixels * 0.95);
        params.height = (int) (getDialog().getContext().getResources().getDisplayMetrics().heightPixels * 0.15);
        getDialog().getWindow().setAttributes(params);
        getDialog().getWindow().setGravity(Gravity.BOTTOM);
    }

    private void initUI() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            txRnrFormSyncTime.setText(arguments.getString(RNR_SYNC_TIME));
            txStockCardSyncTime.setText(arguments.getString(STOCK_SYNC_TIME));
        }
    }


    public void show(FragmentManager fragmentManager) {
        super.show(fragmentManager, "sync_date_bottom_sheet");
    }


}
