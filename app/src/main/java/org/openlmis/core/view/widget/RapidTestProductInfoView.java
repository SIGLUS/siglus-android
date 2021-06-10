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
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class RapidTestProductInfoView extends ScrollView {

  private LinearLayout uniqueChild;

  private Adapter adapter;

  private View cantExceedView;

  private OnScrollChangedListener onScrollChangedListener;

  private OnViewExceedBoundsListener onViewExceedBoundsListener;

  public RapidTestProductInfoView(Context context) {
    this(context, null);
  }

  public RapidTestProductInfoView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public RapidTestProductInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initView();
  }

  private void initView() {
    uniqueChild = new LinearLayout(getContext());
    uniqueChild.setOrientation(LinearLayout.VERTICAL);
    final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    uniqueChild.setLayoutParams(layoutParams);
    addView(uniqueChild);
  }

  @Override
  protected void onScrollChanged(int l, int t, int oldl, int oldt) {
    super.onScrollChanged(l, t, oldl, oldt);
    if (onScrollChangedListener != null) {
      onScrollChangedListener.onScrollChanged(l, t, oldl, oldt);
    }
    if (cantExceedView != null && onViewExceedBoundsListener != null) {
      final int top = cantExceedView.getTop() - getScrollY();
      final boolean isExceedTop = top < 0 && Math.abs(top) > cantExceedView.getMeasuredHeight();
      final boolean isExceedBottom = top > getMeasuredHeight();
      if (isExceedTop || isExceedBottom) {
        onViewExceedBoundsListener.onViewExceed(uniqueChild.indexOfChild(cantExceedView));
        cantExceedView = null;
      }
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

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (adapter != null) {
      adapter.onDetachFromWindow();
    }
  }

  public void setAdapter(Adapter adapter) {
    this.adapter = adapter;
    notifyDataChange();
  }

  public void notifyDataChange() {
    if (adapter == null) {
      return;
    }
    adapter.onNotifyDataChangeCalled();
    for (int i = 0; i < adapter.getItemCount(); i++) {
      View itemView = uniqueChild.getChildAt(i);
      if (itemView == null) {
        itemView = adapter.onCreateView(uniqueChild, i);
        uniqueChild.addView(itemView, i);
      }
      adapter.onUpdateView(itemView, i);
    }
  }

  public void setCantExceedPosition(int position) {
    View target = uniqueChild.getChildAt(position);
    if (target == null) {
      return;
    }
    cantExceedView = target;
  }

  public void scrollToPosition(int position) {
    final View target = uniqueChild.getChildAt(position);
    if (target == null) {
      return;
    }
    scrollTo(0, target.getTop());
  }

  public void setOnScrollChangedListener(OnScrollChangedListener listener) {
    onScrollChangedListener = listener;
  }

  public void setOnViewExceedBoundsListener(OnViewExceedBoundsListener listener) {
    onViewExceedBoundsListener = listener;
  }

  public abstract static class Adapter {

    protected abstract View onCreateView(ViewGroup parent, int position);

    protected abstract void onUpdateView(View itemView, int position);

    protected abstract int getItemCount();

    protected void onDetachFromWindow() {
    }

    protected void onNotifyDataChangeCalled() {
    }
  }

  public interface OnScrollChangedListener {

    void onScrollChanged(int l, int t, int oldl, int oldt);
  }

  public interface OnViewExceedBoundsListener {

    void onViewExceed(int position);
  }
}
