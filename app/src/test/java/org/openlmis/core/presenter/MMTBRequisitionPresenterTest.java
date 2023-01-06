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

package org.openlmis.core.presenter;


import static org.mockito.Mockito.mock;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.presenter.MMTBRequisitionPresenter.MMTBRequisitionView;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;
import rx.Observable;

@RunWith(LMISTestRunner.class)
public class MMTBRequisitionPresenterTest {

  private MMTBRequisitionPresenter presenter;
  private MMTBRequisitionPresenter.MMTBRequisitionView mockMMTBFormView;

  @Before
  public void setup() throws Exception {
    presenter = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(MMTBRequisitionPresenter.class);
    mockMMTBFormView = mock(MMTBRequisitionView.class);
    presenter.attachView(mockMMTBFormView);
  }

  @Test
  public void shouldLoadData() {
    presenter.loadData(10L, null);
    Assert.assertNotNull(presenter.subscriptions);
    Assert.assertEquals(1, presenter.subscriptions.size());
  }

  @Test
  public void shouldGetSaveFormObservable() {
    Observable<Void> saveFormObservable = presenter.getSaveFormObservable();
    Assert.assertNotNull(saveFormObservable);
  }

  @Test
  public void shouldGetRnrFormObservable() {
    Observable<RnRForm> rnrFormObservable = presenter.getRnrFormObservable(100L);
    Assert.assertNotNull(rnrFormObservable);
  }

}
