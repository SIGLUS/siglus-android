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

import org.openlmis.core.googleanalytics.ScreenName;

public class IssueVoucherActivity extends BaseActivity {

  public static final String ORDER_NUMBER = "ORDER_NUMBER";

  public static final String PROGRAM_CODE = "PROGRAM_CODE";

  public static final String MOVEMENT_REASON_CODE = "MOVEMENT_REASON";

  @Override
  protected ScreenName getScreenName() {
    return null;
  }
}
