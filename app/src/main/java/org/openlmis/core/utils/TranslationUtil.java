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

package org.openlmis.core.utils;

import java.util.List;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.model.ReportTypeForm;

public final class TranslationUtil {

  private TranslationUtil() {

  }

  public static void translateReportName(List<ReportTypeForm> reportTypeForms) {
    for (ReportTypeForm reportTypeForm : reportTypeForms) {
      if (Constants.VIA_PROGRAM_CODE.equals(reportTypeForm.getCode())) {
        reportTypeForm.setName(LMISApp.getContext().getString(R.string.requisition_tab));
      }
      if (Constants.AL_PROGRAM_CODE.equals(reportTypeForm.getCode())) {
        reportTypeForm.setName(LMISApp.getContext().getString(R.string.Malaria_tab));
      }
    }
  }

}
