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
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.repository.ALRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.view.viewmodel.ALGridViewModel;
import org.openlmis.core.view.viewmodel.ALReportViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static org.openlmis.core.view.viewmodel.ALGridViewModel.COLUMN_CODE_PREFIX_TREATMENTS;

public class ALRequisitionPresenter extends BaseRequisitionPresenter {
    ALRequisitionView view;
    private ALRepository alRepository;
    public ALReportViewModel alReportViewModel;

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
            alReportViewModel = new ALReportViewModel(rnRForm);
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

    public Observable<Void> getSaveFormObservable() {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    setViewModels();
                    rnrFormRepository.createOrUpdateWithItems(rnRForm);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    e.reportToFabric();
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    public boolean isComplete() {
        return alReportViewModel.isComplete();
    }

    public void setViewModels() {
        List<RegimenItem> regimenItems =  new ArrayList<>();
        for(ALGridViewModel gridViewModel : alReportViewModel.getItemTotal().rapidTestFormGridViewModelList) {
            String columnName = gridViewModel.getColumnCode().getColumnName();
            addTreatment(regimenItems, gridViewModel, columnName);
            addStock(regimenItems, gridViewModel, columnName);
        }
        rnRForm.setRegimenItemListWrapper(regimenItems);
    }


    private void addTreatment(List<RegimenItem> regimenItems, ALGridViewModel gridViewModel, String columnName) {
        RegimenItem itemTreatment = new RegimenItem();
        itemTreatment.setForm(rnRForm);
        itemTreatment.setHf(alReportViewModel.getItemHF().rapidTestFormGridViewModelMap.get(columnName).getTreatmentsValue());
        itemTreatment.setChw(alReportViewModel.getItemCHW().rapidTestFormGridViewModelMap.get(columnName).getTreatmentsValue());
        itemTreatment.setAmount(gridViewModel.getTreatmentsValue());
        Regimen regimenTreatment = new Regimen();
        regimenTreatment.setName(COLUMN_CODE_PREFIX_TREATMENTS + columnName);
        regimenTreatment.setType(getRegimenType(columnName));
        itemTreatment.setRegimen(regimenTreatment);
        regimenItems.add(itemTreatment);
    }

    private void addStock(List<RegimenItem> regimenItems, ALGridViewModel gridViewModel, String columnName) {
        RegimenItem itemStock = new RegimenItem();
        itemStock.setForm(rnRForm);
        itemStock.setHf(alReportViewModel.getItemHF().rapidTestFormGridViewModelMap.get(columnName).getExistentStockValue());
        itemStock.setChw(alReportViewModel.getItemCHW().rapidTestFormGridViewModelMap.get(columnName).getExistentStockValue());
        itemStock.setAmount(gridViewModel.getExistentStockValue());
        Regimen regimenStock = new Regimen();
        regimenStock.setName(COLUMN_CODE_PREFIX_TREATMENTS + columnName);
        regimenStock.setType(getRegimenType(columnName));
        itemStock.setRegimen(regimenStock);
        regimenItems.add(itemStock);
    }

    private Regimen.RegimeType getRegimenType(String columnName) {
        if (columnName.equals(ALGridViewModel.ALColumnCode.OneColumn.getColumnName()) ||
                columnName.equals(ALGridViewModel.ALColumnCode.TwoColumn.getColumnName())) {
            return Regimen.RegimeType.Paediatrics;
        } else {
            return Regimen.RegimeType.Adults;
        }

    }

    @Override
    protected int getCompleteErrorMessage() {
        return R.string.hint_al_complete_failed;
    }

    public interface ALRequisitionView extends BaseRequisitionPresenter.BaseRequisitionView {
        void setProcessButtonName(String buttonName);
    }
}
