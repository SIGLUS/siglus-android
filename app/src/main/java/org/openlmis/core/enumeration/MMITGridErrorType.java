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

package org.openlmis.core.enumeration;

import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;

public enum MMITGridErrorType {
  EMPTY_CONSUMPTION(true, LMISApp.getInstance().getString(R.string.error_rapid_test_consumption)),
  EMPTY_POSITIVE(true, LMISApp.getInstance().getString(R.string.error_rapid_test_positive)),
  EMPTY_UNJUSTIFIED(true, LMISApp.getInstance().getString(R.string.error_rapid_test_unjustified)),
  POSITIVE_MORE_THAN_CONSUMPTION(true,
      LMISApp.getInstance().getString(R.string.error_positive_larger_than_consumption)),
  APE_ALL_EMPTY(true, LMISApp.getInstance().getString(R.string.error_rapid_test_ape)),
  NO_ERROR(false, StringUtils.EMPTY);

  @Getter
  private final boolean isError;
  @Getter
  private final String errorString;

  MMITGridErrorType(boolean isError, String errorString) {
    this.isError = isError;
    this.errorString = errorString;
  }
}
