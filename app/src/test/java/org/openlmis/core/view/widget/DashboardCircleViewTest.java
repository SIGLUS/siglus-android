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

import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyFloat;
import static org.mockito.Matchers.eq;

@RunWith(LMISTestRunner.class)
public class DashboardCircleViewTest {

    private DashboardCircleView dashboardCircleView;

    @Before
    public void setUp() throws Exception {
        dashboardCircleView = new DashboardCircleView(LMISTestApp.getContext());
    }

    @Test
    public void shouldCalculateAngleCorrectWithNormalData() {
        // given
        final ArrayList<DashboardCircleView.Item> normalItems = new ArrayList<>();
        normalItems.add(new DashboardCircleView.Item(Color.parseColor("#abcdef"), 100));
        normalItems.add(new DashboardCircleView.Item(Color.parseColor("#adcbdf"), 200));
        normalItems.add(new DashboardCircleView.Item(Color.parseColor("#dbcdea"), 300));
        normalItems.add(new DashboardCircleView.Item(Color.parseColor("#dbcdea"), 400));

        // when
        dashboardCircleView.setData(normalItems);

        // then
        assertResult(normalItems);
    }

    @Test
    public void shouldCalculateAngleCorrectWithAbnormalData() {
        // given
        final ArrayList<DashboardCircleView.Item> abnormalItems = new ArrayList<>();
        abnormalItems.add(new DashboardCircleView.Item(Color.parseColor("#abcdef"), -1));
        abnormalItems.add(new DashboardCircleView.Item(Color.parseColor("#adcbdf"), -2));
        abnormalItems.add(new DashboardCircleView.Item(Color.parseColor("#dbcdea"), 300));
        abnormalItems.add(new DashboardCircleView.Item(Color.parseColor("#dbcdea"), 400));

        // when
        dashboardCircleView.setData(abnormalItems);

        // then
        assertResult(abnormalItems);
    }

    private void assertResult(ArrayList<DashboardCircleView.Item> items) {
        int totalAmount = 0;
        final float spaceTotalAngle = DashboardCircleView.ONE_PIECE_ANGLE * dashboardCircleView.getSpaceCount(items);
        float totalAngle = spaceTotalAngle;
        for (DashboardCircleView.Item item : items) {
            totalAngle += item.sweepAngle;
            totalAmount += Math.max(item.amount, 0);
        }

        final DashboardCircleView.Item item1 = items.get(0);
        final float anglePercent = new BigDecimal(item1.sweepAngle).divide(new BigDecimal(360 - spaceTotalAngle), 2, BigDecimal.ROUND_HALF_UP).floatValue();
        final float amountPercent = new BigDecimal(Math.max(item1.amount, 0)).divide(new BigDecimal(totalAmount), 2, BigDecimal.ROUND_HALF_UP).floatValue();

        assertThat(totalAngle, equalTo(360F));
        assertThat(anglePercent, equalTo(amountPercent));
    }

    @Test
    public void arcRectFShouldSetCorrect() {
        // given
        final int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(100, View.MeasureSpec.EXACTLY);
        final int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(200, View.MeasureSpec.EXACTLY);

        // when
        dashboardCircleView.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // then
        assertThat(dashboardCircleView.arcRectF.width(), equalTo(100F - dashboardCircleView.ringWidth));
    }

    @Test
    public void drawArcShouldInvokeCorrectTimes() {
        // given
        final int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(100, View.MeasureSpec.EXACTLY);
        final int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(200, View.MeasureSpec.EXACTLY);
        final ArrayList<DashboardCircleView.Item> normalItems = new ArrayList<>();
        normalItems.add(new DashboardCircleView.Item(Color.parseColor("#abcdef"), 100));
        normalItems.add(new DashboardCircleView.Item(Color.parseColor("#adcbdf"), 200));
        normalItems.add(new DashboardCircleView.Item(Color.parseColor("#dbcdea"), 300));
        normalItems.add(new DashboardCircleView.Item(Color.parseColor("#dbcdea"), 400));
        final Canvas mockCanvas = Mockito.mock(Canvas.class);

        // when
        dashboardCircleView.onMeasure(widthMeasureSpec, heightMeasureSpec);
        dashboardCircleView.setData(normalItems);
        dashboardCircleView.onDraw(mockCanvas);

        // then
        Mockito.verify(mockCanvas, Mockito.times(normalItems.size())).drawArc(eq(dashboardCircleView.arcRectF), anyFloat(), anyFloat(), eq(false), eq(dashboardCircleView.circlePaint));
    }
}