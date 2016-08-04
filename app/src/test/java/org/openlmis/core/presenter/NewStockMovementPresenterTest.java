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

import android.support.annotation.NonNull;

import com.google.inject.AbstractModule;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.KitProduct;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.StockMovementItemBuilder;
import org.openlmis.core.model.builder.StockMovementViewModelBuilder;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;
import org.robolectric.RuntimeEnvironment;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;

import roboguice.RoboGuice;
import rx.observers.TestSubscriber;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
    public void shouldValidateQuantityIfItIsLargerThanSohWhenSaving() throws ParseException {
        StockMovementViewModel stockMovementViewModel = new StockMovementViewModelBuilder()
                .withMovementDate("2010-10-10").withSignature("signature")
                .withMovementReason(new MovementReasonManager.MovementReason(StockMovementItem.MovementType.ISSUE, "", "")).build();
        HashMap<StockMovementItem.MovementType, String> quantityMap = new HashMap<>();
        quantityMap.put(StockMovementItem.MovementType.ISSUE, "10");
        stockMovementViewModel.setTypeQuantityMap(quantityMap);

        StockMovementItem previousStockItem = new StockMovementItemBuilder().withStockOnHand(5).build();
        newStockMovementPresenter.previousStockMovement = previousStockItem;
        newStockMovementPresenter.saveStockMovement(stockMovementViewModel, 1L);
        verify(view).showSOHError();
    }

    @Test
    public void shouldNotErrorQuantityIfItIsLargerThanSohButAdditiveWhenSaving() throws ParseException {
        StockMovementViewModel stockMovementViewModel = new StockMovementViewModelBuilder()
                .withMovementDate("2010-10-10").withSignature("signature")
                .withMovementReason(new MovementReasonManager.MovementReason(StockMovementItem.MovementType.RECEIVE, "", "")).build();
        HashMap<StockMovementItem.MovementType, String> quantityMap = new HashMap<>();
        quantityMap.put(StockMovementItem.MovementType.RECEIVE, "10");
        stockMovementViewModel.setTypeQuantityMap(quantityMap);

        StockMovementItem previousStockItem = new StockMovementItemBuilder().withStockOnHand(5).build();
        newStockMovementPresenter.previousStockMovement = previousStockItem;
        newStockMovementPresenter.saveStockMovement(stockMovementViewModel, 1L);
        verify(view, never()).showSOHError();
    }

    @Test
    public void shouldValidateEmptyDateFieldWhenSaving() throws ParseException {
        StockMovementViewModel stockMovementViewModel = new StockMovementViewModelBuilder()
                .withMovementDate("")
                .withMovementReason(new MovementReasonManager.MovementReason(StockMovementItem.MovementType.ISSUE, "", "")).build();
        HashMap<StockMovementItem.MovementType, String> quantityMap = new HashMap<>();
        quantityMap.put(StockMovementItem.MovementType.ISSUE, "10");
        stockMovementViewModel.setTypeQuantityMap(quantityMap);

        StockMovementItem previousStockItem = new StockMovementItemBuilder().withStockOnHand(10).build();
        newStockMovementPresenter.previousStockMovement = previousStockItem;
        newStockMovementPresenter.saveStockMovement(stockMovementViewModel, 1L);
        verify(view).showMovementDateEmpty();
    }

    @Test
    public void shouldValidateEmptySignatureFieldWhenSaving() throws ParseException {
        StockMovementViewModel stockMovementViewModel = new StockMovementViewModelBuilder()
                .withMovementDate("2000")
                .withMovementReason(new MovementReasonManager.MovementReason(StockMovementItem.MovementType.ISSUE, "", "")).build();
        HashMap<StockMovementItem.MovementType, String> quantityMap = new HashMap<>();
        quantityMap.put(StockMovementItem.MovementType.ISSUE, "10");
        stockMovementViewModel.setTypeQuantityMap(quantityMap);

        StockMovementItem previousStockItem = new StockMovementItemBuilder().withStockOnHand(10).build();
        newStockMovementPresenter.previousStockMovement = previousStockItem;
        newStockMovementPresenter.saveStockMovement(stockMovementViewModel, 1L);
        verify(view).showSignatureEmpty();
    }

    @Test
    public void shouldValidateEmptyReasonFieldWhenSaving() throws ParseException {
        StockMovementViewModel stockMovementViewModel = new StockMovementViewModelBuilder()
                .withMovementReason(null)
                .withMovementDate("2000").build();
        HashMap<StockMovementItem.MovementType, String> quantityMap = new HashMap<>();
        quantityMap.put(StockMovementItem.MovementType.ISSUE, "10");
        stockMovementViewModel.setTypeQuantityMap(quantityMap);

        StockMovementItem previousStockItem = new StockMovementItemBuilder().withStockOnHand(10).build();
        newStockMovementPresenter.previousStockMovement = previousStockItem;
        newStockMovementPresenter.saveStockMovement(stockMovementViewModel, 1L);
        verify(view).showMovementReasonEmpty();
    }

    @Test
    public void shouldValidateQuantityNotZero() throws ParseException {
        StockMovementViewModel stockMovementViewModel = new StockMovementViewModelBuilder()
                .withSignature("abc")
                .withMovementDate("2000").build();
        HashMap<StockMovementItem.MovementType, String> quantityMap = new HashMap<>();
        quantityMap.put(StockMovementItem.MovementType.ISSUE, "0");
        stockMovementViewModel.setTypeQuantityMap(quantityMap);

        StockMovementItem previousStockItem = new StockMovementItemBuilder().withStockOnHand(10).build();
        newStockMovementPresenter.previousStockMovement = previousStockItem;
        newStockMovementPresenter.saveStockMovement(stockMovementViewModel, 1L);
        verify(view).showQuantityZero();
    }

    @Test
    public void shouldValidateSignatureWhenLessThan2Characters() throws ParseException {
        StockMovementViewModel stockMovementViewModel = new StockMovementViewModelBuilder()
                .withSignature("a")
                .withMovementReason(new MovementReasonManager.MovementReason(StockMovementItem.MovementType.ISSUE, "", ""))
                .withMovementDate("2000").build();
        HashMap<StockMovementItem.MovementType, String> quantityMap = new HashMap<>();
        quantityMap.put(StockMovementItem.MovementType.ISSUE, "10");
        stockMovementViewModel.setTypeQuantityMap(quantityMap);

        StockMovementItem previousStockItem = new StockMovementItemBuilder().withStockOnHand(10).build();
        newStockMovementPresenter.previousStockMovement = previousStockItem;
        newStockMovementPresenter.saveStockMovement(stockMovementViewModel, 1L);
        verify(view).showSignatureError();
    }

    @Test
    public void shouldSaveStockItemWhenSaving() throws Exception {
        StockCard stockCard = createStockCard(0, true);
        StockMovementItem item = new StockMovementItem();
        item.setStockOnHand(0L);

        newStockMovementPresenter.previousStockMovement = new StockMovementItemBuilder().withStockOnHand(10).build();
        StockMovementViewModel viewModel = mock(StockMovementViewModel.class);
        when(viewModel.convertViewToModel()).thenReturn(item);
        when(stockRepositoryMock.queryStockCardById(stockCard.getId())).thenReturn(stockCard);

        TestSubscriber<StockMovementViewModel> subscriber = new TestSubscriber<>();
        newStockMovementPresenter.getSaveMovementObservable(viewModel, stockCard.getId()).subscribe(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();

        verify(stockRepositoryMock).addStockMovementAndUpdateStockCard(item);
    }

    @NonNull
    private StockCard createStockCard(int stockOnHand, boolean isKit) {
        StockCard stockCard = new StockCard();
        stockCard.setId(200L);
        stockCard.setStockOnHand(stockOnHand);
        Product product = new Product();
        product.setActive(true);
        product.setKit(isKit);
        product.setKitProductList(Arrays.asList(new KitProduct()));
        stockCard.setProduct(product);
        return stockCard;
    }


    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(StockRepository.class).toInstance(stockRepositoryMock);
        }
    }
}