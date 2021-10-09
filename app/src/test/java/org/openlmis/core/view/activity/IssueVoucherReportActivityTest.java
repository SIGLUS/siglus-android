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
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.view.View;
import androidx.fragment.app.DialogFragment;
import com.google.inject.AbstractModule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.builder.PodBuilder;
import org.openlmis.core.presenter.IssueVoucherReportPresenter;
import org.openlmis.core.utils.RobolectricUtils;
import org.openlmis.core.view.viewmodel.IssueVoucherReportViewModel;
import org.openlmis.core.view.widget.OrderInfoView;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class IssueVoucherReportActivityTest {

  private IssueVoucherReportActivity reportActivity;
  private IssueVoucherReportPresenter mockedPresenter;
  private ActivityController<IssueVoucherReportActivity> activityController;

  @Before
  public void setUp() {
    mockedPresenter = mock(IssueVoucherReportPresenter.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(IssueVoucherReportPresenter.class).toInstance(mockedPresenter);
      }
    });
    activityController = Robolectric.buildActivity(IssueVoucherReportActivity.class);
    reportActivity = activityController.create().get();
  }

  @After
  public void teardown() {
    activityController.pause().stop().destroy();
    RoboGuice.Util.reset();
  }

  @Test
  public void shouldCorrectSetScreenName() {
    // when
    ScreenName screenName = reportActivity.getScreenName();

    //then
    Assert.assertEquals(ScreenName.ISSUE_VOUCHER_REPORT_SCREEN, screenName);
  }


  @Test
  public void shouldNotHavePopUpWhenGoBack() throws Exception {
    // given
    Pod pod = PodBuilder.generatePod();
    pod.setOrderStatus(OrderStatus.RECEIVED);
    when(mockedPresenter.getPod()).thenReturn(pod);

    // when
    reportActivity.onBackPressed();

    // then
    assertTrue(reportActivity.isFinishing());
  }

  @Test
  public void shouldHavePopUpWhenGoBack() throws Exception {
    // given
    Pod pod = PodBuilder.generatePod();
    pod.setOrderStatus(OrderStatus.SHIPPED);
    when(mockedPresenter.getPod()).thenReturn(pod);

    // when
    reportActivity.onBackPressed();

    // then
    assertFalse(reportActivity.isFinishing());
  }


  @Test
  public void shouldCorrectUIWhenRefreshForShipped() throws Exception {
    // given
    Pod pod = PodBuilder.generatePod();
    OrderInfoView orderInfoView = mock(OrderInfoView.class);
    IssueVoucherReportViewModel viewModel = new IssueVoucherReportViewModel(pod);
    Program program = new Program();
    program.setProgramCode(Program.VIA_CODE);
    viewModel.setProgram(program);
    when(mockedPresenter.getIssueVoucherReportViewModel()).thenReturn(viewModel);
    reportActivity.setOrderInfo(orderInfoView);

    // when
    reportActivity.refreshIssueVoucherForm(PodBuilder.generatePod());

    // then
    verify(orderInfoView, times(1)).refresh(any(Pod.class), any(IssueVoucherReportViewModel.class));
  }

  @Test
  public void shouldCorrectUIWhenRefreshForReceived() throws Exception {
    // given
    OrderInfoView orderInfoView = mock(OrderInfoView.class);
    Pod pod = PodBuilder.generatePod();
    IssueVoucherReportViewModel viewModel = new IssueVoucherReportViewModel(pod);
    Program program = new Program();
    program.setProgramCode(Program.VIA_CODE);
    viewModel.setProgram(program);
    reportActivity.setOrderInfo(orderInfoView);
    pod.setOrderStatus(OrderStatus.RECEIVED);
    when(mockedPresenter.getIssueVoucherReportViewModel()).thenReturn(viewModel);

    // when
    reportActivity.refreshIssueVoucherForm(pod);

    // then
    assertEquals(View.GONE, reportActivity.getActionPanelView().getVisibility());
    verify(orderInfoView, times(1)).refresh(any(Pod.class), any(IssueVoucherReportViewModel.class));
  }

  @Test
  public void shouldShowSubmitSignatureDialog() throws Exception {
    // given
    Pod pod = PodBuilder.generatePod();
    IssueVoucherReportViewModel issueVoucherReportViewModel = new IssueVoucherReportViewModel(pod);
    Program program = new Program();
    program.setProgramName("VIA");
    issueVoucherReportViewModel.setProgram(program);
    when(mockedPresenter.getIssueVoucherReportViewModel()).thenReturn(issueVoucherReportViewModel);

    // when
    reportActivity.showSignDialog();
    RobolectricUtils.waitLooperIdle();

    // then
    DialogFragment fragment = (DialogFragment) (reportActivity
        .getSupportFragmentManager().findFragmentByTag("signature_dialog"));
    assertThat(fragment).isNotNull();
  }

}
