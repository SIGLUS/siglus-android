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

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

import android.content.Context;
import com.google.inject.Inject;
import java.util.List;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.TestConsumptionLineItem;
import org.openlmis.core.model.UsageColumnsMap;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

public class TestConsumptionLineItemRepository {

  GenericDao<TestConsumptionLineItem> genericDao;

  @Inject
  DbUtil dbUtil;

  @Inject
  UsageColumnsMapRepository usageColumnsMapRepository;

  @Inject
  public TestConsumptionLineItemRepository(Context context) {
    this.genericDao = new GenericDao<>(TestConsumptionLineItem.class, context);
  }

  public void batchCreateOrUpdate(final List<TestConsumptionLineItem> testConsumptionLineItems)
      throws LMISException {
    List<UsageColumnsMap> usageColumnsMaps = usageColumnsMapRepository.list();
    dbUtil.withDaoAsBatch(TestConsumptionLineItem.class, (DbUtil.Operation<TestConsumptionLineItem, Void>) dao -> {
      for (TestConsumptionLineItem item : testConsumptionLineItems) {
        setUsageColumnsMap(item, usageColumnsMaps);
        dao.createOrUpdate(item);
      }
      return null;
    });
  }

  public void batchDelete(final List<TestConsumptionLineItem> testConsumptionLineItemListWrapper)
      throws LMISException {
    dbUtil.withDaoAsBatch(TestConsumptionLineItem.class, (DbUtil.Operation<TestConsumptionLineItem, Void>) dao -> {
      for (TestConsumptionLineItem item : testConsumptionLineItemListWrapper) {
        dao.delete(item);
      }
      return null;
    });
  }

  public List<TestConsumptionLineItem> list() throws LMISException {
    return genericDao.queryForAll();
  }

  private void setUsageColumnsMap(TestConsumptionLineItem lineItem, List<UsageColumnsMap> usageColumnsMaps) {
    List<UsageColumnsMap> usageColumnsMapList = from(usageColumnsMaps).filter(usageColumnsMap ->
        usageColumnsMap.getCode().equals(lineItem.getUsageColumnsMap().getCode()))
        .toList();
    if (!usageColumnsMapList.isEmpty()) {
      lineItem.setUsageColumnsMap(usageColumnsMapList.get(0));
    }
  }

}
