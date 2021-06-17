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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.repository.ProgramRepository;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;
import rx.observers.TestSubscriber;

@RunWith(LMISTestRunner.class)
public class RequisitionPresenterTest {

  RequisitionPresenter.RequisitionView view;
  RequisitionPresenter presenter;
  ProgramRepository mockProgramRepository;

  @Before
  public void setUp() throws Exception {
    view = Mockito.mock(RequisitionPresenter.RequisitionView.class);
    presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(RequisitionPresenter.class);
    mockProgramRepository = Mockito.mock(ProgramRepository.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application,
        binder -> binder.bind(ProgramRepository.class).toInstance(mockProgramRepository));
    presenter.attachView(view);
  }

  @Test
  public void getSupportProgram() throws LMISException {
    // given
    final ArrayList<Program> givenActivePrograms = new ArrayList<>();
    Mockito.when(mockProgramRepository.queryActiveProgram()).thenReturn(givenActivePrograms);
    final TestSubscriber<List<Program>> testSubscriber = new TestSubscriber<>(
        presenter.getSupportProgramsSubscriber);
    presenter.getSupportProgramsSubscriber = testSubscriber;

    // when
    presenter.getSupportPrograms();
    testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS);

    // then
    Mockito.verify(view, Mockito.times(1)).updateSupportProgram(givenActivePrograms);
  }
}