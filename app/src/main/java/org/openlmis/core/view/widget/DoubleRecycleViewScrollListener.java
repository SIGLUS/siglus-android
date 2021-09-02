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

import androidx.recyclerview.widget.RecyclerView;

public final class DoubleRecycleViewScrollListener {

  public static RecyclerView.OnScrollListener[] scrollInSync(RecyclerView rvHostView, RecyclerView rvViceView) {
    final RecyclerView.OnScrollListener[] scrollListeners = new RecyclerView.OnScrollListener[2];
    scrollListeners[0] = new RecyclerView.OnScrollListener() {
      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        rvViceView.removeOnScrollListener(scrollListeners[1]);
        if (dy != 0) {
          rvViceView.scrollBy(dx, dy);
        }
        rvViceView.addOnScrollListener(scrollListeners[1]);
      }
    };
    scrollListeners[1] = new RecyclerView.OnScrollListener() {
      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        rvHostView.removeOnScrollListener(scrollListeners[0]);
        rvHostView.scrollBy(dx, dy);
        rvHostView.addOnScrollListener(scrollListeners[0]);
      }
    };
    rvHostView.addOnScrollListener(scrollListeners[0]);
    rvViceView.addOnScrollListener(scrollListeners[1]);
    return scrollListeners;
  }

}
