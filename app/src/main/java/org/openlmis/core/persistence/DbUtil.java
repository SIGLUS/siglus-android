/*
 * Copyright (c) 2014, Thoughtworks Inc
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */

package org.openlmis.core.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.inject.Inject;
import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;


import org.openlmis.core.exceptions.LMISException;

import java.sql.SQLException;
import java.util.concurrent.Callable;

import static com.j256.ormlite.dao.DaoManager.createDao;

public class DbUtil {

    @Inject
    private Context context;

    public static <T> Dao<T, String> initialiseDao(SQLiteOpenHelper openHelper, Class<T> domainClass) throws SQLException {
        ConnectionSource connectionSource;
        if (openHelper instanceof LMISSqliteOpenHelper) {
            LMISSqliteOpenHelper helper = (LMISSqliteOpenHelper) openHelper;
            connectionSource = helper.getConnectionSource();
        } else {
            connectionSource = new AndroidConnectionSource(openHelper);
        }
        return createDao(connectionSource, domainClass);
    }

    public <DomainType, ReturnType> ReturnType withDao(
            Class<DomainType> domainClass, Operation<DomainType, ReturnType> operation) throws LMISException {
        SQLiteOpenHelper openHelper = LMISSqliteOpenHelper.getInstance(context);
        try {
            Dao<DomainType, String> dao = initialiseDao(openHelper, domainClass);
            return operation.operate(dao);
        } catch (SQLException e) {
            throw new LMISException(e);
        }
    }

    public <DomainType, ReturnType> ReturnType withDao(
            Context context, Class<DomainType> domainClass, Operation<DomainType, ReturnType> operation) throws LMISException {
        SQLiteOpenHelper openHelper = LMISSqliteOpenHelper.getInstance(context);
        try {
            Dao<DomainType, String> dao = initialiseDao(openHelper, domainClass);
            return operation.operate(dao);
        } catch (SQLException e) {
            throw new LMISException(e);
        }
    }

    public <DomainType, ReturnType> ReturnType withDaoAsBatch(
            Context context, Class<DomainType> domainClass, final Operation<DomainType, ReturnType> operation) throws LMISException {
        SQLiteOpenHelper openHelper = LMISSqliteOpenHelper.getInstance(context);
        try {
            final Dao<DomainType, String> dao = initialiseDao(openHelper, domainClass);
            return dao.callBatchTasks(new Callable<ReturnType>() {
                @Override
                public ReturnType call() throws Exception {
                    return operation.operate(dao);
                }
            });
        } catch (SQLException e) {
            throw new LMISException(e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new LMISException(e);
        }
    }

    public <DomainType, ReturnType> ReturnType withDaoAsBatch(
            Class<DomainType> domainClass, final Operation<DomainType, ReturnType> operation) throws LMISException {
        SQLiteOpenHelper openHelper = LMISSqliteOpenHelper.getInstance(context);
        try {
            final Dao<DomainType, String> dao = initialiseDao(openHelper, domainClass);
            return dao.callBatchTasks(new Callable<ReturnType>() {
                @Override
                public ReturnType call() throws Exception {
                    return operation.operate(dao);
                }
            });
        } catch (SQLException e) {
            throw new LMISException(e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new LMISException(e);
        }
    }

    public interface Operation<DomainType, ReturnType> {
        ReturnType operate(Dao<DomainType, String> dao) throws SQLException;
    }
}
