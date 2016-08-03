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

import com.google.inject.AbstractModule;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.StockRepository;
import org.robolectric.RuntimeEnvironment;

import roboguice.RoboGuice;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class NewStockMovementPresenterTest {

    private NewStockMovementPresenter newStockMovementPresenter;
    private StockRepository stockRepositoryMock;
    NewStockMovementPresenter.NewStockMovementView view;

    @Before
    public void setup() throws Exception {
        stockRepositoryMock = mock(StockRepository.class);

        view = mock(NewStockMovementPresenter.NewStockMovementView.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());

        newStockMovementPresenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(NewStockMovementPresenter.class);
        newStockMovementPresenter.attachView(view);
    }


    @Test
    public void shouldLoadDataFromPreviousStockMovement() throws LMISException {
        StockMovementItem item = new StockMovementItem();
        when(stockRepositoryMock.queryLastStockMovementItemByStockCardId(123L)).thenReturn(item);

        Assertions.assertThat(newStockMovementPresenter.loadPreviousMovement(123L)).isEqualTo(item);
    }

    @Test
    public void shouldGetMovementReasonListByMovementType() {
        String[] movementReasons = newStockMovementPresenter.getMovementReasonList("Issues");

        Assertions.assertThat(movementReasons.length).isEqualTo(10);
        Assertions.assertThat(movementReasons[0]).isEqualTo("Public pharmacy");
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(StockRepository.class).toInstance(stockRepositoryMock);
        }
    }
}