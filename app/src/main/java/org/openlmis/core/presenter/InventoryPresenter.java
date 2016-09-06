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

import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.repository.InventoryRepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.BaseView;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import java.util.List;

import rx.Observable;
import rx.functions.Action1;

public abstract class InventoryPresenter extends Presenter {

    @Inject
    ProductRepository productRepository;

    @Inject
    StockRepository stockRepository;

    @Inject
    InventoryRepository inventoryRepository;

    @Inject
    SharedPreferenceMgr sharedPreferenceMgr;

    InventoryView view;

    @Override
    public void attachView(BaseView v) {
        view = (InventoryView) v;
    }

    protected Action1<Object> nextMainPageAction = new Action1<Object>() {
        @Override
        public void call(Object o) {
            view.loaded();
            view.goToParentPage();
        }
    };

    protected Action1<Throwable> errorAction = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            view.loaded();
            view.showErrorMessage(throwable.getMessage());
        }
    };

    public abstract Observable<List<InventoryViewModel>> loadInventory();

    public interface InventoryView extends BaseView {
        void goToParentPage();

        boolean validateInventory();

        void showErrorMessage(String msg);

        void showSignDialog();
    }
}
