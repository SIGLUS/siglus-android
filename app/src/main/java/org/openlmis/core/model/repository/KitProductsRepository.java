package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;

import org.openlmis.core.model.Kit;
import org.openlmis.core.model.KitProducts;
import org.openlmis.core.model.Product;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

import java.util.List;

public class KitProductsRepository {
    @Inject
    DbUtil dbUtil;

    @Inject
    Context context;

    GenericDao<KitProducts> kitProductsDao;
    GenericDao<Product> productDao;

    @Inject
    public KitProductsRepository(Context context) {
        kitProductsDao = new GenericDao<>(KitProducts.class, context);
        productDao = new GenericDao<>(Product.class, context);
    }

    public void createOrUpdateKitWithProducts(List<Kit> kits) {

    }

}
