package org.openlmis.core.presenter;

import org.openlmis.core.exceptions.LMISException;

import rx.Observable;
import rx.Subscriber;

public abstract class BaseReportPresenter extends Presenter{

    public abstract void deleteDraft();

    public abstract boolean isDraft();

    public Observable<Void> getOnSignObservable(final String signature) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    addSignature(signature);
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                    new LMISException(e).reportToFabric();
                }
            }
        });
    }

    protected abstract void addSignature(String signature);
}
