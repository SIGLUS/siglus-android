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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.core.utils.RobolectricUtils.resetNextClickTime;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import androidx.fragment.app.Fragment;
import com.google.inject.AbstractModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.event.CmmCalculateEvent;
import org.openlmis.core.event.SyncStatusEvent;
import org.openlmis.core.event.SyncStatusEvent.SyncStatus;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.User;
import org.openlmis.core.model.builder.ReportTypeBuilder;
import org.openlmis.core.service.SyncService;
import org.openlmis.core.utils.RobolectricUtils;
import org.openlmis.core.view.widget.DashboardView;
import org.openlmis.core.view.widget.NotificationBanner;
import org.openlmis.core.view.widget.SyncTimeView;
import org.robolectric.Robolectric;
import androidx.test.core.app.ApplicationProvider;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.ShadowToast;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class HomeActivityTest {

  private HomeActivity homeActivity;
  private LMISTestApp testApp;
  private SyncService syncService;
  private SyncTimeView syncTimeView;
  private DashboardView dashboardView;
  private SharedPreferenceMgr mockSharedPreferenceMgr;
  private ActivityController<HomeActivity> activityController;

  @Before
  public void setUp() {
    testApp = (LMISTestApp) ApplicationProvider.getApplicationContext();
    mockSharedPreferenceMgr = mock(SharedPreferenceMgr.class);
    syncService = mock(SyncService.class);
    syncTimeView = mock(SyncTimeView.class);
    dashboardView = mock(DashboardView.class);
    UserInfoMgr.getInstance().setUser(new User("user", "password"));
    RoboGuice.overrideApplicationInjector(ApplicationProvider.getApplicationContext(), new AbstractModule() {
      @Override
      protected void configure() {
        bind(SharedPreferenceMgr.class).toInstance(mockSharedPreferenceMgr);
        bind(SyncService.class).toInstance(syncService);
      }
    });
    when(mockSharedPreferenceMgr.getReportTypesData())
        .thenReturn(newArrayList(new ReportTypeBuilder().getMMIAReportTypeForm()));
    activityController = Robolectric.buildActivity(HomeActivity.class);
    homeActivity = activityController.create().get();
    homeActivity.syncTimeView = syncTimeView;
    homeActivity.dvProductDashboard = dashboardView;
  }

  @After
  public void tearDown() {
    activityController.pause().stop().destroy();
    RoboGuice.Util.reset();
  }

  @Test
  public void shouldGoToStockCardsPage() {
    // given
    resetNextClickTime();

    // when
    homeActivity.findViewById(R.id.btn_stock_card).performClick();

    // then
    Intent nextStartedIntent = shadowOf(homeActivity).getNextStartedActivity();
    assertEquals(StockCardListActivity.class.getName(),
        nextStartedIntent.getComponent().getClassName());
  }

  @Test
  public void shouldGoToKitsStockCardsPage() {
    // given
    resetNextClickTime();

    // when
    homeActivity.findViewById(R.id.btn_kits).performClick();

    // then
    Intent nextStartedIntent = shadowOf(homeActivity).getNextStartedActivity();
    assertEquals(KitStockCardListActivity.class.getName(),
        nextStartedIntent.getComponent().getClassName());
  }

  @Test
  public void shouldGoToInventoryPage() {
    // given
    resetNextClickTime();

    // when
    homeActivity.findViewById(R.id.btn_inventory).performClick();

    // then
    Intent startedIntent = shadowOf(homeActivity).getNextStartedActivity();
    assertThat(startedIntent.getComponent().getClassName(),
        equalTo(PhysicalInventoryActivity.class.getName()));
  }

  @Test
  public void shouldGoRequisitionPage() {
    // given
    resetNextClickTime();

    // when
    homeActivity.findViewById(R.id.btn_requisitions).performClick();

    // then
    Intent startedIntent = shadowOf(homeActivity).getNextStartedActivity();
    assertThat(startedIntent.getComponent().getClassName(),
        equalTo(ReportListActivity.class.getName()));
  }

  @Test
  public void shouldGoToIssueVoucherPage() {
    // given
    resetNextClickTime();

    // when
    homeActivity.findViewById(R.id.btn_issue_voucher).performClick();

    // then
    Intent startedIntent = shadowOf(homeActivity).getNextStartedActivity();
    assertThat(startedIntent.getComponent().getClassName(),
        equalTo(IssueVoucherListActivity.class.getName()));
  }

  private void verifyNextPage(String className) {
    ShadowActivity shadowActivity = shadowOf(homeActivity);
    Intent startedIntent = shadowActivity.getNextStartedActivity();
    ShadowIntent shadowIntent = shadowOf(startedIntent);
    assertThat(shadowIntent.getIntentClass().getCanonicalName(), equalTo(className));
  }

  @Test
  public void shouldNotLogOutOrResetTimeIfFirstTimeOperation() {
    testApp.setCurrentTimeMillis(1234L);
    homeActivity.dispatchTouchEvent(mock(MotionEvent.class));
    Intent startedIntent = shadowOf(homeActivity).getNextStartedActivity();
    assertNull(startedIntent);
  }

  @Test
  public void shouldNotLogOutOrResetTimeIfNotTimeOut() {
    UserInfoMgr.getInstance().setUser(new User("user", "password"));
    testApp.setCurrentTimeMillis(10000L);
    homeActivity.dispatchTouchEvent(mock(MotionEvent.class));

    testApp.setCurrentTimeMillis(
        9000L + Long.parseLong(homeActivity.getString(R.string.app_time_out)));
    homeActivity.dispatchTouchEvent(mock(MotionEvent.class));

    Intent startedIntent = shadowOf(homeActivity).getNextStartedActivity();
    assertNull(startedIntent);
  }

  @Test
  public void shouldLogOutAndResetTimeIfTimeOut() {
    testApp.setCurrentTimeMillis(10000L);
    homeActivity.dispatchTouchEvent(mock(MotionEvent.class));

    testApp.setCurrentTimeMillis(
        11000L + Long.parseLong(homeActivity.getString(R.string.app_time_out)));
    homeActivity.dispatchTouchEvent(mock(MotionEvent.class));

    Intent startedIntent = shadowOf(homeActivity).getNextStartedActivity();
    assertNotNull(startedIntent);
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
  public void shouldShowWarningDialogWhenWipeDataWiped() {
    // when
    homeActivity.validateConnectionListener.onResult(true);
    RobolectricUtils.waitLooperIdle();

    // then
    final Fragment autoSyncDataDialog = homeActivity.getSupportFragmentManager()
        .findFragmentByTag("autoSyncDataBeforeResyncDialog");
    assertNotNull(autoSyncDataDialog);
  }

  @Test
  public void shouldShowToastWhenResyncWithoutNetwork() {
    // given
    String expectedMessage = "The network is unavailable, please try again when you have the network";
    // when
    homeActivity.validateConnectionListener.onResult(false);

    // then
    String warningMessage = ShadowToast.getTextOfLatestToast();
    assertEquals(expectedMessage, warningMessage);
  }

  @Test
  public void shouldSyncData() {
    // given
    MenuItem signoutAction = new RoboMenuItem(R.id.action_sync_data);

    // when
    homeActivity.onOptionsItemSelected(signoutAction);

    // then
    verify(syncService, times(1)).requestSyncImmediatelyFromUserTrigger();
  }

  @Test
  public void shouldShowProgressBarWhenReceiveSyncStatusStart() {
    // given
    final SyncStatusEvent syncStatusEvent = new SyncStatusEvent(SyncStatus.START);

    // when
    homeActivity.onReceiveSyncStatusEvent(syncStatusEvent);

    // then
    verify(syncTimeView, times(1)).showSyncProgressBarAndHideIcon();
  }

  @Test
  public void shouldShowErrorMsgWhenReceiveSyncStatusError() {
    // given
    final SyncStatusEvent errorEventWithoutMsg = new SyncStatusEvent(SyncStatus.ERROR);
    final SyncStatusEvent errorEventWithMsg = new SyncStatusEvent(SyncStatus.ERROR, "error msg");

    // when
    homeActivity.onReceiveSyncStatusEvent(errorEventWithoutMsg);

    // then
    verify(syncTimeView, times(1)).setSyncStockCardLastYearError();

    // when
    homeActivity.onReceiveSyncStatusEvent(errorEventWithMsg);

    // then
    verify(syncTimeView, times(1)).setSyncedMovementError("error msg");
  }

  @Test
  public void shouldRefreshDashboardWhenReceiveSyncStatusFinish() {
    // given
    final SyncStatusEvent finishEvent = new SyncStatusEvent(SyncStatus.FINISH);
    final CmmCalculateEvent cmmCalculateEvent = new CmmCalculateEvent(true);
    when(mockSharedPreferenceMgr.shouldSyncLastYearStockData()).thenReturn(true);
    when(mockSharedPreferenceMgr.isSyncingLastYearStockCards()).thenReturn(true);

    // when
    homeActivity.onReceiveSyncStatusEvent(finishEvent);
    homeActivity.onReceiveCmmCalculateEvent(cmmCalculateEvent);

    // then
    verify(syncTimeView, times(1)).setSyncStockCardLastYearText();
    verify(dashboardView, times(1)).showCalculating();
  }

  @Test
  public void shouldShowNotificationWhenHasNewShippedPod() {
    // given
    String shippedProgramNames = "MMIA";
    when(mockSharedPreferenceMgr.getNewShippedProgramNames()).thenReturn(shippedProgramNames);

    // when
    homeActivity.onResume();

    // then
    NotificationBanner notificationBanner = (NotificationBanner) homeActivity.findViewById(
        R.id.view_new_shipped_issue_voucher_banner
    );
    CharSequence actualNotificationMessage = notificationBanner.txMissedRequisition.getText();
    assertEquals(
        homeActivity.getString(
            R.string.new_shipped_issue_voucher_alert_message_for_single_program, shippedProgramNames
        ),
        actualNotificationMessage
    );
    assertEquals(
        "The order for the " + shippedProgramNames + " has been fulfilled",
        actualNotificationMessage
    );
    assertEquals(View.VISIBLE, notificationBanner.getVisibility());
  }

  @Test
  public void shouldShowNotificationWhenHasNewShippedPods() {
    // given
    String shippedProgramNames = "Test, MMIA";
    when(mockSharedPreferenceMgr.getNewShippedProgramNames()).thenReturn(shippedProgramNames);

    // when
    homeActivity.onResume();

    // then
    NotificationBanner notificationBanner = (NotificationBanner) homeActivity.findViewById(
        R.id.view_new_shipped_issue_voucher_banner
    );
    CharSequence actualNotificationMessage = notificationBanner.txMissedRequisition.getText();
    assertEquals(
        homeActivity.getString(
            R.string.new_shipped_issue_voucher_alert_message, shippedProgramNames
        ),
        actualNotificationMessage
    );
    assertEquals(
        "The order for the " + shippedProgramNames + " have been fulfilled",
        actualNotificationMessage
    );
    assertEquals(View.VISIBLE, notificationBanner.getVisibility());
  }

  @Test
  public void shouldHideIVNotificationAndClearNewShippedPodsWhenClickIVEntry() {
    // given
    String shippedProgramNames = "Test, MMIA";
    when(mockSharedPreferenceMgr.getNewShippedProgramNames()).thenReturn(shippedProgramNames);

    resetNextClickTime();
    // when
    homeActivity.onResume();
    homeActivity.findViewById(R.id.btn_issue_voucher).performClick();

    // then
    assertEquals(
        View.INVISIBLE,
        homeActivity.findViewById(R.id.view_new_shipped_issue_voucher_banner).getVisibility()
    );
    verify(mockSharedPreferenceMgr).setNewShippedProgramNames(null);
  }
}
