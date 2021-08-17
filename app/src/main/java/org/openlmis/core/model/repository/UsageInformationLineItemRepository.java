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

import android.content.Context;
import com.google.inject.Inject;
import java.util.List;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.UsageInformationLineItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

public class UsageInformationLineItemRepository {

  GenericDao<UsageInformationLineItem> genericDao;

  @Inject
  DbUtil dbUtil;

  @Inject
  public UsageInformationLineItemRepository(Context context) {
    this.genericDao = new GenericDao<>(UsageInformationLineItem.class, context);
  }

  public void batchCreateOrUpdate(final List<UsageInformationLineItem> usageInformationLineItems)
      throws LMISException {
    dbUtil.withDaoAsBatch(UsageInformationLineItem.class, dao -> {
      for (UsageInformationLineItem item : usageInformationLineItems) {
        dao.createOrUpdate(item);
      }
      return null;
    });
  }

  public void batchDelete(final List<UsageInformationLineItem> usageInformationLineItemListWrapper)
      throws LMISException {
    dbUtil.withDaoAsBatch(UsageInformationLineItem.class, dao -> {
      for (UsageInformationLineItem item : usageInformationLineItemListWrapper) {
        dao.delete(item);
      }
      return null;
    });
  }

  public List<UsageInformationLineItem> list() throws LMISException {
    return genericDao.queryForAll();
  }
}
