package org.openlmis.core.utils.keyboard;

import android.app.Activity;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.PopupWindow;

import org.openlmis.core.R;

public class KeyboardHeightProvider extends PopupWindow {

    private KeyboardHeightObserver observer;

    private final View popupView;

    private final View parentView;

    private final Activity activity;

    private int currentKeyboardHeight = 0;

    public KeyboardHeightProvider(Activity activity) {
        super(activity);
        this.activity = activity;
        this.popupView = ((LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.popupwindow, null, false);
        setContentView(popupView);
        setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_RESIZE | LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        parentView = activity.findViewById(android.R.id.content);
        setWidth(0);
        setHeight(LayoutParams.MATCH_PARENT);
        setFocusable(false);
        popupView.getViewTreeObserver().addOnGlobalLayoutListener(this::handleOnGlobalLayout);
    }

    public void start() {
        if (!isShowing() && parentView.getWindowToken() != null) {
            setBackgroundDrawable(new ColorDrawable(0));
            showAtLocation(parentView, Gravity.NO_GRAVITY, 0, 0);
        }
    }

    public void close() {
        this.observer = null;
        dismiss();
    }

    public void setKeyboardHeightObserver(KeyboardHeightObserver observer) {
        this.observer = observer;
    }

    private void handleOnGlobalLayout() {
        final Rect activityRect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(activityRect);
        Rect popupWindowRect = new Rect();
        popupView.getWindowVisibleDisplayFrame(popupWindowRect);
        int keyboardHeight = activityRect.height() - popupWindowRect.height();
        if (keyboardHeight != currentKeyboardHeight) {
            this.currentKeyboardHeight = keyboardHeight;
            notifyKeyboardHeightChanged(keyboardHeight);
        }
    }

    private void notifyKeyboardHeightChanged(int height) {
        if (observer != null) {
            observer.onKeyboardHeightChanged(height);
        }
    }
}