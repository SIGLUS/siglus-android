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

package org.openlmis.core.model.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.model.builder.ReportTypeFormBuilder;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class ReportTypeFormRepositoryTest {

  private ReportTypeFormRepository repository;

  @Before
  public void setup() {
    repository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ReportTypeFormRepository.class);
  }

  @Test
  public void shouldQueryCorrectAfterCreate() throws LMISException {
    // given
    generateReportTypeForms();

    // when
    final ReportTypeForm reportTypeForm = repository.queryByCode(Program.VIA_CODE);

    // then
    Assert.assertNotNull(reportTypeForm);
  }

  @Test
  public void testListAll() throws LMISException {
    // given
    generateReportTypeForms();

    // when
    final List<ReportTypeForm> queryReportTypeForms = repository.listAll();

    // then
    Assert.assertEquals(2, queryReportTypeForms.size());
  }

  @Test
  public void testListAllWithActive() throws LMISException {
    // given
    generateReportTypeForms();

    // when
    final List<ReportTypeForm> queryReportTypeForms = repository.listAllWithActive();

    // then
    Assert.assertEquals(1, queryReportTypeForms.size());
  }

  private void generateReportTypeForms() throws LMISException {
    final ArrayList<ReportTypeForm> reportTypeForms = new ArrayList<>();
    final ReportTypeForm reportTypeForm1 = new ReportTypeFormBuilder()
        .setCode(Program.VIA_CODE)
        .setName(Program.VIA_CODE)
        .setLastReportEndTime("2021-01-01")
        .setActive(true)
        .setStartTime(new Date(LMISTestApp.getInstance().getCurrentTimeMillis()))
        .build();
    final ReportTypeForm reportTypeForm2 = new ReportTypeFormBuilder()
        .setCode(Program.MALARIA_CODE)
        .setName(Program.MALARIA_CODE)
        .setLastReportEndTime("2021-01-01")
        .setActive(false)
        .setStartTime(new Date(LMISTestApp.getInstance().getCurrentTimeMillis()))
        .build();
    reportTypeForms.add(reportTypeForm1);
    reportTypeForms.add(reportTypeForm2);
    repository.batchCreateOrUpdateReportTypes(reportTypeForms);
  }
}