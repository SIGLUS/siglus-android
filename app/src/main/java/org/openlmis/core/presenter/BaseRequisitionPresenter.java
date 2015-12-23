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

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRFormSignature;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.SyncErrorsRepository;
import org.openlmis.core.service.SyncManager;
import org.openlmis.core.view.BaseView;

import lombok.Getter;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public abstract class BaseRequisitionPresenter extends Presenter {

    RnrFormRepository rnrFormRepository;

    @Inject
    Context context;

    @Inject
    SyncManager syncManager;

    @Inject
    SyncErrorsRepository syncErrorsRepository;

    private BaseRequisitionView view;

    @Getter
    protected RnRForm rnRForm;

    public BaseRequisitionPresenter() {
        rnrFormRepository = initRnrFormRepository();
    }

    protected abstract RnrFormRepository initRnrFormRepository();

    @Override
    public void attachView(BaseView baseView) throws ViewNotMatchException {
        if (baseView instanceof BaseRequisitionView) {
            this.view = (BaseRequisitionView) baseView;
        } else {
            throw new ViewNotMatchException("required VIARequisitionView");
        }
    }

    public void loadData(final long formId) {
        view.loading();
        Subscription subscription = getRnrFormObservable(formId).subscribe(loadDataOnNextAction, loadDataOnErrorAction);
        subscriptions.add(subscription);
    }

    protected Action1<RnRForm> loadDataOnNextAction = new Action1<RnRForm>() {
        @Override
        public void call(RnRForm form) {
            rnRForm = form;
            updateFormUI();
            loadAlertDialogIsFormStatusIsDraft();
            view.loaded();
        }
    };

    protected Action1<Throwable> loadDataOnErrorAction = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            view.loaded();
            view.showErrorMessage(throwable.getMessage());
        }
    };

    public void loadAlertDialogIsFormStatusIsDraft() {
        if (rnRForm.isSubmitted()) {
            view.showMessageNotifyDialog();
        }
    }

    public RnRForm getRnrForm(long formId) throws LMISException {
        if (rnRForm != null) {
            return rnRForm;
        }
        //three branches: history, half completed draft, new draft
        boolean isHistory = formId > 0;
        if (isHistory) {
            return rnrFormRepository.queryRnRForm(formId);
        }
        RnRForm draftVIA = rnrFormRepository.queryUnAuthorized();
        if (draftVIA != null) {
            return draftVIA;
        }
        return rnrFormRepository.initRnrForm();
    }

    protected void saveRequisition() {
        view.loading();
        getSaveFormObservable().observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(getSaveFormSubscriber());
    }

    protected Observable<Void> getSaveFormObservable() {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    rnrFormRepository.save(rnRForm);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    e.reportToFabric();
                    subscriber.onError(e);
                }
            }
        });
    }

    protected Subscriber<Void> getSaveFormSubscriber() {
        return new Subscriber<Void>() {
            @Override
            public void onCompleted() {
                view.loaded();
                view.saveSuccess();
            }

            @Override
            public void onError(Throwable e) {
                view.loaded();
                view.showSaveErrorMessage();
            }

            @Override
            public void onNext(Void o) {

            }
        };
    }

    protected void submitRequisition(final RnRForm rnRForm) {
        view.loading();
        Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    rnrFormRepository.submit(rnRForm);
                    subscriber.onNext(null);
                } catch (LMISException e) {
                    e.reportToFabric();
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
                updateUIAfterSubmit();

            }
        });
    }

    protected void authoriseRequisition(final RnRForm rnRForm) {
        view.loading();
        Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    rnrFormRepository.authorise(rnRForm);
                    subscriber.onNext(null);
                } catch (LMISException e) {
                    e.reportToFabric();
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
                view.showCompleteErrorMessage();
            }

            @Override
            public void onNext(Void aVoid) {
                view.loaded();
                view.completeSuccess();
                syncManager.requestSyncImmediately();
            }
        });
    }

    public void removeRequisition() throws LMISException {
        rnrFormRepository.removeRnrForm(rnRForm);
    }

    public void processSign(String signName, RnRForm rnRForm) {
        if (rnRForm.isDraft()) {
            submitSignature(signName, RnRFormSignature.TYPE.SUBMITTER, rnRForm);
            submitRequisition(rnRForm);
            view.showMessageNotifyDialog();
        } else {
            submitSignature(signName, RnRFormSignature.TYPE.APPROVER, rnRForm);
            authoriseRequisition(rnRForm);
        }
    }

    protected void submitSignature(final String signName, final RnRFormSignature.TYPE type, final RnRForm rnRForm) {
        view.loading();
        Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    rnrFormRepository.setSignature(rnRForm, signName, type);
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    e.reportToFabric();
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

    public RnRForm.STATUS getRnrFormStatus() {
        if (rnRForm != null) {
            return rnRForm.getStatus();
        } else {
            return RnRForm.STATUS.DRAFT;
        }
    }

    public abstract void updateUIAfterSubmit();

    protected abstract void updateFormUI();

    protected abstract Observable<RnRForm> getRnrFormObservable(long formId);

    public interface BaseRequisitionView extends BaseView {

        void refreshRequisitionForm(RnRForm rnRForm);

        void showErrorMessage(String msg);

        void showSignDialog(boolean isFormStatusDraft);

        void completeSuccess();

        void showMessageNotifyDialog();

        void saveSuccess();

        void showSaveErrorMessage();

        void showCompleteErrorMessage();
    }
}
