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

import static junit.framework.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.inject.AbstractModule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.builder.PodBuilder;
import org.openlmis.core.presenter.IssueVoucherReportPresenter;
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

  @Ignore
  @Test
  public void shouldNotRemoveRnrFormWhenGoBack() {
    // when
    reportActivity.onBackPressed();

    // then
    assertFalse(reportActivity.isFinishing());
  }

  @Test
  public void shouldCorrectWhenRefresh() throws Exception {
    // given
    OrderInfoView orderInfoView = mock(OrderInfoView.class);
    when(mockedPresenter.getIssueVoucherReportViewModel()).thenReturn(mock(IssueVoucherReportViewModel.class));
    reportActivity.setOrderInfo(orderInfoView);

    // when
    reportActivity.refreshIssueVoucherForm(PodBuilder.generatePod());

    // then
    verify(orderInfoView, times(1)).refresh(any(Pod.class), any(IssueVoucherReportViewModel.class));
  }

}
