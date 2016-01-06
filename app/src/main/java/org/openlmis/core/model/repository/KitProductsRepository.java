package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;

import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Kit;
import org.openlmis.core.model.KitProducts;
import org.openlmis.core.model.Product;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

public class KitProductsRepository {
    @Inject
    DbUtil dbUtil;

    @Inject
    Context context;

    @Inject
    ProductRepository productRepository;

    GenericDao<KitProducts> kitProductsDao;
    GenericDao<Product> productDao;

    @Inject
    public KitProductsRepository(Context context) {
        kitProductsDao = new GenericDao<>(KitProducts.class, context);
        productDao = new GenericDao<>(Product.class, context);
    }

    public void createOrUpdateKitWithProducts(final List<Kit> kits) throws LMISException {
        try {
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    for (Kit kit : kits) {
                        productRepository.createOrUpdate(kit);
                        batchCreateOrUpdateKitProducts(kit);
                    }
                    return null;
                }
            });
        } catch (SQLException e) {
            throw new LMISException(e);
        }
    }

    private void batchCreateOrUpdateKitProducts(Kit kit) throws LMISException {
        for (Product product : kit.getProducts()) {
            Product productByCode = productRepository.getByCode(product.getCode());
            if (productByCode == null) {
                throw new LMISException(context.getString(R.string.msg_save_kit_products_failed));
            }

            KitProducts existingKitProducts = getByKitIdAndProductId(kit.getId(), productByCode.getId());
            if (existingKitProducts == null) {
                KitProducts kitProducts = new KitProducts(kit, productByCode, product.getQuantityInKit());
                kitProductsDao.create(kitProducts);
            } else {
                existingKitProducts.setQuantity(product.getQuantityInKit());
                kitProductsDao.update(existingKitProducts);
            }
        }
    }

    private KitProducts getByKitIdAndProductId(final long kitId, final long productId) throws LMISException {
        return dbUtil.withDao(KitProducts.class, new DbUtil.Operation<KitProducts, KitProducts>() {
            @Override
            public KitProducts operate(Dao<KitProducts, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("kit_id", String.valueOf(kitId)).and().eq("product_id", String.valueOf(productId)).queryForFirst();
            }
        });
    }

    public List<KitProducts> getByKitId(final long kitId) throws LMISException {
        return dbUtil.withDao(KitProducts.class, new DbUtil.Operation<KitProducts, List<KitProducts>>() {
            @Override
            public List<KitProducts> operate(Dao<KitProducts, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("kit_id", String.valueOf(kitId)).query();
            }
        });
    }
}
