package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.HealthFacilityService;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

import java.util.List;

public class HealthFacilityServiceRepository {

    private Context context;

    @Inject
    DbUtil dbUtil;

    private GenericDao<HealthFacilityService> genericDao;

    @Inject
    public HealthFacilityServiceRepository(Context context) {
        this.genericDao = new GenericDao<>(HealthFacilityService.class, context);
        this.context = context;
    }

    public List<HealthFacilityService> getAll() throws LMISException {
        return genericDao.queryForAll();
    }


}
