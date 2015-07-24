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

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.repository.ProductRepository;

import java.util.List;

public class InventoryPresenter implements Presenter{

    @Inject
    ProductRepository productRepository;

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void attachView(Activity v) {

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

}
