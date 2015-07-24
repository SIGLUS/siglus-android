package org.openlmis.core.persistence.migrations;

import android.database.sqlite.SQLiteDatabase;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.Migration;

import java.sql.SQLException;

public class CreateDummyProducts implements Migration {

    DbUtil dbUtil;

    public CreateDummyProducts(){
        dbUtil = new DbUtil();
    }

    @Override
    public void up(SQLiteDatabase db, ConnectionSource connectionSource) {

        try {
            dbUtil.withDao(Product.class, new DbUtil.Operation<Product, String>() {
                @Override
                public String operate(Dao dao) throws SQLException {
                    for (int i = 0; i < 10; i++) {
                        Product product = new Product();
                        product.setName("Paracetelmol " + i);
                        product.setUnit("500ML *" + i);

                        dao.create(product);
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

    }
}
