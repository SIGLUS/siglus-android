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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyByte;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.core.utils.Constants.PARAM_IS_FROM_ARCHIVE;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.fragment.app.Fragment;
import com.google.inject.AbstractModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.presenter.StockMovementsPresenter;
import org.openlmis.core.utils.RobolectricUtils;
import org.openlmis.core.view.widget.LotInfoGroup;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowToast;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class StockMovementsWithLotActivityTest {

  private StockMovementsWithLotActivity movementWithLotActivity;
  private StockMovementsPresenter mockPresenter;
  private ActivityController<StockMovementsWithLotActivity> activityController;

  @Before
  public void setUp() throws Exception {
    mockPresenter = mock(StockMovementsPresenter.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(StockMovementsPresenter.class).toInstance(mockPresenter);
      }
    });
    when(mockPresenter.getStockCard()).thenReturn(StockCardBuilder.buildStockCard());
    activityController = Robolectric.buildActivity(StockMovementsWithLotActivity.class);
    movementWithLotActivity = activityController.create().start().resume().get();
  }

  @After
  public void teardown() {
    activityController.pause().stop().destroy();
    RoboGuice.Util.reset();
  }

  @Test
  public void testUpdateArchiveMenus() {
    // given
    ActivityController<StockMovementsWithLotActivity> controller = Robolectric
        .buildActivity(StockMovementsWithLotActivity.class);
    StockMovementsWithLotActivity activity = controller.create().visible().get();
    when(mockPresenter.isKitChildrenProduct(anyByte())).thenReturn(false);

    // when
    activity.updateArchiveMenus(true);
    Menu optionsMenu = shadowOf(activity).getOptionsMenu();
    RobolectricUtils.waitLooperIdle();
    MenuItem archiveItem = optionsMenu.findItem(R.id.action_archive);

    // then
    assertTrue(archiveItem.isVisible());
    controller.pause().stop().destroy();
  }

  @Test
  public void testUpdateUnpackKitMenu() {
    // when
    movementWithLotActivity.updateUnpackKitMenu(true);

    // then
    assertEquals(View.VISIBLE, movementWithLotActivity.unpackContainer.getVisibility());

    // when
    movementWithLotActivity.updateUnpackKitMenu(false);

    // then
    assertEquals(View.GONE, movementWithLotActivity.unpackContainer.getVisibility());
  }

  @Test
  public void shouldCorrectUpdateExpiryDate() {
    // given
    LotInfoGroup mockLotInfo = mock(LotInfoGroup.class);
    StockCard mockStockCard = mock(StockCard.class);
    when(mockPresenter.getStockCard()).thenReturn(mockStockCard);
    when(mockStockCard.calculateSOHFromLots()).thenReturn(0L);
    movementWithLotActivity.lotInfoGroup = mockLotInfo;

    // when
    movementWithLotActivity.updateExpiryDateViewGroup();

    // then
    verify(mockLotInfo, timeout(1)).setVisibility(View.INVISIBLE);
    verify(mockLotInfo, timeout(1)).initLotInfoGroup(any());
  }

  @Test
  public void shouldStartStockMovementHistoryAfterActionHistoryClicked() {
    // given
    MenuItem mockMenuItem = mock(MenuItem.class);
    when(mockMenuItem.getItemId()).thenReturn(R.id.action_history);

    // when
    movementWithLotActivity.onOptionsItemSelected(mockMenuItem);

    // then
    Intent intent = shadowOf(movementWithLotActivity).getNextStartedActivity();
    assertNotNull(intent);
    assertEquals(StockMovementHistoryActivity.class.getName(), intent.getComponent().getClassName());
    assertFalse(intent.getBooleanExtra(PARAM_IS_FROM_ARCHIVE, true));
  }

  @Test
  public void shouldArchiveStockCardAfterActionArchiveClicked() {
    // given
    MenuItem mockMenuItem = mock(MenuItem.class);
    when(mockMenuItem.getItemId()).thenReturn(R.id.action_archive);

    // when
    movementWithLotActivity.onOptionsItemSelected(mockMenuItem);

    // then
    verify(mockPresenter, times(1)).archiveStockCard();
    assertEquals(movementWithLotActivity.getString(R.string.msg_drug_archived),
        ShadowToast.getTextOfLatestToast());
  }

  @Test
  public void shouldUnpackKitAfterClickUnpackBtn() {
    // given
    View mockView = mock(View.class);
    when(mockView.getId()).thenReturn(R.id.btn_unpack);
    RobolectricUtils.resetNextClickTime();

    // when
    movementWithLotActivity.getSingleClickButtonListener().onClick(mockView);

    // then
    verify(mockPresenter, times(6)).getStockCard();
    Intent intent = shadowOf(movementWithLotActivity).getNextStartedActivity();
    assertNotNull(intent);
    assertEquals(SelectUnpackKitNumActivity.class.getName(), intent.getComponent().getClassName());
  }

  @Test
  public void shouldShowSelectDialogAfterNewMovementDialogClicked() {
    // given
    View mockView = mock(View.class);
    when(mockView.getId()).thenReturn(R.id.btn_new_movement);
    RobolectricUtils.resetNextClickTime();

    // when
    movementWithLotActivity.getSingleClickButtonListener().onClick(mockView);
    RobolectricUtils.waitLooperIdle();

    // then
    Fragment dialog = movementWithLotActivity.getSupportFragmentManager()
        .findFragmentByTag("new_movement_type_select__dialog");
    assertNotNull(dialog);
  }
}