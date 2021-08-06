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
import static org.mockito.Mockito.times;
import static org.robolectric.Shadows.shadowOf;

import com.google.inject.AbstractModule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.presenter.BulkIssuePresenter;
import org.openlmis.core.view.adapter.BulkIssueAdapter;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowToast;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class BulkIssueActivityTest {

  private BulkIssueActivity bulkIssueActivity;
  private BulkIssuePresenter mockedPresenter;
  private BulkIssueAdapter mockAdapter;
  private ActivityController<BulkIssueActivity> activityController;

  @Before
  public void setUp() throws Exception {
    mockedPresenter = mock(BulkIssuePresenter.class);
    mockAdapter = mock(BulkIssueAdapter.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(BulkIssuePresenter.class).toInstance(mockedPresenter);
      }
    });
    activityController = Robolectric.buildActivity(BulkIssueActivity.class);
    bulkIssueActivity = activityController.create().start().resume().get();
    bulkIssueActivity.bulkIssueAdapter = mockAdapter;
  }

  @After
  public void teardown() {
    activityController.pause().stop().destroy();
    RoboGuice.Util.reset();
  }

  @Test
  public void shouldAdapterUpdateAfterRefreshUI(){
    // when
    bulkIssueActivity.onRefreshViewModels();

    // then
    Mockito.verify(mockAdapter,times(1)).notifyDataSetChanged();
  }

  @Test
  public void shouldFinishOnLoadViewModelsFailed(){
    // when
    bulkIssueActivity.onLoadViewModelsFailed(new NullPointerException());

    // then
    Assert.assertTrue(shadowOf(bulkIssueActivity).isFinishing());
  }

  @Test
  public void shouldCorrectToastOnSaveDraftFinished(){
    // when
    bulkIssueActivity.onSaveDraftFinished(true);

    // then
    Assert.assertEquals("Successfully Saved",ShadowToast.getTextOfLatestToast());

    // when
    bulkIssueActivity.onSaveDraftFinished(false);

    // then
    Assert.assertEquals("Unsuccessfully Saved",ShadowToast.getTextOfLatestToast());
  }
}