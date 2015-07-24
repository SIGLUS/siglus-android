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
