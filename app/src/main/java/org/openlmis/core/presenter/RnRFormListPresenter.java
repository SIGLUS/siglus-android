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


import android.support.annotation.NonNull;

import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.Inventory;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.SyncError;
import org.openlmis.core.model.SyncType;
import org.openlmis.core.model.repository.InventoryRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.SyncErrorsRepository;
import org.openlmis.core.model.service.PeriodService;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.RnRFormViewModel;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import lombok.Setter;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RnRFormListPresenter extends Presenter {

    RnRFormListView view;

    @Inject
    RnrFormRepository repository;

    @Inject
    InventoryRepository inventoryRepository;

    @Inject
    SyncErrorsRepository syncErrorsRepository;

    @Setter
    String programCode;

    @Inject
    SharedPreferenceMgr sharedPreferenceMgr;

    @Inject
    PeriodService periodService;

    @Override
    public void attachView(BaseView v) throws ViewNotMatchException {
        if (v instanceof RnRFormListView) {
            view = (RnRFormListView) v;
        } else {
            throw new ViewNotMatchException("Need RnRFormListView");
        }
    }

    public Observable<List<RnRFormViewModel>> loadRnRFormList() {
        return Observable.create(new Observable.OnSubscribe<List<RnRFormViewModel>>() {
            @Override
            public void call(Subscriber<? super List<RnRFormViewModel>> subscriber) {
                try {
                    subscriber.onNext(buildFormListViewModels());
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @NonNull
    protected List<RnRFormViewModel> buildFormListViewModels() throws LMISException {
        List<RnRFormViewModel> viewModels = new ArrayList<>();

        List<RnRForm> rnRForms = repository.list(programCode);

        if (rnRForms == null) {
            return viewModels;
        }

        Period currentPeriod = generatePeriod();

        if (rnRForms.isEmpty()) {
            if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_home_page_update)) {
                viewModels.add(generateRnrFormViewModelWithoutRnrForm(currentPeriod));
            }
            return viewModels;
        }

        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_home_page_update)) {
            if (hasNoRnrFormInCurrentPeriod(rnRForms, currentPeriod)) {
                viewModels.add(generateRnrFormViewModelWithoutRnrForm(currentPeriod));
            }
        }

        Collections.reverse(rnRForms);

        addMissedPeriodViewModel(viewModels, rnRForms);

        addCurrentPeriodViewModel(viewModels, rnRForms);

        addPreviousPeriodViewModels(viewModels, rnRForms);

        populateSyncErrorsOnViewModels(viewModels);

        return viewModels;
    }

    protected Period generatePeriod() throws LMISException {
        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_requisition_period_logic_change)) {
            return periodService.generatePeriod(programCode, null);
        } else {
            return DateUtil.generateRnRFormPeriodBy(new Date());
        }
    }

    private boolean hasNoRnrFormInCurrentPeriod(List<RnRForm> rnRForms, Period currentPeriod) {
        RnRForm lastRnrForm = rnRForms.get(rnRForms.size() - 1);
        Date currentPeriodBegin = currentPeriod.getBegin().toDate();
        return lastRnrForm.getStatus().equals(RnRForm.STATUS.AUTHORIZED) && lastRnrForm.getPeriodBegin().before(currentPeriodBegin);
    }

    private RnRFormViewModel generateRnrFormViewModelWithoutRnrForm(Period currentPeriod) throws LMISException {

        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_requisition_period_logic_change)) {
            if (isCanNotCreateRnr(currentPeriod)) {
                return new RnRFormViewModel(currentPeriod, programCode, RnRFormViewModel.TYPE_CAN_NOT_CREATE_RNR);
            }

            List<Inventory> physicalInventories = inventoryRepository.queryPeriodInventory(currentPeriod);

            if (physicalInventories == null || physicalInventories.size() == 0) {
                return new RnRFormViewModel(currentPeriod, programCode, RnRFormViewModel.TYPE_UNCOMPLETE_INVENTORY);
            } else {
                return new RnRFormViewModel(currentPeriod, programCode, RnRFormViewModel.TYPE_SELECT_CLOSE_OF_PERIOD);
            }
        } else {
            Date latestPhysicalInventoryTime = DateUtil.parseString(sharedPreferenceMgr.getLatestPhysicInventoryTime(), DateUtil.DATE_TIME_FORMAT);

            Date periodBegin = currentPeriod.getBegin().toDate();
            if (latestPhysicalInventoryTime.before(periodBegin)) {
                return new RnRFormViewModel(currentPeriod, programCode, RnRFormViewModel.TYPE_UNCOMPLETE_INVENTORY);
            } else {
                return new RnRFormViewModel(currentPeriod, programCode, RnRFormViewModel.TYPE_CLOSE_OF_PERIOD_SELECTED);
            }
        }
    }

    private boolean isCanNotCreateRnr(Period currentPeriod) {
        DateTime dateTime = new DateTime(LMISApp.getInstance().getCurrentTimeMillis());
        return dateTime.isAfter(currentPeriod.getBegin()) && dateTime.isBefore(currentPeriod.getInventoryBegin());
    }

    private void populateSyncErrorsOnViewModels(final List<RnRFormViewModel> rnrViewModels) {
        for (RnRFormViewModel rnrViewModel : rnrViewModels) {
            rnrViewModel.setSyncServerErrorMessage(getRnrFormSyncError(rnrViewModel.getId()));
        }
    }

    private String getRnrFormSyncError(long rnrId) {
        List<SyncError> syncErrorList = syncErrorsRepository.getBySyncTypeAndObjectId(SyncType.RnRForm, rnrId);
        if (null == syncErrorList || syncErrorList.isEmpty())
            return null;
        return syncErrorList.get(syncErrorList.size() - 1).getErrorMessage();
    }

    protected void addPreviousPeriodViewModels(List<RnRFormViewModel> viewModels, List<RnRForm> rnRForms) {
        viewModels.addAll(FluentIterable.from(rnRForms).transform(new Function<RnRForm, RnRFormViewModel>() {
            @Override
            public RnRFormViewModel apply(RnRForm form) {
                return new RnRFormViewModel(form);
            }
        }).toList());
    }

    protected void addCurrentPeriodViewModel(List<RnRFormViewModel> viewModels, List<RnRForm> rnRForms) {
        if (rnRForms.get(0).getStatus() != RnRForm.STATUS.AUTHORIZED) {
            viewModels.add(new RnRFormViewModel(rnRForms.get(0)));
            rnRForms.remove(0);
        }
    }

    private void addMissedPeriodViewModel(List<RnRFormViewModel> viewModels, List<RnRForm> rnRForms) {
        int currentMonth = new DateTime().getMonthOfYear();
        int periodEndMonth = new DateTime(rnRForms.get(0).getPeriodEnd()).getMonthOfYear();
        if (currentMonth != periodEndMonth) {
            viewModels.add(0, RnRFormViewModel.buildMissedPeriod());
        }
    }

    public void deleteRnRForm(RnRForm form) throws LMISException {
        repository.removeRnrForm(form);
    }

    public interface RnRFormListView extends BaseView {

    }
}
