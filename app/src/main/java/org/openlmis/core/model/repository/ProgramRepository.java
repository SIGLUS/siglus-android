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

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Program;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.openlmis.core.persistence.LmisSqliteOpenHelper;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

public class ProgramRepository {

    GenericDao<Program> genericDao;

    @Inject
    ProductRepository productRepository;

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

    public void createOrUpdate(Program program) throws LMISException {
        Program existingProgram = queryByCode(program.getProgramCode());
        if (existingProgram != null) {
            program.setId(existingProgram.getId());
            genericDao.update(program);
        } else {
            genericDao.create(program);
        }
    }

    public void createOrUpdateProgramWithProduct(final List<Program> programs) throws LMISException {
        try {
            TransactionManager.callInTransaction(LmisSqliteOpenHelper.getInstance(context).getConnectionSource(), new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    for (Program program : programs) {
                        createOrUpdate(program);
                        for (Product product : program.getProducts()) {
                            product.setProgram(program);
                            productRepository.createOrUpdate(product);
                        }
                    }
                    return null;
                }
            });
        } catch (SQLException e) {
            throw new LMISException(e);
        }
    }

    public void refresh(Program programsWithProducts) {
        try {
            genericDao.refresh(programsWithProducts);
        } catch (LMISException e) {
            e.reportToFabric();
        }
    }

    public Program queryByCode(final String programCode) throws LMISException {
        return dbUtil.withDao(Program.class, new DbUtil.Operation<Program, Program>() {
            @Override
            public Program operate(Dao<Program, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("programCode", programCode).queryForFirst();
            }
        });
    }

    public List<Long> queryProgramIdsByProgramCodeOrParentCode(final String programCode) throws LMISException {
        List<Program> programs = queryProgramsByProgramCodeOrParentCode(programCode);

        List<Long> programIds = FluentIterable.from(programs).transform(new Function<Program, Long>() {
            @Override
            public Long apply(Program program) {
                return program.getId();
            }
        }).toList();
        return programIds;
    }

    public List<String> queryProgramCodesByProgramCodeOrParentCode(final String programCode) throws LMISException {
        List<Program> programs = queryProgramsByProgramCodeOrParentCode(programCode);

        List<String> programCodes = FluentIterable.from(programs).transform(new Function<Program, String>() {
            @Override
            public String apply(Program program) {
                return program.getProgramCode();
            }
        }).toList();

        return programCodes;
    }

    public List<Program> queryProgramsByProgramCodeOrParentCode(final String programCode) throws LMISException {
        return dbUtil.withDao(Program.class, new DbUtil.Operation<Program, List<Program>>() {
                @Override
                public List<Program> operate(Dao<Program, String> dao) throws SQLException, LMISException {
                    return dao.queryBuilder().where().eq("parentCode", programCode)
                            .or().eq("programCode", programCode).query();
                }
            });
    }

    public List<Long> getProgramIdsByProgramCode(String programCode) throws LMISException {
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_rnr_multiple_programs)) {
            return queryProgramIdsByProgramCodeOrParentCode(programCode);
        } else {
            Program program = queryByCode(programCode);
            return newArrayList(program.getId());
        }
    }
}
