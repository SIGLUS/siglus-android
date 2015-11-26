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
import org.openlmis.core.exceptions.PeriodNotUniqueException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.repository.MMIARepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.view.BaseView;

import java.util.ArrayList;
import java.util.Objects;

import roboguice.RoboGuice;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MMIARequisitionPresenter extends BaseRequisitionPresenter {

    MMIARequisitionView view;

    @Override
    protected RnrFormRepository initRnrFormRepository() {
        return RoboGuice.getInjector(LMISApp.getContext()).getInstance(MMIARepository.class);
    }

    @Override
    public void attachView(BaseView baseView) throws ViewNotMatchException {
        if (baseView instanceof MMIARequisitionView) {
            this.view = (MMIARequisitionView) baseView;
        } else {
            throw new ViewNotMatchException(MMIARequisitionView.class.getName());
        }
        super.attachView(baseView);
    }

    @Override
    public void loadData(final long formId) {
        view.loading();
        subscribe = getRnrFormObservable(formId).subscribe(loadDataOnNextAction, loadDataOnErrorAction);
    }

    @Override
    public void updateUIAfterSubmit() {

        view.setProcessButtonName(context.getResources().getString(R.string.btn_complete));
    }

    @Override
    protected Observable<RnRForm> getRnrFormObservable(final long formId) {
        return Observable.create(new Observable.OnSubscribe<RnRForm>() {
            @Override
            public void call(Subscriber<? super RnRForm> subscriber) {
                try {
                    rnRForm = getRnrForm(formId);
                    subscriber.onNext(rnRForm);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    @Override
    protected void updateFormUI() {
        if (rnRForm != null) {
            view.initView(rnRForm);
        }
    }

    @Override
    public RnRForm getRnrForm(final long formId) throws LMISException {

        if (rnRForm != null) {
            return rnRForm;
        }

        if (formId > 0) {
            rnRForm = rnrFormRepository.queryRnRForm(formId);
        } else {
            RnRForm draftMMIAForm = rnrFormRepository.queryUnAuthorized();
            rnRForm = draftMMIAForm == null ? rnrFormRepository.initRnrForm() : draftMMIAForm;
        }
        return rnRForm;
    }

    public void processRequisition(ArrayList<RegimenItem> regimenItemList, ArrayList<BaseInfoItem> baseInfoItemList, String comments) {
        rnRForm.setRegimenItemListWrapper(regimenItemList);
        rnRForm.setBaseInfoItemListWrapper(baseInfoItemList);
        rnRForm.setComments(comments);

        if (!validateTotalsMatch(rnRForm) && comments.length() < 5) {
            view.showValidationAlert();
            return;
        }

        if (LMISApp.getInstance().getFeatureToggleFor(R.bool.display_mmia_form_signature)) {
            view.showSignDialog(rnRForm.isDraft());
        } else {
            authoriseRequisition(rnRForm);
        }
    }

    protected Observable<Void> getAuthoriseFormObservable() {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    rnrFormRepository.authorise(rnRForm);
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    e.printStackTrace();
                    subscriber.onError(e);

                }
            }
        });
    }

    protected Action1<Void> authoriseFormOnNextAction = new Action1<Void>() {
        @Override
        public void call(Void aVoid) {
            view.loaded();
            view.completeSuccess();
            syncManager.requestSyncImmediately();
        }
    };

    protected Action1<Throwable> authorizeFormOnErrorAction = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            view.loaded();
            if (throwable instanceof PeriodNotUniqueException) {
                view.showErrorMessage(context.getResources().getString(R.string.msg_mmia_not_unique));
            } else {
                view.showErrorMessage(context.getString(R.string.hint_complete_failed));
            }
        }
    };

    private boolean validateTotalsMatch(RnRForm form) {
        return RnRForm.calculateTotalRegimenAmount(form.getRegimenItemListWrapper()) == rnrFormRepository.getTotalPatients(form);
    }

    public void saveDraftForm(ArrayList<RegimenItem> regimenItemList, ArrayList<BaseInfoItem> baseInfoItemList, String comments) {
        rnRForm.setRegimenItemListWrapper(regimenItemList);
        rnRForm.setBaseInfoItemListWrapper(baseInfoItemList);
        rnRForm.setComments(comments);
        saveForm();
    }




    public void setBtnCompleteText() {
        if (rnRForm.getStatus() == RnRForm.STATUS.DRAFT) {
            view.setProcessButtonName(context.getResources().getString(R.string.btn_submit));
        } else {
            view.setProcessButtonName(context.getResources().getString(R.string.btn_complete));
        }
    }

    public interface MMIARequisitionView extends BaseRequisitionView {

        void showValidationAlert();

        void initView(RnRForm form);

        void setProcessButtonName(String name);

        void showMessageNotifyDialog();
    }
}
