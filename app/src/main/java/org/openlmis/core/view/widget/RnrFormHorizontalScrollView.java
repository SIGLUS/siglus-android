package org.openlmis.core.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

public class RnrFormHorizontalScrollView extends HorizontalScrollView {
    public RnrFormHorizontalScrollView(Context context) {
        super(context);
    }

    public RnrFormHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public interface OnScrollChangedListener {
        void onScrollChanged(int l, int t, int oldl, int oldt);
    }

    private OnScrollChangedListener mOnScrollChangedListener;

    public void setOnScrollChangedListener(OnScrollChangedListener listener) {
        mOnScrollChangedListener = listener;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mOnScrollChangedListener != null) {
            mOnScrollChangedListener.onScrollChanged(l, t, oldl, oldt);
        }
    }

    @Override
    public void fling(int velocityX) {
        super.fling(0);
    }

}
