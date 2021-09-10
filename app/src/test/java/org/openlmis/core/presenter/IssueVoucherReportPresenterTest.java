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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.builder.PodBuilder;
import org.openlmis.core.model.repository.PodRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.presenter.IssueVoucherReportPresenter.IssueVoucherView;
import org.openlmis.core.utils.Constants;

@RunWith(LMISTestRunner.class)
public class IssueVoucherReportPresenterTest {
  @Mock
  PodRepository podRepository;

  @Mock
  ProgramRepository programRepository;

  @Mock
  IssueVoucherView issueVoucherView;

  @InjectMocks
  IssueVoucherReportPresenter presenter;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void shouldCorrectLoadViewModel() throws Exception {
    // given
    Program program = buildProgram();
    when(programRepository.queryByCode(any())).thenReturn(program);

    // when
    presenter.loadViewModelByPod(PodBuilder.generatePod(), false);

    //then
    assertEquals(program.getProgramName(), presenter.getIssueVoucherReportViewModel().getProgram().getProgramName());
    assertEquals(1, presenter.getIssueVoucherReportViewModel().getProductViewModels().size());
    verify(issueVoucherView, times(1)).loaded();
  }

  @NotNull
  private Program buildProgram() {
    String programName = "Program Name";
    Program program = new Program();
    program.setId(123);
    program.setProgramCode(Constants.MMIA_PROGRAM_CODE);
    program.setProgramName(programName);
    return program;
  }

}
