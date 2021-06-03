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

import android.content.Intent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.google.inject.AbstractModule;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.User;
import org.openlmis.core.model.builder.ReportTypeBuilder;
import org.openlmis.core.network.InternetCheck;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.activity.mocks.InternetCheckMockForHomeActivity;
import org.openlmis.core.view.fragment.WarningDialogFragment;
import org.openlmis.core.view.fragment.builders.WarningDialogFragmentBuilder;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.ShadowToast;

import roboguice.RoboGuice;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;
import static org.robolectric.Shadows.shadowOf;

@RunWith(LMISTestRunner.class)
public class HomeActivityTest {

    private HomeActivity homeActivity;
    private LMISTestApp testApp;
    private SharedPreferenceMgr mockSharedPreferenceMgr;
    private InternetCheck internetCheck;
    private WarningDialogFragmentBuilder warningDialogFragmentBuilder;

    @Before
    public void setUp() {
        testApp = (LMISTestApp) RuntimeEnvironment.application;
        warningDialogFragmentBuilder = mock(WarningDialogFragmentBuilder.class);
        internetCheck = new InternetCheckMockForHomeActivity(true, warningDialogFragmentBuilder);
        mockSharedPreferenceMgr = mock(SharedPreferenceMgr.class);

        UserInfoMgr.getInstance().setUser(new User("user", "password"));
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(InternetCheck.class).toInstance(internetCheck);
                bind(WarningDialogFragmentBuilder.class).toInstance(warningDialogFragmentBuilder);
                bind(SharedPreferenceMgr.class).toInstance(mockSharedPreferenceMgr);
            }
        });
        when(mockSharedPreferenceMgr.getReportTypesData()).
                thenReturn(newArrayList(new ReportTypeBuilder().getMMIAReportTypeForm()));
        homeActivity = Robolectric.buildActivity(HomeActivity.class).create().get();
    }

    @Test
    public void shouldGoToStockCardsPage() {
        homeActivity.btnStockCard.performClick();

        Intent nextStartedIntent = shadowOf(homeActivity).getNextStartedActivity();
        assertEquals(StockCardListActivity.class.getName(), nextStartedIntent.getComponent().getClassName());
    }

    @Test
    public void shouldGoToKitsStockCardsPage() throws Exception {
        homeActivity.btnKitStockCard.performClick();

        Intent nextStartedIntent = shadowOf(homeActivity).getNextStartedActivity();
        assertEquals(KitStockCardListActivity.class.getName(), nextStartedIntent.getComponent().getClassName());
    }

    @Test
    public void shouldGoToInventoryPage() {
        homeActivity.btnInventory.performClick();

        Intent startedIntent = shadowOf(homeActivity).getNextStartedActivity();

        assertThat(startedIntent.getComponent().getClassName(), equalTo(PhysicalInventoryActivity.class.getName()));
    }

    @Test
    public void shouldGoToMMIAHistoryPage() {
        homeActivity.btnMMIAList.performClick();

        Intent startedIntent = shadowOf(homeActivity).getNextStartedActivity();

        assertThat(startedIntent.getComponent().getClassName(), equalTo(RnRFormListActivity.class.getName()));
        assertThat(startedIntent.getSerializableExtra(Constants.PARAM_PROGRAM_CODE), is(Constants.Program.MMIA_PROGRAM));
    }

    @Test
    public void shouldGoToViaHistoryPage() {
        homeActivity.btnVIAList.performClick();

        Intent startedIntent = shadowOf(homeActivity).getNextStartedActivity();

        assertThat(startedIntent.getComponent().getClassName(), equalTo(RnRFormListActivity.class.getName()));
        assertThat(startedIntent.getSerializableExtra(Constants.PARAM_PROGRAM_CODE), is(Constants.Program.VIA_PROGRAM));
    }

    private void verifyNextPage(String className) {
        ShadowActivity shadowActivity = shadowOf(homeActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertThat(shadowIntent.getIntentClass().getCanonicalName(), equalTo(className));
    }

    @Test
    public void shouldNotLogOutOrResetTimeIfFirstTimeOperation() throws Exception {
        testApp.setCurrentTimeMillis(1234L);
        homeActivity.dispatchTouchEvent(mock(MotionEvent.class));
        Assert.assertThat(homeActivity.getLastOperateTime(), Is.is(not(0L)));
    }

    @Test
    public void shouldNotLogOutOrResetTimeIfNotTimeOut() throws Exception {
        UserInfoMgr.getInstance().setUser(new User("user", "password"));
        testApp.setCurrentTimeMillis(10000L);
        homeActivity.dispatchTouchEvent(mock(MotionEvent.class));

        testApp.setCurrentTimeMillis(9000L + Long.parseLong(homeActivity.getString(R.string.app_time_out)));
        homeActivity.dispatchTouchEvent(mock(MotionEvent.class));

        Assert.assertThat(homeActivity.getLastOperateTime(), Is.is(not(0L)));
        Intent startedIntent = shadowOf(homeActivity).getNextStartedActivity();
        assertNull(startedIntent);
    }

    @Test
    public void shouldLogOutAndResetTimeIfTimeOut() throws Exception {
        testApp.setCurrentTimeMillis(10000L);
        homeActivity.dispatchTouchEvent(mock(MotionEvent.class));

        testApp.setCurrentTimeMillis(11000L + Long.parseLong(homeActivity.getString(R.string.app_time_out)));
        homeActivity.dispatchTouchEvent(mock(MotionEvent.class));

        Assert.assertThat(homeActivity.getLastOperateTime(), is(0L));

        Intent startedIntent = shadowOf(homeActivity).getNextStartedActivity();
        assertThat(startedIntent.getComponent().getClassName(), equalTo(LoginActivity.class.getName()));
    }

    @Test
    public void shouldToastWarningMessageWhenClickBackButtonFirstTime() {
        homeActivity.onBackPressed();

        String warningMessage = ShadowToast.getTextOfLatestToast();

        assertThat(warningMessage, equalTo(homeActivity.getString(R.string.msg_back_twice_to_exit)));
    }

    @Test
    public void shouldFinishMainActivityAndStartLoginActivityWhenSighOutClicked() {
        MenuItem signoutAction = new RoboMenuItem(R.id.action_sign_out);

        homeActivity.onOptionsItemSelected(signoutAction);

        assertTrue(homeActivity.isFinishing());
        verifyNextPage(LoginActivity.class.getName());
    }

    @Test
    public void shouldShowNewTextOfMMIAListAndVIALIstButtons() throws Exception {
        HomeActivity activity = Robolectric.buildActivity(HomeActivity.class).create().get();

        assertThat(activity.btnMMIAList.getText().toString(), is(activity.getString(R.string.mmia_list)));
        assertThat(activity.btnVIAList.getText().toString(), is(activity.getString(R.string.requisition_list)));
    }

    @Test
    public void shouldShowMMIARapidTestALButtons() {
        when(mockSharedPreferenceMgr.getReportTypesData()).thenReturn(ReportTypeBuilder.getReportTypeForms(getClass(), "HomeActivityEntry.json"));
        HomeActivity homeActivity = Robolectric.buildActivity(HomeActivity.class).create().get();

        assertEquals(homeActivity.btnMMIAList.getVisibility(), View.VISIBLE);
        assertEquals(homeActivity.btnRapidTestReport.getVisibility(), View.VISIBLE);
        assertEquals(homeActivity.btnALReport.getVisibility(), View.VISIBLE);
        assertEquals(homeActivity.btnPTVReport.getVisibility(), View.GONE);
    }

    @Test
    public void shouldNotShowMMIAAndPTVBoth() {
        when(mockSharedPreferenceMgr.getReportTypesData()).thenReturn(ReportTypeBuilder.getReportTypeForms(getClass(), "HomeActivityNoMMIAAndPTV.json"));
        HomeActivity homeActivity = Robolectric.buildActivity(HomeActivity.class).create().get();

        assertEquals(homeActivity.btnMMIAList.getVisibility(), View.GONE);
        assertEquals(homeActivity.btnRapidTestReport.getVisibility(), View.VISIBLE);
        assertEquals(homeActivity.btnALReport.getVisibility(), View.VISIBLE);
        assertEquals(homeActivity.btnPTVReport.getVisibility(), View.GONE);
    }


    @Test
    public void shouldShowWarningDialogWhenWipeDataWiped() throws Exception {
        WarningDialogFragment.DialogDelegate delegate = anyObject();
        int message = anyInt();
        int positiveMessageButton = anyInt();
        int negativeMessageButton = anyInt();
        when(warningDialogFragmentBuilder.build(delegate, message, positiveMessageButton, negativeMessageButton)).thenReturn(mock(WarningDialogFragment.class));

        homeActivity.onOptionsItemSelected(new RoboMenuItem(R.id.action_wipe_data));
    }

    @Test
    public void shouldShowToastWhenResyncWithoutNetwork() {
        boolean isAvailableInternet = false;
        internetCheck = new InternetCheckMockForHomeActivity(isAvailableInternet, warningDialogFragmentBuilder);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(InternetCheck.class).toInstance(internetCheck);
                bind(SharedPreferenceMgr.class).toInstance(mockSharedPreferenceMgr);
            }
        });
        homeActivity = Robolectric.buildActivity(HomeActivity.class).create().get();

        homeActivity.onOptionsItemSelected(new RoboMenuItem(R.id.action_wipe_data));
    }
}
