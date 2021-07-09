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
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.openlmis.core.R;

public class DashboardView extends ConstraintLayout {

  private enum ShowState {
    FINISHED,
    LOADING,
    CALCULATING
  }

  private static final String DEFAULT_TEXT = "--";

  DashboardCircleView circleView;
  LinearLayoutCompat llTotalProducts;
  ImageView ivLoading;
  TextView tvTotalProduct;
  TextView tvCalculatingCMMTips;
  TextView tvRegularAmount;
  TextView tvOutAmount;
  TextView tvLowAmount;
  TextView tvOverAmount;

  private ShowState currentShowState;

  public DashboardView(@NonNull Context context) {
    this(context, null);
  }

  public DashboardView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public DashboardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initView(context);
  }

  public void showFinished(int regularAmount, int outAmount, int lowAmount, int overAmount) {
    changeState(ShowState.FINISHED);
    circleView.setData(createNewData(regularAmount, outAmount, lowAmount, overAmount));
    tvTotalProduct.setText(String.valueOf(regularAmount + outAmount + lowAmount + overAmount));
    tvRegularAmount.setText(String.valueOf(regularAmount));
    tvOutAmount.setText(String.valueOf(outAmount));
    tvLowAmount.setText(String.valueOf(lowAmount));
    tvOverAmount.setText(String.valueOf(overAmount));
  }

  public void showCalculating() {
    changeState(ShowState.CALCULATING);
    tvCalculatingCMMTips.setText(getContext().getString(R.string.msg_calculating_cmm));
    tvCalculatingCMMTips
        .setTextSize(TypedValue.COMPLEX_UNIT_PX, getContext().getResources().getDimension(R.dimen.px_18));
  }

  public void showLoading(int current, int total) {
    changeState(ShowState.LOADING);
    int percent = (int) ((current * 100.0f) / total);
    tvCalculatingCMMTips.setText(String.format(Locale.getDefault(), "%d%%", percent));
    tvCalculatingCMMTips
        .setTextSize(TypedValue.COMPLEX_UNIT_PX, getContext().getResources().getDimension(R.dimen.px_25));
  }

  private void changeState(ShowState showState) {
    if (currentShowState == showState) {
      return;
    }
    this.currentShowState = showState;
    if (showState == ShowState.FINISHED) {
      ivLoading.clearAnimation();
    } else {
      startLoading();
    }
    tvCalculatingCMMTips.setVisibility(showState != ShowState.FINISHED ? View.VISIBLE : View.INVISIBLE);
    ivLoading.setVisibility(showState != ShowState.FINISHED ? View.VISIBLE : View.INVISIBLE);
    llTotalProducts.setVisibility(showState != ShowState.FINISHED ? View.INVISIBLE : View.VISIBLE);
    circleView.setVisibility(showState != ShowState.FINISHED ? View.INVISIBLE : View.VISIBLE);
  }

  protected List<DashboardCircleView.Item> createNewData(int regularAmount, int outAmount,
      int lowAmount, int overAmount) {
    final ArrayList<DashboardCircleView.Item> result = new ArrayList<>();
    result.add(new DashboardCircleView.Item(
        ContextCompat.getColor(getContext(), R.color.color_regular_stock), regularAmount));
    result.add(
        new DashboardCircleView.Item(ContextCompat.getColor(getContext(), R.color.color_stock_out),
            outAmount));
    result.add(
        new DashboardCircleView.Item(ContextCompat.getColor(getContext(), R.color.color_low_stock),
            lowAmount));
    result.add(
        new DashboardCircleView.Item(ContextCompat.getColor(getContext(), R.color.color_over_stock),
            overAmount));
    return result;
  }

  @Override
  protected void onDetachedFromWindow() {
    ivLoading.clearAnimation();
    super.onDetachedFromWindow();
  }

  private void initView(Context context) {
    final View rootView = LayoutInflater.from(context).inflate(R.layout.view_dashboard, this);
    circleView = rootView.findViewById(R.id.dc_product_total);
    ivLoading = rootView.findViewById(R.id.iv_dashboard_loading);
    llTotalProducts = rootView.findViewById(R.id.ll_dashboard_total_product);
    tvCalculatingCMMTips = rootView.findViewById(R.id.tv_percent_and_loading_tips);
    tvTotalProduct = rootView.findViewById(R.id.tv_total_product);
    tvRegularAmount = rootView.findViewById(R.id.tv_regular_stock_amount);
    tvOutAmount = rootView.findViewById(R.id.tv_stock_out_amount);
    tvLowAmount = rootView.findViewById(R.id.tv_low_stock_amount);
    tvOverAmount = rootView.findViewById(R.id.tv_over_stock_amount);
    changeState(ShowState.LOADING);
  }

  private void startLoading() {
    // set amount
    tvRegularAmount.setText(DEFAULT_TEXT);
    tvOutAmount.setText(DEFAULT_TEXT);
    tvLowAmount.setText(DEFAULT_TEXT);
    tvOverAmount.setText(DEFAULT_TEXT);

    // start rotate
    Animation anim = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f);
    anim.setFillAfter(false);
    anim.setDuration(1500);
    anim.setRepeatCount(Animation.INFINITE);
    anim.setInterpolator(new LinearInterpolator());
    ivLoading.startAnimation(anim);
  }
}
