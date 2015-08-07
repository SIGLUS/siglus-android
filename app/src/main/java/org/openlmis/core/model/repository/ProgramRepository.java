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

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Program;
import org.openlmis.core.network.RestRepository;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

import java.sql.SQLException;
import java.util.List;

public class ProgramRepository extends RestRepository {

    GenericDao<Program> genericDao;

    @Inject
    DbUtil dbUtil;

    @Inject
    public ProgramRepository(Context context) {
        genericDao = new GenericDao<>(Program.class, context);
    }

    public List<Program> list() throws LMISException{
        return  genericDao.queryForAll();
    }

    public void save(final List<Program> products) {
        try {
            dbUtil.withDaoAsBatch(Program.class, new DbUtil.Operation<Program, Void>() {
                @Override
                public Void operate(Dao<Program, String> dao) throws SQLException {
                    for (Program product : products){
                        dao.create(product);
                    }
                    return null;
                }
            });
        } catch (LMISException e) {
            e.printStackTrace();
        }
    }

    public void create(Program program) throws LMISException {
        genericDao.create(program);
    }

    public void refresh(Program programsWithProducts) {
        try {
            genericDao.refresh(programsWithProducts);
        } catch (LMISException e) {
            e.printStackTrace();
        }
    }
}
