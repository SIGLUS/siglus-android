package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;

import org.openlmis.core.BuildConfig;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.ProductProgram;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.List;

public class ProductProgramRepository {

    GenericDao<ProductProgram> genericDao;

    @Inject
    DbUtil dbUtil;

    @Inject
    ProductRepository productRepository;


    @Inject
    public ProductProgramRepository(Context context) {
        genericDao = new GenericDao<>(ProductProgram.class, context);
    }

    public ProductProgram queryByCode(final String productCode, final String programCode) throws LMISException {
        return dbUtil.withDao(ProductProgram.class, dao -> dao.queryBuilder().where().eq("programCode", programCode).and().eq("productCode", productCode).queryForFirst());
    }

    public ProductProgram queryByCode(final String productCode, final List<String> programCodes) throws LMISException {
        return dbUtil.withDao(ProductProgram.class, dao -> dao.queryBuilder().where().in("programCode", programCodes).and().eq("productCode", productCode).queryForFirst());
    }

    public void batchSave(final List<ProductProgram> productPrograms) {
        try {
            for (ProductProgram productProgram : productPrograms) {
                createOrUpdate(productProgram);
            }
        } catch (LMISException e) {
            new LMISException(e, "ProductProgramRepository.batchSave").reportToFabric();
        }
    }

    public List<ProductProgram> listActiveProductProgramsByProgramCodes(final List<String> programCodes) throws LMISException {
        return dbUtil.withDao(ProductProgram.class, dao -> dao.queryBuilder()
                .where().eq("isActive", true)
                .and().in("programCode", programCodes)
                .query());
    }


    public List<ProductProgram> listActiveProductProgramsForMMIA(final List<String> programCodes) throws LMISException {
        return dbUtil.withDao(ProductProgram.class, dao -> dao.queryBuilder()
                .where().eq("isActive", true)
                .and().eq("versionCode", BuildConfig.VERSION_CODE)
                .and().in("programCode", programCodes)
                .query());
    }

    public void createOrUpdate(ProductProgram productProgram) throws LMISException {
        ProductProgram existingProductProgram = queryByCode(productProgram.getProductCode(), productProgram.getProgramCode());
        if (existingProductProgram == null) {
            genericDao.create(productProgram);
        } else {
            productProgram.setId(existingProductProgram.getId());
            genericDao.update(productProgram);
        }
    }


    protected List<ProductProgram> listAll() throws LMISException {
        return genericDao.queryForAll();
    }


    public List<Long> queryActiveProductIdsByProgramsWithKits(List<String> programCodes, boolean isWithKit) throws LMISException {
        List<ProductProgram> productPrograms = listActiveProductProgramsByProgramCodes(programCodes);
        List<String> productCodes = FluentIterable.from(productPrograms).transform(productProgram -> productProgram.getProductCode()).toList();

        return FluentIterable.from(productRepository.queryActiveProductsByCodesWithKits(productCodes, isWithKit)).transform(product -> product.getId()).toList();
    }

    public List<Long> queryActiveProductIdsForMMIA(List<String> programCodes) throws LMISException {
        List<ProductProgram> productPrograms = listActiveProductProgramsForMMIA(programCodes);
        List<String> productCodes = FluentIterable.from(productPrograms).transform(productProgram -> productProgram.getProductCode()).toList();

        return FluentIterable.from(productRepository.queryActiveProductsByCodesWithKits(productCodes, false)).transform(product -> product.getId()).toList();
    }
}
