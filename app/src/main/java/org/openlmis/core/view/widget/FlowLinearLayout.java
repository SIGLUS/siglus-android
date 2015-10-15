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
import android.view.View;
import android.widget.LinearLayout;


public class FlowLinearLayout extends LinearLayout {
    public FlowLinearLayout(Context context) {
        super(context);
    }

    public FlowLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxWidth = MeasureSpec.getSize(widthMeasureSpec);

        int childCount = getChildCount();
        int measuredX = 0;
        int measuredY = 0;
        int raw = 1;
        int maxRowItemHeight = 0;
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                return;
            }
            child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            if (height > maxRowItemHeight) {
                measuredY = getMeasuredY(measuredY, raw, maxRowItemHeight, height);
                maxRowItemHeight = height;
            }
            measuredX += width;
            if (measuredX > maxWidth) {
                measuredX = width;
                raw++;
                measuredY += height;
                maxRowItemHeight = 0;
            }
        }
        measuredY += getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(maxWidth, measuredY);
    }

    private int getMeasuredY(int measuredY, int raw, int maxRowItemHeight, int height) {
        if (raw == 1) {
            measuredY = height;
        } else {
            measuredY += height - maxRowItemHeight;
        }
        return measuredY;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int rightBorder, int b) {

        int childCount = getChildCount();
        int maxRowItemHeight = 0;
        int measuredX = getPaddingLeft();
        int measuredY;
        int raw = 1;
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                return;
            }
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            if (height > maxRowItemHeight) {
                maxRowItemHeight = height;
            }
            measuredX += width;
            if (measuredX > rightBorder) {
                measuredX = getPaddingLeft() + width;
                raw++;
            }
            measuredY = getPaddingTop() + raw * maxRowItemHeight;
            child.layout(measuredX - width, measuredY - maxRowItemHeight, measuredX,
                    measuredY);
        }
    }

}
