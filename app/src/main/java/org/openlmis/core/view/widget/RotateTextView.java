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
import android.util.AttributeSet;
import android.view.Gravity;
import androidx.appcompat.widget.AppCompatTextView;

public class RotateTextView extends AppCompatTextView {

  private boolean topDown;

  public RotateTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private void init() {
    final int gravity = getGravity();
    if (Gravity.isVertical(gravity)
        && (gravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.BOTTOM) {
      setGravity((gravity & Gravity.HORIZONTAL_GRAVITY_MASK) | Gravity.TOP);
      topDown = false;
    } else {
      topDown = true;
    }
  }

  public RotateTextView(Context context) {
    super(context);
    init();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(heightMeasureSpec, widthMeasureSpec);
    setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
  }

  @Override
  protected void onDraw(Canvas canvas) {
    getPaint().setColor(getCurrentTextColor());
    getPaint().drawableState = getDrawableState();

    canvas.save();

    if (topDown) {
      canvas.translate(getWidth(), getHeight() / 4f);
      canvas.rotate(90);
    } else {
      canvas.translate(getWidth() / 4f, getHeight());
      canvas.rotate(-90);
    }
    canvas.translate(getCompoundPaddingLeft(), getExtendedPaddingTop());

    getLayout().draw(canvas);
    canvas.restore();
  }
}