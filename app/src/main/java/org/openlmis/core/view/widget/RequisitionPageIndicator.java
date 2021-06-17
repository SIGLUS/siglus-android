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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import java.util.Arrays;
import java.util.List;
import net.lucode.hackware.magicindicator.FragmentContainerHelper;
import net.lucode.hackware.magicindicator.buildins.ArgbEvaluatorHolder;
import net.lucode.hackware.magicindicator.buildins.UIUtil;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.model.PositionData;

public class RequisitionPageIndicator extends View implements IPagerIndicator {

  private Interpolator startInterpolator = new LinearInterpolator();
  private Interpolator endInterpolator = new LinearInterpolator();

  private float lineHeight;

  private List<PositionData> mPositionDataList;
  private List<Integer> lineColors;
  private List<Integer> backgroundColors;

  private Paint linePaint;
  private Paint backgroundPaint;

  private final RectF lineRect = new RectF();
  private final RectF backgroundRect = new RectF();

  public RequisitionPageIndicator(Context context) {
    super(context);
    init(context);
  }

  @Override
  public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    if (mPositionDataList == null || mPositionDataList.isEmpty()) {
      return;
    }

    if (lineColors != null && !lineColors.isEmpty()) {
      int currentColor = lineColors.get(Math.abs(position) % lineColors.size());
      int nextColor = lineColors.get(Math.abs(position + 1) % lineColors.size());
      int color = ArgbEvaluatorHolder.eval(positionOffset, currentColor, nextColor);
      linePaint.setColor(color);
    }

    if (backgroundColors != null && !backgroundColors.isEmpty()) {
      int currentColor = backgroundColors.get(Math.abs(position) % backgroundColors.size());
      int nextColor = backgroundColors.get(Math.abs(position + 1) % backgroundColors.size());
      int color = ArgbEvaluatorHolder.eval(positionOffset, currentColor, nextColor);
      backgroundPaint.setColor(color);
    }

    // 计算锚点位置
    PositionData current = FragmentContainerHelper.getImitativePositionData(mPositionDataList, position);
    PositionData next = FragmentContainerHelper.getImitativePositionData(mPositionDataList, position + 1);

    float leftX = current.mLeft;
    float nextLeftX = next.mLeft;
    float rightX = current.mRight;
    float nextRightX = next.mRight;

    lineRect.left = leftX + (nextLeftX - leftX) * startInterpolator.getInterpolation(positionOffset);
    lineRect.right = rightX + (nextRightX - rightX) * endInterpolator.getInterpolation(positionOffset);
    lineRect.top = getHeight() - lineHeight;
    lineRect.bottom = getHeight();

    backgroundRect.left = leftX + (nextLeftX - leftX) * startInterpolator.getInterpolation(positionOffset);
    backgroundRect.right = rightX + (nextRightX - rightX) * endInterpolator.getInterpolation(positionOffset);
    backgroundRect.top = 0;
    backgroundRect.bottom = getHeight() - lineHeight;

    invalidate();
  }

  @Override
  public void onPageSelected(int position) {
  }

  @Override
  public void onPageScrollStateChanged(int state) {
  }

  @Override
  public void onPositionDataProvide(List<PositionData> dataList) {
    mPositionDataList = dataList;
  }

  public float getLineHeight() {
    return lineHeight;
  }

  public void setLineHeight(float lineHeight) {
    this.lineHeight = lineHeight;
  }

  public Paint getPaint() {
    return linePaint;
  }

  public List<Integer> getColors() {
    return lineColors;
  }

  public void setLineColors(Integer... colors) {
    lineColors = Arrays.asList(colors);
  }

  public void setSelectedBackgroundColor(Integer... backgroundColors) {
    this.backgroundColors = Arrays.asList(backgroundColors);
  }

  public Interpolator getStartInterpolator() {
    return startInterpolator;
  }

  public void setStartInterpolator(Interpolator startInterpolator) {
    this.startInterpolator = startInterpolator;
    if (this.startInterpolator == null) {
      this.startInterpolator = new LinearInterpolator();
    }
  }

  public Interpolator getEndInterpolator() {
    return endInterpolator;
  }

  public void setEndInterpolator(Interpolator endInterpolator) {
    this.endInterpolator = endInterpolator;
    if (this.endInterpolator == null) {
      this.endInterpolator = new LinearInterpolator();
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
    canvas.drawRect(lineRect, linePaint);
    canvas.drawRect(backgroundRect, backgroundPaint);
  }

  private void init(Context context) {
    linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    linePaint.setStyle(Paint.Style.FILL);
    backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    backgroundPaint.setStyle(Paint.Style.FILL);
    lineHeight = UIUtil.dip2px(context, 3);
  }
}
