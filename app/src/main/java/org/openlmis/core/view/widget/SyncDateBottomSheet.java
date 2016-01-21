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
import android.widget.ImageView;
import android.widget.TextView;

import com.google.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.model.SyncType;
import org.openlmis.core.presenter.SyncErrorsPresenter;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.fragment.BaseDialogFragment;

import roboguice.inject.InjectView;

public class SyncDateBottomSheet extends BaseDialogFragment {

    @InjectView(R.id.tx_last_synced_rnrform)
    private TextView txRnrFormSyncTime;

    @InjectView(R.id.tx_last_synced_stockcard)
    private TextView txStockCardSyncTime;

    @InjectView(R.id.iv_rnr_error)
    ImageView ivRnRError;

    @InjectView(R.id.iv_stockcard_error)
    ImageView ivStockcardError;

    @Inject
    private SyncErrorsPresenter presenter;

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
        if (arguments == null) {
            return;
        }
        txRnrFormSyncTime.setText(arguments.getString(RNR_SYNC_TIME));
        txStockCardSyncTime.setText(arguments.getString(STOCK_SYNC_TIME));

        if (presenter.hasSyncError(SyncType.RnRForm)) {
            ivRnRError.setVisibility(View.VISIBLE);
        }

        if (presenter.hasSyncError(SyncType.StockCards)) {
            ivStockcardError.setVisibility(View.VISIBLE);
        }
    }


    public void show(FragmentManager fragmentManager) {
        super.show(fragmentManager, "sync_date_bottom_sheet");
    }


    public static Bundle getArgumentsToMe(long rnrLastSyncTime, long stockLastSyncTime) {
        Bundle bundle = new Bundle();
        bundle.putString(SyncDateBottomSheet.RNR_SYNC_TIME, formatRnrLastSyncTime(rnrLastSyncTime));
        bundle.putString(SyncDateBottomSheet.STOCK_SYNC_TIME, formatStockCardLastSyncTime(stockLastSyncTime));
        return bundle;
    }

    //This method will move static and change to private after remove home page update feature toggle
    private static String formatLastSyncTime(long stockSyncedTimestamp, int syncTimeStringRId) {
        if (stockSyncedTimestamp == 0) {
            return StringUtils.EMPTY;
        }
        long diff = DateUtil.calculateTimeIntervalFromNow(stockSyncedTimestamp);
        String syncTimeIntervalWithUnit;
        if (diff < DateUtil.MILLISECONDS_HOUR) {
            int quantity = (int) (diff / DateUtil.MILLISECONDS_MINUTE);
            syncTimeIntervalWithUnit = LMISApp.getContext().getResources().getQuantityString(R.plurals.minute_unit, quantity, quantity);
        } else if (diff < DateUtil.MILLISECONDS_DAY) {
            int quantity = (int) (diff / DateUtil.MILLISECONDS_HOUR);
            syncTimeIntervalWithUnit = LMISApp.getContext().getResources().getQuantityString(R.plurals.hour_unit, quantity, quantity);
        } else {
            int quantity = (int) (diff / DateUtil.MILLISECONDS_DAY);
            syncTimeIntervalWithUnit = LMISApp.getContext().getResources().getQuantityString(R.plurals.day_unit, quantity, quantity);
        }
        return LMISApp.getContext().getResources().getString(syncTimeStringRId, syncTimeIntervalWithUnit);
    }

    //This method will move static and change to private after remove home page update feature toggle
    public static String formatRnrLastSyncTime(long rnrSyncedTimestamp) {
        return formatLastSyncTime(rnrSyncedTimestamp, R.string.label_rnr_form_last_synced_time_ago);
    }

    //This method will move static and change to private after remove home page update feature toggle
    public static String formatStockCardLastSyncTime(long rnrSyncedTimestamp) {
        return formatLastSyncTime(rnrSyncedTimestamp, R.string.label_stock_card_last_synced_time_ago);
    }
}
