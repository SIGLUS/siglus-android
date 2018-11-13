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

package org.openlmis.core.presenter;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.repository.ALRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.view.viewmodel.ALReportViewModel;

import java.util.Date;

import roboguice.RoboGuice;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ALRequisitionPresenter extends BaseRequisitionPresenter {

    ALRequisitionView view;
    private ALRepository alRepository;
    protected ALReportViewModel alReportViewModel;

    @Override
    protected RnrFormRepository initRnrFormRepository() {
        alRepository = RoboGuice.getInjector(LMISApp.getContext()).getInstance(ALRepository.class);
        return alRepository;
    }

    @Override
    public void loadData(long formId, Date periodEndDate) {
        this.periodEndDate = periodEndDate;
        view.loading();
        Subscription subscription = getRnrFormObservable(formId).subscribe(loadDataOnNextAction, loadDataOnErrorAction);
        subscriptions.add(subscription);

    }

    @Override
    public void updateUIAfterSubmit() {
        view.setProcessButtonName(context.getResources().getString(R.string.btn_complete));

    }

    @Override
    public void updateFormUI() {
        if (rnRForm != null) {
            view.refreshRequisitionForm(rnRForm);
            view.setProcessButtonName(rnRForm.isDraft()?
                    context.getResources().getString(R.string.btn_submit) :
                    context.getResources().getString(R.string.btn_complete));
        }
    }

    @Override
    protected Observable<RnRForm> getRnrFormObservable(long formId) {
        return Observable.create(new Observable.OnSubscribe<RnRForm>() {
            @Override
            public void call(Subscriber<? super RnRForm> subscriber) {
                try {
                    rnRForm = getRnrForm(formId);
                    subscriber.onNext(rnRForm);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    e.reportToFabric();
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

//    public Observable<Void> getSaveFormObservable(final List<RnrFormItem> rnrFormItems, final List<RegimenItem> regimenItems, final List<BaseInfoItem> baseInfoItems, final String comment) {
//        return Observable.create(new Observable.OnSubscribe<Void>() {
//            @Override
//            public void call(Subscriber<? super Void> subscriber) {
//                try {
//                    setViewModels(rnrFormItems, regimenItems, baseInfoItems, comment);
//                    rnrFormRepository.createOrUpdateWithItems(rnRForm);
//                    subscriber.onCompleted();
//                } catch (LMISException e) {
//                    e.reportToFabric();
//                    subscriber.onError(e);
//                }
//            }
//        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
//    }

    @Override
    protected int getCompleteErrorMessage() {
        return R.string.hint_al_complete_failed;
    }


    public interface ALRequisitionView extends BaseRequisitionPresenter.BaseRequisitionView {
        void setProcessButtonName(String buttonName);
    }
}
