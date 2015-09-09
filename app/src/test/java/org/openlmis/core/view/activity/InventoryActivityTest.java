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

package org.openlmis.core.view.activity;


import android.view.Menu;

import com.google.inject.AbstractModule;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.view.viewmodel.StockCardViewModel;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;

import roboguice.RoboGuice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class InventoryActivityTest {

    private InventoryActivity inventoryActivity;
    private ProductRepository productRepository;

    @Before
    public void setUp() throws LMISException{
        productRepository = mock(ProductRepository.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(ProductRepository.class).toInstance(productRepository);
            }
        });

        ArrayList<Product> products = new ArrayList<>();
        Product product = new Product();
        product.setPrimaryName("test product");
        product.setStrength("500 ml");
        products.add(product);

        when(productRepository.list()).thenReturn(products);

        inventoryActivity = Robolectric.buildActivity(InventoryActivityMock.class).create().get();
    }

    @After
    public void teardown() {
        RoboGuice.Util.reset();
    }


    @Test
    public void shouldCheckQuantityNotEmpty(){
        ((StockCardViewModel)inventoryActivity.mAdapter.getData().get(0)).setChecked(true);
        inventoryActivity.btnDone.performClick();
        assertThat(((StockCardViewModel)inventoryActivity.mAdapter.getData().get(0)).isValidate(), is(false));
    }

    static class InventoryActivityMock extends InventoryActivity {
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            return false;
        }
    }
}
