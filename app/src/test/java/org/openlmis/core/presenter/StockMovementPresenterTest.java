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
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;

import roboguice.RoboGuice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());

        stockMovementPresenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(StockMovementPresenter.class);
        stockMovementPresenter.attachView(view);
    }

    @Test
    public void shouldValidateStockMovementViewModelBeforeSaveAndReturnErrorIfInvalid() {
        StockMovementViewModel stockMovementViewModelMock = mock(StockMovementViewModel.class);
        when(stockMovementViewModelMock.validateEmpty()).thenReturn(false);

        stockMovementPresenter.submitStockMovement(stockMovementViewModelMock);
        verify(stockMovementViewModelMock,times(2)).validateEmpty();
        verify(view).showErrorAlert(anyString());
    }

    @Test
    public void shouldSaveStockMovement() throws LMISException {
        StockCard stockCard = new StockCard();
        when(stockRepositoryMock.queryStockCardById(123)).thenReturn(stockCard);
        stockMovementPresenter.setStockCard(123);

        StockMovementItem item = new StockMovementItem();

        StockMovementViewModel viewModel = mock(StockMovementViewModel.class);
        when(viewModel.validateInputValid()).thenReturn(true);
        when(viewModel.validateEmpty()).thenReturn(true);
        when(viewModel.convertViewToModel()).thenReturn(item);

        stockMovementPresenter.submitStockMovement(viewModel);
        verify(stockRepositoryMock).addStockMovementAndUpdateStockCard(stockCard, item);
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(StockRepository.class).toInstance(stockRepositoryMock);
        }
    }

    @Test
    public void shouldGetStockCardExpireDates() throws Exception {
        StockCard stockCard = new StockCard();
        when(stockRepositoryMock.queryStockCardById(123)).thenReturn(stockCard);
        stockMovementPresenter.setStockCard(123);
        ArrayList<String> stockCardExpireDates = stockMovementPresenter.getStockCardExpireDates();
        assertThat(stockCardExpireDates.size(), is(0));
    }
}
