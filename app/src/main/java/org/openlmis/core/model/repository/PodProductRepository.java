package org.openlmis.core.model.repository;

import android.content.Context;
import androidx.annotation.Nullable;
import com.google.inject.Inject;
import com.j256.ormlite.misc.TransactionManager;
import java.sql.SQLException;
import java.util.List;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.PodProduct;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.openlmis.core.utils.DateUtil;

public class PodProductRepository {

  private final GenericDao<PodProduct> podProductGenericDao;

  @Inject
  private final DbUtil dbUtil;

  private final Context context;

  @Inject
  PodLotItemRepository podLotItemRepository;

  @Inject
  public PodProductRepository(DbUtil dbUtil, Context context) {
    this.podProductGenericDao = new GenericDao<>(PodProduct.class, context);
    this.dbUtil = dbUtil;
    this.context = context;
  }

  public void batchCreatePodProductsWithItems(@Nullable final List<PodProduct> podProducts, Pod pod)
      throws LMISException {
    if (podProducts == null) {
      return;
    }
    try {
      TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), () -> {
        for (PodProduct podProduct : podProducts) {
          podProduct.setPod(pod);
          podProduct.setCreatedAt(DateUtil.getCurrentDate());
          podProduct.setUpdatedAt(DateUtil.getCurrentDate());
          createOrUpdateWithItems(podProduct);
        }
        return null;
      });
    } catch (SQLException e) {
      throw new LMISException(e);
    }
  }

  public PodProduct queryByPodIdAndProductCode(long podId, String productCode) throws LMISException {
    return dbUtil.withDao(PodProduct.class,
        dao -> dao.queryBuilder().where().eq("pod_id", podId).and().eq("code", productCode)
            .queryForFirst());
  }

  private void createOrUpdateWithItems(final PodProduct podProduct) throws LMISException {
    try {
      TransactionManager
          .callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(),
              () -> {
                PodProduct savedPodProduct = createOrUpdate(podProduct);
                podLotItemRepository
                    .batchCreatePodLotItemsWithLotInfo(podProduct.getPodLotItemsWrapper(), savedPodProduct);
                return null;
              });
    } catch (SQLException e) {
      throw new LMISException(e);
    }
  }


  private PodProduct createOrUpdate(PodProduct podProduct) throws LMISException {
    PodProduct existingPodProduct = queryByPodIdAndProductCode(podProduct.getPod().getId(), podProduct.getCode());
    if (existingPodProduct != null) {
      podProduct.setId(existingPodProduct.getId());
    }
    return podProductGenericDao.createOrUpdate(podProduct);
  }

}
