package org.openlmis.core.persistence.migrations;

import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.support.ConnectionSource;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.Migration;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by kwhu on 10/9/15.
 */
public class AddSignatureFieldInStockMovementItemTable implements Migration {

    private final DbUtil dbUtil;

    public AddSignatureFieldInStockMovementItemTable() {
        dbUtil = new DbUtil();
    }

    @Override
    public void up(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            dbUtil.withDao(StockMovementItem.class, new DbUtil.Operation<StockMovementItem, String>() {

                @Override
                public String operate(Dao<StockMovementItem, String> dao) throws SQLException {
                    boolean isColumnExist = false;
                    final GenericRawResults<String[]> rawResults = dao.queryRaw("PRAGMA table_info(stock_items)");
                    final List<String[]> results = rawResults.getResults();
                    for (String[] field : results) {
                        if (field[1].equals("signature")) {
                            isColumnExist = true;
                            break;
                        }
                    }

                    if (!isColumnExist) {
                        dao.executeRaw("ALTER TABLE 'stock_items' ADD COLUMN signature TEXT");
                    }
                    return null;
                }
            });
        } catch (LMISException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void down(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            dbUtil.withDao(StockMovementItem.class, new DbUtil.Operation<StockMovementItem, String>() {

                @Override
                public String operate(Dao<StockMovementItem, String> dao) throws SQLException {
                    dao.executeRaw("ALTER TABLE 'stock_items' DELETE COLUMN signature TEXT");
                    return null;
                }
            });
        } catch (LMISException e) {
            e.printStackTrace();
        }
    }
}
