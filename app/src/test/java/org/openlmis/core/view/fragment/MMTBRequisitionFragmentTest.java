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
import static org.mockito.Mockito.mock;

import com.google.inject.AbstractModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.R;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.presenter.MMTBRequisitionPresenter;
import org.openlmis.core.view.activity.MMTBRequisitionActivity;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class MMTBRequisitionFragmentTest {

  private MMTBRequisitionPresenter mmtbFormPresenter;
  private MMTBRequisitionFragment mmtbRequisitionFragment;

  @Before
  public void setUp() throws Exception {
    mmtbFormPresenter = mock(MMTBRequisitionPresenter.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(MMTBRequisitionPresenter.class).toInstance(mmtbFormPresenter);
      }
    });
    SharedPreferenceMgr.getInstance().setShouldSyncLastYearStockCardData(false);
    mmtbRequisitionFragment = getFragment();
  }

  private MMTBRequisitionFragment getFragment() {
    MMTBRequisitionActivity mmtbRequisitionActivity = Robolectric
        .buildActivity(MMTBRequisitionActivity.class)
        .create()
        .start()
        .resume()
        .get();
    return (MMTBRequisitionFragment) mmtbRequisitionActivity
        .getSupportFragmentManager()
        .findFragmentById(R.id.fragment_requisition);
  }

  @Test
  public void shouldShowRequisitionPeriodOnTitle() {
    assertEquals("MMIA - %1$s to %2$s", mmtbRequisitionFragment.requireActivity().getTitle());
  }
}
