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

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;

import lombok.Getter;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class NewStockMovementPresenter extends Presenter {

    @Getter
    StockMovementViewModel stockMovementModel;

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

    public void saveStockMovement(final StockMovementViewModel viewModel, final Long stockCardId) {
        if (showErrors(viewModel)) return;

        getSaveMovementObservable(viewModel, stockCardId).subscribe(new Action1<StockMovementViewModel>() {
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

    private boolean showErrors(StockMovementViewModel viewModel) {
        MovementReasonManager.MovementType movementType = viewModel.getTypeQuantityMap().keySet().iterator().next();
        if (StringUtils.isBlank(viewModel.getMovementDate())) {
            view.showMovementDateEmpty();
            return true;
        }
        if (viewModel.getReason() == null) {
            view.showMovementReasonEmpty();
            return true;
        }
        if (StringUtils.isBlank(viewModel.getTypeQuantityMap().get(movementType))) {
            view.showQuantityEmpty();
            return true;
        }
        if (StringUtils.isBlank(viewModel.getSignature())) {
            view.showSignatureEmpty();
            return true;
        }

        if (!viewModel.validateQuantitiesNotZero()) {
            view.showQuantityZero();
            return true;
        }

        if (quantityIsLargerThanSoh(viewModel.getTypeQuantityMap().get(movementType), movementType)) {
            view.showSOHError();
            return true;
        }

        if(!checkSignature(viewModel.getSignature())) {
            view.showSignatureError();
            return true;
        }
        return false;
    }

    private void convertViewModelToDataModelAndSave(StockMovementViewModel viewModel, Long stockCardId) {
        try {
            viewModel.populateStockExistence(previousStockMovement.getStockOnHand());
            StockMovementItem stockMovementItem = viewModel.convertViewToModel();
            StockCard stockCard = stockRepository.queryStockCardById(stockCardId);
            stockMovementItem.setStockCard(stockCard);
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

    private boolean checkSignature(String signature) {
        return signature.length() >= 2 && signature.length() <= 5 && signature.matches("\\D+");
    }

    private boolean quantityIsLargerThanSoh(String quantity, MovementReasonManager.MovementType type) {
        if (MovementReasonManager.MovementType.ISSUE.equals(type) || MovementReasonManager.MovementType.NEGATIVE_ADJUST.equals(type)) {
            return Long.parseLong(quantity) > previousStockMovement.getStockOnHand();
        }
        return false;
    }

    public interface NewStockMovementView extends BaseView {
        void showMovementDateEmpty();

        void showMovementReasonEmpty();

        void showQuantityEmpty();

        void showSignatureEmpty();

        void showSOHError();

        void showQuantityZero();

        void showSignatureError();

        void goToStockCard();
    }
}
