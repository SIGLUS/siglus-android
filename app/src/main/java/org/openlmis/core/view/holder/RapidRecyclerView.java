package org.openlmis.core.view.holder;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class RapidRecyclerView extends RecyclerView {
    private static  final String TAG= RapidRecyclerView.class.getSimpleName();
    public RapidRecyclerView(Context context) {
        super(context);
    }

    public RapidRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RapidRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
//        Log.d("RapidRecyclerView", ""+e);
        if (MotionEvent.ACTION_MOVE == e.getAction()) {
            //scrolling
//            scrollBy((int)e.getX(),(int)e.getY());
//            scrollToPosition();
        } else if (MotionEvent.ACTION_UP == e.getAction()) {
            //stop to scroll
        }
        return super.onTouchEvent(e);
    }

    @Override
    public void addOnScrollListener(OnScrollListener listener) {
        super.addOnScrollListener(listener);
        Log.d(TAG,"addOnScrollListener");
    }
}
