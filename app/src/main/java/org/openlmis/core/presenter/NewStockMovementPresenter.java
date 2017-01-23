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
import org.apache.commons.lang3.StringUtils;
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
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;
import org.roboguice.shaded.goole.common.base.Function;
import org.roboguice.shaded.goole.common.base.Predicate;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

import java.util.Comparator;
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
            e.reportToFabric();
        }
        loadExistingLotMovementViewModels(movementType);
    }

    public void saveStockMovement() {
        Subscription subscription = getSaveMovementObservable().subscribe(new Action1<StockMovementViewModel>() {
            @Override
            public void call(StockMovementViewModel viewModel) {
                view.goToStockCard();
            }
        });
        subscriptions.add(subscription);
    }

    protected Observable<StockMovementViewModel> getSaveMovementObservable() {
        return Observable.create(new Observable.OnSubscribe<StockMovementViewModel>() {
            @Override
            public void call(Subscriber<? super StockMovementViewModel> subscriber) {
                convertViewModelToDataModelAndSave();
                subscriber.onNext(viewModel);
                subscriber.onCompleted();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }

    private void convertViewModelToDataModelAndSave() {
        viewModel.populateStockExistence(stockCard.getStockOnHand());
        StockMovementItem stockMovementItem = viewModel.convertViewToModel(stockCard);
        stockCard.setStockOnHand(stockMovementItem.getStockOnHand());

        stockRepository.addStockMovementAndUpdateStockCard(stockMovementItem);
        if (stockCard.getStockOnHand() == 0 && !stockCard.getProduct().isActive()) {
            SharedPreferenceMgr.getInstance().setIsNeedShowProductsUpdateBanner(true, stockCard.getProduct().getPrimaryName());
        }
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
                return DateUtil.parseString(lot1.getExpiryDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR).compareTo(DateUtil.parseString(lot2.getExpiryDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR));
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

    public boolean validate() {
        if (StringUtils.isBlank(viewModel.getMovementDate())) {
            view.showMovementDateEmpty();
            return false;
        }
        if (viewModel.getReason() == null) {
            view.showMovementReasonEmpty();
            return false;
        }

        if (isKit() && checkKitQuantityError()) return false;

        if (StringUtils.isBlank(viewModel.getSignature())) {
            view.showSignatureErrors(LMISApp.getContext().getString(R.string.msg_empty_signature));
            return false;
        }
        if (isKit() && !viewModel.validateQuantitiesNotZero()) {
            view.showQuantityErrors(LMISApp.getContext().getString(R.string.msg_entries_error));
            return false;
        }
        if (!checkSignature()) {
            view.showSignatureErrors(LMISApp.getContext().getString(R.string.hint_signature_error_message));
            return false;
        }

        return isKit() || !view.showLotListError() && !lotListEmptyError();
    }

    private boolean checkSignature() {
        return viewModel.getSignature().length() >= 2 && viewModel.getSignature().length() <= 5 && viewModel.getSignature().matches("\\D+");
    }

    public boolean shouldLoadKitMovementPage() {
        return !(isKit() && SharedPreferenceMgr.getInstance().shouldSyncLastYearStockData());
    }

    public boolean checkKitQuantityError() {
        MovementReasonManager.MovementType movementType = viewModel.getTypeQuantityMap().keySet().iterator().next();
        if (StringUtils.isBlank(viewModel.getTypeQuantityMap().get(movementType))) {
            view.showQuantityErrors(LMISApp.getContext().getString(R.string.msg_empty_quantity));
            return true;
        }
        if (quantityIsLargerThanSoh(viewModel.getTypeQuantityMap().get(movementType), movementType)) {
            view.showQuantityErrors(LMISApp.getContext().getString(R.string.msg_invalid_quantity));
            return true;
        }
        return false;
    }

    public boolean quantityIsLargerThanSoh(String quantity, MovementReasonManager.MovementType type) {
        return (MovementReasonManager.MovementType.ISSUE.equals(type) || MovementReasonManager.MovementType.NEGATIVE_ADJUST.equals(type)) && Long.parseLong(quantity) > stockCard.getStockOnHand();
    }

    public boolean lotListEmptyError() {
        view.clearErrorAlerts();
        if (viewModel.isLotEmpty()) {
            view.showEmptyLotError();
            return true;
        }
        if (!viewModel.movementQuantitiesExist()) {
            view.showLotQuantityError();
            return true;
        }
        return false;
    }

    public boolean onComplete() {
        if (!validate()) {
            if (!isKit()) {
                view.refreshLotListView();
            }
            view.loaded();
            return false;
        }
        saveStockMovement();
        return true;
    }

    public interface NewStockMovementView extends BaseView {

        void refreshLotListView();

        void clearErrorAlerts();

        void showLotQuantityError();

        void showEmptyLotError();

        void showMovementDateEmpty();

        void showQuantityErrors(String errorMsg);

        void showMovementReasonEmpty();

        void showSignatureErrors(String errorMsg);

        boolean showLotListError();

        void goToStockCard();
    }
}
