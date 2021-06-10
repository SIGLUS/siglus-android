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
import android.graphics.Rect;
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

  @Override
  protected int computeScrollDeltaToGetChildRectOnScreen(Rect rect) {
    return 0;
  }
}
