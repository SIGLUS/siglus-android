package org.openlmis.core.presenter;

import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.view.BaseView;

import rx.Subscription;

public abstract class BaseRequisitionPresenter implements Presenter {
    protected Subscription subscribe;

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

    }

    public abstract void loadData(final long formId);
}
