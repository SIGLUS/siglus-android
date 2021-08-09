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

import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import androidx.fragment.app.Fragment;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.manager.MovementReasonManager.MovementReason;
import org.openlmis.core.utils.RobolectricUtils;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class BulkIssueChooseDestinationActivityTest {

  private BulkIssueChooseDestinationActivity bulkIssueChooseDestinationActivity;
  private ActivityController<BulkIssueChooseDestinationActivity> controller;

  @Before
  public void setUp() throws Exception {
    controller = Robolectric.buildActivity(BulkIssueChooseDestinationActivity.class);
    bulkIssueChooseDestinationActivity = controller.create().start().resume().get();
  }

  @After
  public void teardown() {
    controller.pause().stop().destroy();
    RoboGuice.Util.reset();
  }

  @Test
  public void shouldClickListenerCorrectSet() {
    // given
    boolean chooseDestinationHasListener = bulkIssueChooseDestinationActivity.getEtDestination().hasOnClickListeners();
    boolean nextHasListener = bulkIssueChooseDestinationActivity.getBtNext().hasOnClickListeners();

    // then
    Assert.assertTrue(chooseDestinationHasListener);
    Assert.assertTrue(nextHasListener);
  }

  @Test
  public void shouldGotoAddProductActivityAfterValidate() {
    // given
    RobolectricUtils.resetNextClickTime();
    bulkIssueChooseDestinationActivity.setChosenReason(Mockito.mock(MovementReason.class));

    // when
    bulkIssueChooseDestinationActivity.findViewById(R.id.bt_next).performClick();
    RobolectricUtils.waitLooperIdle();
    Intent shadowIntent = shadowOf(bulkIssueChooseDestinationActivity).getNextStartedActivity();

    // then
    Assert.assertEquals(AddProductsToBulkEntriesActivity.class.getName(), shadowIntent.getComponent().getClassName());
  }

  @Test
  public void shouldShowDialogFragmentAfterClickDestination() {
    // when
    RobolectricUtils.resetNextClickTime();
    bulkIssueChooseDestinationActivity.getEtDestination().performClick();
    RobolectricUtils.waitLooperIdle();

    // then
    Fragment fragment = bulkIssueChooseDestinationActivity.getSupportFragmentManager()
        .findFragmentByTag("SELECT_REASONS");
    Assert.assertNotNull(fragment);
  }
}