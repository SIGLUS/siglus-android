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
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.repository.ALRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.view.BaseView;
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

import static org.openlmis.core.view.viewmodel.ALGridViewModel.COLUMN_CODE_PREFIX_STOCK;
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
    public void attachView(BaseView baseView) throws ViewNotMatchException {
        if (baseView instanceof ALRequisitionPresenter.ALRequisitionView) {
            this.view = (ALRequisitionPresenter.ALRequisitionView) baseView;
        } else {
            throw new ViewNotMatchException(ALRequisitionPresenter.ALRequisitionView.class.getName());
        }
        super.attachView(baseView);
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
            alReportViewModel = new ALReportViewModel(rnRForm);
            view.refreshRequisitionForm(rnRForm);
            view.setProcessButtonName(rnRForm.isDraft()
                    ? context.getResources().getString(R.string.btn_submit)
                    : context.getResources().getString(R.string.btn_complete));
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

    public void setViewModels() throws LMISException {
        List<RegimenItem> regimenItems = new ArrayList<>();
        for (ALGridViewModel gridViewModel : alReportViewModel.getItemTotal().getAlGridViewModelList()) {
            String columnName = gridViewModel.getColumnCode().getColumnName();
            addTreatment(regimenItems, gridViewModel, columnName);
            addStock(regimenItems, gridViewModel, columnName);
        }
        rnRForm.setRegimenItemListWrapper(regimenItems);
    }


    private void addTreatment(List<RegimenItem> regimenItems, ALGridViewModel gridViewModel, String columnName) throws LMISException {
        RegimenItem itemTreatment = getRegimenItem(COLUMN_CODE_PREFIX_TREATMENTS + columnName, getRegimenType(columnName));
        itemTreatment.setHf(alReportViewModel.getItemHF().getAlGridViewModelMap().get(columnName).getTreatmentsValue());
        itemTreatment.setChw(alReportViewModel.getItemCHW().getAlGridViewModelMap().get(columnName).getTreatmentsValue());
        itemTreatment.setAmount(gridViewModel.getTreatmentsValue());
        regimenItems.add(itemTreatment);
    }

    private RegimenItem getRegimenItem(String name, Regimen.RegimeType category) throws LMISException {
        RegimenItem regimenItem = getRegimenItemFromFormList(name);
        if (regimenItem == null) {
            RegimenItem newRegimenItem = new RegimenItem();
            Regimen regimen = alRepository.getByNameAndCategory(name, category);
            newRegimenItem.setRegimen(regimen);
            newRegimenItem.setForm(rnRForm);
            return newRegimenItem;
        }
        return regimenItem;
    }

    private RegimenItem getRegimenItemFromFormList(String name) {
        for (RegimenItem regimenItem : rnRForm.getRegimenItemList()) {
            if (regimenItem.getRegimen().getName().equals(name)) {
                return regimenItem;
            }
        }
        return null;
    }

    private void addStock(List<RegimenItem> regimenItems, ALGridViewModel gridViewModel, String columnName) throws LMISException {
        RegimenItem itemStock = getRegimenItem(COLUMN_CODE_PREFIX_STOCK + columnName, getRegimenType(columnName));
        itemStock.setHf(alReportViewModel.getItemHF().getAlGridViewModelMap().get(columnName).getExistentStockValue());
        itemStock.setChw(alReportViewModel.getItemCHW().getAlGridViewModelMap().get(columnName).getExistentStockValue());
        itemStock.setAmount(gridViewModel.getExistentStockValue());
        regimenItems.add(itemStock);
    }

    private Regimen.RegimeType getRegimenType(String columnName) {
        if (columnName.equals(ALGridViewModel.ALColumnCode.OneColumn.getColumnName())
                || columnName.equals(ALGridViewModel.ALColumnCode.TwoColumn.getColumnName())) {
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
