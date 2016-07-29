package org.openlmis.core.view.widget;

import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

public final class DoubleListScrollListener implements OnScrollListener {
    private ListView list1;
    private ListView list2;

    private DoubleListScrollListener(ListView list1, ListView list2) {
        this.list1 = list1;
        this.list2 = list2;
    }

    public static void scrollInSync(ListView list1, ListView list2) {
        list1.setOnScrollListener(new DoubleListScrollListener(list1, list2));
        list2.setOnScrollListener(new DoubleListScrollListener(list2, list1));
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == 0 || scrollState == 1) {
            View subView1 = view.getChildAt(0);
            if (subView1 != null) {
                final int top1 = subView1.getTop();
                View subview2 = list2.getChildAt(0);
                if (subview2 != null) {
                    int top2 = subview2.getTop();
                    int position = view.getFirstVisiblePosition();

                    if (top1 != top2) {
                        list2.setSelectionFromTop(position, top1);
                    }
                }
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        View subView1 = view.getChildAt(0);
        if (subView1 != null) {
            int top1 = subView1.getTop();

            View subView2 = list2.getChildAt(0);
            if (subView2 != null) {
                int top2 = list2.getChildAt(0).getTop();
                if (top1 != top2) {
                    list1.setSelectionFromTop(firstVisibleItem, top1);
                    list2.setSelectionFromTop(firstVisibleItem, top1);
                }
            }
        }
    }
}
