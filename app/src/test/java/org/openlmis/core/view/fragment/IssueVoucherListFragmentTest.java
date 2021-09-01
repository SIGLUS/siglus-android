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

package org.openlmis.core.view.fragment;

import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import androidx.fragment.app.Fragment;
import com.google.inject.AbstractModule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.constant.FieldConstants;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.presenter.IssueVoucherListPresenter;
import org.openlmis.core.utils.RobolectricUtils;
import org.openlmis.core.view.activity.EditOrderNumberActivity;
import org.openlmis.core.view.activity.IssueVoucherListActivity;
import org.openlmis.core.view.adapter.IssueVoucherListAdapter;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowToast;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class IssueVoucherListFragmentTest {

  private ActivityController<IssueVoucherListActivity> activityController;

  private IssueVoucherListPresenter mockPresenter;

  private IssueVoucherListFragment fragment;
  private IssueVoucherListActivity listActivity;

  @Before
  public void setup() {
    mockPresenter = Mockito.mock(IssueVoucherListPresenter.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(IssueVoucherListPresenter.class).toInstance(mockPresenter);
      }
    });
    activityController = Robolectric.buildActivity(IssueVoucherListActivity.class);
    listActivity = activityController.create().start().resume().get();
    fragment = IssueVoucherListFragment.newInstance(true);
    listActivity.getSupportFragmentManager().beginTransaction().add(fragment, null).commit();
    RobolectricUtils.waitLooperIdle();
  }

  @After
  public void teardown() {
    activityController.pause().stop().destroy();
    RoboGuice.Util.reset();
  }

  @Test
  public void shouldShowConfirmDialogAfterOrderOperation() {
    // when
    fragment.orderDeleteOrEditOperation(OrderStatus.SHIPPED, FieldConstants.ORDER_CODE);
    RobolectricUtils.waitLooperIdle();

    // then
    Fragment dialog = fragment.getParentFragmentManager().findFragmentByTag("delete_issue_voucher_confirm_dialog");
    Assert.assertNotNull(dialog);
  }

  @Test
  public void shouldGotoEditOrderNumber() {
    // when
    fragment.orderDeleteOrEditOperation(OrderStatus.RECEIVED, FieldConstants.ORDER_CODE);
    RobolectricUtils.waitLooperIdle();

    // when
    ShadowActivity shadowActivity = shadowOf(listActivity);
    Intent startedIntent = shadowActivity.getNextStartedActivity();

    // then
    Assert.assertEquals(EditOrderNumberActivity.class.getName(),startedIntent.getComponent().getClassName());
  }

  @Test
  public void shouldToastAndFinishAfterLoadDataFailed() {
    // when
    LMISException lmisException = new LMISException("test exception");
    fragment.onLoadDataFailed(lmisException);

    // then
    Assert.assertEquals(lmisException.getMsg(), ShadowToast.getTextOfLatestToast());
    Assert.assertTrue(shadowOf(listActivity).isFinishing());
  }

  @Test
  public void shouldRefreshAdapterAfterLoadData() {
    // given
    IssueVoucherListAdapter mockAdapter = Mockito.mock(IssueVoucherListAdapter.class);
    fragment.setAdapter(mockAdapter);

    // when
    fragment.onRefreshList();

    // then
    Mockito.verify(mockAdapter, Mockito.times(1)).notifyDataSetChanged();
  }
}