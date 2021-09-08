package org.openlmis.core.model.repository;

import android.content.Context;
import com.google.inject.Inject;
import com.j256.ormlite.table.TableUtils;
import java.util.Collections;
import java.util.List;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.DraftIssueVoucherProductItem;
import org.openlmis.core.model.DraftIssueVoucherProductLotItem;
import org.openlmis.core.model.Pod;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;

public class IssueVoucherDraftRepository {

  private final GenericDao<DraftIssueVoucherProductItem> productItemGenericDao;

  private final GenericDao<DraftIssueVoucherProductLotItem> productLotItemGenericDao;

  @Inject
  private final DbUtil dbUtil;

  @Inject
  private PodRepository podRepository;

  @Inject
  public IssueVoucherDraftRepository(Context context, DbUtil dbUtil) {
    this.productItemGenericDao = new GenericDao<>(DraftIssueVoucherProductItem.class ,context);
    this.productLotItemGenericDao = new GenericDao<>(DraftIssueVoucherProductLotItem.class, context);
    this.dbUtil = dbUtil;
  }

  public void saveDraft(final List<DraftIssueVoucherProductItem> productItems, Pod pod) throws LMISException {
    if (productItems.isEmpty()) {
      return;
    }
    dbUtil.withDaoAsBatch(DraftIssueVoucherProductItem.class, dao -> {
      deleteDraft();
      for (DraftIssueVoucherProductItem productItem : productItems) {
        podRepository.batchCreatePodsWithItems(Collections.singletonList(pod));
        productItemGenericDao.createOrUpdate(productItem);
        for (DraftIssueVoucherProductLotItem productLotItem : productItem.getDraftLotItemListWrapper()) {
          productLotItemGenericDao.createOrUpdate(productLotItem);
        }
      }
      return null;
    });
  }

  public List<DraftIssueVoucherProductItem> listAll() throws LMISException {
    return productItemGenericDao.queryForAll();
  }

  public List<DraftIssueVoucherProductItem> queryByPodId(long podId) throws LMISException {
    return dbUtil.withDao(DraftIssueVoucherProductItem.class, dao ->
        dao.queryBuilder().where().eq("pod_id", podId).query());
  }

  public boolean hasDraft(long podId) throws LMISException {
    DraftIssueVoucherProductItem productItem = dbUtil.withDao(DraftIssueVoucherProductItem.class, dao ->
        dao.queryBuilder()
            .where()
            .eq("pod_id", podId)
            .queryForFirst());
    return productItem != null;
  }

  public void deleteDraft() throws LMISException {
    dbUtil.withDaoAsBatch(DraftIssueVoucherProductItem.class, dao -> {
      TableUtils.clearTable(LmisSqliteOpenHelper.getInstance(LMISApp.getContext())
          .getConnectionSource(), DraftIssueVoucherProductItem.class);
      TableUtils.clearTable(LmisSqliteOpenHelper.getInstance(LMISApp.getContext())
          .getConnectionSource(), DraftIssueVoucherProductLotItem.class);
      return null;
    });
  }


}
