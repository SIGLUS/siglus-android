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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

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
import org.openlmis.core.model.Program;
import org.openlmis.core.model.builder.ProgramBuilder;
import org.openlmis.core.presenter.ReportListPresenter;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.ShadowToast;
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

    verify(mockedPresenter, times(1)).getSupportPrograms();
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
    final ArrayList<Program> programs = new ArrayList<>();
    final Program program = new ProgramBuilder()
        .setProgramCode("123")
        .setProgramName("123")
        .build();
    programs.add(program);

    // when
    reportListActivity.updateSupportProgram(programs);

    // then
    Assertions.assertThat(reportListActivity.navigatorAdapter.getCount()).isEqualTo(programs.size());
    Assertions.assertThat(reportListActivity.pageAdapter.getItemCount()).isEqualTo(programs.size());
  }

  @Test
  public void shouldShowToastWhenDateNotInEmergencyDate() throws Exception {
    // given
    LMISTestApp.getInstance().setCurrentTimeMillis(
        DateUtil.parseString("2015-05-18 17:30:00", DateUtil.DATE_TIME_FORMAT).getTime());
    when(mockedPresenter.isHasVCProgram()).thenReturn(true);

    // when
    reportListActivity.checkAndGotoEmergencyPage();

    // then
    MatcherAssert.assertThat(ShadowToast.getTextOfLatestToast(),
        is("You are not allowed to create an emergency between 18th and 25th, please submit request using the monthly requisition form."));
  }

  @Test
  public void shouldShowToastWhenNoVcProgramAfterCreateEmergency(){
    // given
    when(mockedPresenter.isHasVCProgram()).thenReturn(false);

    // when
    reportListActivity.checkAndGotoEmergencyPage();

    MatcherAssert.assertThat(ShadowToast.getTextOfLatestToast(),
        is("Cannot create emergency requisition"));
  }

  @Test
  public void shouldShowToastWhenHasMissed() throws Exception {
    // given
    LMISTestApp.getInstance().setCurrentTimeMillis(
        DateUtil.parseString("2015-05-17 17:30:00", DateUtil.DATE_TIME_FORMAT).getTime());
    Observable<Boolean> value = Observable.create(subscriber -> subscriber.onNext(true));
    when(mockedPresenter.isHasVCProgram()).thenReturn(true);
    when(mockedPresenter.hasMissedPeriod()).thenReturn(value);

    // when
    reportListActivity.checkAndGotoEmergencyPage();

    // then
    MatcherAssert.assertThat(ShadowToast.getTextOfLatestToast(),
        is("You are not allowed to create an emergency requisition until you complete all your previous monthly requisitions."));
  }

  @Test
  public void shouldGotoEmergencyPage() throws Exception {
    // given
    LMISTestApp.getInstance().setCurrentTimeMillis(
        DateUtil.parseString("2015-05-17 17:30:00", DateUtil.DATE_TIME_FORMAT).getTime());
    Observable<Boolean> value = Observable.create(subscriber -> subscriber.onNext(false));
    when(mockedPresenter.hasMissedPeriod()).thenReturn(value);
    when(mockedPresenter.isHasVCProgram()).thenReturn(true);

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