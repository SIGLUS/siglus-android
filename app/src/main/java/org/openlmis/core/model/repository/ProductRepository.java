/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */


package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class ProductRepository {

    GenericDao<Product> genericDao;

    @Inject
    DbUtil dbUtil;

    @Inject
    public ProductRepository(Context context) {
        genericDao = new GenericDao<>(Product.class, context);
    }

    public List<Product> listActiveProducts() throws LMISException {

        List<Product> activeProducts = dbUtil.withDao(Product.class, new DbUtil.Operation<Product, List<Product>>() {
            @Override
            public List<Product> operate(Dao<Product, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("isActive", true).query();
            }
        });
        return activeProducts;
    }

    public void save(final List<Product> products) {
        try {
            dbUtil.withDaoAsBatch(Product.class, new DbUtil.Operation<Product, Void>() {
                @Override
                public Void operate(Dao<Product, String> dao) throws SQLException {
                    for (Product product : products) {
                        dao.create(product);
                    }
                    return null;
                }
            });
        } catch (LMISException e) {
            e.reportToFabric();
        }
    }

    public void createOrUpdate(Product product) throws LMISException {
        Product existingProduct = getByCode(product.getCode());
        if (existingProduct != null) {
            product.setId(existingProduct.getId());
            genericDao.update(product);
        } else {
            genericDao.create(product);
        }
    }

    public Product getById(long id) {
        try {
            return genericDao.getById(String.valueOf(id));
        } catch (LMISException e) {
            e.reportToFabric();
            return null;
        }
    }

    public Product getByCode(final String code) throws LMISException {
        return dbUtil.withDao(Product.class, new DbUtil.Operation<Product, Product>() {
            @Override
            public Product operate(Dao<Product, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("code", code).queryForFirst();
            }
        });
    }

    public List<Product> queryProducts(final long programId) throws LMISException {
        return dbUtil.withDao(Product.class, new DbUtil.Operation<Product, List<Product>>() {
            @Override
            public List<Product> operate(Dao<Product, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("program_id", programId).query();
            }
        });
    }
}
