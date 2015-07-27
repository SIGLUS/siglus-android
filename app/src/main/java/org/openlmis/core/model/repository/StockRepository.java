package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

import java.sql.SQLException;
import java.util.List;

public class StockRepository {
    @Inject
    DbUtil dbUtil;

    GenericDao<StockCard> genericDao;

    @Inject
    public StockRepository(Context context) {
        genericDao = new GenericDao<>(StockCard.class,context);
    }


    public void batchSave(final List<StockCard> stockCards) {
        try {
            dbUtil.withDaoAsBatch(StockCard.class, new DbUtil.Operation<StockCard, Object>() {
                @Override
                public Object operate(Dao<StockCard, String> dao) throws SQLException {
                    for (StockCard stockCard : stockCards) {
                        dao.createOrUpdate(stockCard);
                    }
                    return null;
                }
            });
        } catch (LMISException e) {
            e.printStackTrace();
        }
    }


    public void save(final StockCard stockCard) {
        try {
            genericDao.create(stockCard);
        } catch (LMISException e) {
            e.printStackTrace();
        }
    }
}
