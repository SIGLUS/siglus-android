package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.service.SyncService;

import rx.Observable;

public abstract class BaseReportPresenter extends Presenter {

    @Inject
    SyncService syncService;

    public abstract void deleteDraft();

    public abstract boolean isDraft();

    public Observable<Void> getOnSignObservable(final String signature) {
        return Observable.create(subscriber -> {
            try {
                addSignature(signature);
                subscriber.onNext(null);
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
                new LMISException(e, "BaseReportPresenter.getOnSignObservable").reportToFabric();
            }
        });
    }

    protected abstract void addSignature(String signature);
}
