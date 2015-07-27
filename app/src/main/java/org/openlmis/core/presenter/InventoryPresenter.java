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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import java.util.ArrayList;
import java.util.List;

public class InventoryPresenter implements Presenter{

    @Inject
    ProductRepository productRepository;

    @Inject
    StockRepository stockRepository;

    Activity view;

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void attachView(Activity v) {
        view = v;
    }

    @Override
    public void attachIncomingIntent(Intent intent) {

    }

    @Override
    public void initPresenter(Context context) {

    }

    public List<Product> loadMasterProductList() {
        List<Product> list = null;
        try {
            list = productRepository.loadProductList();
        }catch (LMISException e){
            e.printStackTrace();
        }

        return list;
    }

    public void initStockCard(List<InventoryViewModel> list){
        List<StockCard> stockCards = new ArrayList<>();

        for (InventoryViewModel model : list){
            if (model.isChecked()){
                StockCard stockCard = new StockCard();
                stockCard.setProduct(model.getProduct());
                stockCard.setStockOnHand(Integer.parseInt(model.getQuantity()));
                stockCard.setExpireDates(model.getExpireDate());

                stockCards.add(stockCard);
            }
        }
        stockRepository.batchSave(stockCards);
        Toast.makeText(view , "Inventory Complete: you created " + stockCards.size() + "", Toast.LENGTH_SHORT).show();
    }
}
