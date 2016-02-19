package org.openlmis.core.presenter;

import android.support.annotation.NonNull;

import com.google.inject.Inject;

import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.Inventory;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.repository.InventoryRepository;
import org.openlmis.core.model.service.PeriodService;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.SelectInventoryViewModel;
import org.roboguice.shaded.goole.common.base.Function;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

public class SelectPeriodPresenter extends Presenter {

    @Inject
    InventoryRepository inventoryRepository;

    @Inject
    private PeriodService periodService;

    private SelectPeriodView view;

    @Override
    public void attachView(BaseView v) throws ViewNotMatchException {
        view = (SelectPeriodView) v;
    }

    public void loadData(final String programCode) {
        view.loading();
        Subscription subscription = Observable.create(new Observable.OnSubscribe<List<SelectInventoryViewModel>>() {
            @Override
            public void call(Subscriber<? super List<SelectInventoryViewModel>> subscriber) {
                try {
                    Period currentPeriod = periodService.generatePeriod(programCode, null);
                    List<SelectInventoryViewModel> selectInventoryViewModels = generateSelectInventoryViewModels(inventoryRepository.queryPeriodInventory(currentPeriod));
                    subscriber.onNext(selectInventoryViewModels);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    e.reportToFabric();
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(getSubscriber());
        subscriptions.add(subscription);
    }

    private List<SelectInventoryViewModel> generateSelectInventoryViewModels(final List<Inventory> inventories) {
        return from(inventories).transform(new Function<Inventory, SelectInventoryViewModel>() {
            @Override
            public SelectInventoryViewModel apply(Inventory inventory) {
                SelectInventoryViewModel selectInventoryViewModel = new SelectInventoryViewModel(inventory);

                for (Inventory comparedInventory : inventories) {
                    if (inventory == comparedInventory) continue;
                    String formattedInventoryDate = DateUtil.formatDate(inventory.getCreatedAt(), DateUtil.DB_DATE_FORMAT);
                    String formattedComparedInventoryDate = DateUtil.formatDate(comparedInventory.getCreatedAt(), DateUtil.DB_DATE_FORMAT);
                    if (formattedInventoryDate.equals(formattedComparedInventoryDate)) {
                        selectInventoryViewModel.setShowTime(true);
                        break;
                    }
                }
                return selectInventoryViewModel;
            }
        }).toList();
    }

    @NonNull
    protected Subscriber<List<SelectInventoryViewModel>> getSubscriber() {
        return new Subscriber<List<SelectInventoryViewModel>>() {
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
            public void onNext(List<SelectInventoryViewModel> inventories) {
                view.refreshDate(inventories);
            }
        };
    }


    public interface SelectPeriodView extends BaseView {
        void refreshDate(List<SelectInventoryViewModel> inventories);
    }
}
