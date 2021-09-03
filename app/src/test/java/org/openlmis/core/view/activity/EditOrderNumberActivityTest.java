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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;
import static org.robolectric.Shadows.shadowOf;

import com.google.android.material.textfield.TextInputLayout;
import com.google.inject.AbstractModule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.presenter.EditOrderNumberPresenter;
import org.openlmis.core.utils.RobolectricUtils;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowToast;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class EditOrderNumberActivityTest {

  private EditOrderNumberActivity editOrderNumberActivity;

  private EditOrderNumberPresenter mockedPresenter;

  private ActivityController<EditOrderNumberActivity> activityController;

  @Before
  public void setUp() {
    mockedPresenter = mock(EditOrderNumberPresenter.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(EditOrderNumberPresenter.class).toInstance(mockedPresenter);
      }
    });
    activityController = Robolectric.buildActivity(EditOrderNumberActivity.class);
    editOrderNumberActivity = activityController.create().get();
  }

  @After
  public void teardown() {
    activityController.pause().stop().destroy();
    RoboGuice.Util.reset();
  }

  @Test
  public void shouldShowOrderNumberListAfterClickOrderNumber() {
    // given
    when(mockedPresenter.getOrderNumbers()).thenReturn(newArrayList("order1", "order2"));
    RobolectricUtils.resetNextClickTime();

    // when
    editOrderNumberActivity.findViewById(R.id.et_new_order_number).performClick();
    RobolectricUtils.waitLooperIdle();

    // then
    Assert.assertNotNull(
        editOrderNumberActivity.getSupportFragmentManager().findFragmentByTag("select_new_order_number"));
  }

  @Test
  public void shouldShowErrorWhenNotChooseOrderNumberAfterClickConfirm() {
    // given
    RobolectricUtils.resetNextClickTime();

    // when
    editOrderNumberActivity.findViewById(R.id.tv_confirm).performClick();
    RobolectricUtils.waitLooperIdle();

    // then
    CharSequence error = ((TextInputLayout) editOrderNumberActivity.findViewById(R.id.til_new_order_number)).getError();
    Assert.assertEquals(LMISTestApp.getContext().getString(R.string.msg_new_order_number_error), error);
  }

  @Test
  public void shouldShowConfirmDialogWhenChosenOrderNumberAfterClickConfirm() {
    // given
    RobolectricUtils.resetNextClickTime();
    editOrderNumberActivity.setNewOrderNumber("test");

    // when
    editOrderNumberActivity.findViewById(R.id.tv_confirm).performClick();
    RobolectricUtils.waitLooperIdle();

    // then
    Assert.assertNotNull(
        editOrderNumberActivity.getSupportFragmentManager().findFragmentByTag("edit_order_number_confirm_dialog"));
  }

  @Test
  public void testGetScreenName() {
    // then
    Assert.assertEquals(ScreenName.EDIT_ORDER_NUMBER_SCREEN, editOrderNumberActivity.getScreenName());
  }

  @Test
  public void shouldShowToastAndFinishAfterLoadDataFailed() {
    // when
    editOrderNumberActivity.loadDataFailed();

    // then
    Assert.assertEquals("Load Order List Failed", ShadowToast.getTextOfLatestToast());
    Assert.assertTrue(shadowOf(editOrderNumberActivity).isFinishing());
  }

  @Test
  public void shouldShowToastAndFinishAfterUpdateOrderNumberFailed() {
    // when
    editOrderNumberActivity.updateOrderNumberFailed();

    // then
    Assert.assertEquals("Change Order Number Failed", ShadowToast.getTextOfLatestToast());
    Assert.assertTrue(shadowOf(editOrderNumberActivity).isFinishing());
  }

  @Test
  public void shouldFinishAfterUpdateOrderNumberSuccess(){
    // when
    editOrderNumberActivity.updateOrderNumberSuccess();

    // then
    Assert.assertTrue(shadowOf(editOrderNumberActivity).isFinishing());
  }
}