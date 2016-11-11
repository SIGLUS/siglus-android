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
import org.openlmis.core.manager.MovementReasonManager;
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
import org.roboguice.shaded.goole.common.base.Predicate;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import org.roboguice.shaded.goole.common.collect.ImmutableList;

import java.util.Comparator;

import lombok.Getter;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class NewStockMovementPresenter extends Presenter {
    @Getter
    final StockMovementViewModel stockMovementViewModel = new StockMovementViewModel();

    @Inject
    StockRepository stockRepository;

    NewStockMovementView view;

    @Inject
    SharedPreferenceMgr sharedPreferenceMgr;

    @Getter
    private StockCard stockCard;

    public NewStockMovementPresenter() {
    }

    @Override
    public void attachView(BaseView v) {
        this.view = (NewStockMovementView) v;
    }

    public void loadData(Long stockCardId, MovementReasonManager.MovementType movementType) {
        try {
            stockCard = stockRepository.queryStockCardById(stockCardId);
            stockMovementViewModel.setStockCard(stockCard);
        } catch (LMISException e) {
            e.reportToFabric();
        }
        loadExistingLotMovementViewModels(movementType);
    }

    public void saveStockMovement() {
        getSaveMovementObservable().subscribe(new Action1<StockMovementViewModel>() {
            @Override
            public void call(StockMovementViewModel viewModel) {
                view.goToStockCard();
            }
        });
    }

    protected Observable<StockMovementViewModel> getSaveMovementObservable() {
        return Observable.create(new Observable.OnSubscribe<StockMovementViewModel>() {
            @Override
            public void call(Subscriber<? super StockMovementViewModel> subscriber) {
                convertViewModelToDataModelAndSave();
                subscriber.onNext(stockMovementViewModel);
                subscriber.onCompleted();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    private void convertViewModelToDataModelAndSave() {
        stockMovementViewModel.populateStockExistence(stockCard.getStockOnHand());
        StockMovementItem stockMovementItem = stockMovementViewModel.convertViewToModel(stockCard);
        stockCard.setStockOnHand(stockMovementItem.getStockOnHand());

        if (stockCard.getStockOnHand() == 0) {
            stockCard.setExpireDates("");
        }
        stockRepository.addStockMovementAndUpdateStockCard(stockMovementItem);
        if (stockCard.getStockOnHand() == 0 && !stockCard.getProduct().isActive()) {
            sharedPreferenceMgr.setIsNeedShowProductsUpdateBanner(true, stockCard.getProduct().getPrimaryName());
        }
    }

    private void loadExistingLotMovementViewModels(final MovementReasonManager.MovementType movementType) {
        ImmutableList<LotMovementViewModel> lotMovementViewModels = FluentIterable.from(stockCard.getNonEmptyLotOnHandList()).transform(new Function<LotOnHand, LotMovementViewModel>() {
            @Override
            public LotMovementViewModel apply(LotOnHand lotOnHand) {
                return new LotMovementViewModel(lotOnHand.getLot().getLotNumber(),
                        DateUtil.formatDate(lotOnHand.getLot().getExpirationDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR),
                        lotOnHand.getQuantityOnHand().toString(), movementType);
            }
        }).filter(new Predicate<LotMovementViewModel>() {
            @Override
            public boolean apply(LotMovementViewModel lotMovementViewModel) {
                for (LotMovementViewModel existingLot : stockMovementViewModel.getExistingLotMovementViewModelList()) {
                    if (existingLot.getLotNumber().equals(lotMovementViewModel.getLotNumber())) {
                        return false;
                    }
                }
                return true;
            }
        }).toSortedList(new Comparator<LotMovementViewModel>() {
            @Override
            public int compare(LotMovementViewModel lot1, LotMovementViewModel lot2) {
                return DateUtil.parseString(lot1.getExpiryDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR).compareTo(DateUtil.parseString(lot2.getExpiryDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR));
            }
        });

        stockMovementViewModel.getExistingLotMovementViewModelList().addAll(lotMovementViewModels);
    }

    public interface NewStockMovementView extends BaseView {
        void showMovementDateEmpty();

        void showQuantityErrors(String errorMsg);

        void showMovementReasonEmpty();

        boolean showLotListError();

        void goToStockCard();
    }
}
