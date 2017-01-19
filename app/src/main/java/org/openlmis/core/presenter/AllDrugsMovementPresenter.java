package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.StockHistoryViewModel;
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

    List<StockHistoryViewModel> viewModelList = new ArrayList<>();

    List<StockHistoryViewModel> filteredViewModelList = new ArrayList<>();

    @Override
    public void attachView(BaseView v) throws ViewNotMatchException {
        view = (AllDrugsMovementView) v;
    }

    public void loadMovementHistory(final int days) {
        view.loading();
        Subscription subscription = Observable.create(new Observable.OnSubscribe<List<StockHistoryViewModel>>() {
            @Override
            public void call(Subscriber<? super List<StockHistoryViewModel>> subscriber) {
                try {
                    loadAllMovementHistory();
                    filterViewModels(days);
                    subscriber.onNext(filteredViewModelList);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(new Observer<List<StockHistoryViewModel>>() {
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
            public void onNext(List<StockHistoryViewModel> stockMovementHistoryViewModels) {
                view.refreshRecyclerView(stockMovementHistoryViewModels);
                view.updateHistoryCount(filteredViewModelList.size(), getMovementCount());
            }
        });
        subscriptions.add(subscription);
    }

    private int getMovementCount() {
        int movementCount = 0;
        for (StockHistoryViewModel viewModel : filteredViewModelList) {
            movementCount += viewModel.getFilteredMovementItemViewModelList().size();
        }
        return movementCount;
    }

    protected void filterViewModels(final int days) {
        filteredViewModelList.clear();
        filteredViewModelList.addAll(FluentIterable.from(viewModelList).filter(new Predicate<StockHistoryViewModel>() {
            @Override
            public boolean apply(StockHistoryViewModel stockHistoryViewModel) {
                return !stockHistoryViewModel.filter(days).isEmpty();
            }
        }).toList());
    }

    protected void loadAllMovementHistory() {
        if (viewModelList.isEmpty()) {
            viewModelList.addAll(FluentIterable.from(stockRepository.list()).transform(new Function<StockCard, StockHistoryViewModel>() {
                @Override
                public StockHistoryViewModel apply(StockCard stockCard) {
                    return new StockHistoryViewModel(stockCard);
                }
            }).toList());
        }
    }

    public interface AllDrugsMovementView extends BaseView {
        void refreshRecyclerView(List<StockHistoryViewModel> stockHistoryViewModels);

        void updateHistoryCount(int productCount, int movementCount);
    }
}
