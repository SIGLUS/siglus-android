package org.openlmis.core.model.repository;

import android.content.Context;
import androidx.annotation.Nullable;
import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import java.sql.SQLException;
import java.util.List;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.PodLotItem;
import org.openlmis.core.model.PodProduct;
import org.openlmis.core.model.Product;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.openlmis.core.utils.DateUtil;

public class PodLotItemRepository {

  private final GenericDao<PodLotItem> podLotItemGenericDao;

  @Inject
  private final DbUtil dbUtil;

  private final Context context;

  @Inject
  LotRepository lotRepository;

  @Inject
  ProductRepository productRepository;

  @Inject
  public PodLotItemRepository(Context context, DbUtil dbUtil) {
    this.podLotItemGenericDao = new GenericDao<>(PodLotItem.class, context);
    this.dbUtil = dbUtil;
    this.context = context;
  }

  public PodLotItem queryByPodProductIdAndLotId(long podProductId, long lotId) throws LMISException {
    return dbUtil.withDao(PodLotItem.class,
        dao -> dao.queryBuilder()
            .where()
            .eq("podProduct_id", podProductId)
            .and()
            .eq("lot_id", lotId)
            .queryForFirst());
  }

  public void batchCreatePodLotItemsWithLotInfo(@Nullable final List<PodLotItem> podLotItems, PodProduct podProduct)
      throws LMISException {
    if (podLotItems == null) {
      return;
    }
    try {
      TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), () -> {
        for (PodLotItem podLotItem : podLotItems) {
          Product product = productRepository.getByCode(podProduct.getCode());
          Lot lot = podLotItem.getLot();
          lot.setProduct(product);
          Lot savedLot = lotRepository.createOrUpdate(lot);
          podLotItem.setPodProduct(podProduct);
          podLotItem.setLot(savedLot);
          createOrUpdateItem(podLotItem);
        }
        return null;
      });
    } catch (SQLException e) {
      throw new LMISException(e);
    }
  }

  private void createOrUpdateItem(final PodLotItem podLotItem) throws LMISException {
    try {
      TransactionManager
          .callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(),
              () -> {
                podLotItem.setCreatedAt(DateUtil.getCurrentDate());
                podLotItem.setUpdatedAt(DateUtil.getCurrentDate());
                createdOrUpdate(podLotItem);
                return null;
              });
    } catch (SQLException e) {
      throw new LMISException(e);
    }
  }

  private void createdOrUpdate(PodLotItem podLotItem) throws LMISException {
    PodLotItem existingPodLotItem = queryByPodProductIdAndLotId(podLotItem.getPodProduct().getId(),
        podLotItem.getLot().getId());
    if (existingPodLotItem != null) {
      podLotItem.setId(existingPodLotItem.getId());
    }
    podLotItemGenericDao.createOrUpdate(podLotItem);
  }

}
