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
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.network.LMISRestApi;
import org.openlmis.core.network.LMISRestManager;
import org.openlmis.core.network.model.SyncDownRequisitionsResponse;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Singleton
public class SyncBackManager {
    private boolean saveRequisitionLock = false;

    @Inject
    SharedPreferenceMgr sharedPreferenceMgr;

    @Inject
    RnrFormRepository rnrFormRepository;

    protected LMISRestApi lmisRestApi;

    public SyncBackManager() {
        lmisRestApi = new LMISRestManager().getLmisRestApi();
    }

    public void syncBackRnr(Observer<Void> observer) {
        Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    fetchAndSaveRequisitionData();
                } catch (LMISException e) {
                    e.reportToFabric();
                    subscriber.onError(new LMISException("Syncing back data failed"));
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
    }

    public void fetchAndSaveRequisitionData() throws LMISException {
        SyncDownRequisitionsResponse syncDownRequisitionsResponse = lmisRestApi.fetchRequisitions(UserInfoMgr.getInstance().getUser().getFacilityCode());

        if (syncDownRequisitionsResponse == null) {
            throw new LMISException("Can't get SyncDownRequisitionsResponse, you can check json parse to POJO logic");
        }

        if (saveRequisitionLock || sharedPreferenceMgr.isRequisitionDataSynced()) {
            throw new LMISException("Sync Requisition Background or Loaded");
        }
        saveRequisitionLock = true;

        try {
            List<RnRForm> rnRForms = syncDownRequisitionsResponse.getRequisitions();
            for (RnRForm form : rnRForms) {
                rnrFormRepository.createFormAndItems(form);
            }
            sharedPreferenceMgr.setRequisitionDataSynced(true);
        } finally {
            saveRequisitionLock = false;
        }
    }
}
