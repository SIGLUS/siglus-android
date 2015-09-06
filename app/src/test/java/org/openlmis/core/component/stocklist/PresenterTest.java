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

package org.openlmis.core.component.stocklist;


import android.support.annotation.NonNull;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.repository.RnrFormItemRepository;
import org.robolectric.Robolectric;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class PresenterTest {


    private Presenter presenter;

    RnrFormItemRepository rnrFormItemRepositoryMock;

    @Before
    public void setup() {
        rnrFormItemRepositoryMock = mock(RnrFormItemRepository.class);
        RoboGuice.overrideApplicationInjector(Robolectric.application, new MyTestModule());

        presenter = RoboGuice.getInjector(Robolectric.application).getInstance(Presenter.class);
    }

    @Test
    public void shouldGetNormalLevelWhenSOHGreaterThanAvg() throws LMISException {
        List<RnrFormItem> rnrFormItemList = new ArrayList<>();

        int issued = 100;
        rnrFormItemList.add(getRnrFormItem(issued));
        rnrFormItemList.add(getRnrFormItem(issued));
        rnrFormItemList.add(getRnrFormItem(issued));

        when(rnrFormItemRepositoryMock.queryListForLowStockByProductId(any(Product.class))).thenReturn(rnrFormItemList);

        StockCard stockCard = new StockCard();
        stockCard.setStockOnHand(100);

        int stockOnHandLevel = presenter.getStockOnHandLevel(stockCard);

        assertThat(stockOnHandLevel, is(Presenter.STOCK_ON_HAND_NORMAL));

    }

    @Test
    public void shouldGetLowLevelWhenSOHGreaterThanAvg() throws LMISException {
        List<RnrFormItem> rnrFormItemList = new ArrayList<>();

        int issued = 100;
        rnrFormItemList.add(getRnrFormItem(issued));
        rnrFormItemList.add(getRnrFormItem(issued));
        rnrFormItemList.add(getRnrFormItem(issued));

        when(rnrFormItemRepositoryMock.queryListForLowStockByProductId(any(Product.class))).thenReturn(rnrFormItemList);

        StockCard stockCard = new StockCard();
        stockCard.setStockOnHand(2);

        int stockOnHandLevel = presenter.getStockOnHandLevel(stockCard);

        assertThat(stockOnHandLevel, is(Presenter.STOCK_ON_HAND_LOW_STOCK));
    }

    @Test
    public void shouldGetStockOutLevelWhenSOHGreaterThanAvg() throws LMISException {
        List<RnrFormItem> rnrFormItemList = new ArrayList<>();

        int issued = 100;
        rnrFormItemList.add(getRnrFormItem(issued));
        rnrFormItemList.add(getRnrFormItem(issued));
        rnrFormItemList.add(getRnrFormItem(issued));

        when(rnrFormItemRepositoryMock.queryListForLowStockByProductId(any(Product.class))).thenReturn(rnrFormItemList);

        StockCard stockCard = new StockCard();
        stockCard.setStockOnHand(0);

        int stockOnHandLevel = presenter.getStockOnHandLevel(stockCard);

        assertThat(stockOnHandLevel, is(Presenter.STOCK_ON_HAND_STOCK_OUT));
    }

    @NonNull
    private RnrFormItem getRnrFormItem(long issued) {
        RnrFormItem rnrFormItem = new RnrFormItem();
        rnrFormItem.setInventory(new Long(10));
        rnrFormItem.setIssued(issued);
        return rnrFormItem;
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(RnrFormItemRepository.class).toInstance(rnrFormItemRepositoryMock);
        }
    }
}
