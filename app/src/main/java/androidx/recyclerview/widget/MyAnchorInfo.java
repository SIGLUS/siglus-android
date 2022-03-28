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

package androidx.recyclerview.widget;

import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager.AnchorInfo;
import androidx.recyclerview.widget.RecyclerView.Recycler;
import androidx.recyclerview.widget.RecyclerView.State;

/**
 * To fix https://trello.com/c/ghTtClqv/510-rapid-test-second-report-sometimes-not-aligned.
 *
 * @see LinearLayoutManager#onLayoutChildren(Recycler, State)
 * LinearLayoutManager will adjust anchor view appear on the viewport
 * when the anchor child is the focused view and due to layout shrinking the focused view fell outside the viewport.
 */
public class MyAnchorInfo extends AnchorInfo {

  @Override
  public void assignFromViewAndKeepVisibleRect(View child, int position) {
    if (child != null && (mOrientationHelper.getDecoratedStart(child)
        >= mOrientationHelper.getEndAfterPadding()
        || mOrientationHelper.getDecoratedEnd(child)
        <= mOrientationHelper.getStartAfterPadding())) {
      return;
    }
    super.assignFromViewAndKeepVisibleRect(child, position);
  }
}
