package org.openlmis.core.presenter;

import android.support.annotation.NonNull;

import com.google.inject.Inject;

import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.Inventory;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.repository.InventoryRepository;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.BaseView;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SelectPeriodPresenter extends Presenter {

    @Inject
    InventoryRepository inventoryRepository;

    private SelectPeriodView view;

    @Override
    public void attachView(BaseView v) throws ViewNotMatchException {
        view = (SelectPeriodView) v;
    }

    public void loadData() {
        view.loading();
        Subscription subscription = Observable.create(new Observable.OnSubscribe<List<Inventory>>() {
            @Override
            public void call(Subscriber<? super List<Inventory>> subscriber) {
                try {
                    subscriber.onNext(inventoryRepository.queryPeriodInventory(Period.of(DateUtil.today())));
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    e.reportToFabric();
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(getSubscriber());
        subscriptions.add(subscription);
    }

    @NonNull
    protected Subscriber<List<Inventory>> getSubscriber() {
        return new Subscriber<List<Inventory>>() {
            @Override
            public void onCompleted() {
                view.loaded();
            }

            @Override
            public void onError(Throwable e) {
                view.loaded();
                ToastUtil.show(R.string.loading_inventory_list_failed);
            }

            @Override
            public void onNext(List<Inventory> inventories) {
                view.refreshDate(inventories);
            }
        };
    }


    public interface SelectPeriodView extends BaseView {
        void refreshDate(List<Inventory> inventories);
    }
}
