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

import com.google.inject.AbstractModule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.repository.BulkIssueRepository;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class StockCardListPresenterTest {

  StockCardListPresenter presenter;

  BulkIssueRepository repository;

  @Before
  public void setUp() throws Exception {
    repository = Mockito.mock(BulkIssueRepository.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(BulkIssueRepository.class).toInstance(repository);
      }
    });
    presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(StockCardListPresenter.class);
  }

  @Test
  public void testHasDraft() throws LMISException {
    // given
    Mockito.when(repository.hasDraft()).thenReturn(true);

    // then
    Assert.assertTrue(presenter.hasBulkIssueDraft());
  }

  @Test
  public void shouldReturnFalseWhenThrowException() throws LMISException {
    // given
    Mockito.when(repository.hasDraft()).thenThrow(new NullPointerException());

    // then
    Assert.assertFalse(presenter.hasBulkIssueDraft());
  }
}