package org.openlmis.core.model.repository;

import android.content.Context;
import com.google.inject.Inject;
import java.util.List;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.RegimenItemThreeLines;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

public class RegimenItemThreeLineRepository {

  GenericDao<RegimenItemThreeLines> genericDao;
  @Inject
  DbUtil dbUtil;

  @Inject
  public RegimenItemThreeLineRepository(Context context) {
    this.genericDao = new GenericDao<>(RegimenItemThreeLines.class, context);
  }

  public void batchCreateOrUpdate(final List<RegimenItemThreeLines> list) throws LMISException {
    dbUtil.withDaoAsBatch(RegimenItemThreeLines.class,
        (DbUtil.Operation<RegimenItemThreeLines, Void>) dao -> {
          for (RegimenItemThreeLines item : list) {
            dao.createOrUpdate(item);
          }
          return null;
        });
  }

  public void deleteRegimeThreeLineItems(final List<RegimenItemThreeLines> itemThreeLinesWrapper)
      throws LMISException {
    dbUtil.withDao(RegimenItemThreeLines.class,
        (DbUtil.Operation<RegimenItemThreeLines, Void>) dao -> {
          dao.delete(itemThreeLinesWrapper);
          return null;
        });
  }
}
