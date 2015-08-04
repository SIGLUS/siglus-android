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
