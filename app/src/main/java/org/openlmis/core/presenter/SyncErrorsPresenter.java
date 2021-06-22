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

import com.google.inject.Inject;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.SyncType;
import org.openlmis.core.model.repository.SyncErrorsRepository;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.widget.SyncDateBottomSheet;

@SuppressWarnings("PMD")
public class SyncErrorsPresenter extends Presenter {

  private SyncDateBottomSheet view;

  @Inject
  SyncErrorsRepository repository;

  @Override
  public void attachView(BaseView v) throws ViewNotMatchException {
    this.view = (SyncDateBottomSheet) v;
  }

  public boolean hasRnrSyncError() {
    return hasSyncError(SyncType.RNR_FORM);
  }

  public boolean hasStockCardSyncError() {
    return hasSyncError(SyncType.STOCK_CARDS);
  }

  private boolean hasSyncError(SyncType syncType) {
    try {
      return repository.hasSyncErrorOf(syncType);
    } catch (LMISException e) {
      new LMISException(e, "SyncErrorsPresenter.hasSyncError").reportToFabric();
      return false;
    }
  }
}
