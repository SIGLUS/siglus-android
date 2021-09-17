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
import com.j256.ormlite.stmt.DeleteBuilder;
import java.util.List;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.TestConsumptionItem;
import org.openlmis.core.model.UsageColumnsMap;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

public class TestConsumptionLineItemRepository {

  GenericDao<TestConsumptionItem> genericDao;

  @Inject
  DbUtil dbUtil;

  @Inject
  UsageColumnsMapRepository usageColumnsMapRepository;

  @Inject
  public TestConsumptionLineItemRepository(Context context) {
    this.genericDao = new GenericDao<>(TestConsumptionItem.class, context);
  }

  public void batchCreateOrUpdate(final List<TestConsumptionItem> testConsumptionLineItems, long formId)
      throws LMISException {
    deleteFormBasicItems(formId);
    List<UsageColumnsMap> usageColumnsMaps = usageColumnsMapRepository.list();
    dbUtil.withDaoAsBatch(TestConsumptionItem.class, (DbUtil.Operation<TestConsumptionItem, Void>) dao -> {
      for (TestConsumptionItem item : testConsumptionLineItems) {
        setUsageColumnsMap(item, usageColumnsMaps);
        dao.createOrUpdate(item);
      }
      return null;
    });
  }

  public void batchDelete(final List<TestConsumptionItem> testConsumptionLineItemListWrapper)
      throws LMISException {
    dbUtil.withDaoAsBatch(TestConsumptionItem.class, (DbUtil.Operation<TestConsumptionItem, Void>) dao -> {
      for (TestConsumptionItem item : testConsumptionLineItemListWrapper) {
        dao.delete(item);
      }
      return null;
    });
  }

  private void deleteFormBasicItems(final long formId) throws LMISException {
    dbUtil.withDao(TestConsumptionItem.class, (DbUtil.Operation<TestConsumptionItem, Void>) dao -> {
      DeleteBuilder<TestConsumptionItem, String> deleteBuilder = dao.deleteBuilder();
      deleteBuilder.where().eq("form_id", formId);
      deleteBuilder.delete();
      return null;
    });
  }

  public List<TestConsumptionItem> list() throws LMISException {
    return genericDao.queryForAll();
  }

  private void setUsageColumnsMap(TestConsumptionItem lineItem, List<UsageColumnsMap> usageColumnsMaps) {
    List<UsageColumnsMap> usageColumnsMapList = from(usageColumnsMaps).filter(usageColumnsMap ->
        usageColumnsMap.getCode().equals(lineItem.getUsageColumnsMap().getCode()))
        .toList();
    if (!usageColumnsMapList.isEmpty()) {
      lineItem.setUsageColumnsMap(usageColumnsMapList.get(0));
    }
  }

}
