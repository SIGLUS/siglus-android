package org.openlmis.core.model.repository;

import android.content.Context;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.Service;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

import java.sql.SQLException;
import java.util.List;

public class ServiceFormRepository {

    GenericDao<Service> genericDao;

    @Inject
    DbUtil dbUtil;

    @Inject
    public ServiceFormRepository(Context context) {
        genericDao = new GenericDao<>(Service.class, context);
    }

    public void batchCreateOrUpdateServiceList(final List<Service> serviceList) throws LMISException {
        dbUtil.withDaoAsBatch(Service.class, new DbUtil.Operation<Service, Void>() {
            @Override
            public Void operate(Dao<Service, String> dao) throws SQLException, LMISException {
                for (Service service : serviceList) {
                    createOrUpdate(service);
                }
                return null;
            }
        });
    }

    public void createOrUpdate(Service service) throws LMISException {
        Service existingService = queryByCode(service.getCode());
        if (existingService == null) {
            genericDao.create(service);
        } else {
            service.setId(existingService.getId());
            genericDao.update(service);
        }
    }

    public Service queryByCode(final String serviceCode) throws LMISException {
        return dbUtil.withDao(Service.class, new DbUtil.Operation<Service, Service>() {
            @Override
            public Service operate(Dao<Service, String> dao) throws SQLException, LMISException {
                return dao.queryBuilder().where().eq("code", serviceCode).queryForFirst();
            }
        });
    }

    protected List<Service> listAllActive() throws LMISException {
        List<Service> activeService = dbUtil.withDao(Service.class, new DbUtil.Operation<Service, List<Service>>() {
            @Override
            public List<Service> operate(Dao<Service, String> dao) throws SQLException, LMISException {
                return dao.queryBuilder().where().eq("active", true).query();
            }
        });
        return activeService;
    }
    public List<Service> listAllActiveWithProgram(Program program) throws LMISException {
        List<Service> activeService = dbUtil.withDao(Service.class, new DbUtil.Operation<Service, List<Service>>() {
            @Override
            public List<Service> operate(Dao<Service, String> dao) throws SQLException, LMISException {
                return dao.queryBuilder().where().eq("active", true).and().eq("program_id", program.getId()).query();
            }
        });
        return activeService;
    }

}
