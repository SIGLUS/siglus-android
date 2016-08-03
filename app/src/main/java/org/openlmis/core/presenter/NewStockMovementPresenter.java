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
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;

import lombok.Getter;

public class NewStockMovementPresenter extends Presenter {

    @Getter
    StockMovementViewModel stockMovementModel;

    @Inject
    StockRepository stockRepository;

    NewStockMovementView view;

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
        return stockRepository.queryLastStockMovementItemByStockCardId(stockCardId);
    }


    public interface NewStockMovementView extends BaseView {
    }
}
