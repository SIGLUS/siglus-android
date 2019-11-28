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
import org.openlmis.core.model.User;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;

import java.sql.SQLException;
import java.util.List;

public class UserRepository {

    @Inject
    DbUtil dbUtil;

    GenericDao<User> genericDao;

    @Inject
    public UserRepository(Context context) {
        genericDao = new GenericDao<>(User.class, context);
    }

    public User mapUserFromLocal(final User user) {
        try {
            User userQueried = dbUtil.withDao(User.class, new DbUtil.Operation<User, User>() {
                @Override
                public User operate(Dao<User, String> dao) throws SQLException {
                    return dao.queryBuilder().where().eq("username", user.getUsername()).and().eq("password", user.getPasswordMD5()).queryForFirst();
                }
            });
            if (userQueried != null) {
                userQueried.setPassword(user.getPassword());
            }
            return userQueried;
        } catch (LMISException e) {
            new LMISException(e,"UserRepository.mapUserFromLocal").reportToFabric();
        }
        return null;
    }

    public void createOrUpdate(final User user) {
        try {
            List<User> userByUsername = getUserByUsername(user.getUsername());
            if (userByUsername.isEmpty()) {
                genericDao.create(user);
            } else {
                user.setId(userByUsername.get(userByUsername.size() - 1).getId());
                genericDao.update(user);
            }
        } catch (LMISException e) {
            new LMISException(e,"UserRepository.createOrUpdate").reportToFabric();
        }
    }

    protected List<User> getUserByUsername(final String userName) throws LMISException {
        return dbUtil.withDao(User.class, new DbUtil.Operation<User, List<User>>() {
            @Override
            public List<User> operate(Dao<User, String> dao) throws SQLException {
                return dao.queryBuilder().where().eq("username", userName).query();
            }
        });
    }

    public User getLocalUser() {
        User user = null;
        try {
            user = dbUtil.withDao(User.class, new DbUtil.Operation<User, User>() {
                @Override
                public User operate(Dao<User, String> dao) throws SQLException {
                    return dao.queryBuilder().queryForFirst();
                }
            });
        } catch (LMISException e) {
            e.printStackTrace();
            new LMISException(e,"UserRepository.getLocalUser").reportToFabric();
        }
        return user;
    }
}
