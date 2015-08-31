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
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;
import org.robolectric.Robolectric;

import roboguice.RoboGuice;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class StockMovementPresenterTest extends LMISRepositoryUnitTest {

    private StockMovementPresenter stockMovementPresenter;

    StockRepository stockRepositoryMock;
    StockMovementPresenter.StockMovementView view;

    @Before
    public void setup() throws Exception{
        stockRepositoryMock = mock(StockRepository.class);

        view = mock(StockMovementPresenter.StockMovementView.class);
        RoboGuice.overrideApplicationInjector(Robolectric.application, new MyTestModule());

        stockMovementPresenter = RoboGuice.getInjector(Robolectric.application).getInstance(StockMovementPresenter.class);
        stockMovementPresenter.attachView(view);
    }

    @Test
    public void shouldValidateStockMovementViewModelBeforeSaveAndReturnErrorIfInvalid() {
        StockMovementViewModel stockMovementViewModelMock = mock(StockMovementViewModel.class);
        when(stockMovementViewModelMock.validate()).thenReturn(false);

        stockMovementPresenter.submitStockMovement(stockMovementViewModelMock);
        verify(stockMovementViewModelMock).validate();
        verify(view).showErrorAlert(anyString());
    }

    @Test
    public void shouldSaveStockMovement() throws LMISException {
        StockCard stockCard = new StockCard();
        when(stockRepositoryMock.queryStockCardById(123)).thenReturn(stockCard);
        stockMovementPresenter.setStockCard(123);

        StockMovementItem item = new StockMovementItem();

        StockMovementViewModel viewModel = mock(StockMovementViewModel.class);
        when(viewModel.validate()).thenReturn(true);
        when(viewModel.convertViewToModel()).thenReturn(item);

        stockMovementPresenter.submitStockMovement(viewModel);
        verify(stockRepositoryMock).addStockMovementItem(stockCard, item);
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(StockRepository.class).toInstance(stockRepositoryMock);
        }
    }
}
