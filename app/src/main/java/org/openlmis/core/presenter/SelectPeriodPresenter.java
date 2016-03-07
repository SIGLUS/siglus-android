package org.openlmis.core.presenter;

import android.support.annotation.NonNull;

import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.openlmis.core.LMISApp;
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
                    Period periodInSchedule = periodService.generatePeriod(programCode, null);
                    List<Inventory> inventories = inventoryRepository.queryPeriodInventory(periodInSchedule);
                    boolean isDefaultIventoryDate = false;
                    if (inventories.isEmpty() && LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_requisition_period_logic_change)) {
                        isDefaultIventoryDate = true;
                        generateDefaultInventoryDates(periodInSchedule, inventories);
                    }
                    List<SelectInventoryViewModel> selectInventoryViewModels = generateSelectInventoryViewModels(inventories, isDefaultIventoryDate);
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

    private void generateDefaultInventoryDates(Period periodInSchedule, List<Inventory> inventories) {
        DateTime inventoryDate = new DateTime().secondOfDay().withMaximumValue().withDate(periodInSchedule.getEnd().getYear(), periodInSchedule.getEnd().getMonthOfYear(), Period.INVENTORY_BEGIN_DAY);
        for (int i = 0; i < Period.INVENTORY_END_DAY_NEXT - Period.INVENTORY_BEGIN_DAY; i++) {
            Inventory inventory = new Inventory();
            inventory.setCreatedAt(inventoryDate.toDate());
            inventories.add(inventory);
            inventoryDate = inventoryDate.plusDays(1);
        }
    }

    private List<SelectInventoryViewModel> generateSelectInventoryViewModels(final List<Inventory> inventories, final boolean isDefaultInventoryDate) {
        return from(inventories).transform(new Function<Inventory, SelectInventoryViewModel>() {
            @Override
            public SelectInventoryViewModel apply(Inventory inventory) {
                SelectInventoryViewModel selectInventoryViewModel = new SelectInventoryViewModel(inventory);

                if (isDefaultInventoryDate && new DateTime(selectInventoryViewModel.getInventoryDate()).getDayOfMonth() == Period.DEFAULT_INVENTORY_DAY) {
                    selectInventoryViewModel.setChecked(true);
                }

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
