package org.openlmis.core.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.inject.Inject;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.presenter.SyncErrorsPresenter;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.activity.BaseActivity;

import roboguice.RoboGuice;
import roboguice.inject.InjectView;

public class SyncTimeView extends LinearLayout implements View.OnClickListener {

    @InjectView(R.id.pb_sync_data)
    ProgressBar progressBar;

    @InjectView(R.id.tx_sync_time)
    TextView txSyncTime;

    @InjectView(R.id.iv_sync_time_icon)
    ImageView ivSyncTimeIcon;

    @Inject
    SyncErrorsPresenter syncErrorsPresenter;

    protected Context context;
    protected long rnrLastSyncTime;
    protected long stockLastSyncTime;

    public SyncTimeView(Context context) {
        super(context);
        init(context);
    }

    public SyncTimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.view_sync_time, this);
        RoboGuice.injectMembers(getContext(), this);
        RoboGuice.getInjector(getContext()).injectViewMembers(this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        txSyncTime.setOnClickListener(this);
    }

    public void showLastSyncTime() {
        hideSyncProgressBarAndShowIcon();

        rnrLastSyncTime = SharedPreferenceMgr.getInstance().getRnrLastSyncTime();
        stockLastSyncTime = SharedPreferenceMgr.getInstance().getStockLastSyncTime();

        if (isNeverSyncSuccessful()) {
            if (hasSyncFailed()) {
                txSyncTime.setText(LMISApp.getContext().getString(R.string.initial_sync_failed));
            }
            return;
        }

        updateSyncTimeViewUI();
    }

    private void updateSyncTimeViewUI() {
        long syncTimeInterval = getSyncTimeInterval(rnrLastSyncTime, stockLastSyncTime);

        String syncTimeIntervalWithUnit;
        if (syncTimeInterval < DateUtil.MILLISECONDS_HOUR) {
            int quantity = (int) (syncTimeInterval / DateUtil.MILLISECONDS_MINUTE);
            syncTimeIntervalWithUnit = getResources().getQuantityString(R.plurals.minute_unit, quantity, quantity);
            ivSyncTimeIcon.setImageResource(R.drawable.icon_circle_green);
        } else if (syncTimeInterval < DateUtil.MILLISECONDS_DAY) {
            int quantity = (int) (syncTimeInterval / DateUtil.MILLISECONDS_HOUR);
            syncTimeIntervalWithUnit = getResources().getQuantityString(R.plurals.hour_unit, quantity, quantity);
            ivSyncTimeIcon.setImageResource(R.drawable.icon_circle_green);
        } else if (syncTimeInterval < DateUtil.MILLISECONDS_DAY * 3) {
            int quantity = (int) (syncTimeInterval / DateUtil.MILLISECONDS_DAY);
            syncTimeIntervalWithUnit = getResources().getQuantityString(R.plurals.day_unit, quantity, quantity);
            ivSyncTimeIcon.setImageResource(R.drawable.icon_circle_yellow);
        } else {
            int quantity = (int) (syncTimeInterval / DateUtil.MILLISECONDS_DAY);
            syncTimeIntervalWithUnit = getResources().getQuantityString(R.plurals.day_unit, quantity, quantity);
            ivSyncTimeIcon.setImageResource(R.drawable.icon_circle_red);
        }

        txSyncTime.setText(LMISApp.getContext().getResources().getString(R.string.label_last_synced_ago, syncTimeIntervalWithUnit));
    }

    private boolean isNeverSyncSuccessful() {
        return rnrLastSyncTime == 0 && stockLastSyncTime == 0;
    }

    private boolean hasSyncFailed() {
        return syncErrorsPresenter.hasRnrSyncError() || syncErrorsPresenter.hasStockCardSyncError();
    }

    private long getSyncTimeInterval(long rnrLastSyncTime, long stockLastSyncTime) {
        long latestSyncTime;
        if (rnrLastSyncTime > stockLastSyncTime) {
            latestSyncTime = rnrLastSyncTime;
        } else {
            latestSyncTime = stockLastSyncTime;
        }
        return DateUtil.calculateTimeIntervalFromNow(latestSyncTime);
    }

    @Override
    public void onClick(View v) {
        showLastSyncTime();
        popUpBottomSheet();
    }

    private void popUpBottomSheet() {
        SyncDateBottomSheet syncDateBottomSheet = new SyncDateBottomSheet();
        syncDateBottomSheet.setArguments(SyncDateBottomSheet.getArgumentsToMe(rnrLastSyncTime, stockLastSyncTime));
        syncDateBottomSheet.show(((BaseActivity) context).getFragmentManager());
    }

    public void showSyncProgressBarAndHideIcon() {
        ivSyncTimeIcon.setVisibility(GONE);
        progressBar.setVisibility(VISIBLE);
    }

    private void hideSyncProgressBarAndShowIcon() {
        progressBar.setVisibility(GONE);
        ivSyncTimeIcon.setVisibility(VISIBLE);
    }

    public void setSyncStockCardLastYearText(){
        txSyncTime.setText(R.string.last_year_stock_cards_sync);
    }
}
