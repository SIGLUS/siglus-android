package org.openlmis.core.presenter;

import android.support.annotation.NonNull;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.inject.Inject;

import org.joda.time.DateTime;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.Inventory;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.repository.InventoryRepository;
import org.openlmis.core.model.service.RequisitionPeriodService;
import org.openlmis.core.service.DirtyDataManager;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.SelectInventoryViewModel;

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
    private RequisitionPeriodService requisitionPeriodService;

    private SelectPeriodView view;
    @Inject
    DirtyDataManager dirtyDataManager;

    @Override
    public void attachView(BaseView v) throws ViewNotMatchException {
        view = (SelectPeriodView) v;
    }

    public Observable<Constants.Program> correctDirtyObservable(Constants.Program from) {
        return Observable.create((Observable.OnSubscribe<Constants.Program>) subscriber -> {
            List<StockCard> deletedStockCards = dirtyDataManager.correctData();
            if (!CollectionUtils.isEmpty(deletedStockCards)) {
                subscriber.onNext(from);
            } else {
                subscriber.onCompleted();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    public void loadData(final String programCode, Period period) {
        view.loading();
        Subscription subscription = Observable.create((Observable.OnSubscribe<List<SelectInventoryViewModel>>) subscriber -> {
            try {
                Period periodInSchedule;
                if (programCode.equals(Constants.RAPID_TEST_CODE)) {
                    periodInSchedule = new Period(period.getBegin(), period.getEnd());
                } else {
                    periodInSchedule = requisitionPeriodService.generateNextPeriod(programCode, null);
                }
                List<Inventory> inventories = inventoryRepository.queryPeriodInventory(periodInSchedule);
                boolean isDefaultInventoryDate = false;
                if (inventories.isEmpty()) {
                    isDefaultInventoryDate = true;
                    generateDefaultInventoryDates(periodInSchedule, inventories);
                }
                List<SelectInventoryViewModel> selectInventoryViewModels = generateSelectInventoryViewModels(inventories, isDefaultInventoryDate);
                subscriber.onNext(selectInventoryViewModels);
                subscriber.onCompleted();
            } catch (LMISException e) {
                new LMISException(e, "SelectPeriodPresenter.loadData").reportToFabric();
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(getSubscriber());
        subscriptions.add(subscription);
    }

    private void generateDefaultInventoryDates(Period periodInSchedule, List<Inventory> inventories) {
        DateTime inventoryDate = new DateTime()
                .secondOfDay()
                .withMaximumValue()
                // Year, Month, Days
                .withDate(periodInSchedule.getEnd().getYear(),
                        periodInSchedule.getEnd().getMonthOfYear(),
                        Period.INVENTORY_BEGIN_DAY)
                // Hours, Min, Seconds
                .withTime(23, 59, 59, 0);
        for (int i = 0; i < Period.INVENTORY_END_DAY_NEXT - Period.INVENTORY_BEGIN_DAY; i++) {
            Inventory inventory = new Inventory();
            inventory.setCreatedAt(inventoryDate.toDate());
            inventories.add(inventory);
            inventoryDate = inventoryDate.plusDays(1);
        }
    }

    private List<SelectInventoryViewModel> generateSelectInventoryViewModels(final List<Inventory> inventories, final boolean isDefaultInventoryDate) {
        return from(inventories).transform(inventory -> {
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
