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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

import android.content.Intent;
import android.view.View;
import com.google.inject.AbstractModule;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Program;
import org.openlmis.core.presenter.RnRFormListPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.view.activity.ALRequisitionActivity;
import org.openlmis.core.view.activity.MMIARequisitionActivity;
import org.openlmis.core.view.activity.PhysicalInventoryActivity;
import org.openlmis.core.view.activity.SelectPeriodActivity;
import org.openlmis.core.view.activity.VIARequisitionActivity;
import org.openlmis.core.view.viewmodel.RnRFormViewModel;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowApplication;
import roboguice.RoboGuice;
import roboguice.fragment.SupportFragmentController;
import rx.Observable;

@RunWith(LMISTestRunner.class)
public class ReportListFragmentTest {

  private RnRFormListPresenter mockedPresenter;

  private ReportListFragment reportListFragment;
  private SupportFragmentController<ReportListFragment> fragmentController;

  @Before
  public void setUp() throws Exception {
    mockedPresenter = mock(RnRFormListPresenter.class);
    Observable observable = Observable.just(newArrayList());
    when(mockedPresenter.loadRnRFormList()).thenReturn(observable);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(RnRFormListPresenter.class).toInstance(mockedPresenter);
      }
    });
    reportListFragment = ReportListFragment.newInstance(Program.VIA_CODE);
    fragmentController = SupportFragmentController.of(reportListFragment);
    fragmentController.create().start().resume().get();
  }

  @After
  public void teardown() {
    fragmentController.pause().stop().destroy();
  }

  @Test
  public void shouldStartPhysicalInventoryWhenBtnClickedWithUncompleteInventory() throws Exception {
    // given
    View view = mock(View.class);
    RnRFormViewModel viewModel = generateRnRFormViewModel(Program.VIA_CODE,
        RnRFormViewModel.TYPE_UNCOMPLETE_INVENTORY_IN_CURRENT_PERIOD);

    // when
    reportListFragment.rnRFormItemClickListener.clickBtnView(viewModel, view);

    // then
    Intent nextStartedIntent = ShadowApplication.getInstance().getNextStartedActivity();
    assertNotNull(nextStartedIntent);
    assertEquals(nextStartedIntent.getComponent().getClassName(), PhysicalInventoryActivity.class.getName());
  }

  @Test
  public void shouldSelectPeriodWhenBtnClickedWithSelectClosePeriod() throws Exception {
    // given
    View view = mock(View.class);
    RnRFormViewModel viewModel = generateRnRFormViewModel(Program.VIA_CODE, RnRFormViewModel.TYPE_INVENTORY_DONE);

    // when
    reportListFragment.rnRFormItemClickListener.clickBtnView(viewModel, view);

    // then
    Intent nextStartedIntent = ShadowApplication.getInstance().getNextStartedActivity();
    assertNotNull(nextStartedIntent);
    assertEquals(nextStartedIntent.getComponent().getClassName(), SelectPeriodActivity.class.getName());
    assertEquals(Program.VIA_CODE, nextStartedIntent.getStringExtra(Constants.PARAM_PROGRAM_CODE));
  }

  @Test
  public void shouldStartSelectPeriodPageWhenBtnClickedWithTypeMissedPeriod() throws Exception {
    // given
    View view = mock(View.class);
    RnRFormViewModel viewModel = generateRnRFormViewModel(Program.VIA_CODE, RnRFormViewModel.TYPE_FIRST_MISSED_PERIOD);

    // when
    reportListFragment.rnRFormItemClickListener.clickBtnView(viewModel, view);

    // then
    Intent nextStartedIntent = ShadowApplication.getInstance().getNextStartedActivity();
    assertNotNull(nextStartedIntent);
    assertEquals(nextStartedIntent.getComponent().getClassName(), SelectPeriodActivity.class.getName());
    assertEquals(Program.VIA_CODE, nextStartedIntent.getStringExtra(Constants.PARAM_PROGRAM_CODE));
  }

  @Test
  public void shouldStartMMIAHistoryWhenBtnClickedWithTypeHistory() {
    // given
    View view = mock(View.class);
    reportListFragment.programCode = Program.TARV_CODE;
    RnRFormViewModel viewModel = generateRnRFormViewModel(Program.TARV_CODE, RnRFormViewModel.TYPE_SYNCED_HISTORICAL);
    viewModel.setId(999L);

    // when
    reportListFragment.rnRFormItemClickListener.clickBtnView(viewModel, view);

    // then
    Intent nextStartedIntent = ShadowApplication.getInstance().getNextStartedActivity();
    assertNotNull(nextStartedIntent);
    assertEquals(MMIARequisitionActivity.class.getName(), nextStartedIntent.getComponent().getClassName());
    assertEquals(999L, nextStartedIntent.getLongExtra(Constants.PARAM_FORM_ID, 0));
  }

  @Test
  public void shouldStartVIAHistoryWhenBtnClickedWithTypeHistory() throws Exception {
    // given
    View view = mock(View.class);
    RnRFormViewModel viewModel = generateRnRFormViewModel(Program.VIA_CODE, RnRFormViewModel.TYPE_SYNCED_HISTORICAL);
    viewModel.setId(999L);

    // when
    reportListFragment.rnRFormItemClickListener.clickBtnView(viewModel, view);

    // then
    Intent nextStartedIntent = ShadowApplication.getInstance().getNextStartedActivity();
    assertNotNull(nextStartedIntent);
    assertEquals(nextStartedIntent.getComponent().getClassName(), VIARequisitionActivity.class.getName());
    assertEquals(999L, nextStartedIntent.getLongExtra(Constants.PARAM_FORM_ID, 0));
  }

  @Test
  public void shouldStartMMIAEditPageWhenBtnClickedWithTypeUnauthorized() throws Exception {
    // given
    View view = mock(View.class);
    reportListFragment.programCode = Program.TARV_CODE;
    RnRFormViewModel viewModel = generateRnRFormViewModel(Program.TARV_CODE, RnRFormViewModel.TYPE_DRAFT);

    // when
    reportListFragment.rnRFormItemClickListener.clickBtnView(viewModel, view);

    // then
    Intent nextStartedIntent = ShadowApplication.getInstance().getNextStartedActivity();
    assertNotNull(nextStartedIntent);
    assertEquals(nextStartedIntent.getComponent().getClassName(), MMIARequisitionActivity.class.getName());
    assertEquals(0L, nextStartedIntent.getLongExtra(Constants.PARAM_FORM_ID, 0));
  }

  @Test
  public void shouldNotLoadSameFormIdAfterLoadedViaHistoryForm() {
    // given
    View view = mock(View.class);
    reportListFragment.programCode = Program.MALARIA_CODE;
    RnRFormViewModel historyViewModel = generateRnRFormViewModel(Program.TARV_CODE,
        RnRFormViewModel.TYPE_SYNCED_HISTORICAL);
    historyViewModel.setId(1L);

    // when
    reportListFragment.rnRFormItemClickListener.clickBtnView(historyViewModel, view);

    // then
    Intent startedIntentWhenIsHistory = ShadowApplication.getInstance().getNextStartedActivity();
    assertNotNull(startedIntentWhenIsHistory);
    assertEquals(startedIntentWhenIsHistory.getComponent().getClassName(), ALRequisitionActivity.class.getName());
    assertEquals(1L, startedIntentWhenIsHistory.getLongExtra(Constants.PARAM_FORM_ID, 0));

    // given
    RnRFormViewModel defaultViewModel = generateRnRFormViewModel(Program.VIA_CODE, RnRFormViewModel.TYPE_DRAFT);

    // when
    reportListFragment.rnRFormItemClickListener.clickBtnView(defaultViewModel, view);

    // then
    Intent startedIntentWhenIsDefault = ShadowApplication.getInstance().getNextStartedActivity();
    assertNotNull(startedIntentWhenIsDefault);
    assertEquals(startedIntentWhenIsDefault.getComponent().getClassName(), ALRequisitionActivity.class.getName());
    assertEquals(0L, startedIntentWhenIsDefault.getLongExtra(Constants.PARAM_FORM_ID, 0));
  }

  private Program generateProgram() {
    return Program.builder()
        .programCode(Program.VIA_CODE)
        .programName(Program.VIA_CODE)
        .isSupportEmergency(false)
        .build();
  }

  private RnRFormViewModel generateRnRFormViewModel(String programCode, int viewModelType) {
    return new RnRFormViewModel(new Period(new DateTime()), programCode, viewModelType);
  }
}