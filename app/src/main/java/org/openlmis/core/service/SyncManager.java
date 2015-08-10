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

package org.openlmis.core.service;



import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.User;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.LMISRestManager;

import java.util.List;

import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


@Singleton
public class SyncManager {

    @Inject
    ProgramRepository programRepository;

    LMISRestApi lmisRestApi;

    public SyncManager(){
        lmisRestApi = new LMISRestManager().getLmisRestApi();
    }

    public void syncProductsWithProgram() throws Exception{
        User user = UserInfoMgr.getInstance().getUser();
        ProductRepository.ProductsResponse response = lmisRestApi.getProducts(user.getFacilityCode());
        List<Program> programsWithProducts = response.getProgramsWithProducts();
        for (Program programWithProducts : programsWithProducts) {
            try {
                programRepository.saveProgramWithProduct(programWithProducts);
            } catch (LMISException e) {
                e.printStackTrace();
            }
        }
    }

    public void syncProductsWithProgramAsync(Observer<Void> observer){
        rx.Observable.create(new rx.Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    syncProductsWithProgram();
                } catch (Exception e){
                    subscriber.onError(new LMISException("Get Product List Failed."));
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
    }

    public void syncStockCards(){

    }

    public void authorizeUser(){

    }
}
