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
import android.database.Cursor;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.stmt.DeleteBuilder;

import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.KitProduct;
import org.openlmis.core.model.Product;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProductRepository {

    GenericDao<Product> genericDao;

    GenericDao<KitProduct> kitProductGenericDao;

    private Context context;

    @Inject
    DbUtil dbUtil;


    @Inject
    StockRepository stockRepository;

    @Inject
    public ProductRepository(Context context) {
        genericDao = new GenericDao<>(Product.class, context);
        this.context = context;
        kitProductGenericDao = new GenericDao<>(KitProduct.class, context);
    }

    public List<Product> listActiveProducts(final Product.IsKit isKit) throws LMISException {
        List<Product> activeProducts = dbUtil.withDao(Product.class, new DbUtil.Operation<Product, List<Product>>() {
            @Override
            public List<Product> operate(Dao<Product, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("isActive", true).and().eq("isKit", isKit.isKit()).query();
            }
        });
        Collections.sort(activeProducts);
        return activeProducts;
    }

    public List<Product> listBasicProducts() throws LMISException {

        String rawSql = "SELECT * FROM products "
                + "WHERE isactive = '1' "
                + "AND products.isbasic = '1' "
                + "AND (isarchived = '1' "
                + "OR id NOT IN ("
                + "SELECT product_id from stock_cards));";


        final Cursor cursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().rawQuery(rawSql, null);
        List<Product> activeProducts = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                Product product = new Product();

                product.setActive(Boolean.TRUE);
                product.setBasic(Boolean.TRUE);
                product.setPrimaryName(cursor.getString(cursor.getColumnIndexOrThrow("primaryName")));
                product.setArchived(cursor.getInt(cursor.getColumnIndexOrThrow("isArchived")) != 0);
                product.setCode(cursor.getString(cursor.getColumnIndexOrThrow("code")));
                product.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                product.setStrength(cursor.getString(cursor.getColumnIndexOrThrow("strength")));
                product.setType(cursor.getString(cursor.getColumnIndexOrThrow("type")));
                activeProducts.add(product);
            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        Collections.sort(activeProducts);
        return activeProducts;
    }

    public List<Product> listProductsArchivedOrNotInStockCard() throws LMISException {
        String rawSql = "SELECT * FROM products "
                + "WHERE isactive = '1' "
                + "AND products.iskit = '0' "
                + "AND (isarchived = '1' "
                + "OR id NOT IN ("
                + "SELECT product_id from stock_cards));";

        final Cursor cursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().rawQuery(rawSql, null);
        List<Product> activeProducts = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                Product product = new Product();

                product.setActive(true);
                product.setKit(false);
                product.setPrimaryName(cursor.getString(cursor.getColumnIndexOrThrow("primaryName")));
                product.setArchived(cursor.getInt(cursor.getColumnIndexOrThrow("isArchived")) != 0);
                product.setCode(cursor.getString(cursor.getColumnIndexOrThrow("code")));
                product.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                product.setStrength(cursor.getString(cursor.getColumnIndexOrThrow("strength")));
                product.setType(cursor.getString(cursor.getColumnIndexOrThrow("type")));
                activeProducts.add(product);
            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        Collections.sort(activeProducts);
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

    public void batchCreateOrUpdateProducts(final List<Product> productList) throws LMISException {
        dbUtil.withDaoAsBatch(Product.class, new DbUtil.Operation<Product, Void>() {
            @Override
            public Void operate(Dao<Product, String> dao) throws LMISException {
                for (Product product : productList) {
                    createOrUpdate(product);
                }
                return null;
            }
        });
    }

    //DON'T USE - THIS WILL BE PRIVATE WHEN KIT FEATURE TOGGLE IS ON
    public void createOrUpdate(Product product) throws LMISException {
        Product existingProduct = getByCode(product.getCode());
        if (existingProduct != null) {
            product.setId(existingProduct.getId());
            product.setArchived(existingProduct.isArchived());
            updateProduct(product);
            if (!(existingProduct.isKit() == product.isKit())) {//isKit changed
                stockRepository.deletedData(product, product.isKit());
            }

        } else {
            genericDao.create(product);
        }

        createKitProductsIfNotExist(product);
    }

    public void updateProduct(Product product) throws LMISException {
        genericDao.update(product);
    }

    private void createKitProductsIfNotExist(Product product) throws LMISException {
        if (product.getKitProductList() != null && !product.getKitProductList().isEmpty()) {
            // product as kit product
            deleteKitProductByCode(product.getCode());
            for (KitProduct kitProduct : product.getKitProductList()) {
                createProductForKitIfNotExist(kitProduct);
                KitProduct kitProductInDB = queryKitProductByCode(kitProduct.getKitCode(), kitProduct.getProductCode());
                if (kitProductInDB == null) {
                    kitProductGenericDao.create(kitProduct);
                }
            }
        }
    }

    private void createProductForKitIfNotExist(KitProduct kitProduct) throws LMISException {
        Product existingProduct = getByCode(kitProduct.getProductCode());
        if (existingProduct == null) {
            Product newProduct = new Product();
            newProduct.setCode(kitProduct.getProductCode());
            createOrUpdate(newProduct);
        }
    }

    protected KitProduct queryKitProductByCode(final String kitCode, final String productCode) throws LMISException {
        return dbUtil.withDao(KitProduct.class, new DbUtil.Operation<KitProduct, KitProduct>() {
            @Override
            public KitProduct operate(Dao<KitProduct, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("kitCode", kitCode).and().eq("productCode", productCode).queryForFirst();
            }
        });
    }

    private  void  deleteKitProductByCode(final String productCode) throws LMISException {
        dbUtil.withDao(KitProduct.class, new DbUtil.Operation<KitProduct, KitProduct>() {
            @Override
            public KitProduct operate(Dao<KitProduct, String> dao) throws SQLException, LMISException{
                DeleteBuilder<KitProduct, String> deleteBuilder = dao.deleteBuilder();
                deleteBuilder.where().eq("kitCode", productCode);
                deleteBuilder.delete();
                return null;
            }
        });
    }

    public List<KitProduct> queryKitProductByKitCode(final String kitCode) throws LMISException {
        return dbUtil.withDao(KitProduct.class, new DbUtil.Operation<KitProduct, List<KitProduct>>() {
            @Override
            public List<KitProduct> operate(Dao<KitProduct, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("kitCode", kitCode).query();
            }
        });
    }

    public Product getByCode(final String code) throws LMISException {
        return dbUtil.withDao(Product.class, new DbUtil.Operation<Product, Product>() {
            @Override
            public Product operate(Dao<Product, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("code", code).queryForFirst();
            }
        });
    }

    public Product getProductById(final long id) throws LMISException {
        return dbUtil.withDao(Product.class, new DbUtil.Operation<Product, Product>() {
            @Override
            public Product operate(Dao<Product, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("id", id).queryForFirst();
            }
        });
    }


    public List<Product> queryActiveProductsByCodesWithKits(final List<String> productCodes, final boolean isWithKit) throws LMISException {
        return dbUtil.withDao(Product.class, new DbUtil.Operation<Product, List<Product>>() {
            @Override
            public List<Product> operate(Dao<Product, String> dao) throws SQLException {
                Where<Product, String> queryBuilder = dao.queryBuilder().where().in("code", productCodes).and().eq("isActive", true).and().eq("isArchived", false);
                if (!isWithKit) {
                    queryBuilder.and().eq("isKit", false);
                }
                return queryBuilder.query();
            }
        });
    }

    public List<KitProduct> queryKitProductByProductCode(final String productCode) throws LMISException {
        return dbUtil.withDao(KitProduct.class, new DbUtil.Operation<KitProduct, List<KitProduct>>() {
            @Override
            public List<KitProduct> operate(Dao<KitProduct, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("productCode", productCode).query();
            }
        });
    }

    public List<String> listArchivedProductCodes() throws LMISException {
        List<Product> isArchived = dbUtil.withDao(Product.class, new DbUtil.Operation<Product, List<Product>>() {
            @Override
            public List<Product> operate(Dao<Product, String> dao) throws SQLException {
                return dao.queryBuilder().selectColumns("code").where().eq("isArchived", true).query();
            }
        });

        return FluentIterable.from(isArchived).transform(new Function<Product, String>() {
            @Override
            public String apply(Product product) {
                return product.getCode();
            }
        }).toList();
    }

    public List<Product> queryProductsByProductIds(final List<Long> productIds) throws LMISException {
        return dbUtil.withDao(Product.class, new DbUtil.Operation<Product, List<Product>>() {
            @Override
            public List<Product> operate(Dao<Product, String> dao) throws SQLException, LMISException {
                return dao.queryBuilder().where().in("id", productIds).query();
            }
        });
    }

    public List<Product> queryActiveProductsInVIAProgramButNotInDraftVIAForm() throws LMISException {
        String rawSql = "SELECT p1.* FROM products p1 "
                + "JOIN product_programs p2 "
                + "ON p1.code = p2.productCode "
                + "JOIN programs p3 "
                + "ON p2.programCode = p3.programCode "
                + "WHERE p3.programCode = 'VIA' OR p3.parentCode = 'VIA' "
                + "AND p2.isActive = 1 AND p1.isActive = 1 "
                + "AND p1.isKit = 0 "
                + "AND p1.id NOT IN "
                + "(SELECT product_id FROM rnr_form_items ri "
                + "WHERE ri.form_id IN "
                + "(SELECT id FROM rnr_forms r1 WHERE r1.emergency = 0 AND r1.status = 'DRAFT'))";
        final Cursor cursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().rawQuery(rawSql, null);
        List<Product> products = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Product product = new Product();
                product.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
                product.setCode(cursor.getString(cursor.getColumnIndexOrThrow("code")));
                product.setPrimaryName(cursor.getString(cursor.getColumnIndexOrThrow("primaryName")));
                product.setStrength(cursor.getString(cursor.getColumnIndexOrThrow("strength")));
                product.setType(cursor.getString(cursor.getColumnIndexOrThrow("type")));
                product.setArchived(cursor.getInt(cursor.getColumnIndexOrThrow("isArchived")) == 1);
                products.add(product);
            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return products;
    }

    public List<Product> listNonBasicProducts() {
        String rawSql = "SELECT * FROM products "
                + "WHERE isactive = '1' "
                + "AND products.isbasic = '0' "
                + "AND (isarchived = '1' "
                + "OR id NOT IN ("
                + "SELECT product_id from stock_cards));";

        final Cursor cursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().rawQuery(rawSql, null);
        List<Product> nonBasicProducts = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                Product product = new Product();

                product.setActive(Boolean.TRUE);
                product.setPrimaryName(cursor.getString(cursor.getColumnIndexOrThrow("primaryName")));
                product.setArchived(cursor.getInt(cursor.getColumnIndexOrThrow("isArchived")) != 0);
                product.setCode(cursor.getString(cursor.getColumnIndexOrThrow("code")));
                product.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                product.setStrength(cursor.getString(cursor.getColumnIndexOrThrow("strength")));
                product.setType(cursor.getString(cursor.getColumnIndexOrThrow("type")));
                nonBasicProducts.add(product);
            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        Collections.sort(nonBasicProducts);
        return nonBasicProducts;
    }

    public List<Product> getProductsByCodes(final List<String> codes) throws LMISException {
        final List<Product> products = new ArrayList<>();
        dbUtil.withDaoAsBatch(context, Product.class, new DbUtil.Operation<Product,Void>() {
            @Override
            public Void operate(Dao<Product, String> dao) throws SQLException, LMISException {
                for (String code : codes) {
                    Product product = dao.queryBuilder().where().eq("code", code).queryForFirst();
                    products.add(product);

                }
                return null;
            }
        });
        return products;
    }

    public enum IsWithKit {
        Yes(true),
        No(false);

        public boolean IsWithKit() {
            return IsWithKit;
        }

        private boolean IsWithKit;

        IsWithKit(boolean IsWithKit) {
            this.IsWithKit = IsWithKit;
        }
    }
}
