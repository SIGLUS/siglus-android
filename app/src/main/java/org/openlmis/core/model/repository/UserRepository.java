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
import org.openlmis.core.network.RestRepository;
import org.openlmis.core.persistence.DbUtil;
import org.openlmis.core.persistence.GenericDao;
import org.w3c.dom.UserDataHandler;

import java.sql.SQLException;
import java.util.List;

import lombok.Data;
import retrofit.Callback;

public class UserRepository extends RestRepository {

    @Inject
    DbUtil dbUtil;

    GenericDao<User> genericDao;


    @Inject
    public UserRepository(Context context){
        genericDao = new GenericDao<>(User.class, context);
    }


    public void getUser(String username, String password, Callback<UserResponse> callback) {
        lmisRestApi.authorizeUser(username, password, callback);
    }

    public User getUserForLocalDatabase(final String userName, final String password) {
        List<User> users = null;
        try {
            users = dbUtil.withDao(User.class, new DbUtil.Operation<User, List<User>>() {
                @Override
                public List<User> operate(Dao<User, String> dao) throws SQLException {
                    User user = new User();
                    user.setUserName(userName);
                    user.setPassword(password);
                    return dao.queryForMatchingArgs(user);
                }
            });
        } catch (LMISException e) {
            e.printStackTrace();
        }

        return users == null ? null : users.get(0);
    }

    public void save(final User user) {
        try {
            genericDao.createOrUpdate(user);
        } catch (LMISException e) {
            e.printStackTrace();
        }
    }

    public
    @Data
    class UserResponse {
        User userProfile;
    }
}
