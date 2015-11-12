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

import android.content.Context;
import android.text.TextUtils;

import com.google.inject.Inject;

import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.PeriodNotUniqueException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.repository.VIARepository;
import org.openlmis.core.service.SyncManager;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;
import org.roboguice.shaded.goole.common.base.Function;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static org.roboguice.shaded.goole.common.base.Preconditions.checkNotNull;
import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;


public class RequisitionPresenter implements Presenter {

    @Inject
    VIARepository viaRepository;

    @Inject
    Context context;

    @Inject
    SyncManager syncManager;

    RequisitionView view;

    @Getter
    protected RnRForm rnRForm;
    protected List<RequisitionFormItemViewModel> requisitionFormItemViewModelList;
    private Subscription subscribe;

    public RequisitionPresenter() {
        requisitionFormItemViewModelList = new ArrayList<>();
    }


    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {
        if (subscribe != null) {
            subscribe.unsubscribe();
            subscribe = null;
        }
    }

    @Override
    public void attachView(BaseView v) throws ViewNotMatchException {
        if (v instanceof RequisitionView) {
            this.view = (RequisitionView) v;
        } else {
            throw new ViewNotMatchException("required RequisitionView");
        }
    }

    public RnRForm loadRnrForm(long formId) {
        try {
            if (formId > 0) {
                rnRForm = viaRepository.queryRnRForm(formId);
            } else {
                RnRForm draftVIA = viaRepository.getDraftVIA();
                if (draftVIA != null) {
                    rnRForm = draftVIA;
                } else {
                    rnRForm = viaRepository.initVIA();
                }
            }
            return rnRForm;
        } catch (LMISException e) {
            view.showErrorMessage(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


    public List<RequisitionFormItemViewModel> getRequisitionViewModelList() {
        return requisitionFormItemViewModelList;
    }

    protected List<RequisitionFormItemViewModel> createViewModelsFromRnrForm(long formId) {
        if (rnRForm == null) {
            loadRnrForm(formId);
        }
        return from(rnRForm.getRnrFormItemList()).transform(new Function<RnrFormItem, RequisitionFormItemViewModel>() {
            @Override
            public RequisitionFormItemViewModel apply(RnrFormItem item) {
                return new RequisitionFormItemViewModel(item);
            }
        }).toList();
    }

    public void loadRequisitionFormList(final long formId) {

        if (requisitionFormItemViewModelList.size() > 0) {
            updateRequisitionFormUI();
            return;
        }

        view.loading();

        subscribe = Observable.create(new Observable.OnSubscribe<List<RequisitionFormItemViewModel>>() {
            @Override
            public void call(Subscriber<? super List<RequisitionFormItemViewModel>> subscriber) {
                subscriber.onNext(createViewModelsFromRnrForm(formId));
                subscriber.onCompleted();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(new Action1<List<RequisitionFormItemViewModel>>() {
            @Override
            public void call(List<RequisitionFormItemViewModel> requisitionFormItemViewModels) {
                requisitionFormItemViewModelList.addAll(requisitionFormItemViewModels);
                updateRequisitionFormUI();
                view.loaded();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                view.loaded();
            }
        });
    }

    protected void updateRequisitionFormUI() {
        if (rnRForm.getStatus() == RnRForm.STATUS.DRAFT) {
            view.highLightRequestAmount();
        } else if (rnRForm.getStatus() == RnRForm.STATUS.SUBMITTED) {
            view.setProcessButtonName(context.getString(R.string.btn_complete));
            view.highLightApprovedAmount();
        }
        view.refreshRequisitionForm();
    }


    protected boolean validateFormInput() {
        List<RequisitionFormItemViewModel> requisitionViewModelList = getRequisitionViewModelList();
        for (int i = 0; i < requisitionViewModelList.size(); i++) {
            RequisitionFormItemViewModel itemViewModel = requisitionViewModelList.get(i);
            if (TextUtils.isEmpty(itemViewModel.getRequestAmount())
                    || TextUtils.isEmpty(itemViewModel.getApprovedAmount())) {
                view.showListInputError(i);
                return false;
            }
        }
        return true;
    }

    public void processRequisition(String consultationNumbers) {
        if (!validateFormInput()) {
            return;
        }
        setRnrFormAmount();
        rnRForm.getBaseInfoItemListWrapper().get(0).setValue(consultationNumbers);

        if (rnRForm.getStatus() == RnRForm.STATUS.DRAFT) {
            submitRequisition();
        } else {
            authorise();
        }
    }

    public void submitRequisition() {
        view.loading();
        Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    viaRepository.submit(rnRForm);
                    subscriber.onNext(null);
                } catch (LMISException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(new Subscriber<Void>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                view.loaded();
                view.showErrorMessage(e.getMessage());
            }

            @Override
            public void onNext(Void aVoid) {
                view.loaded();
                view.highLightApprovedAmount();
                view.refreshRequisitionForm();
                view.setProcessButtonName(context.getResources().getString(R.string.btn_complete));

            }
        });
    }

    private void authorise() {
        view.loading();
        Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    viaRepository.authorise(rnRForm);
                    subscriber.onNext(null);
                } catch (LMISException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(new Subscriber<Void>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                view.loaded();
                if (e instanceof PeriodNotUniqueException) {
                    view.showErrorMessage(context.getResources().getString(R.string.msg_requisition_not_unique));
                } else {
                    view.showErrorMessage(e.getMessage());
                }
            }

            @Override
            public void onNext(Void aVoid) {
                view.loaded();
                view.completeSuccess();
                syncManager.requestSyncImmediately();
            }
        });

    }

    private void setRnrFormAmount() {
        ArrayList<RnrFormItem> rnrFormItemListWrapper = rnRForm.getRnrFormItemListWrapper();
        for (int i = 0; i < rnrFormItemListWrapper.size(); i++) {
            String requestAmount = requisitionFormItemViewModelList.get(i).getRequestAmount();
            if (!TextUtils.isEmpty(requestAmount)) {
                rnrFormItemListWrapper.get(i).setRequestAmount(Long.valueOf(requestAmount));
            }

            String approvedAmount = requisitionFormItemViewModelList.get(i).getApprovedAmount();
            if (!TextUtils.isEmpty(approvedAmount)) {
                rnrFormItemListWrapper.get(i).setApprovedAmount(Long.valueOf(approvedAmount));
            }
        }
    }

    public void saveRequisition(String consultationNumbers) {
        view.loading();
        setRnrFormAmount();
        if (!TextUtils.isEmpty(consultationNumbers)) {
            rnRForm.getBaseInfoItemListWrapper().get(0).setValue(Long.valueOf(consultationNumbers).toString());
        }

        Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    viaRepository.save(rnRForm);
                    subscriber.onNext(null);
                } catch (LMISException e) {
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                view.loaded();
                view.goToHomePage();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                view.loaded();
                view.showErrorMessage(throwable.getMessage());
            }
        });
    }

    public String getConsultationNumbers() {
        String value;
        try {
            value = rnRForm.getBaseInfoItemListWrapper().get(0).getValue();
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            e.printStackTrace();
            value = "";
        }
        return value == null ? "" : value;
    }

    public void removeRnrForm() {
        try {
            viaRepository.removeRnrForm(rnRForm);
        } catch (LMISException e) {
            e.printStackTrace();
        }
    }

    public void setConsultationNumbers(String consultationNumbers) {
        ArrayList<BaseInfoItem> baseInfoItemListWrapper = rnRForm.getBaseInfoItemListWrapper();
        if (baseInfoItemListWrapper != null) {
            baseInfoItemListWrapper.get(0).setValue(consultationNumbers);
        }
    }

    public boolean formIsEditable() {
        return !checkNotNull(rnRForm).getStatus().equals(RnRForm.STATUS.AUTHORIZED);
    }


    public interface RequisitionView extends BaseView {

        void showListInputError(int index);

        void refreshRequisitionForm();

        void showErrorMessage(String msg);

        void completeSuccess();

        void goToHomePage();

        void highLightRequestAmount();

        void highLightApprovedAmount();

        void setProcessButtonName(String name);
    }

}
