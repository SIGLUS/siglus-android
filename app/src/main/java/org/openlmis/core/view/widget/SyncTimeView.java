package org.openlmis.core.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.utils.DateUtil;

import roboguice.RoboGuice;
import roboguice.inject.InjectView;

public class SyncTimeView extends LinearLayout {

    @InjectView(R.id.tx_sync_time)
    TextView txSyncTime;

    @InjectView(R.id.iv_sync_time_icon)
    ImageView ivSyncTimeIcon;

    public SyncTimeView(Context context) {
        super(context);
        init(context);
    }

    public SyncTimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_sync_time, this);
        RoboGuice.injectMembers(getContext(), this);
        RoboGuice.getInjector(getContext()).injectViewMembers(this);
    }

    public void showLastSyncTime() {
        long rnrLastSyncTime = SharedPreferenceMgr.getInstance().getPreference().getLong(SharedPreferenceMgr.KEY_LAST_SYNCED_TIME_RNR_FORM, 0);
        long stockLastSyncTime = SharedPreferenceMgr.getInstance().getPreference().getLong(SharedPreferenceMgr.KEY_LAST_SYNCED_TIME_STOCKCARD, 0);
        long handShakeTime = SharedPreferenceMgr.getInstance().getLastMovementHandShakeDate();

        if (rnrLastSyncTime == 0 && stockLastSyncTime == 0 && handShakeTime == 0) {
            return;
        }

        long syncTimeInterval = getSyncTimeInterval(rnrLastSyncTime, stockLastSyncTime, handShakeTime);

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

    private long getSyncTimeInterval(long rnrLastSyncTime, long stockLastSyncTime, long handShakeTime) {
        long latestSyncTime = 0;
        if (rnrLastSyncTime > stockLastSyncTime && rnrLastSyncTime > handShakeTime) {
            latestSyncTime = rnrLastSyncTime;
        }else if (stockLastSyncTime > rnrLastSyncTime && stockLastSyncTime > handShakeTime) {
            latestSyncTime = stockLastSyncTime;
        }else if (handShakeTime > stockLastSyncTime && handShakeTime > rnrLastSyncTime) {
            latestSyncTime = handShakeTime;
        }
        return DateUtil.calculateTimeIntervalFromNow(latestSyncTime);
    }

}
