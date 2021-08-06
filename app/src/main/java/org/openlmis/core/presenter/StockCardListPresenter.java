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

import android.util.Log;
import com.google.inject.Inject;
import org.openlmis.core.model.repository.BulkIssueRepository;
import org.openlmis.core.view.BaseView;

public class StockCardListPresenter extends Presenter {

  @Inject
  private BulkIssueRepository bulkIssueRepository;

  @Override
  public void attachView(BaseView v) {
    // do nothing
  }

  public boolean hasBulkIssueDraft() {
    try {
      return bulkIssueRepository.hasDraft();
    } catch (Exception e) {
      Log.w("StockCardListPresenter", e);
      return false;
    }
  }
}
