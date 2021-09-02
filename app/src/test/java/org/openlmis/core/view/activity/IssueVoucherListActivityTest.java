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
import static org.openlmis.core.view.activity.IssueVoucherListActivity.TITLE_RES;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;
import android.view.MenuItem;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.google.inject.AbstractModule;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.constant.IntentConstants;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.presenter.IssueVoucherListPresenter;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.fakes.RoboMenu;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadows.ShadowActivity;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class IssueVoucherListActivityTest {

  private IssueVoucherListActivity listActivity;
  private IssueVoucherListPresenter mockedPresenter;
  private ActivityController<IssueVoucherListActivity> activityController;

  @Before
  public void setUp() {
    mockedPresenter = mock(IssueVoucherListPresenter.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(IssueVoucherListPresenter.class).toInstance(mockedPresenter);
      }
    });
    activityController = Robolectric.buildActivity(IssueVoucherListActivity.class);
    listActivity = activityController.create().get();
  }

  @After
  public void teardown() {
    activityController.pause().stop().destroy();
    RoboGuice.Util.reset();
  }

  @Test
  public void shouldCorrectSetScreenName() {
    // when
    ScreenName screenName = listActivity.getScreenName();

    //then
    Assert.assertEquals(ScreenName.ISSUE_VOUCHER_AND_POD, screenName);
  }

  @Test
  public void shouldCorrectCreateOptionsMenu() {
    // given
    RoboMenu roboMenu = new RoboMenu();

    // when
    listActivity.onCreateOptionsMenu(roboMenu);

    // then
    Assert.assertEquals(LMISTestApp.getContext().getString(R.string.label_create_issue_voucher_draft),
        roboMenu.getItem(0).getTitle());
  }

  @Test
  public void shouldGotoIssueVoucherInputOrderNumberActivity() {
    // given
    MenuItem menuItem = new RoboMenuItem(R.id.action_create_issue_voucher);

    // when
    listActivity.onOptionsItemSelected(menuItem);
    ShadowActivity shadowActivity = shadowOf(listActivity);
    Intent startedIntent = shadowActivity.getNextStartedActivity();

    // then
    Assert.assertEquals(IssueVoucherInputOrderNumberActivity.class.getName(),
        startedIntent.getComponent().getClassName());
  }

  @Test
  public void shouldCorrectSetAdapterCount() {
    // when
    int viewpagerCount = listActivity.getViewPager().getAdapter().getItemCount();
    int NavigatorCount = ((CommonNavigator) listActivity.getMagicIndicator().getNavigator()).getAdapter().getCount();

    // then
    Assert.assertEquals(2, viewpagerCount);
    Assert.assertEquals(2, NavigatorCount);
  }

  @Test
  public void shouldCorrectCreateFragment() {
    // given
    Fragment fragmentAtZero = ((FragmentStateAdapter) listActivity.getViewPager().getAdapter()).createFragment(0);
    Fragment fragmentAtOne = ((FragmentStateAdapter) listActivity.getViewPager().getAdapter()).createFragment(1);

    // then
    Assert.assertTrue(fragmentAtZero.getArguments().getBoolean(IntentConstants.PARAM_IS_ISSUE_VOUCHER));
    Assert.assertFalse(fragmentAtOne.getArguments().getBoolean(IntentConstants.PARAM_IS_ISSUE_VOUCHER));
  }

  @Test
  public void shouldCorrectCreateNavigator() {
    // given
    CommonNavigatorAdapter adapter = ((CommonNavigator) listActivity.getMagicIndicator().getNavigator()).getAdapter();

    // when
    IPagerIndicator indicator = adapter.getIndicator(LMISTestApp.getContext());
    ColorTransitionPagerTitleView titleAtZero = ((ColorTransitionPagerTitleView) adapter.getTitleView(LMISTestApp.getContext(), 0));
    ColorTransitionPagerTitleView titleAtOne = (ColorTransitionPagerTitleView) adapter.getTitleView(LMISTestApp.getContext(), 1);

    // then
    Assert.assertNotNull(indicator);
    Assert.assertEquals(LMISTestApp.getContext().getString(TITLE_RES[0]), titleAtZero.getText());
    Assert.assertEquals(LMISTestApp.getContext().getString(TITLE_RES[1]), titleAtOne.getText());
  }
}