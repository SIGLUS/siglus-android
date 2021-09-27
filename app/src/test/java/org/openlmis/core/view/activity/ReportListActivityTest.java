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
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.Intent;
import com.google.inject.AbstractModule;
import java.util.ArrayList;
import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.model.builder.ReportTypeFormBuilder;
import org.openlmis.core.presenter.ReportListPresenter;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.ToastUtil;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;
import roboguice.RoboGuice;
import rx.Observable;

@RunWith(LMISTestRunner.class)
public class ReportListActivityTest {

  private ReportListActivity reportListActivity;
  private ReportListPresenter mockedPresenter;
  private ActivityController<ReportListActivity> requisitionActivityActivityController;

  @Before
  public void setUp() throws Exception {
    mockedPresenter = mock(ReportListPresenter.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(ReportListPresenter.class).toInstance(mockedPresenter);
      }
    });
    requisitionActivityActivityController = Robolectric.buildActivity(ReportListActivity.class);
    reportListActivity = requisitionActivityActivityController.create().get();
    LMISTestApp.getInstance().SetActiveActivity((Activity) reportListActivity);

    verify(mockedPresenter, times(1)).getSupportReportTypes();
  }

  @After
  public void tearDown() {
    requisitionActivityActivityController.pause().stop().destroy();
    RoboGuice.Util.reset();
  }

  @Test
  public void shouldSetRequisitionTitle() {
    // then
    assertThat(reportListActivity.getTitle())
        .isEqualTo(reportListActivity.getResources().getString(R.string.home_label_requisition_and_report));
  }

  @Test
  public void shouldSetDataAfterLoad() {
    // given
    final ArrayList<ReportTypeForm> reportTypeForms = new ArrayList<>();
    final ReportTypeForm reportTypeForm = new ReportTypeFormBuilder()
        .setCode("123")
        .setName("123")
        .build();
    reportTypeForms.add(reportTypeForm);

    // when
    reportListActivity.updateSupportReportTypes(reportTypeForms);

    // then
    Assertions.assertThat(reportListActivity.navigatorAdapter.getCount()).isEqualTo(reportTypeForms.size());
    Assertions.assertThat(reportListActivity.pageAdapter.getItemCount()).isEqualTo(reportTypeForms.size());
  }

  @Test
  public void shouldShowToastWhenDateNotInEmergencyDate() {
    // given
    LMISTestApp.getInstance().setCurrentTimeMillis(
        DateUtil.parseString("2015-05-18 17:30:00", DateUtil.DATE_TIME_FORMAT).getTime());
    when(mockedPresenter.isHasVCReportType()).thenReturn(true);

    // when
    reportListActivity.checkAndGotoEmergencyPage();

    // then
    assertEquals(
        "You are not allowed to create an emergency between 18th and 25th, please submit request using the monthly requisition form.",
        ToastUtil.activityToast.getText());
  }

  @Test
  public void shouldShowToastWhenNoVcProgramAfterCreateEmergency(){
    // given
    when(mockedPresenter.isHasVCReportType()).thenReturn(false);

    // when
    reportListActivity.checkAndGotoEmergencyPage();

    // then
    assertEquals(
        "Cannot create emergency requisition",
        ToastUtil.activityToast.getText());
  }

  @Test
  public void shouldShowToastWhenHasMissed() throws Exception {
    // given
    LMISTestApp.getInstance().setCurrentTimeMillis(
        DateUtil.parseString("2015-05-17 17:30:00", DateUtil.DATE_TIME_FORMAT).getTime());
    Observable<Boolean> value = Observable.create(subscriber -> subscriber.onNext(true));
    when(mockedPresenter.isHasVCReportType()).thenReturn(true);
    when(mockedPresenter.hasMissedViaProgramPeriod()).thenReturn(value);

    // when
    reportListActivity.checkAndGotoEmergencyPage();

    // then
    assertEquals(
        "You are not allowed to create an emergency requisition until you complete all your previous monthly requisitions.",
        ToastUtil.activityToast.getText());
  }

  @Test
  public void shouldGotoEmergencyPage() throws Exception {
    // given
    LMISTestApp.getInstance().setCurrentTimeMillis(
        DateUtil.parseString("2015-05-17 17:30:00", DateUtil.DATE_TIME_FORMAT).getTime());
    Observable<Boolean> value = Observable.create(subscriber -> subscriber.onNext(false));
    when(mockedPresenter.hasMissedViaProgramPeriod()).thenReturn(value);
    when(mockedPresenter.isHasVCReportType()).thenReturn(true);

    // when
    reportListActivity.checkAndGotoEmergencyPage();

    // then
    ShadowActivity shadowActivity = shadowOf(reportListActivity);
    Intent startedIntent = shadowActivity.getNextStartedActivity();
    ShadowIntent shadowIntent = shadowOf(startedIntent);
    MatcherAssert.assertThat(shadowIntent.getIntentClass().getCanonicalName(),
        equalTo(SelectEmergencyProductsActivity.class.getName()));
  }

  @After
  public void teardown() {
    RoboGuice.Util.reset();
  }
}