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


import android.content.Context;

import com.google.inject.Inject;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;
import org.roboguice.shaded.goole.common.base.Function;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

public class StockMovementPresenter implements Presenter {

    @Getter
    List<StockMovementViewModel> stockMovementModelList;

    @Inject
    StockRepository stockRepository;

    StockCard stockCard;

    StockMovementView view;


    @Inject
    Context context;

    public StockMovementPresenter() {
        stockMovementModelList = new ArrayList<>();
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void attachView(BaseView v) throws ViewNotMatchException {
        if (v instanceof StockMovementView) {
            this.view = (StockMovementView) v;
        } else {
            throw new ViewNotMatchException("Need StockMovementView");
        }
    }

    public void setStockCard(long stockCardId) throws LMISException {
        this.stockCard = stockRepository.queryStockCardById(stockCardId);
    }

    public void loadStockMovementViewModels() {
        if (!stockMovementModelList.isEmpty()) {
            view.loaded();
            return;
        }

        loadStockMovementViewModelsObserver().subscribe(loadStockMovementViewModelSubscriber());
    }
    public Observer<List<StockMovementViewModel>> loadStockMovementViewModelSubscriber(){
        return new Observer<List<StockMovementViewModel>>(){
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable throwable) {
                ToastUtil.show("Database query error :" + throwable.getMessage());
            }

            @Override
            public void onNext(List<StockMovementViewModel> stockMovementViewModels) {
                stockMovementModelList.clear();
                stockMovementModelList.addAll(stockMovementViewModels);
                stockMovementModelList.add(new StockMovementViewModel());

                view.refreshStockMovement();
                view.loaded();
            }
        };
    }

    public Observable<List<StockMovementViewModel>> loadStockMovementViewModelsObserver() {
        return Observable.create(new Observable.OnSubscribe<List<StockMovementViewModel>>() {
            @Override
            public void call(final Subscriber<? super List<StockMovementViewModel>> subscriber) {
                try {
                    List<StockMovementViewModel> list = from(stockRepository.listLastFive(stockCard.getId())).transform(new Function<StockMovementItem, StockMovementViewModel>() {
                        @Override
                        public StockMovementViewModel apply(StockMovementItem stockMovementItem) {
                            return new StockMovementViewModel(stockMovementItem);
                        }
                    }).toList();

                    subscriber.onNext(list);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }


    private void saveStockMovement(StockMovementItem stockMovementItem) throws LMISException {
        stockRepository.addStockMovementAndUpdateStockCard(stockCard, stockMovementItem);
    }

    public void submitStockMovement(StockMovementViewModel viewModel) {
        if (viewModel.validateEmpty() && viewModel.validateInputValid()) {
            if (LMISApp.getInstance().getFeatureToggleFor(R.bool.display_stock_movement_signature)) {
                view.showSignDialog();
            } else {
                saveAndRefresh(viewModel);
            }
        } else if (!viewModel.validateEmpty()) {
            view.showErrorAlert(context.getResources().getString(R.string.msg_validation_empty_error));
        } else if (!viewModel.validateInputValid()) {
            view.showErrorAlert(context.getResources().getString(R.string.msg_validation_error));
        }
    }

    public void saveAndRefresh(StockMovementViewModel viewModel) {
        try {
            StockMovementItem stockMovementItem = viewModel.convertViewToModel();
            stockMovementItem.setStockCard(stockCard);
            stockCard.setStockOnHand(stockMovementItem.getStockOnHand());
            saveStockMovement(stockMovementItem);

            viewModel.setDraft(false);
            stockMovementModelList.add(new StockMovementViewModel());

            view.refreshStockMovement();
            view.deactivatedStockDraft();
        } catch (LMISException e) {
            e.reportToFabric();
        }
    }

    public StockCard getStockCard() {
        return stockCard;
    }


    public interface StockMovementView extends BaseView {
        void showErrorAlert(String msg);

        void refreshStockMovement();

        void deactivatedStockDraft();

        void showSignDialog();
    }
}
