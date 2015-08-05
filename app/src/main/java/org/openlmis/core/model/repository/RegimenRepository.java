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

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.persistence.GenericDao;

import java.util.List;

public class RegimenRepository {

    GenericDao<Regimen> regimenGenericDao;

    @Inject
    public RegimenRepository(Context context){
        this.regimenGenericDao = new GenericDao<>(Regimen.class, context);
    }


    public List<Regimen> list() throws LMISException {
        return  regimenGenericDao.queryForAll();
    }

    public void save(Regimen regimen) throws LMISException{
        regimenGenericDao.create(regimen);
    }
}
