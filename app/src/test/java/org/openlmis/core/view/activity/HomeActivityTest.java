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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(LMISTestRunner.class)
public class HomeActivityTest {

    HomeActivity homeActivity;

    @Before
    public void setUp() {
        homeActivity = Robolectric.buildActivity(HomeActivity.class).create().get();
    }

    @Test
    public void shouldGoToStockCardsPage() {
        homeActivity.btnStockCard.performClick();

        verifyNextPage(StockCardListActivity.class.getName());
    }

    @Test
    public void shouldGoToMMIAFormPage(){
        homeActivity.btnMMIA.performClick();

        verifyNextPage(MMIASpreadActivity.class.getName());
    }

    @Test
    public void shouldShowLastSyncedTimeCorrectly() {
        SharedPreferences sharedPreferences = Robolectric.application.getSharedPreferences(SharedPreferenceMgr.MY_PREFERENCE, Context.MODE_PRIVATE);
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

    private void verifyNextPage(String className){
        ShadowActivity shadowActivity = shadowOf(homeActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertThat(shadowIntent.getComponent().getClassName(), equalTo(className));
    }
}
