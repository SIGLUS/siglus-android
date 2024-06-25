/*
 *
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
 *
 */

package org.openlmis.core.presenter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;

import android.app.Application;
import androidx.test.core.app.ApplicationProvider;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.model.repository.ReportTypeFormRepository;
import org.openlmis.core.model.service.RequisitionPeriodService;
import org.openlmis.core.presenter.ReportListPresenter.ReportListView;
import roboguice.RoboGuice;
import rx.observers.TestSubscriber;

@RunWith(LMISTestRunner.class)
public class ReportListPresenterTest {

  ReportListView view;
  ReportListPresenter presenter;
  ReportTypeFormRepository mockReportTypeFormRepository;
  RequisitionPeriodService mockRequisitionPeriodService;

  @Before
  public void setUp() throws Exception {
    view = Mockito.mock(ReportListView.class);
    Application application = ApplicationProvider.getApplicationContext();

    mockReportTypeFormRepository = Mockito.mock(ReportTypeFormRepository.class);
    mockRequisitionPeriodService = Mockito.mock(RequisitionPeriodService.class);
    RoboGuice.overrideApplicationInjector(application,
        binder -> {
          binder.bind(ReportTypeFormRepository.class).toInstance(mockReportTypeFormRepository);
          binder.bind(RequisitionPeriodService.class).toInstance(mockRequisitionPeriodService);
        });

    presenter = RoboGuice.getInjector(application).getInstance(ReportListPresenter.class);
    presenter.attachView(view);
  }

  @Test
  public void testGetSupportReportTypes() {
    // given
    final ArrayList<ReportTypeForm> givenActiveReportTypes = new ArrayList<>();
    Mockito.when(mockReportTypeFormRepository.listAllWithActive()).thenReturn(givenActiveReportTypes);
    final TestSubscriber<List<ReportTypeForm>> testSubscriber = new TestSubscriber<>(
        presenter.getSupportReportTypesSubscriber);
    presenter.getSupportReportTypesSubscriber = testSubscriber;

    // when
    presenter.getSupportReportTypes();
    testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS);

    // then
    Mockito.verify(view, Mockito.times(1)).updateSupportReportTypes(givenActiveReportTypes);
  }

  @Test
  public void shouldCallHasOverLimitWhenHasMoreThan2ViaProgramEmergencyRequisitionIsCalled()
      throws LMISException {
    // given
    boolean hasOverLimit = false;
    Mockito.when(
        mockRequisitionPeriodService.hasOverLimit(anyString(), anyInt(), any(Date.class))
    ).thenReturn(hasOverLimit);

    TestSubscriber<Boolean> testSubscriber = new TestSubscriber<>();
    // when
    presenter.hasMoreThan2ViaProgramEmergencyRequisition().subscribe(testSubscriber);
    testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS);
    // then
    Mockito.verify(mockRequisitionPeriodService)
        .hasOverLimit(eq(Program.VIA_CODE), eq(2), any(Date.class));

    List<Boolean> nextEvents = testSubscriber.getOnNextEvents();
    assertEquals(1, nextEvents.size());
    assertEquals(hasOverLimit, nextEvents.get(0));
  }
}