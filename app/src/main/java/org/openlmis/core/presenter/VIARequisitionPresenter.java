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

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.PeriodNotUniqueException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRFormSignature;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.repository.VIARepository;
import org.openlmis.core.service.SyncManager;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;


public class VIARequisitionPresenter extends BaseRequisitionPresenter {

    @Inject
    VIARepository viaRepository;

    @Inject
    Context context;

    @Inject
    SyncManager syncManager;

    VIARequisitionView view;

    @Getter
    protected RnRForm rnRForm;
    protected List<RequisitionFormItemViewModel> requisitionFormItemViewModelList;

    public VIARequisitionPresenter() {
        requisitionFormItemViewModelList = new ArrayList<>();
    }

    @Override
    public void attachView(BaseView baseView) throws ViewNotMatchException {
        if (baseView instanceof VIARequisitionView) {
            this.view = (VIARequisitionView) baseView;
        } else {
            throw new ViewNotMatchException("required VIARequisitionView");
        }
    }

    public RnRForm loadRnrForm(long formId) throws LMISException {
        //three branches: history, half completed draft, new draft
        boolean isHistory = formId > 0;
        if (isHistory) {
            return viaRepository.queryRnRForm(formId);
        }
        RnRForm draftVIA = viaRepository.getDraftVIA();
        if (draftVIA != null) {
            return draftVIA;
        }
        return viaRepository.initVIA();
    }

    public List<RequisitionFormItemViewModel> getRequisitionViewModelList() {
        return requisitionFormItemViewModelList;
    }

    protected List<RequisitionFormItemViewModel> createViewModelsFromRnrForm(long formId) throws LMISException {
        if (rnRForm == null) {
            rnRForm = loadRnrForm(formId);
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
                try {
                    List<RequisitionFormItemViewModel> viewModelsFromRnrForm = createViewModelsFromRnrForm(formId);
                    subscriber.onNext(viewModelsFromRnrForm);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(new Action1<List<RequisitionFormItemViewModel>>() {
            @Override
            public void call(List<RequisitionFormItemViewModel> requisitionFormItemViewModels) {
                requisitionFormItemViewModelList.addAll(requisitionFormItemViewModels);
                updateRequisitionFormUI();
                loadAlertDialogIsFormStatusIsDraft();
                view.loaded();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                view.loaded();
                view.showErrorMessage(throwable.getMessage());
            }
        });
    }

    protected void updateRequisitionFormUI() {
        if (rnRForm.isDraft()) {
            view.highLightRequestAmount();
        } else if (rnRForm.isSubmitted()) {
            view.setProcessButtonName(context.getString(R.string.btn_complete));
            view.highLightApprovedAmount();
        }
        view.refreshRequisitionForm();
        view.setEditable();
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
        dataViewToModel(consultationNumbers);

        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.display_via_form_signature_10)) {
            view.showSignDialog(rnRForm.isDraft());
        } else {
            if (rnRForm.isDraft()) {
                submitRequisition(rnRForm);
            } else {
                authorise(rnRForm);
            }
        }
    }

    private void dataViewToModel(String consultationNumbers) {
        ImmutableList<RnrFormItem> rnrFormItems = from(requisitionFormItemViewModelList).transform(new Function<RequisitionFormItemViewModel, RnrFormItem>() {
            @Override
            public RnrFormItem apply(RequisitionFormItemViewModel requisitionFormItemViewModel) {
                return requisitionFormItemViewModel.toRnrFormItem();
            }
        }).toList();
        rnRForm.setRnrFormItemListWrapper(new ArrayList<>(rnrFormItems));
        rnRForm.getBaseInfoItemListWrapper().get(0).setValue(consultationNumbers);
    }

    private void submitRequisition(final RnRForm rnRForm) {
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

    private void authorise(final RnRForm rnRForm) {
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

    public void saveRequisition(String consultationNumbers) {
        view.loading();
        ImmutableList<RnrFormItem> rnrFormItems = from(requisitionFormItemViewModelList).transform(new Function<RequisitionFormItemViewModel, RnrFormItem>() {
            @Override
            public RnrFormItem apply(RequisitionFormItemViewModel requisitionFormItemViewModel) {
                return requisitionFormItemViewModel.toRnrFormItem();
            }
        }).toList();
        rnRForm.setRnrFormItemListWrapper(new ArrayList<>(rnrFormItems));
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
                view.backToHomePage();
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
        String value = null;
        try {
            value = rnRForm.getBaseInfoItemListWrapper().get(0).getValue();
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return value;
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

    public void processSign(String signName, RnRForm rnRForm) {
        if (rnRForm.isDraft()) {
            submitSignature(signName, RnRFormSignature.TYPE.SUBMITTER, rnRForm);
            submitRequisition(rnRForm);
            view.showMessageNotifyDialog();
        } else {
            submitSignature(signName, RnRFormSignature.TYPE.APPROVER, rnRForm);
            authorise(rnRForm);
        }
    }

    public RnRForm.STATUS getRnrFormStatus() {
        if (rnRForm != null) {
            return rnRForm.getStatus();
        } else {
            return RnRForm.STATUS.DRAFT;
        }
    }


    private void submitSignature(final String signName, final RnRFormSignature.TYPE type, final RnRForm rnRForm) {
        view.loading();
        Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    viaRepository.setSignature(rnRForm, signName, type);
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
            }
        });
    }

    public void loadAlertDialogIsFormStatusIsDraft() {
        if (rnRForm.isSubmitted()) {
            view.showMessageNotifyDialog();
        }
    }

    public void setBtnCompleteText() {
        if (rnRForm == null){
            return;
        }

        if (rnRForm.isDraft()) {
            view.setProcessButtonName(context.getResources().getString(R.string.btn_submit));
        } else {
            view.setProcessButtonName(context.getResources().getString(R.string.btn_complete));
        }
    }

    public interface VIARequisitionView extends BaseView {

        void showListInputError(int index);

        void refreshRequisitionForm();

        void showErrorMessage(String msg);

        void completeSuccess();

        void backToHomePage();

        void highLightRequestAmount();

        void highLightApprovedAmount();

        void setProcessButtonName(String name);

        void showSignDialog(boolean isFormStatusDraft);

        void showMessageNotifyDialog();

        void setEditable();
    }
}