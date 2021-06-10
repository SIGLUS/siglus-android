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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import javax.annotation.Nullable;
import org.openlmis.core.R;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.ProgramDataFormBasicItem;
import org.openlmis.core.view.adapter.RapidTestTopProductCodeAdapter;
import org.openlmis.core.view.adapter.RapidTestTopProductInfoAdapter;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import org.roboguice.shaded.goole.common.collect.ImmutableList;

public class RapidTestRnrForm extends LinearLayout {

  private static final int HEIGHT_FORM_TITLE_DP = 44;
  private static final int HEIGHT_ACTION_PANEL_DP = 60;

  private RecyclerView rvLeftProductCode;
  private RapidTestProductInfoView rvRightProductInfo;
  private RapidTestTopProductInfoAdapter infoAdapter;

  private int maxHeight = 0;

  public RapidTestRnrForm(Context context) {
    super(context);
    init(context);
  }

  public RapidTestRnrForm(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  private void init(Context context) {
    View container = LayoutInflater.from(context)
        .inflate(R.layout.view_rapid_test_rnr_form, this, true);
    rvLeftProductCode = container.findViewById(R.id.rv_top_left_product_code);
    rvRightProductInfo = container.findViewById(R.id.rv_top_right_product_info);
    calculateMaxHeight();
  }

  public void initView(List<ProgramDataFormBasicItem> itemFormList) {
    initProductCode(itemFormList);
    initProductInfo(itemFormList);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    final int height = MeasureSpec.getSize(heightMeasureSpec);
    final int newHeightMeasureSpec = MeasureSpec
        .makeMeasureSpec(maxHeight == 0 ? height : Math.min(height, maxHeight),
            MeasureSpec.getMode(heightMeasureSpec));
    super.onMeasure(widthMeasureSpec, newHeightMeasureSpec);
  }

  @SuppressLint("ClickableViewAccessibility")
  private void initProductCode(List<ProgramDataFormBasicItem> itemFormList) {
    final ImmutableList<String> productCodes = FluentIterable.from(itemFormList)
        .transform(new Function<ProgramDataFormBasicItem, String>() {
          @Nullable
          @Override
          public String apply(@Nullable ProgramDataFormBasicItem programDataFormBasicItem) {
            if (programDataFormBasicItem == null) {
              return "";
            }
            final Product product = programDataFormBasicItem.getProduct();
            return product == null ? "" : product.getCode();
          }
        }).toList();
    final RapidTestTopProductCodeAdapter rapidTestTopProductCodeAdapter = new RapidTestTopProductCodeAdapter(
        productCodes);
    rvLeftProductCode.setLayoutManager(
        new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    rvLeftProductCode.setAdapter(rapidTestTopProductCodeAdapter);
    rvLeftProductCode.setOnTouchListener((v, event) -> true);
  }

  private void initProductInfo(List<ProgramDataFormBasicItem> itemFormList) {
    infoAdapter = new RapidTestTopProductInfoAdapter(itemFormList);
    rvRightProductInfo.setAdapter(infoAdapter);
    rvRightProductInfo
        .setOnScrollChangedListener((l, t, oldl, oldt) -> rvLeftProductCode.scrollBy(0, t - oldt));
    rvRightProductInfo
        .setOnViewExceedBoundsListener(position -> infoAdapter.clearFocusByPosition(position));
  }

  private void calculateMaxHeight() {
    final DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
    int screenHeight = displayMetrics.heightPixels;
    int formTitleHeightPX = (int) (HEIGHT_FORM_TITLE_DP * displayMetrics.density + 0.5f);
    int actionPanelHeightPX = (int) (HEIGHT_ACTION_PANEL_DP * displayMetrics.density + 0.5f);
    int actionBarHeight = getActionBarHeight();
    maxHeight = (screenHeight - formTitleHeightPX - actionPanelHeightPX - actionBarHeight) / 2;
  }

  private int getActionBarHeight() {
    try {
      final TypedArray styledAttributes = getContext().getTheme()
          .obtainStyledAttributes(new int[]{android.R.attr.actionBarSize});
      int actionBarSize = styledAttributes.getDimensionPixelSize(0, 0);
      styledAttributes.recycle();
      return actionBarSize;
    } catch (Exception e) {
      return 0;
    }
  }

  public boolean isCompleted() {
    if (infoAdapter == null) {
      return false;
    }
    final int notCompletePosition = infoAdapter.getNotCompletePosition();
    if (notCompletePosition != RapidTestTopProductInfoAdapter.ALL_COMPLETE) {
      rvRightProductInfo.scrollToPosition(notCompletePosition);
      rvRightProductInfo.setCantExceedPosition(notCompletePosition);
      infoAdapter.showError(notCompletePosition);
      return false;
    }
    return true;
  }
}
