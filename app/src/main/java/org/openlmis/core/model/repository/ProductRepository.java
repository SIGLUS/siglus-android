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

import static org.openlmis.core.constant.FieldConstants.CODE;
import static org.openlmis.core.constant.FieldConstants.ID;
import static org.openlmis.core.constant.FieldConstants.IS_ACTIVE;
import static org.openlmis.core.constant.FieldConstants.IS_ARCHIVED;
import static org.openlmis.core.constant.FieldConstants.IS_BASIC;
import static org.openlmis.core.constant.FieldConstants.IS_KIT;
import static org.openlmis.core.constant.FieldConstants.KIT_CODE;
import static org.openlmis.core.constant.FieldConstants.PRICE;
import static org.openlmis.core.constant.FieldConstants.PRIMARY_NAME;
import static org.openlmis.core.constant.FieldConstants.PRODUCT_CODE;
import static org.openlmis.core.constant.FieldConstants.PROGRAM_CODE;
import static org.openlmis.core.constant.FieldConstants.STRENGTH;
import static org.openlmis.core.constant.FieldConstants.TYPE;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.inject.Inject;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.Where;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.AdditionalProductProgram;
import org.openlmis.core.model.KitProduct;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.network.model.ProductAndSupportedPrograms;
import org.openlmis.core.network.model.SyncDownLatestProductsResponse;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

public class ProductRepository {

  private static final String TAG = ProductRepository.class.getSimpleName();
  private static final String SELECT_PRODUCTS = "SELECT * FROM products WHERE isactive = '1' ";
  private static final String ARCHIVED = "AND (isarchived = '1' OR id NOT IN (SELECT product_id from stock_cards));";

  private final Context context;

  GenericDao<Product> genericDao;

  GenericDao<KitProduct> kitProductGenericDao;

  @Inject
  DbUtil dbUtil;

  @Inject
  StockRepository stockRepository;

  @Inject
  LotRepository lotRepository;

  @Inject
  ProductProgramRepository productProgramRepository;

  @Inject
  AdditionalProductProgramRepository additionalProductProgramRepository;


  @Inject
  public ProductRepository(Context context) {
    this.context = context;
    genericDao = new GenericDao<>(Product.class, context);
    kitProductGenericDao = new GenericDao<>(KitProduct.class, context);
  }

  public List<Product> listActiveProducts(final Product.IsKit isKit) throws LMISException {
    List<Product> activeProducts = dbUtil.withDao(Product.class,
        dao -> dao.queryBuilder().where().eq(IS_ACTIVE, true).and().eq(IS_KIT, isKit.isKit()).query());
    Collections.sort(activeProducts);
    return activeProducts;
  }


  public Map<String, String> listProductCodeToProgramCode() {
    String rawSql = "SELECT productCode, programCode FROM product_programs";
    Cursor cursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().rawQuery(rawSql, null);
    Map<String, String> productCodeToProgramCode = new HashMap<>();
    if (cursor.moveToFirst()) {
      do {
        productCodeToProgramCode.put(cursor.getString(cursor.getColumnIndexOrThrow(PRODUCT_CODE)),
            cursor.getString(cursor.getColumnIndexOrThrow(PROGRAM_CODE)));
      } while (cursor.moveToNext());
    }
    if (!cursor.isClosed()) {
      cursor.close();
    }
    return productCodeToProgramCode;
  }

  public List<Product> listBasicProducts() {
    String rawSql = SELECT_PRODUCTS + "AND products.isbasic = '1' " + ARCHIVED;
    return queryProducts(rawSql);
  }

  public List<Product> listProductsArchivedOrNotInStockCard() {
    String rawSql = SELECT_PRODUCTS + "AND products.iskit = '0' " + ARCHIVED;
    return queryProducts(rawSql);
  }

  public List<Product> listAllProductsWithoutKit() throws LMISException {
    return dbUtil.withDao(Product.class, dao -> dao.queryBuilder().where().eq(IS_KIT, false).query());
  }

  public void save(final List<Product> products) {
    try {
      dbUtil.withDaoAsBatch(Product.class, dao -> {
        for (Product product : products) {
          dao.create(product);
        }
        return null;
      });
    } catch (LMISException e) {
      new LMISException(e, "ProductRepository.save").reportToFabric();
    }
  }

  public void batchCreateOrUpdateProducts(final List<Product> productList) throws LMISException {
    dbUtil.withDaoAsBatch(Product.class, dao -> {
      for (Product product : productList) {
        createOrUpdate(product);
      }
      return null;
    });
  }

  public void saveProductAndProductProgram(SyncDownLatestProductsResponse response) throws LMISException {
    List<Product> productList = new ArrayList<>();
    try {
      TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), () -> {
        for (ProductAndSupportedPrograms productAndSupportedPrograms : response.getLatestProducts()) {
          Product product = productAndSupportedPrograms.getProduct();
          productProgramRepository.batchSave(product, productAndSupportedPrograms.getProductPrograms());
          if (Program.MALARIA_CODE.equals(product.getAdditionalProgramCode())) {
            additionalProductProgramRepository.createOrUpdate(AdditionalProductProgram
                .builder()
                .productCode(product.getCode())
                .programCode(product.getAdditionalProgramCode())
                .originProgramCode(productAndSupportedPrograms.getProductPrograms().get(0).getProgramCode())
                .build());
          }
          updateDeactivateProductNotifyList(product);
          productList.add(product);
        }
        batchCreateOrUpdateProducts(productList);
        SharedPreferenceMgr.getInstance().setKeyIsFirstLoginVersion200();
        SharedPreferenceMgr.getInstance().setLastSyncProductTime(response.getLastSyncTime());
        return null;
      });
    } catch (SQLException e) {
      throw new LMISException(e);
    }
  }

  //DON'T USE - THIS WILL BE PRIVATE WHEN KIT FEATURE TOGGLE IS ON
  public void createOrUpdate(Product product) throws LMISException {
    Product existingProduct = getByCode(product.getCode());
    if (existingProduct != null) {
      deleteWrongKitInfo(existingProduct, product);
      product.setId(existingProduct.getId());
      product.setArchived(existingProduct.isArchived());
      updateProduct(product);
    } else {
      genericDao.create(product);
    }
    try {
      createKitProductsIfNotExist(product);
    } catch (SQLException e) {
      Log.w(TAG, e);
    }
  }

  public void updateDeactivateProductNotifyList(Product product) throws LMISException {
    Product existingProduct = getByCode(product.getCode());

    if (existingProduct == null) {
      return;
    }

    if (product.isActive() == existingProduct.isActive()) {
      return;
    }
    if (product.isActive()) {
      SharedPreferenceMgr.getInstance().removeShowUpdateBannerTextWhenReactiveProduct(existingProduct.getPrimaryName());
      return;
    }

    StockCard stockCard = stockRepository.queryStockCardByProductId(existingProduct.getId());
    if (stockCard == null) {
      return;
    }

    if (stockCard.getProduct().isArchived()) {
      return;
    }

    if (stockCard.getStockOnHand() == 0) {
      SharedPreferenceMgr.getInstance().setIsNeedShowProductsUpdateBanner(true, product.getPrimaryName());
    }
  }

  private void deleteWrongKitInfo(Product existingProduct, Product product) {
    try {
      if (existingProduct.isKit() == product.isKit()) { //isKit changed
        return;
      }
      StockCard stockCard = stockRepository.queryStockCardByProductCode(product.getCode());
      if (stockCard == null) {
        return;
      }
      lotRepository.deleteLotInfo(stockCard);
      stockRepository.deletedData(stockCard);
      Product localProduct = getByCode(product.getCode());
      if (!product.isKit()) {
        genericDao.delete(localProduct);
      }
    } catch (LMISException e) {
      e.reportToFabric();
    }
  }

  public void updateProduct(Product product) throws LMISException {
    genericDao.update(product);
  }

  private void createKitProductsIfNotExist(Product product) throws LMISException, SQLException {
    List<KitProduct> kitProductList = product.getKitProductList();
    if (CollectionUtils.isEmpty(kitProductList)) {
      return;
    }
    // product as kit product
    deleteKitProductByCode(product.getCode());
    for (KitProduct kitProduct : kitProductList) {
      createProductForKitIfNotExist(kitProduct);
      KitProduct kitProductInDB = queryKitProductByCode(kitProduct.getKitCode(), kitProduct.getProductCode());
      if (kitProductInDB == null) {
        kitProductGenericDao.create(kitProduct);
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

  protected KitProduct queryKitProductByCode(final String kitCode, final String productCode)
      throws LMISException {
    return dbUtil.withDao(KitProduct.class,
        dao -> dao.queryBuilder().where().eq(KIT_CODE, kitCode).and().eq(PRODUCT_CODE, productCode).queryForFirst());
  }

  private void deleteKitProductByCode(final String productCode) throws SQLException {
    TransactionManager
        .callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(),
            () -> dbUtil
                .withDao(KitProduct.class, dao -> {
                  DeleteBuilder<KitProduct, String> deleteBuilder = dao.deleteBuilder();
                  deleteBuilder.where().eq(KIT_CODE, productCode);
                  deleteBuilder.delete();
                  return null;
                }));
  }

  public List<KitProduct> queryKitProductByKitCode(final String kitCode) throws LMISException {
    return dbUtil.withDao(KitProduct.class,
        dao -> dao.queryBuilder().where().eq(KIT_CODE, kitCode).query());
  }

  public Product getByCode(final String code) throws LMISException {
    return dbUtil
        .withDao(Product.class, dao -> dao.queryBuilder().where().eq(CODE, code).queryForFirst());
  }

  public Product getProductById(final long id) throws LMISException {
    return dbUtil
        .withDao(Product.class, dao -> dao.queryBuilder().where().eq(ID, id).queryForFirst());
  }

  public List<Product> queryActiveProductsByCodesWithKits(final List<String> productCodes,
      final boolean isWithKit) throws LMISException {
    return dbUtil.withDao(Product.class, dao -> {
      Where<Product, String> queryBuilder = dao.queryBuilder()
          .where().in(CODE, productCodes)
          .and().eq(IS_ACTIVE, true)
          .and().eq(IS_ARCHIVED, false);
      if (!isWithKit) {
        queryBuilder.and().eq(IS_KIT, false);
      }
      return queryBuilder.query();
    });
  }

  public List<KitProduct> queryKitProductByProductCode(final String productCode) throws LMISException {
    return dbUtil.withDao(KitProduct.class,
        dao -> dao.queryBuilder().where().eq(PRODUCT_CODE, productCode).query());
  }

  public List<String> listArchivedProductCodes() throws LMISException {
    List<Product> isArchived = dbUtil.withDao(Product.class,
        dao -> dao.queryBuilder().selectColumns(CODE).where().eq(IS_ARCHIVED, true).query());
    return FluentIterable.from(isArchived).transform(Product::getCode).toList();
  }

  public List<Product> queryActiveProductsInVIAProgramButNotInDraftVIAForm() {
    String rawSql = "SELECT p1.* FROM products p1 "
        + "JOIN product_programs p2 "
        + "ON p1.code = p2.productCode "
        + "JOIN programs p3 "
        + "ON p2.programCode = p3.programCode "
        + "WHERE p3.programCode = 'VC' "
        + "AND p2.isActive = 1 AND p1.isActive = 1 "
        + "AND p1.isKit = 0 "
        + "AND p1.id NOT IN "
        + "(SELECT product_id FROM rnr_form_items ri "
        + "WHERE ri.form_id IN "
        + "(SELECT id FROM rnr_forms r1 WHERE r1.emergency = 0 AND r1.status = 'DRAFT'))";
    Cursor cursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().rawQuery(rawSql, null);
    List<Product> products = new ArrayList<>();
    if (cursor.moveToFirst()) {
      do {
        products.add(buildProductFromCursor(cursor));
      } while (cursor.moveToNext());
    }
    if (!cursor.isClosed()) {
      cursor.close();
    }
    return products;
  }

  public List<Product> listNonBasicProducts() {
    String rawSql = SELECT_PRODUCTS + "AND products.isbasic = '0' " + ARCHIVED;

    Cursor cursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().rawQuery(rawSql, null);
    List<Product> nonBasicProducts = new ArrayList<>();
    if (cursor.moveToFirst()) {
      do {
        Product product = buildProductFromCursor(cursor);
        nonBasicProducts.add(product);
      } while (cursor.moveToNext());
    }
    if (!cursor.isClosed()) {
      cursor.close();
    }
    Collections.sort(nonBasicProducts);
    return nonBasicProducts;
  }

  public List<Product> queryProductsInStockCard() {
    String rawSql = "SELECT * FROM products "
        + "WHERE id IN (SELECT product_id FROM stock_cards WHERE stockOnHand > 0) "
        + "AND isKit = 0 "
        + "AND isArchived = 0;";
    return queryProducts(rawSql);
  }

  public List<Product> queryProductsByProgramCode(String programCode) {
    String rawSql = "SELECT p1.* FROM products p1 "
        + "JOIN product_programs p2 "
        + "ON p1.code = p2.productCode "
        + "WHERE p2.programCode = '"
        + programCode
        + "'";
    Cursor cursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().rawQuery(rawSql, null);
    List<Product> products = new ArrayList<>();
    if (cursor.moveToFirst()) {
      do {
        products.add(buildProductFromCursor(cursor));
      } while (cursor.moveToNext());
    }
    if (!cursor.isClosed()) {
      cursor.close();
    }
    return products;
  }

  public void updateProductInArchived(List<Long> productIds) {
    if (productIds.isEmpty()) {
      return;
    }
    String ids = StringUtils.join(productIds, ',');
    String updateArchive = "UPDATE products SET isArchived = '0' where id in ('" + ids + "')";
    LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().execSQL(updateArchive);
  }

  public boolean isKitChildrenProduct(long stockCardId) {
    String rawSql = "SELECT * FROM kit_products "
        + "WHERE productCode IN "
        + "(SELECT code FROM products WHERE id IN "
        + "(SELECT product_id FROM stock_cards WHERE id = '"
        + stockCardId
        + "'))";
    try (Cursor cursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase()
        .rawQuery(rawSql, null)) {
      return cursor.getCount() != 0;
    }
  }

  @NonNull
  public Product buildProductFromCursor(Cursor cursor) {
    Product product = new Product();
    product.setBasic(cursor.getInt(cursor.getColumnIndexOrThrow(IS_BASIC)) == 1);
    product.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(IS_ACTIVE)) == 1);
    product.setKit(cursor.getInt(cursor.getColumnIndexOrThrow(IS_KIT)) == 1);
    product.setPrimaryName(cursor.getString(cursor.getColumnIndexOrThrow(PRIMARY_NAME)));
    product.setArchived(cursor.getInt(cursor.getColumnIndexOrThrow(IS_ARCHIVED)) == 1);
    product.setCode(cursor.getString(cursor.getColumnIndexOrThrow(CODE)));
    product.setId(cursor.getInt(cursor.getColumnIndexOrThrow(ID)));
    product.setStrength(cursor.getString(cursor.getColumnIndexOrThrow(STRENGTH)));
    product.setType(cursor.getString(cursor.getColumnIndexOrThrow(TYPE)));
    product.setPrice(cursor.getString(cursor.getColumnIndexOrThrow(PRICE)));
    return product;
  }

  private List<Product> queryProducts(String rawSql) {
    Cursor cursor = LmisSqliteOpenHelper.getInstance(LMISApp.getContext()).getWritableDatabase().rawQuery(rawSql, null);
    List<Product> activeProducts = new ArrayList<>();
    if (cursor.moveToFirst()) {
      do {
        activeProducts.add(buildProductFromCursor(cursor));
      } while (cursor.moveToNext());
    }
    if (!cursor.isClosed()) {
      cursor.close();
    }
    Collections.sort(activeProducts);
    return activeProducts;
  }
}
