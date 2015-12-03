package org.openlmis.core.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class ViewUtil {
    private ViewUtil() {
    }

    public static void syncViewHeight(final View leftView, final View rightView) {
        int leftHeight = leftView.getHeight();
        int rightHeight = rightView.getHeight();
        if (leftHeight > rightHeight) {
            ViewGroup.LayoutParams layoutParams = rightView.getLayoutParams();
            layoutParams.height = leftHeight;
            rightView.setLayoutParams(layoutParams);
        } else {
            ViewGroup.LayoutParams layoutParams = leftView.getLayoutParams();
            layoutParams.height = rightHeight;
            leftView.setLayoutParams(layoutParams);

            if (rightView.getLayoutParams() instanceof FrameLayout.LayoutParams) {
                rightView.setLayoutParams(new FrameLayout.LayoutParams(layoutParams.width, layoutParams.height));
            } else {
                rightView.setLayoutParams(layoutParams);
            }
        }
    }
}
