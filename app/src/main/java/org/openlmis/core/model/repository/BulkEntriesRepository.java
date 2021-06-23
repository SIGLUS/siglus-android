package org.openlmis.core.model.repository;

import android.content.Context;
import com.google.inject.Inject;
import com.j256.ormlite.table.TableUtils;
import java.util.List;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.DraftBulkEntriesProduct;
import org.openlmis.core.model.DraftBulkEntriesProductLotItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;

public class BulkEntriesRepository {

  GenericDao<DraftBulkEntriesProduct> productGenericDao;
  GenericDao<DraftBulkEntriesProductLotItem> productLotItemGenericDao;

  @Inject
  DbUtil dbUtil;

  @Inject
  public BulkEntriesRepository(Context context) {
    productGenericDao = new GenericDao<>(DraftBulkEntriesProduct.class, context);
    productLotItemGenericDao = new GenericDao<>(DraftBulkEntriesProductLotItem.class, context);
  }

  public void createBulkEntriesProductDraft(final DraftBulkEntriesProduct draftBulkEntriesProduct)
      throws LMISException {
    dbUtil.withDaoAsBatch(DraftBulkEntriesProduct.class, dao -> {
      productGenericDao.createOrUpdate(draftBulkEntriesProduct);
      for (DraftBulkEntriesProductLotItem draftBulkEntriesProductLotItem : draftBulkEntriesProduct
          .getDraftLotItemListWrapper()) {
        productLotItemGenericDao.createOrUpdate(draftBulkEntriesProductLotItem);
      }
      return null;
    });
  }

  public List<DraftBulkEntriesProduct> queryAllBulkEntriesDraft() throws LMISException {
    return productGenericDao.queryForAll();
  }

  public void clearBulkEntriesDraft() throws LMISException {
    dbUtil.withDaoAsBatch(DraftBulkEntriesProduct.class, dao -> {
      TableUtils.clearTable(LmisSqliteOpenHelper.getInstance(LMISApp.getContext())
          .getConnectionSource(), DraftBulkEntriesProduct.class);
      TableUtils.clearTable(LmisSqliteOpenHelper.getInstance(LMISApp.getContext())
          .getConnectionSource(), DraftBulkEntriesProductLotItem.class);
      return null;
    });

  }

}
