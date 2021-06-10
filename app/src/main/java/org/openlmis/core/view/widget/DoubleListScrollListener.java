/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

package org.openlmis.core.view.widget;

import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

public final class DoubleListScrollListener implements OnScrollListener {

  private final ListView list1;
  private final ListView list2;

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
  public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
      int totalItemCount) {
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
