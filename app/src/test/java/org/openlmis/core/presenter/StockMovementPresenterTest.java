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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.StockRepository;
import org.robolectric.Robolectric;

import roboguice.RoboGuice;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class StockMovementPresenterTest {

    private StockMovementPresenter stockMovementPresenter;

    StockRepository stockRepositoryMock;

    @Before
    public void setup() {
        stockRepositoryMock = mock(StockRepository.class);
        RoboGuice.overrideApplicationInjector(Robolectric.application, new MyTestModule());

        stockMovementPresenter = RoboGuice.getInjector(Robolectric.application).getInstance(StockMovementPresenter.class);
    }

    @Test
    public void shouldSaveStockMovement() throws LMISException {
        StockMovementItem stockMovementItem = new StockMovementItem();
        StockCard stockCard = new StockCard();
        when(stockRepositoryMock.queryStockCardById(anyInt())).thenReturn(stockCard);

        stockMovementPresenter.saveStockMovement(stockMovementItem);

        verify(stockRepositoryMock).update(stockCard);
        verify(stockRepositoryMock).saveStockItem(stockMovementItem);
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(StockRepository.class).toInstance(stockRepositoryMock);
        }
    }
}
