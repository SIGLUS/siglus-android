package org.openlmis.core.model.repository;

import android.content.Context;
import com.google.inject.Inject;
import java.util.List;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.Service;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

public class ServiceFormRepository {

  GenericDao<Service> genericDao;

  @Inject
  DbUtil dbUtil;

  @Inject
  public ServiceFormRepository(Context context) {
    genericDao = new GenericDao<>(Service.class, context);
  }

  public void batchCreateOrUpdateServiceList(final List<Service> serviceList) throws LMISException {
    dbUtil.withDaoAsBatch(Service.class, (DbUtil.Operation<Service, Void>) dao -> {
      for (Service service : serviceList) {
        createOrUpdate(service);
      }
      return null;
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
    return dbUtil.withDao(Service.class,
        dao -> dao.queryBuilder().where().eq("code", serviceCode).queryForFirst());
  }

  protected List<Service> listAllActive() throws LMISException {
    List<Service> activeService = dbUtil
        .withDao(Service.class, dao -> dao.queryBuilder().where().eq("active", true).query());
    return activeService;
  }

  public List<Service> listAllActiveWithProgram(Program program) throws LMISException {
    List<Service> activeService = dbUtil.withDao(Service.class,
        dao -> dao.queryBuilder().where().eq("active", true).and().eq("program_id", program.getId())
            .query());
    return activeService;
  }

}
