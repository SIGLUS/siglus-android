package org.openlmis.core.view.widget;

import android.os.Handler;
import android.view.View;

import org.openlmis.core.LMISApp;

public abstract class SingleClickButtonListener implements View.OnClickListener {

    public static long MIN_CLICK_INTERVAL = 5000;

    public static boolean isViewClicked = false;

    private long lastClickTime;

    public abstract void onSingleClick(View v);

    @Override
    public final void onClick(View v) {
        long currentClickTime = LMISApp.getInstance().getCurrentTimeMillis();
        long elapsedTime = currentClickTime - lastClickTime;

        lastClickTime = currentClickTime;

        if(elapsedTime <= MIN_CLICK_INTERVAL) {
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

    private void startTimer() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                isViewClicked = false;
            }
        }, MIN_CLICK_INTERVAL);
    }
}