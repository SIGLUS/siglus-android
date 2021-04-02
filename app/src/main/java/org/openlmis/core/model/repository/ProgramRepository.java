/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */


package org.openlmis.core.model.repository;

import android.content.Context;
import android.database.Cursor;

import com.google.inject.Inject;
import com.j256.ormlite.misc.TransactionManager;

import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Program;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.openlmis.core.utils.Constants;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProgramRepository {

    GenericDao<Program> genericDao;

    @Inject
    ProductRepository productRepository;

    @Inject
    RegimenRepository regimenRepository;

    @Inject
    DbUtil dbUtil;
    private Context context;

    @Inject
    public ProgramRepository(Context context) {
        this.context = context;
        genericDao = new GenericDao<>(Program.class, context);
    }

    public List<Program> list() throws LMISException {
        return genericDao.queryForAll();
    }

    public List<Program> listEmergencyPrograms() throws LMISException {
        return dbUtil.withDao(Program.class, dao -> dao.queryBuilder().where().eq("isSupportEmergency", true).query());
    }

    public void createOrUpdate(Program program) throws LMISException {
        Program existingProgram = queryByCode(program.getProgramCode());
        if (existingProgram != null) {
            program.setId(existingProgram.getId());
            genericDao.update(program);
        } else {
            genericDao.create(program);
        }
    }

    public void updateProgramWithRegimen(final List<Program> programs) throws LMISException {
        createOrUpdateProgramWithRegimen(programs);
    }

    private void createOrUpdateProgramWithRegimen(final List<Program> programs) throws LMISException {
        try {
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), () -> {
                for (Program program : programs) {
                    createOrUpdate(program);
                    regimenRepository.batchSave(new ArrayList(program.getRegimens()));
                }
                return null;
            });
        } catch (SQLException e) {
            throw new LMISException(e);
        }
    }

    public void createOrUpdateProgramWithProduct(final List<Program> programs) throws LMISException {
        try {
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), () -> {
                for (Program program : programs) {
                    createOrUpdate(program);
                    for (Product product : program.getProducts()) {
                        productRepository.createOrUpdate(product);
                    }
                }
                return null;
            });
        } catch (SQLException e) {
            throw new LMISException(e);
        }
    }

    public void refresh(Program programsWithProducts) {
        try {
            genericDao.refresh(programsWithProducts);
        } catch (LMISException e) {
            new LMISException(e, "ProgramRepository.refresh").reportToFabric();
        }
    }

    public Program queryByCode(final String programCode) throws LMISException {
        return dbUtil.withDao(Program.class, dao -> dao.queryBuilder().where().eq("programCode", programCode).queryForFirst());
    }

    public List<Long> queryProgramIdsByProgramCodeOrParentCode(final String programCode) throws LMISException {
        List<Program> programs = queryProgramsByProgramCodeOrParentCode(programCode);

        List<Long> programIds = FluentIterable.from(programs).transform(program -> program.getId()).toList();
        return programIds;
    }

    public List<String> queryProgramCodesByProgramCodeOrParentCode(final String programCode) throws LMISException {
        List<Program> programs = queryProgramsByProgramCodeOrParentCode(programCode);

        List<String> programCodes = FluentIterable.from(programs).transform(program -> program.getProgramCode()).toList();

        return programCodes;
    }

    public List<Program> queryProgramsByProgramCodeOrParentCode(final String programCode) throws LMISException {
        return dbUtil.withDao(Program.class, dao -> dao.queryBuilder()
                .where().eq("parentCode", programCode)
                .or().eq("programCode", programCode).query());
    }
}
