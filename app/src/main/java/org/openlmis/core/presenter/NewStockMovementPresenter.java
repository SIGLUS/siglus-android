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

import org.apache.commons.lang3.ArrayUtils;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.base.Predicate;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class NewStockMovementPresenter extends Presenter {
    @Inject
    StockRepository stockRepository;

    @Getter
    final StockMovementViewModel viewModel = new StockMovementViewModel();

    @Getter
    private StockCard stockCard;

    @Getter
    private List<MovementReasonManager.MovementReason> movementReasons;
    private String[] reasonDescriptionList;

    NewStockMovementView view;

    @Override
    public void attachView(BaseView v) {
        this.view = (NewStockMovementView) v;
    }

    public void loadData(Long stockCardId, MovementReasonManager.MovementType movementType, boolean isKit) {
        try {
            movementReasons = MovementReasonManager.getInstance().buildReasonListForMovementType(movementType);
            stockCard = stockRepository.queryStockCardById(stockCardId);
            viewModel.setProduct(stockCard.getProduct());
            viewModel.setMovementType(movementType);
            viewModel.setKit(isKit);
        } catch (LMISException e) {
            new LMISException(e," NewStockM.loadData").reportToFabric();
        }
        loadExistingLotMovementViewModels(movementType);
    }

    public void saveStockMovement() {
        Subscription subscription = getSaveMovementObservable().subscribe(successAction, errorAction);
        subscriptions.add(subscription);
    }

    protected Action1<StockMovementViewModel> successAction = new Action1<StockMovementViewModel>() {
        @Override
        public void call(StockMovementViewModel viewModel) {
            view.goToStockCard();
        }
    };

    protected Action1<Throwable> errorAction = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            view.loaded();
            ToastUtil.show(throwable.getMessage());
        }
    };


    protected Observable<StockMovementViewModel> getSaveMovementObservable() {
        return Observable.create(new Observable.OnSubscribe<StockMovementViewModel>() {
            @Override
            public void call(Subscriber<? super StockMovementViewModel> subscriber) {
                if (convertViewModelToDataModelAndSave()) {
                    subscriber.onNext(viewModel);
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(new Exception(LMISApp.getContext().getResources().getString(R.string.msg_invalid_stock_movement)));
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    private boolean convertViewModelToDataModelAndSave() {
        viewModel.populateStockExistence(stockCard.calculateSOHFromLots());
        StockMovementItem stockMovementItem = viewModel.convertViewToModel(stockCard);
        stockCard.setStockOnHand(stockMovementItem.getStockOnHand());

        Date lastMovementDate = getLastMovementCreateDate();
        if (lastMovementDate == null || stockMovementItem.getCreatedAt().after(lastMovementDate)) {
            stockRepository.addStockMovementAndUpdateStockCard(stockMovementItem);
            if (stockCard.calculateSOHFromLots() == 0 && !stockCard.getProduct().isActive()) {
                SharedPreferenceMgr.getInstance().setIsNeedShowProductsUpdateBanner(true, stockCard.getProduct().getPrimaryName());
            }
            return true;
        }
        return false;
    }

    private void loadExistingLotMovementViewModels(final MovementReasonManager.MovementType movementType) {
        List<LotMovementViewModel> lotMovementViewModels = FluentIterable.from(stockCard.getNonEmptyLotOnHandList()).transform(new Function<LotOnHand, LotMovementViewModel>() {
            @Override
            public LotMovementViewModel apply(LotOnHand lotOnHand) {
                return new LotMovementViewModel(lotOnHand.getLot().getLotNumber(),
                        DateUtil.formatDate(lotOnHand.getLot().getExpirationDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR),
                        lotOnHand.getQuantityOnHand().toString(), movementType);
            }
        }).filter(new Predicate<LotMovementViewModel>() {
            @Override
            public boolean apply(LotMovementViewModel lotMovementViewModel) {
                for (LotMovementViewModel existingLot : viewModel.getExistingLotMovementViewModelList()) {
                    if (existingLot.getLotNumber().equals(lotMovementViewModel.getLotNumber())) {
                        return false;
                    }
                }
                return true;
            }
        }).toSortedList(new Comparator<LotMovementViewModel>() {
            @Override
            public int compare(LotMovementViewModel lot1, LotMovementViewModel lot2) {
                Date localDate = DateUtil.parseString(lot1.getExpiryDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR);
                if (localDate != null) {
                    return localDate.compareTo(DateUtil.parseString(lot2.getExpiryDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR));
                } else {
                    return 0;
                }
            }
        });

        viewModel.getExistingLotMovementViewModelList().addAll(lotMovementViewModels);
    }

    public MovementReasonManager.MovementType getMovementType() {
        return viewModel.getMovementType();
    }

    public boolean isKit() {
        return viewModel.isKit();
    }

    public String[] getMovementReasonDescriptionList() {
        if (ArrayUtils.isEmpty(reasonDescriptionList)) {
            reasonDescriptionList = FluentIterable.from(movementReasons).transform(new Function<MovementReasonManager.MovementReason, String>() {
                @Override
                public String apply(MovementReasonManager.MovementReason movementReason) {
                    return movementReason.getDescription();
                }
            }).toArray(String.class);
        }
        return reasonDescriptionList;
    }

    public boolean shouldLoadKitMovementPage() {
        return !(isKit() && SharedPreferenceMgr.getInstance().shouldSyncLastYearStockData());
    }

    public boolean validateKitQuantity() {
        MovementReasonManager.MovementType movementType = viewModel.getTypeQuantityMap().keySet().iterator().next();
        if (quantityIsLargerThanSoh(viewModel.getTypeQuantityMap().get(movementType), movementType)) {
            view.showQuantityErrors(LMISApp.getContext().getString(R.string.msg_invalid_quantity));
            return false;
        }
        return true;
    }

    public boolean quantityIsLargerThanSoh(String quantity, MovementReasonManager.MovementType type) {
        return (MovementReasonManager.MovementType.ISSUE.equals(type) || MovementReasonManager.MovementType.NEGATIVE_ADJUST.equals(type)) && Long.parseLong(quantity) > stockCard.calculateSOHFromLots();
    }

    public Date getLastMovementDate() {
        return stockCard.getLastStockMovementDate();
    }

    public Date getLastMovementCreateDate() {
        return stockCard.getLastStockMovementCreatedTime();
    }

    public interface NewStockMovementView extends BaseView {

        void clearErrorAlerts();

        void showQuantityErrors(String errorMsg);

        void goToStockCard();
    }
}
