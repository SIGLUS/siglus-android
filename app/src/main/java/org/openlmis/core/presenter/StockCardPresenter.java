/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

package org.openlmis.core.presenter;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.StockCardViewModel;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.base.Predicate;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

public class StockCardPresenter extends Presenter {


    protected List<StockCardViewModel> stockCardViewModels;

    @Inject
    StockRepository stockRepository;

    Observer<List<StockCard>> afterLoadHandler = getLoadStockCardsSubscriber();

    private StockCardListView view;

    public StockCardPresenter() {
        stockCardViewModels = new ArrayList<>();
    }

    public List<StockCardViewModel> getStockCardViewModels() {
        return stockCardViewModels;
    }

    public void loadStockCards(ArchiveStatus status) {
        view.loading();
        Subscription subscription = getLoadStockCardsObserver(status).subscribe(afterLoadHandler);
        subscriptions.add(subscription);
    }

    public void refreshStockCardViewModelsSOH() {
        for (StockCardViewModel stockCardViewModel : stockCardViewModels) {
            final StockCard stockCard = stockCardViewModel.getStockCard();
            stockRepository.refresh(stockCard);
            stockCardViewModel.setStockOnHand(stockCard.getStockOnHand());
        }
    }

    @Override
    public void attachView(BaseView v) {
        view = (StockCardListView) v;
    }

    private Observable<List<StockCard>> getLoadStockCardsObserver(final ArchiveStatus status) {
        return Observable.create(new Observable.OnSubscribe<List<StockCard>>() {
            @Override
            public void call(Subscriber<? super List<StockCard>> subscriber) {
                try {
                    subscriber.onNext(from(stockRepository.list()).filter(new Predicate<StockCard>() {
                        @Override
                        public boolean apply(StockCard stockCard) {
                            return stockCard.getProduct().isArchived() == status.isArchived() && stockCard.getProduct().isActive();
                        }
                    }).toList());
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private Observer<List<StockCard>> getLoadStockCardsSubscriber() {
        return new Observer<List<StockCard>>() {
            @Override
            public void onCompleted() {
                view.loaded();
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                ToastUtil.show(e.getMessage());
                view.loaded();
            }

            @Override
            public void onNext(List<StockCard> stockCards) {
                List<StockCardViewModel> stockCardViewModelList = from(stockCards).transform(new Function<StockCard, StockCardViewModel>() {
                    @Override
                    public StockCardViewModel apply(StockCard stockCard) {
                        return new StockCardViewModel(stockCard);
                    }
                }).toList();
                stockCardViewModels.clear();
                stockCardViewModels.addAll(stockCardViewModelList);
                view.refresh();
            }
        };
    }

    public void archiveBackStockCard(StockCard stockCard) {
        stockCard.getProduct().setArchived(false);
        stockRepository.updateProductOfStockCard(stockCard);
    }

    public enum ArchiveStatus {
        Archived(true),
        Active(false);

        private boolean isArchived;

        ArchiveStatus(boolean isArchived) {
            this.isArchived = isArchived;
        }

        public boolean isArchived() {
            return isArchived;
        }
    }

    public interface StockCardListView extends BaseView {
        void refresh();
    }
}
