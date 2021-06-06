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

import android.view.View;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;

import java.util.List;

@RunWith(LMISTestRunner.class)
public class DashboardViewTest {
    private DashboardView dashboardView;

    @Before
    public void setUp() throws Exception {
        dashboardView = new DashboardView(LMISTestApp.getContext());
    }

    @Test
    public void shouldSetCorrectData() {
        // given
        int regularAmount = Mockito.anyInt();
        int outAmount = Mockito.anyInt();
        int lowAmount = Mockito.anyInt();
        int overAmount = Mockito.anyInt();

        // when
        dashboardView.setData(regularAmount, outAmount, lowAmount, overAmount);

        // then
        MatcherAssert.assertThat(dashboardView.ivLoading.getAnimation(), Matchers.equalTo(null));
        MatcherAssert.assertThat(dashboardView.ivLoading.getVisibility(), Matchers.equalTo(View.INVISIBLE));
        MatcherAssert.assertThat(dashboardView.llTotalProducts.getVisibility(), Matchers.equalTo(View.VISIBLE));
        MatcherAssert.assertThat(dashboardView.circleView.getVisibility(), Matchers.equalTo(View.VISIBLE));
        MatcherAssert.assertThat(dashboardView.tvTotalProduct.getText(), Matchers.equalTo(String.valueOf(regularAmount + outAmount + lowAmount + overAmount)));
    }

    @Test
    public void shouldCreateCorrectNewData() {
        // given
        int regularAmount = Mockito.anyInt();
        int outAmount = Mockito.anyInt();
        int lowAmount = Mockito.anyInt();
        int overAmount = Mockito.anyInt();

        // when
        final List<DashboardCircleView.Item> newData = dashboardView.createNewData(regularAmount, outAmount, lowAmount, overAmount);


        // then
        MatcherAssert.assertThat(newData.get(0).amount, Matchers.equalTo(regularAmount));
        MatcherAssert.assertThat(newData.get(1).amount, Matchers.equalTo(outAmount));
        MatcherAssert.assertThat(newData.get(2).amount, Matchers.equalTo(lowAmount));
        MatcherAssert.assertThat(newData.get(3).amount, Matchers.equalTo(overAmount));
    }

    @Test
    public void shouldClearAnimationAfterDetachedFromWindow(){
        // given
        // when
        dashboardView.onDetachedFromWindow();

        // then
        MatcherAssert.assertThat(dashboardView.ivLoading.getAnimation(), Matchers.equalTo(null));
    }
}