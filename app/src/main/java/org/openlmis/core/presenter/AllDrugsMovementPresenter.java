package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.StockMovementHistoryViewModel;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.base.Predicate;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AllDrugsMovementPresenter extends Presenter {

    @Inject
    StockRepository stockRepository;

    AllDrugsMovementView view;

    List<StockMovementHistoryViewModel> viewModelList = new ArrayList<>();

    List<StockMovementHistoryViewModel> filteredViewModelList = new ArrayList<>();

    @Override
    public void attachView(BaseView v) throws ViewNotMatchException {
        view = (AllDrugsMovementView) v;
    }

    public void loadMovementHistory(final int days) {
        view.loading();
        Subscription subscription = Observable.create(new Observable.OnSubscribe<List<StockMovementHistoryViewModel>>() {
            @Override
            public void call(Subscriber<? super List<StockMovementHistoryViewModel>> subscriber) {
                try {
                    loadAllMovementHistory();
                    filterViewModels(days);
                    subscriber.onNext(filteredViewModelList);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(new Observer<List<StockMovementHistoryViewModel>>() {
            @Override
            public void onCompleted() {
                view.loaded();
            }

            @Override
            public void onError(Throwable e) {
                view.loaded();
                new LMISException(e).reportToFabric();
                ToastUtil.show(e.getMessage());
            }

            @Override
            public void onNext(List<StockMovementHistoryViewModel> stockMovementHistoryViewModels) {
                view.refreshRecyclerView(stockMovementHistoryViewModels);
                view.updateHistoryCount(filteredViewModelList.size(), getMovementCount());
            }
        });
        subscriptions.add(subscription);
    }

    private int getMovementCount() {
        int movementCount = 0;
        for (StockMovementHistoryViewModel viewModel : filteredViewModelList) {
            movementCount += viewModel.getFilteredMovementList().size();
        }
        return movementCount;
    }

    private void filterViewModels(final int days) {
        filteredViewModelList.clear();
        filteredViewModelList.addAll(FluentIterable.from(viewModelList).filter(new Predicate<StockMovementHistoryViewModel>() {
            @Override
            public boolean apply(StockMovementHistoryViewModel stockMovementHistoryViewModel) {
                stockMovementHistoryViewModel.filter(days);
                return !stockMovementHistoryViewModel.getFilteredMovementList().isEmpty();
            }
        }).toList());
    }

    private void loadAllMovementHistory() {
        if (viewModelList.isEmpty()) {
            viewModelList.addAll(FluentIterable.from(stockRepository.list()).filter(new Predicate<StockCard>() {
                @Override
                public boolean apply(StockCard stockCard) {
                    return !stockCard.getStockMovementItemsWrapper().isEmpty();
                }
            }).transform(new Function<StockCard, StockMovementHistoryViewModel>() {
                @Override
                public StockMovementHistoryViewModel apply(StockCard stockCard) {
                    return new StockMovementHistoryViewModel(stockCard);
                }
            }).toList());
        }
    }

    public interface AllDrugsMovementView extends BaseView {
        void refreshRecyclerView(List<StockMovementHistoryViewModel> stockMovementHistoryViewModels);

        void updateHistoryCount(int productCount, int movementCount);
    }
}
