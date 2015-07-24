package org.openlmis.core.model.repository;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.persistence.DbUtil;

import java.sql.SQLException;
import java.util.List;

public class ProductRepository {

    @Inject
    DbUtil dbUtil;

    public List<Product> loadProductList() throws LMISException{
        return dbUtil.withDao(Product.class, new DbUtil.Operation<Product, List<Product>>() {
            @Override
            public List<Product> operate(Dao<Product, String> dao) throws SQLException {
                List<Product> products = dao.queryForAll();
                return products;
            }
        });
    }
}
