package org.openlmis.core.model.repository;


import android.content.Context;

import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Implementation;
import org.openlmis.core.model.MalariaProgram;
import org.openlmis.core.model.Treatment;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.roboguice.shaded.goole.common.base.Optional;

import java.util.Collection;
import java.util.List;

public class MalariaProgramRepository {

    @Inject
    DbUtil dbUtil;

    @Inject
    ProductRepository productRepository;

    private GenericDao<MalariaProgram> genericDao;

    private GenericDao<Implementation> implementationGenericDao;

    private GenericDao<Treatment> treatmentGenericDao;

    @Inject
    public MalariaProgramRepository(Context context) {
        this.genericDao = new GenericDao<>(MalariaProgram.class, context);
        this.implementationGenericDao = new GenericDao<>(Implementation.class, context);
        this.treatmentGenericDao = new GenericDao<>(Treatment.class, context);
    }

    public Optional<MalariaProgram> save(final MalariaProgram malariaProgram) throws LMISException {
        MalariaProgram malariaProgramModified = genericDao.createOrUpdate(malariaProgram);
        saveImplementations(malariaProgram);
        return Optional.of(malariaProgramModified);
    }

    public MalariaProgram getPatientDataReportByPeriodAndType(final DateTime beginDate, final DateTime endDate) throws LMISException {
        return (MalariaProgram) dbUtil.withDao(MalariaProgram.class, (DbUtil.Operation<MalariaProgram, Object>) dao -> {
            MalariaProgram queryPatientDataReport = dao.queryBuilder().where()
                    .eq("startPeriodDate", beginDate)
                    .and().eq("endPeriodDate", endDate).queryForFirst();
            return queryPatientDataReport;
        });
    }

    public List<MalariaProgram> getPendingForSync() throws LMISException {
        return null;
    }

    public MalariaProgram getFirstMovement() throws LMISException {
        return dbUtil.withDao(MalariaProgram.class, dao -> {
            MalariaProgram malariaProgram = dao.queryBuilder().orderBy("createdAt", true).queryForFirst();
            return malariaProgram;
        });
    }

    private void saveImplementations(MalariaProgram malariaProgram) throws LMISException {
        for (Implementation implementation : malariaProgram.getImplementations()) {
            implementation.setMalariaProgram(malariaProgram);
            implementationGenericDao.createOrUpdate(implementation);
        }
        saveTreatments(malariaProgram.getImplementations());
    }

    private void saveTreatments(Collection<Implementation> implementations) throws LMISException {
        for (Implementation implementation : implementations) {
            for (Treatment treatment : implementation.getTreatments()) {
                treatment.setImplementation(implementation);
                treatmentGenericDao.createOrUpdate(treatment);
            }
        }
    }

    public void bulkUpdateAsSynced(List<MalariaProgram> pendingForSync) throws LMISException {

    }

    public List<MalariaProgram> getAll() throws LMISException {
        return genericDao.queryForAll();
    }
}
