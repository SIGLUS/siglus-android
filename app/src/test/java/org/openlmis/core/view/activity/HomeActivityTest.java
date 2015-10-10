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

package org.openlmis.core.view.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.MotionEvent;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.repository.MMIARepository;
import org.openlmis.core.model.repository.VIARepository;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;

import java.util.Date;

import static junit.framework.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(LMISTestRunner.class)
public class HomeActivityTest {

    HomeActivity homeActivity;
    protected MotionEvent mockMotion1;
    protected MotionEvent mockMotion2;

    @Before
    public void setUp() {
        homeActivity = Robolectric.buildActivity(HomeActivity.class).create().get();

        mockMotion1 = mock(MotionEvent.class);
        mockMotion2 = mock(MotionEvent.class);
    }

    @Test
    public void shouldGoToStockCardsPage() {
        homeActivity.btnStockCard.performClick();

        verifyNextPage(StockCardListActivity.class.getName());
    }

    @Test
    public void shouldGoToMMIAFormPage() {
        homeActivity.btnMMIA.performClick();

        verifyNextPage(MMIAActivity.class.getName());
    }

    @Test
    public void shouldGoToInventoryPage() {
        homeActivity.btnInventory.performClick();

        Intent startedIntent = shadowOf(homeActivity).getNextStartedActivity();

        assertThat(startedIntent.getComponent().getClassName(), equalTo(InventoryActivity.class.getName()));
        assertThat(startedIntent.getBooleanExtra(InventoryActivity.PARAM_IS_PHYSICAL_INVENTORY, false), is(true));
    }

    @Test
    public void shouldGoToMMIAHistoryPage() {
        homeActivity.btnMMIAList.performClick();

        Intent startedIntent = shadowOf(homeActivity).getNextStartedActivity();

        assertThat(startedIntent.getComponent().getClassName(), equalTo(RnRFormListActivity.class.getName()));
        assertThat(startedIntent.getStringExtra(RnRFormListActivity.PARAM_PROGRAM_CODE), is(MMIARepository.MMIA_PROGRAM_CODE));
    }

    @Test
    public void shouldGoToViaHistoryPage() {
        homeActivity.btnVIAList.performClick();

        Intent startedIntent = shadowOf(homeActivity).getNextStartedActivity();

        assertThat(startedIntent.getComponent().getClassName(), equalTo(RnRFormListActivity.class.getName()));
        assertThat(startedIntent.getStringExtra(RnRFormListActivity.PARAM_PROGRAM_CODE), is(VIARepository.VIA_PROGRAM_CODE));
    }

    @Test
    public void shouldShowLastSyncedTimeCorrectly() {
        SharedPreferences sharedPreferences = RuntimeEnvironment.application.getSharedPreferences(SharedPreferenceMgr.MY_PREFERENCE, Context.MODE_PRIVATE);
        sharedPreferences.edit().putLong(SharedPreferenceMgr.KEY_LAST_SYNCED_TIME, new Date().getTime() - 20 * DateUtil.MILLISECONDS_MINUTE).apply();

        homeActivity.onResume();
        assertThat(homeActivity.txLastSynced.getText().toString(), equalTo(homeActivity.getResources().getString(R.string.label_last_synced_mins_ago, "20")));

        sharedPreferences.edit().putLong(SharedPreferenceMgr.KEY_LAST_SYNCED_TIME, new Date().getTime() - 20 * DateUtil.MILLISECONDS_HOUR).apply();

        homeActivity.onResume();
        assertThat(homeActivity.txLastSynced.getText().toString(), equalTo(homeActivity.getResources().getString(R.string.label_last_synced_hours_ago, "20")));


        sharedPreferences.edit().putLong(SharedPreferenceMgr.KEY_LAST_SYNCED_TIME, new Date().getTime() - 20 * DateUtil.MILLISECONDS_DAY).apply();

        homeActivity.onResume();
        assertThat(homeActivity.txLastSynced.getText().toString(), equalTo(homeActivity.getResources().getString(R.string.label_last_synced_days_ago, "20")));

    }

    private void verifyNextPage(String className) {
        ShadowActivity shadowActivity = shadowOf(homeActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertThat(shadowIntent.getComponent().getClassName(), equalTo(className));
    }

    @Test
    public void shouldNotLogOutOrResetTimeIfFirstTimeOperation() throws Exception {
        when(mockMotion1.getEventTime()).thenReturn(1234L);
        homeActivity.dispatchTouchEvent(mockMotion1);
        Assert.assertThat(LMISApp.lastOperateTime, Is.is(not(0L)));
    }

    @Test
    public void shouldNotLogOutOrResetTimeIfNotTimeOut() throws Exception {
        when(mockMotion1.getEventTime()).thenReturn(10000L);
        homeActivity.dispatchTouchEvent(mockMotion1);

        when(mockMotion2.getEventTime()).thenReturn(16000L);
        homeActivity.dispatchTouchEvent(mockMotion2);

        Assert.assertThat(LMISApp.lastOperateTime, Is.is(not(0L)));
        Intent startedIntent = shadowOf(homeActivity).getNextStartedActivity();
        assertNull(startedIntent);
    }

    @Test
    public void shouldLogOutAndResetTimeIfTimeOut() throws Exception {
        when(mockMotion1.getEventTime()).thenReturn(10000L);
        homeActivity.dispatchTouchEvent(mockMotion1);

        when(mockMotion2.getEventTime()).thenReturn(23000L);
        homeActivity.dispatchTouchEvent(mockMotion2);

        Assert.assertThat(LMISApp.lastOperateTime, is(0L));

        Intent startedIntent = shadowOf(homeActivity).getNextStartedActivity();
        assertThat(startedIntent.getComponent().getClassName(), equalTo(LoginActivity.class.getName()));
    }
}
