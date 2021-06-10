package org.openlmis.core.model.repository;

import android.content.Context;
import com.google.inject.Inject;
import java.util.List;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

public class BaseInfoItemRepository {

  GenericDao<BaseInfoItem> genericDao;

  @Inject
  DbUtil dbUtil;

  @Inject
  public BaseInfoItemRepository(Context context) {
    this.genericDao = new GenericDao<>(BaseInfoItem.class, context);
  }

  public void batchCreateOrUpdate(final List<BaseInfoItem> baseInfoItemList) throws LMISException {
    dbUtil.withDaoAsBatch(BaseInfoItem.class, (DbUtil.Operation<BaseInfoItem, Void>) dao -> {
      for (BaseInfoItem item : baseInfoItemList) {
        dao.createOrUpdate(item);
      }
      return null;
    });
  }

  public void batchDelete(final List<BaseInfoItem> baseInfoItemListWrapper) throws LMISException {
    dbUtil.withDaoAsBatch(BaseInfoItem.class, (DbUtil.Operation<BaseInfoItem, Void>) dao -> {
      for (BaseInfoItem item : baseInfoItemListWrapper) {
        dao.delete(item);
      }
      return null;
    });
  }
}
