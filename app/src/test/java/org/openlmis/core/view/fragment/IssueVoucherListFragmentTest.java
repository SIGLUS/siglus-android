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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import androidx.fragment.app.Fragment;
import com.google.inject.AbstractModule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.builder.PodBuilder;
import org.openlmis.core.presenter.IssueVoucherListPresenter;
import org.openlmis.core.utils.RobolectricUtils;
import org.openlmis.core.view.activity.EditOrderNumberActivity;
import org.openlmis.core.view.activity.IssueVoucherInputOrderNumberActivity;
import org.openlmis.core.view.activity.IssueVoucherListActivity;
import org.openlmis.core.view.activity.IssueVoucherReportActivity;
import org.openlmis.core.view.adapter.IssueVoucherListAdapter;
import org.openlmis.core.view.viewmodel.IssueVoucherListViewModel;
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
    mockPresenter = mock(IssueVoucherListPresenter.class);
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
  public void shouldShowDeleteConfirmDialogAfterOperation() throws Exception {
    // given
    IssueVoucherListViewModel mockViewModel = mock(IssueVoucherListViewModel.class);
    Pod pod = PodBuilder.generatePod();
    when(mockViewModel.getPod()).thenReturn(pod);
    when(mockViewModel.isIssueVoucher()).thenReturn(true);

    // when
    fragment.orderDeleteOrEditOperation(mockViewModel);
    RobolectricUtils.waitLooperIdle();

    // then
    Fragment dialog = fragment.getParentFragmentManager().findFragmentByTag("delete_issue_voucher_confirm_dialog");
    Assert.assertNotNull(dialog);
  }

  @Test
  public void shouldShowWarningDialogAfterOperation() throws Exception {
    // given
    IssueVoucherListViewModel mockViewModel = mock(IssueVoucherListViewModel.class);
    Pod pod = PodBuilder.generatePod();
    when(mockViewModel.getPod()).thenReturn(pod);
    when(mockViewModel.isIssueVoucher()).thenReturn(false);
    when(mockPresenter.editablePodOrder(any())).thenReturn(false);

    // when
    fragment.orderDeleteOrEditOperation(mockViewModel);
    RobolectricUtils.waitLooperIdle();

    // then
    Fragment dialog = fragment.getParentFragmentManager().findFragmentByTag("cannot_edit_order_number_dialog");
    Assert.assertNotNull(dialog);
  }

  @Test
  public void shouldGotoEditOrderNumber() throws Exception {
    // given
    IssueVoucherListViewModel mockViewModel = mock(IssueVoucherListViewModel.class);
    when(mockViewModel.isIssueVoucher()).thenReturn(false);
    Pod pod = PodBuilder.generatePod();
    when(mockViewModel.getPod()).thenReturn(pod);
    when(mockPresenter.editablePodOrder(anyString())).thenReturn(true);

    // when
    fragment.orderDeleteOrEditOperation(mockViewModel);
    RobolectricUtils.waitLooperIdle();

    // when
    ShadowActivity shadowActivity = shadowOf(listActivity);
    Intent startedIntent = shadowActivity.getNextStartedActivity();

    // then
    Assert.assertEquals(EditOrderNumberActivity.class.getName(), startedIntent.getComponent().getClassName());
  }

  @Test
  public void shouldGotoReportPage() throws Exception {
    // given
    IssueVoucherListViewModel mockViewModel = mock(IssueVoucherListViewModel.class);
    when(mockViewModel.isIssueVoucher()).thenReturn(false);
    Pod pod = PodBuilder.generatePod();
    when(mockViewModel.getPod()).thenReturn(pod);
    when(mockViewModel.isRemoteIssueVoucherOrPod()).thenReturn(true);
    when(mockViewModel.isNeedEnterInputOrderNumber()).thenReturn(false);

    // when
    fragment.orderEditOrViewOperation(mockViewModel);
    RobolectricUtils.waitLooperIdle();

    // when
    ShadowActivity shadowActivity = shadowOf(listActivity);
    Intent startedIntent = shadowActivity.getNextStartedActivity();

    // then
    Assert.assertEquals(IssueVoucherReportActivity.class.getName(), startedIntent.getComponent().getClassName());
  }

  @Test
  public void shouldGoToInputOrderNumberPage() throws Exception {
    // given
    IssueVoucherListViewModel mockViewModel = mock(IssueVoucherListViewModel.class);
    when(mockViewModel.isIssueVoucher()).thenReturn(false);
    Pod pod = PodBuilder.generatePod();
    when(mockViewModel.getPod()).thenReturn(pod);
    when(mockViewModel.isRemoteIssueVoucherOrPod()).thenReturn(true);
    when(mockViewModel.isNeedEnterInputOrderNumber()).thenReturn(true);

    // when
    fragment.orderEditOrViewOperation(mockViewModel);
    RobolectricUtils.waitLooperIdle();

    // when
    ShadowActivity shadowActivity = shadowOf(listActivity);
    Intent startedIntent = shadowActivity.getNextStartedActivity();

    // then
    Assert.assertEquals(IssueVoucherInputOrderNumberActivity.class.getName(), startedIntent.getComponent().getClassName());
  }

  @Test
  public void shouldShowWarningDialogWhenHasUnmatchedPod() throws Exception {
    // given
    IssueVoucherListViewModel mockViewModel = mock(IssueVoucherListViewModel.class);
    Pod pod = PodBuilder.generatePod();
    when(mockViewModel.getPod()).thenReturn(pod);
    when(mockViewModel.isIssueVoucher()).thenReturn(true);
    when(mockPresenter.hasUnmatchedPod(any())).thenReturn(true);

    // when
    fragment.orderEditOrViewOperation(mockViewModel);
    RobolectricUtils.waitLooperIdle();

    // then
    Fragment dialog = fragment.getParentFragmentManager().findFragmentByTag("has_unmatched_pod_dialog");
    Assert.assertNotNull(dialog);
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
    IssueVoucherListAdapter mockAdapter = mock(IssueVoucherListAdapter.class);
    fragment.setAdapter(mockAdapter);

    // when
    fragment.onRefreshList();

    // then
    verify(mockAdapter, times(1)).notifyDataSetChanged();
  }
}