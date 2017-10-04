package org.openlmis.core.model.repository;


import android.content.Context;
import android.support.annotation.NonNull;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;

import org.joda.time.DateTime;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Implementation;
import org.openlmis.core.model.MalariaProgram;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Treatment;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.utils.MalariaProgramMapper;
import org.roboguice.shaded.goole.common.base.Optional;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

public class MalariaProgramRepository {

    @Inject
    DbUtil dbUtil;

    @Inject
    ProductRepository productRepository;

    private GenericDao<MalariaProgram> genericDao;

    private GenericDao<Implementation> implementationGenericDao;

    private GenericDao<Treatment> treatmentGenericDaoDao;

    @Inject
    public MalariaProgramRepository(Context context) {
        this.genericDao = new GenericDao<>(MalariaProgram.class, context);

        this.implementationGenericDao = new GenericDao<>(Implementation.class, context);
        this.treatmentGenericDaoDao = new GenericDao<>(Treatment.class, context);
    }

    public Optional<MalariaProgram> saveMovement(final MalariaProgram malariaProgram) throws LMISException {
        if (!malariaProgram.isStatusComplete() && !malariaProgram.isStatusSynced()) {
            final MalariaProgram existingMalariaProgram = getPatientDataReportByPeriodAndType(malariaProgram.getStartDatePeriod(), malariaProgram.getEndDatePeriod());
            if (existingMalariaProgram == null) {
                malariaProgram.setStatusMissing(Boolean.FALSE);
                malariaProgram.setStatusDraft(Boolean.TRUE);
                malariaProgram.setReportedDate(new DateTime());
                MalariaProgram malariaProgramSaved = genericDao.create(malariaProgram);
                saveImplementationsAndTreatments(malariaProgram, malariaProgramSaved, existingMalariaProgram);
                return Optional.of(malariaProgramSaved);
            } else {
                malariaProgram.setId(existingMalariaProgram.getId());
                if (!malariaProgram.isStatusComplete()) {
                    malariaProgram.setStatusMissing(Boolean.FALSE);
                    malariaProgram.setStatusDraft(Boolean.TRUE);
                }
                malariaProgram.setReportedDate(new DateTime());
                MalariaProgram malariaProgramModified = genericDao.createOrUpdate(malariaProgram);
                saveImplementationsAndTreatments(malariaProgram, malariaProgramModified, existingMalariaProgram);
                return Optional.of(malariaProgramModified);
            }
        }
        return Optional.absent();
    }

    private void saveImplementationsAndTreatments(MalariaProgram malariaProgram, MalariaProgram malariaProgramSaved, MalariaProgram existingMalariaProgram) throws LMISException {
        if (malariaProgramSaved != null) {
            List<Product> products = getProducts();
            List<Implementation> implementations;
            List<Treatment> usTreatments;
            List<Treatment> apeTreatments;
            if (existingMalariaProgram != null) {
                implementations = new ArrayList<>(existingMalariaProgram.getImplementations());
                usTreatments = new ArrayList<>(implementations.get(MalariaProgramMapper.FIRST_ELEMENT_INDEX).getTreatments());
                apeTreatments = new ArrayList<>(implementations.get(MalariaProgramMapper.SECOND_ELEMENT_INDEX).getTreatments());
                for (int i = 0; i < implementations.size(); i++) {
                    ArrayList<Implementation> currentImplementations = new ArrayList<>(malariaProgram.getImplementations());
                    currentImplementations.get(i).setMalariaProgram(malariaProgramSaved);
                    currentImplementations.get(i).setId(implementations.get(i).getId());
                    Implementation implementationSaved = implementationGenericDao.createOrUpdate(currentImplementations.get(i));
                    List<Treatment> treatments = (List<Treatment>) currentImplementations.get(i).getTreatments();
                    for (int j = 0; j < treatments.size(); j++) {
                        if (i == 0) {
                            treatments.get(j).setId(usTreatments.get(j).getId());
                        } else {
                            treatments.get(j).setId(apeTreatments.get(j).getId());
                        }
                        treatments.get(j).setImplementation(implementationSaved);
                        treatments.get(j).setProduct(products.get(j));
                        treatmentGenericDaoDao.createOrUpdate(treatments.get(j));
                    }
                }
            } else {
                for (Implementation implementation : malariaProgram.getImplementations()) {
                    implementation.setMalariaProgram(malariaProgramSaved);
                    Implementation implementationSaved = implementationGenericDao.createOrUpdate(implementation);
                    List<Treatment> treatments = (List<Treatment>) implementation.getTreatments();
                    for (int i = 0; i < treatments.size(); i++) {
                        treatments.get(i).setImplementation(implementationSaved);
                        treatments.get(i).setProduct(products.get(i));
                        treatmentGenericDaoDao.createOrUpdate(treatments.get(i));
                    }
                }
            }

        }
    }

    @NonNull
    private List<Product> getProducts() throws LMISException {
        Product product6x1 = productRepository.getByCode("08O05");
        Product product6x2 = productRepository.getByCode("08O05X");
        Product product6x3 = productRepository.getByCode("08O05Y");
        Product product6x4 = productRepository.getByCode("08O05Z");
        return newArrayList(product6x1, product6x2, product6x3, product6x4);
    }

    public MalariaProgram getPatientDataReportByPeriodAndType(final DateTime beginDate, final DateTime endDate) throws LMISException {
        return (MalariaProgram) dbUtil.withDao(MalariaProgram.class, new DbUtil.Operation<MalariaProgram, Object>() {
            @Override
            public MalariaProgram operate(Dao<MalariaProgram, String> dao) throws SQLException, LMISException {
                MalariaProgram queryPatientDataReport = dao.queryBuilder().where()
                        .eq("startDatePeriod", beginDate)
                        .and().eq("endDatePeriod", endDate).queryForFirst();
                return queryPatientDataReport;
            }
        });
    }


    public Optional<MalariaProgram> getFirstMovement() throws LMISException {
        MalariaProgram malariaProgram = (MalariaProgram) dbUtil.withDao(MalariaProgram.class, new DbUtil.Operation<MalariaProgram, Object>() {
            @Override
            public MalariaProgram operate(Dao<MalariaProgram, String> dao) throws SQLException, LMISException {
                MalariaProgram patientDataReport = dao.queryBuilder().orderBy("reportedDate", true).queryForFirst();
                return patientDataReport;
            }
        });
        if (malariaProgram != null) {
            return Optional.of(malariaProgram);
        }
        return Optional.absent();
    }
}
