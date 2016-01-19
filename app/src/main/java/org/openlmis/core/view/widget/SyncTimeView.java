package org.openlmis.core.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.activity.BaseActivity;

import roboguice.RoboGuice;
import roboguice.inject.InjectView;

public class SyncTimeView extends LinearLayout implements View.OnClickListener{

    @InjectView(R.id.tx_sync_time)
    TextView txSyncTime;

    @InjectView(R.id.iv_sync_time_icon)
    ImageView ivSyncTimeIcon;
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
        rnrLastSyncTime = SharedPreferenceMgr.getInstance().getRnrLastSyncTime();
        stockLastSyncTime = SharedPreferenceMgr.getInstance().getStockLastSyncTime();
    }

    public void showLastSyncTime() {
        if (rnrLastSyncTime == 0 && stockLastSyncTime == 0) {
            return;
        }

        long syncTimeInterval = getSyncTimeInterval(rnrLastSyncTime, stockLastSyncTime);

        if (syncTimeInterval < DateUtil.MILLISECONDS_HOUR) {
            txSyncTime.setText(getResources().getString(R.string.label_last_synced_mins_ago, syncTimeInterval / DateUtil.MILLISECONDS_MINUTE));
            ivSyncTimeIcon.setImageResource(R.drawable.icon_circle_green);
        } else if (syncTimeInterval < DateUtil.MILLISECONDS_DAY) {
            txSyncTime.setText(getResources().getString(R.string.label_last_synced_hours_ago, syncTimeInterval / DateUtil.MILLISECONDS_HOUR));
            ivSyncTimeIcon.setImageResource(R.drawable.icon_circle_green);
        } else if (syncTimeInterval < DateUtil.MILLISECONDS_DAY * 3) {
            txSyncTime.setText(getResources().getString(R.string.label_last_synced_days_ago, syncTimeInterval / DateUtil.MILLISECONDS_DAY));
            ivSyncTimeIcon.setImageResource(R.drawable.icon_circle_yellow);
        } else {
            txSyncTime.setText(getResources().getString(R.string.label_last_synced_days_ago, syncTimeInterval / DateUtil.MILLISECONDS_DAY));
            ivSyncTimeIcon.setImageResource(R.drawable.icon_circle_red);
        }
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
        SyncDateBottomSheet syncDateBottomSheet = new SyncDateBottomSheet();
        syncDateBottomSheet.setArguments(SyncDateBottomSheet.getArgumentsToMe(rnrLastSyncTime, stockLastSyncTime));
        syncDateBottomSheet.show(((BaseActivity)context).getFragmentManager());
    }
}
