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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.inject.AbstractModule;
import java.util.ArrayList;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.builder.ProgramBuilder;
import org.openlmis.core.presenter.RequisitionPresenter;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class RequisitionActivityTest {

  private RequisitionActivity requisitionActivity;
  private RequisitionPresenter mockedPresenter;
  private ActivityController<RequisitionActivity> requisitionActivityActivityController;

  @Before
  public void setUp() throws Exception {
    mockedPresenter = mock(RequisitionPresenter.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(RequisitionPresenter.class).toInstance(mockedPresenter);
      }
    });
    requisitionActivityActivityController = Robolectric
        .buildActivity(RequisitionActivity.class);
    requisitionActivity = requisitionActivityActivityController.create().get();

    verify(mockedPresenter, times(1)).getSupportPrograms();
  }

  @After
  public void tearDown() {
    requisitionActivityActivityController.pause().stop().destroy();
    RoboGuice.Util.reset();
  }

  @Test
  public void shouldSetRequisitionTitle() {
    // then
    assertThat(requisitionActivity.getTitle())
        .isEqualTo(requisitionActivity.getResources().getString(R.string.home_label_requisition_and_report));
  }

  @Test
  public void shouldSetDataAfterLoad() {
    // given
    final ArrayList<Program> programs = new ArrayList<>();
    final Program program = new ProgramBuilder()
        .setProgramCode("123")
        .setProgramName("123")
        .build();
    programs.add(program);

    // when
    requisitionActivity.updateSupportProgram(programs);

    // then
    Assertions.assertThat(requisitionActivity.navigatorAdapter.getCount()).isEqualTo(programs.size());
    Assertions.assertThat(requisitionActivity.pageAdapter.getItemCount()).isEqualTo(programs.size());
  }

  @After
  public void teardown() {
    RoboGuice.Util.reset();
  }
}