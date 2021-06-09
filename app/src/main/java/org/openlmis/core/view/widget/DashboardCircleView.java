/*
 *
 *  * This program is part of the OpenLMIS logistics management information
 *  * system platform software.
 *  *
 *  * Copyright © 2015 ThoughtWorks, Inc.
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU Affero General Public License as published
 *  * by the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version. This program is distributed in the
 *  * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 *  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  * See the GNU Affero General Public License for more details. You should
 *  * have received a copy of the GNU Affero General Public License along with
 *  * this program. If not, see http://www.gnu.org/licenses. For additional
 *  * information contact info@OpenLMIS.org
 *
 */
package org.openlmis.core.view.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import org.openlmis.core.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DashboardCircleView extends View {

    static final float ONE_PIECE_ANGLE = 3F;

    private static final float DEFAULT_START_ANGLE = -90F;

    final Paint circlePaint = new Paint();

    final RectF arcRectF = new RectF();

    int ringWidth = 0;

    @NonNull
    private List<Item> data = new ArrayList<>();

    public DashboardCircleView(Context context) {
        this(context, null);
    }

    public DashboardCircleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DashboardCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        circlePaint.setAntiAlias(true);
        ringWidth = context.getResources().getDimensionPixelOffset(R.dimen.dash_board_circle_ring_width);
        circlePaint.setStrokeWidth(ringWidth);
        circlePaint.setStyle(Paint.Style.STROKE);
    }

    public void setData(List<Item> data) {
        if (data == null) return;
        this.data = data;
        calculateAngle(data);
        postInvalidate();
    }

    private void calculateAngle(@NonNull List<Item> data) {
        int totalAmount = 0;
        for (Item item : data) {
            totalAmount += Math.max(item.amount, 0);
        }
        if (totalAmount == 0) return;
        float startAngle = DEFAULT_START_ANGLE;
        for (Item item : data) {
            item.startAngle = startAngle;
            item.sweepAngle = (360 - (ONE_PIECE_ANGLE * getSpaceCount(data))) * (Math.max(item.amount, 0)) / totalAmount;
            startAngle += item.sweepAngle + ONE_PIECE_ANGLE;
        }
    }

    int getSpaceCount(@NonNull List<Item> data) {
        int result = 0;
        for (Item item : data) {
            if (item.amount > 0) result++;
        }
        return result <= 1 ? 0 : result;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = MeasureSpec.getSize(heightMeasureSpec);
        // make sure it`s a square
        final int minLength = Math.min(width, height);
        arcRectF.left = (ringWidth / 2F);
        arcRectF.top = (ringWidth / 2F);
        arcRectF.right = minLength - (ringWidth / 2F);
        arcRectF.bottom = minLength - (ringWidth / 2F);
        setMeasuredDimension(minLength, minLength);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Item item : data) {
            drawOnePiece(canvas, item);
        }
    }

    private void drawOnePiece(Canvas canvas, Item item) {
        circlePaint.setColor(item.color);
        canvas.drawArc(arcRectF, item.startAngle, item.sweepAngle, false, circlePaint);
    }

    static class Item {

        @ColorInt
        int color;

        int amount;

        float startAngle;

        float sweepAngle;

        public Item(@ColorInt int color, int amount) {
            this.color = color;
            this.amount = amount;
        }
    }
}
