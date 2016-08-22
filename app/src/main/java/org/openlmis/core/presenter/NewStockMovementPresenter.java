/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2016 ThoughtWorks, Inc.
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
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import org.roboguice.shaded.goole.common.collect.ImmutableList;

import java.util.Comparator;
import java.util.List;

import lombok.Getter;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class NewStockMovementPresenter extends Presenter {
    @Getter
    final StockMovementViewModel stockMovementModel;

    @Inject
    StockRepository stockRepository;

    NewStockMovementView view;

    StockMovementItem previousStockMovement;

    @Inject
    SharedPreferenceMgr sharedPreferenceMgr;

    public NewStockMovementPresenter() {
        stockMovementModel = new StockMovementViewModel();
    }

    @Override
    public void attachView(BaseView v) throws ViewNotMatchException {
        if (v instanceof NewStockMovementView) {
            this.view = (NewStockMovementView) v;
        } else {
            throw new ViewNotMatchException("Need NewStockMovementView");
        }
    }

    public StockMovementItem loadPreviousMovement(Long stockCardId) throws LMISException {
        previousStockMovement = stockRepository.queryLastStockMovementItemByStockCardId(stockCardId);
        return previousStockMovement;
    }

    public void saveStockMovement() {
        getSaveMovementObservable(stockMovementModel, previousStockMovement.getStockCard().getId()).subscribe(new Action1<StockMovementViewModel>() {
            @Override
            public void call(StockMovementViewModel viewModel) {
                view.goToStockCard();
            }
        });
    }

    protected Observable<StockMovementViewModel> getSaveMovementObservable(final StockMovementViewModel viewModel, final Long stockCardId) {
        return Observable.create(new Observable.OnSubscribe<StockMovementViewModel>() {
            @Override
            public void call(Subscriber<? super StockMovementViewModel> subscriber) {
                convertViewModelToDataModelAndSave(viewModel, stockCardId);
                subscriber.onNext(viewModel);
                subscriber.onCompleted();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    private void convertViewModelToDataModelAndSave(StockMovementViewModel viewModel, Long stockCardId) {
        try {
            viewModel.populateStockExistence(previousStockMovement.getStockOnHand());
            StockCard stockCard = stockRepository.queryStockCardById(stockCardId);
            StockMovementItem stockMovementItem = viewModel.convertViewToModel(stockCard);
            stockCard.setStockOnHand(stockMovementItem.getStockOnHand());

            if (stockCard.getStockOnHand() == 0) {
                stockCard.setExpireDates("");
            }
            stockRepository.addStockMovementAndUpdateStockCard(stockMovementItem);
            if (stockCard.getStockOnHand() == 0 && !stockCard.getProduct().isActive()) {
                sharedPreferenceMgr.setIsNeedShowProductsUpdateBanner(true, stockCard.getProduct().getPrimaryName());
            }
        } catch (LMISException e) {
            e.reportToFabric();
        }
    }

    public Observable<List<LotMovementViewModel>> addLotMovement(final LotMovementViewModel lotMovementViewModel) {
        return Observable.create(new Observable.OnSubscribe<List<LotMovementViewModel>>() {
            @Override
            public void call(Subscriber<? super List<LotMovementViewModel>> subscriber) {
                stockMovementModel.getNewLotMovementViewModelList().add(lotMovementViewModel);
                subscriber.onNext(stockMovementModel.getNewLotMovementViewModelList());
                subscriber.onCompleted();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    public List<LotMovementViewModel> getExistingLotViewModelsByStockCard(Long stockCardId) {
        if (stockMovementModel.getExistingLotMovementViewModelList().isEmpty()) {
            ImmutableList<LotMovementViewModel> lotMovementViewModels = null;
            try {
                lotMovementViewModels = FluentIterable.from(stockRepository.getNonEmptyLotOnHandByStockCard(stockCardId)).transform(new Function<LotOnHand, LotMovementViewModel>() {
                    @Override
                    public LotMovementViewModel apply(LotOnHand lotOnHand) {
                        return new LotMovementViewModel(lotOnHand.getLot().getLotNumber(),
                                DateUtil.formatDate(lotOnHand.getLot().getExpirationDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR),
                                lotOnHand.getQuantityOnHand().toString());
                    }
                }).toSortedList(new Comparator<LotMovementViewModel>() {
                    @Override
                    public int compare(LotMovementViewModel lot1, LotMovementViewModel lot2) {
                        return DateUtil.parseString(lot1.getExpiryDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR).compareTo(DateUtil.parseString(lot2.getExpiryDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR));
                    }
                });
            } catch (LMISException e) {
                e.printStackTrace();
            }
            stockMovementModel.setExistingLotMovementViewModelList(lotMovementViewModels);
        }
        return stockMovementModel.getExistingLotMovementViewModelList();

    }

    public interface NewStockMovementView extends BaseView {
        void showMovementDateEmpty();

        void showMovementReasonEmpty();

        boolean showLotError();

        void goToStockCard();
    }
}
