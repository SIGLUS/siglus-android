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

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ListView;

public class ExpandableHeightListView extends ListView {

  boolean expanded = true;

  public ExpandableHeightListView(Context context) {
    super(context);
  }

  public ExpandableHeightListView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ExpandableHeightListView(Context context, AttributeSet attrs,
      int defStyle) {
    super(context, attrs, defStyle);
  }

  public boolean isExpanded() {
    return expanded;
  }

  @Override
  public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // HACK! TAKE THAT ANDROID!
    if (isExpanded()) {
      // Calculate entire height by providing a very large height hint.
      // View.MEASURED_SIZE_MASK represents the largest height possible.
      int expandSpec = MeasureSpec.makeMeasureSpec(MEASURED_SIZE_MASK,
          MeasureSpec.AT_MOST);
      super.onMeasure(widthMeasureSpec, expandSpec);

      ViewGroup.LayoutParams params = getLayoutParams();
      params.height = getMeasuredHeight();
    } else {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
  }

  public void setExpanded(boolean expanded) {
    this.expanded = expanded;
  }
}