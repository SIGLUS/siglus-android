package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.ServiceDispensation;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

import java.util.ArrayList;

public class ServiceDispensationRepository {

    @Inject
    DbUtil dbUtil;

    private GenericDao<ServiceDispensation> genericDao;

    @Inject
    public ServiceDispensationRepository(Context context) {
        genericDao = new GenericDao<>(ServiceDispensation.class, context);

    }

    public boolean save(ArrayList<ServiceDispensation> serviceDispensations) throws LMISException {
        return genericDao.create(serviceDispensations);
    }
}
