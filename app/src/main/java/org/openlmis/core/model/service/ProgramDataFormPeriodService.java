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

package org.openlmis.core.model.service;

import com.google.inject.Inject;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.ProgramDataFormRepository;
import org.openlmis.core.model.repository.ReportTypeFormRepository;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.roboguice.shaded.goole.common.base.Optional;

public class ProgramDataFormPeriodService {

  @Inject
  private ReportTypeFormRepository reportTypeFormRepository;

  @Inject
  private ProgramDataFormRepository programDataFormRepository;

  @Inject
  private StockMovementRepository stockMovementRepository;

  @SuppressWarnings("squid:S3776")
  public Optional<Period> getFirstStandardPeriod() throws LMISException {
    DateTime initializeDateTime;
    Period period = null;
    DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    ReportTypeForm reportTypeForm = reportTypeFormRepository.queryByCode(Program.RAPID_TEST_CODE);
    if (reportTypeForm.lastReportEndTime != null) {
      DateTime lastReportEndTime = dateTimeFormatter.parseDateTime(reportTypeForm.lastReportEndTime);
      if (Months.monthsBetween(lastReportEndTime, new DateTime()).getMonths() > 12) {
        initializeDateTime = new DateTime().plusMonths(-12).toDateTime();
        period = new Period(initializeDateTime);
      } else {
        List<ProgramDataForm> forms = programDataFormRepository.listByProgramCode(Program.RAPID_TEST_CODE);
        if (forms != null && !forms.isEmpty()) {
          period = new Period(new DateTime(forms.get(0).getPeriodBegin()));
        } else {
          StockMovementItem firstStockMovement = stockMovementRepository.getFirstStockMovement();
          if (firstStockMovement != null) {
            period = firstStockMovement.getMovementPeriod();
          }
        }
      }
    } else {
      StockMovementItem firstStockMovement = stockMovementRepository.getFirstStockMovement();
      if (firstStockMovement != null) {
        period = firstStockMovement.getMovementPeriod();
      }
    }
    if (period != null && period.isOpenToRequisitions()) {
      return Optional.of(period);
    }
    return Optional.absent();
  }
}
