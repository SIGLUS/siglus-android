package org.openlmis.core.model.repository;

import android.content.Context;
import com.google.inject.Inject;
import java.util.List;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Cmm;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

public class CmmRepository {

  @Inject
  DbUtil dbUtil;

  private final GenericDao<Cmm> cmmDao;

  @Inject
  public CmmRepository(Context context) {
    cmmDao = new GenericDao<>(Cmm.class, context);
  }

  public void save(final Cmm cmm) throws LMISException {
    Cmm sameCardSamePeriodCmm = dbUtil.withDao(Cmm.class, dao -> dao.queryBuilder()
        .where().eq("stockCard_id", cmm.getStockCard().getId())
        .and().eq("periodBegin", cmm.getPeriodBegin())
        .and().eq("periodEnd", cmm.getPeriodEnd())
        .queryForFirst());

    if (sameCardSamePeriodCmm != null) {
      cmm.setId(sameCardSamePeriodCmm.getId());
    }
    cmmDao.createOrUpdate(cmm);
  }

  public List<Cmm> list() throws LMISException {
    return cmmDao.queryForAll();
  }

  public List<Cmm> listUnsynced() throws LMISException {
    return FluentIterable.from(cmmDao.queryForAll()).filter(cmm -> !cmm.isSynced()).toList();
  }

  public void resetCmm(List<String> productCodeList) {
    for (String productCode : productCodeList) {
      String resetCmmValueAndSynced = "UPDATE cmm "
          + "SET cmmValue=-1.0,synced=0 WHERE stockCard_id=(SELECT stockCard_id "
          + "FROM stock_cards WHERE product_id=(SELECT id FROM products WHERE code='" + productCode
          + "' ));";
      LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase()
          .execSQL(resetCmmValueAndSynced);
    }
  }

  public void deleteCmm(final StockCard stockCard) throws LMISException {
    List<Cmm> cmms = dbUtil.withDao(Cmm.class, dao -> dao.queryBuilder()
        .where().eq("stockCard_id", stockCard != null ? stockCard.getId() : "0")
        .query());
    for (Cmm cmm : cmms) {
      cmmDao.delete(cmm);
    }
  }
}
