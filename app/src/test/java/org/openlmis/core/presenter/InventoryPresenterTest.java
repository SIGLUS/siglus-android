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

import com.google.inject.AbstractModule;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.viewmodel.StockCardViewModel;
import org.robolectric.Robolectric;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(LMISTestRunner.class)
public class InventoryPresenterTest extends LMISRepositoryUnitTest {

    private InventoryPresenter inventoryPresenter;

    StockRepository stockRepositoryMock;
    InventoryPresenter.InventoryView view;
    private Product product;
    private StockCard stockCard;

    @Before
    public void setup() throws Exception{
        stockRepositoryMock = mock(StockRepository.class);

        view = mock(InventoryPresenter.InventoryView.class);
        RoboGuice.overrideApplicationInjector(Robolectric.application, new MyTestModule());

        inventoryPresenter = RoboGuice.getInjector(Robolectric.application).getInstance(InventoryPresenter.class);
        inventoryPresenter.attachView(view);

        product = new Product();
        product.setPrimaryName("Test Product");
        product.setCode("ABC");

        stockCard = new StockCard();
        stockCard.setStockOnHand(100);
        stockCard.setProduct(product);
        stockCard.setExpireDates(StringUtils.EMPTY);
    }


    @Test
    public void shouldInitStockCardAndCreateAInitInventoryMovementItem() throws LMISException {
        StockCardViewModel model = new StockCardViewModel(product);
        model.setChecked(true);
        model.setQuantity("100");
        StockCardViewModel model2 = new StockCardViewModel(product);
        model2.setChecked(false);
        model2.setQuantity("200");

        List<StockCardViewModel> stockCardViewModelList = new ArrayList<>();
        stockCardViewModelList.add(model);
        stockCardViewModelList.add(model2);

        inventoryPresenter.initStockCards(stockCardViewModelList);

        verify(stockRepositoryMock, times(1)).save(any(StockCard.class));
        verify(stockRepositoryMock, times(1)).addStockMovementItem(any(StockCard.class), any(StockMovementItem.class));
    }


    @Test
    public void shouldMakePositiveAdjustment() throws LMISException{

        StockCardViewModel model = new StockCardViewModel(stockCard);
        model.setQuantity("120");

        StockMovementItem item = inventoryPresenter.calculateAdjustment(model);

        assertThat(item.getMovementType(), is(StockMovementItem.MovementType.POSITIVE_ADJUST));
        assertThat(item.getMovementQuantity(), is(20L));
    }

    @Test
    public void shouldMakeNegativeAdjustment() throws LMISException{
        StockCardViewModel model = new StockCardViewModel(stockCard);
        model.setQuantity("80");

        StockMovementItem item = inventoryPresenter.calculateAdjustment(model);

        assertThat(item.getMovementType(), is(StockMovementItem.MovementType.NEGATIVE_ADJUST));
        assertThat(item.getMovementQuantity(), is(20L));
    }



    @Test
    public void shouldMakePhysicalInventory() throws LMISException{
        StockCardViewModel model = new StockCardViewModel(stockCard);
        model.setQuantity("100");

        StockMovementItem item = inventoryPresenter.calculateAdjustment(model);

        assertThat(item.getMovementType(), is(StockMovementItem.MovementType.PHYSICAL_INVENTORY));
        assertThat(item.getMovementQuantity(), is(0L));
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(StockRepository.class).toInstance(stockRepositoryMock);
        }
    }
}
