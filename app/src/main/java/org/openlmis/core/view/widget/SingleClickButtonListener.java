package org.openlmis.core.view.widget;

import android.os.Handler;
import android.view.View;
import org.openlmis.core.LMISApp;
import org.openlmis.core.utils.DateUtil;

public abstract class SingleClickButtonListener implements View.OnClickListener {

    private long minClickInterval = 500;

    public static boolean isViewClicked = false;

    private long lastClickTime;

    public abstract void onSingleClick(View v);

    @Override
    public final void onClick(View v) {
        long currentClickTime = DateUtil.getCurrentDate().getTime();
        long elapsedTime = currentClickTime - lastClickTime;

        lastClickTime = currentClickTime;

        if (elapsedTime <= minClickInterval) {
            return;
        }
        if (!isViewClicked) {
            isViewClicked = true;
            startTimer();
        } else {
            return;
        }
        onSingleClick(v);
    }

    public void setMinClickInterval(long minClickInterval) {
        this.minClickInterval = minClickInterval;
    }

    private void startTimer() {
        Handler handler = new Handler();
        handler.postDelayed(() -> isViewClicked = false, minClickInterval);
    }
}