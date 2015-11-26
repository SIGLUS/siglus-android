package org.openlmis.core.presenter;

import android.content.Context;

import com.google.inject.Inject;

import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.PeriodNotUniqueException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRFormSignature;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.service.SyncManager;
import org.openlmis.core.view.BaseView;

import lombok.Getter;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public abstract class BaseRequisitionPresenter implements Presenter {

    @Inject
    RnrFormRepository rnrFormRepository;

    @Inject
    Context context;

    @Inject
    SyncManager syncManager;

    protected Subscription subscribe;
    private BaseRequisitionView view;

    @Getter
    protected RnRForm rnRForm;


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
    public void attachView(BaseView baseView) throws ViewNotMatchException {
        if (baseView instanceof BaseRequisitionView) {
            this.view = (BaseRequisitionView) baseView;
        } else {
            throw new ViewNotMatchException("required VIARequisitionView");
        }
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

    protected void submitRequisition(final RnRForm rnRForm) {
        view.loading();
        Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    rnrFormRepository.submit(rnRForm);
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
                    view.showErrorMessage(context.getString(R.string.hint_complete_failed));
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

    public abstract void loadData(final long formId);

    protected abstract void updateFormUI();

    public abstract void updateUIAfterSubmit();

    public interface BaseRequisitionView extends BaseView {

        void showErrorMessage(String msg);

        void showSignDialog(boolean isFormStatusDraft);

        void completeSuccess();

        void showMessageNotifyDialog();
    }
}
