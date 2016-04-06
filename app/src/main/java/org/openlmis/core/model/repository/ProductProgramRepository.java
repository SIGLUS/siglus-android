package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.ProductProgram;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

import java.sql.SQLException;
import java.util.List;

public class ProductProgramRepository {

    GenericDao<ProductProgram> genericDao;

    @Inject
    DbUtil dbUtil;

    @Inject
    public ProductProgramRepository(Context context) {
        genericDao = new GenericDao<>(ProductProgram.class, context);
    }

    public ProductProgram queryByCode(final String productCode, final String programCode) throws LMISException {
        return dbUtil.withDao(ProductProgram.class, new DbUtil.Operation<ProductProgram, ProductProgram>() {

            @Override
            public ProductProgram operate(Dao<ProductProgram, String> dao) throws SQLException, LMISException {
                return dao.queryBuilder().where().eq("programCode", programCode).and().eq("productCode", productCode).queryForFirst();
            }
        });
    }

    public void batchSave(final List<ProductProgram> productPrograms) {
        try {
            for (ProductProgram productProgram : productPrograms) {
                createOrUpdate(productProgram);
            }
        } catch (LMISException e) {
            e.reportToFabric();
        }
    }

    public List<ProductProgram> listActiveProductProgramsByProgramCodes(final List<String> programCodes) throws LMISException {
        return dbUtil.withDao(ProductProgram.class, new DbUtil.Operation<ProductProgram, List<ProductProgram>>() {
            @Override
            public List<ProductProgram> operate(Dao<ProductProgram, String> dao) throws SQLException, LMISException {
                return dao.queryBuilder().where().eq("isActive", true).and().in("programCode", programCodes).query();
            }
        });
    }

    public void createOrUpdate(ProductProgram productProgram) throws LMISException {
        ProductProgram existingProductProgram = queryByCode(productProgram.getProductCode(), productProgram.getProgramCode());
        if (existingProductProgram == null) {
            genericDao.create(productProgram);
        } else {
            existingProductProgram.setActive(productProgram.isActive());
            genericDao.update(existingProductProgram);
        }
    }


    protected List<ProductProgram> listAll() throws LMISException {
        return genericDao.queryForAll();
    }
}
