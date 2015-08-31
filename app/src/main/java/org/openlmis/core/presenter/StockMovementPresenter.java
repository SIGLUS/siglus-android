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

import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.View;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;
import org.roboguice.shaded.goole.common.base.Function;

import java.util.ArrayList;
import java.util.List;

import static org.roboguice.shaded.goole.common.collect.FluentIterable.from;

public class StockMovementPresenter implements Presenter {

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
    public void attachView(View v) throws ViewNotMatchException {
        if (v instanceof StockMovementView) {
            this.view = (StockMovementView) v;
        } else {
            throw new ViewNotMatchException("Need StockMovementView");
        }
    }

    public void setStockCard(long stockCardId) {
        this.stockCard = getStockCard(stockCardId);
    }

    public List<StockMovementViewModel> getStockMovementModels() {
        try {
            return from(stockRepository.listLastFive(stockCard.getId())).transform(new Function<StockMovementItem, StockMovementViewModel>() {
                @Override
                public StockMovementViewModel apply(StockMovementItem stockMovementItem) {
                    return new StockMovementViewModel(stockMovementItem);
                }
            }).toList();
        } catch (LMISException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void saveStockMovement(StockMovementItem stockMovementItem) throws LMISException {
        stockRepository.addStockMovementItem(stockCard, stockMovementItem);
    }

    public void submitStockMovement(StockMovementViewModel viewModel) {
        if (viewModel.validate()) {
            try {
                saveStockMovement(viewModel.convertViewToModel());
                view.finish();
            } catch (LMISException e) {
                view.showErrorAlert(e.getMessage());
            }
        } else {
            view.showErrorAlert(context.getResources().getString(R.string.msg_validation_error));
        }
    }

    public StockCard getStockCard(long stockId) {
        try {
            return stockRepository.queryStockCardById(stockId);
        } catch (LMISException e) {
            e.printStackTrace();
        }
        return null;
    }


    public interface StockMovementView extends View {
        void showErrorAlert(String msg);

        void finish();
    }
}
