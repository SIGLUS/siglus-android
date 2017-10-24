package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.PTVProgramStockInformation;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

import java.util.List;

public class PTVProgramStockInformationRepository {

    private Context context;

    @Inject
    DbUtil dbUtil;

    private GenericDao<PTVProgramStockInformation> genericDao;

    @Inject
    public PTVProgramStockInformationRepository(Context context) {
        this.genericDao = new GenericDao<>(PTVProgramStockInformation.class, context);
        this.context = context;
    }

    public boolean save(List<PTVProgramStockInformation> ptvProgramStocksInformation) throws LMISException {
        return genericDao.create(ptvProgramStocksInformation);
    }
}
