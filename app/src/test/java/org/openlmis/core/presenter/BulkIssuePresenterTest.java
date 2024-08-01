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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.core.presenter.BulkIssuePresenter.MOVEMENT_REASON_CODE;
import static org.openlmis.core.view.activity.AddProductsToBulkEntriesActivity.SELECTED_PRODUCTS;

import android.content.Intent;
import com.google.inject.AbstractModule;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.DraftBulkIssueProduct;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.repository.BulkIssueRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.presenter.BulkIssuePresenter.BulkIssueView;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.BulkIssueProductViewModel;
import androidx.test.core.app.ApplicationProvider;
import roboguice.RoboGuice;
import rx.observers.TestSubscriber;

@RunWith(LMISTestRunner.class)
public class BulkIssuePresenterTest {

  private BulkIssuePresenter presenter;

  private BulkIssueRepository mockBulkIssueRepository;

  private StockRepository mockStockRepository;

  private BulkIssueView mockView;

  private Lot mockLot;
  private LotOnHand mockLotOnHand;

  @Before
  public void setUp() throws Exception {
    mockBulkIssueRepository = mock(BulkIssueRepository.class);
    mockStockRepository = mock(StockRepository.class);
    mockView = mock(BulkIssueView.class);
    mockLot = mock(Lot.class);
    mockLotOnHand = mock(LotOnHand.class);
    when(mockLotOnHand.getLot()).thenReturn(mockLot);

    RoboGuice.overrideApplicationInjector(ApplicationProvider.getApplicationContext(), new AbstractModule() {
      @Override
      protected void configure() {
        bind(BulkIssueRepository.class).toInstance(mockBulkIssueRepository);
        bind(StockRepository.class).toInstance(mockStockRepository);
        bind(BulkIssueView.class).toInstance(mockView);
      }
    });
    presenter = RoboGuice.getInjector(ApplicationProvider.getApplicationContext()).getInstance(BulkIssuePresenter.class);
    presenter.attachView(mockView);
  }

  @Test
  public void shouldCorrectInitViewModelsFromGivenProducts() throws LMISException {
    // given
    StockCard stockCard = StockCardBuilder.buildStockCard();
    stockCard.setLotOnHandListWrapper(Collections.singletonList(mockLotOnHand));
    Intent intent = new Intent();
    intent.putExtra(SELECTED_PRODUCTS, (Serializable) Collections.emptyList());
    intent.putExtra(MOVEMENT_REASON_CODE, "movementReasonCode");
    when(mockStockRepository.listStockCardsByProductIds(any())).thenReturn(Collections.singletonList(stockCard));
    when(mockLot.getLotNumber()).thenReturn("lotNumber");
    when(mockLot.getExpirationDate()).thenReturn(DateUtil.parseString("2021-08-06", DateUtil.DB_DATE_FORMAT));
    TestSubscriber<List<BulkIssueProductViewModel>> subscriber = new TestSubscriber<>(presenter.viewModelsSubscribe);
    presenter.viewModelsSubscribe = subscriber;

    // when
    presenter.initialViewModels(intent);
    subscriber.awaitTerminalEvent();

    // then
    Assert.assertEquals(1, presenter.getCurrentViewModels().size());
    verify(mockView, times(1)).loading();
    verify(mockView, times(1)).loaded();
    verify(mockView, times(1)).onRefreshViewModels();
  }

  @Test
  public void shouldCorrectInitViewModelsFromDraft() throws LMISException {
    // given
    StockCard stockCard = StockCardBuilder.buildStockCard();
    stockCard.setLotOnHandListWrapper(Collections.singletonList(mockLotOnHand));
    DraftBulkIssueProduct mockDraftProduct = mock(DraftBulkIssueProduct.class);
    when(mockDraftProduct.getStockCard()).thenReturn(stockCard);
    when(mockBulkIssueRepository.queryUsableProductAndLotDraft())
        .thenReturn(Collections.singletonList(mockDraftProduct));
    when(mockStockRepository.listStockCardsByProductIds(any())).thenReturn(Collections.singletonList(stockCard));
    when(mockLot.getLotNumber()).thenReturn("lotNumber");
    when(mockLot.getExpirationDate()).thenReturn(DateUtil.parseString("2021-08-06", DateUtil.DB_DATE_FORMAT));
    TestSubscriber<List<BulkIssueProductViewModel>> subscriber = new TestSubscriber<>(presenter.viewModelsSubscribe);
    presenter.viewModelsSubscribe = subscriber;

    // when
    presenter.initialViewModels(new Intent());

    // then
    subscriber.awaitTerminalEvent();
    Assert.assertEquals(1, presenter.getCurrentViewModels().size());
    verify(mockView, times(1)).loading();
    verify(mockView, times(1)).loaded();
    verify(mockView, times(1)).onRefreshViewModels();
  }

  @Test
  public void shouldCorrectAddProducts() throws LMISException {
    // given
    StockCard stockCard = StockCardBuilder.buildStockCard();
    stockCard.setLotOnHandListWrapper(Collections.singletonList(mockLotOnHand));
    when(mockStockRepository.listStockCardsByProductIds(any())).thenReturn(Collections.singletonList(stockCard));
    when(mockLot.getLotNumber()).thenReturn("lotNumber");
    when(mockLot.getExpirationDate()).thenReturn(DateUtil.parseString("2021-08-06", DateUtil.DB_DATE_FORMAT));
    TestSubscriber<List<BulkIssueProductViewModel>> subscriber = new TestSubscriber<>(presenter.viewModelsSubscribe);
    presenter.getCurrentViewModels().clear();
    presenter.viewModelsSubscribe = subscriber;

    // when
    presenter.addProducts(Collections.singletonList(stockCard.getProduct()));
    subscriber.awaitTerminalEvent();

    // then
    Assert.assertEquals(1, presenter.getCurrentViewModels().size());
    verify(mockView, times(1)).loading();
    verify(mockView, times(1)).loaded();
    verify(mockView, times(1)).onRefreshViewModels();
  }

  @Test
  public void shouldGetCorrectProductCodes() {
    // given
    BulkIssueProductViewModel mockViewModel = mock(BulkIssueProductViewModel.class);
    StockCard mockStockCard = mock(StockCard.class);
    Product mockProduct = mock(Product.class);
    when(mockViewModel.getStockCard()).thenReturn(mockStockCard);
    when(mockStockCard.getProduct()).thenReturn(mockProduct);
    when(mockProduct.getCode()).thenReturn("productCode");
    presenter.getCurrentViewModels().clear();
    presenter.getCurrentViewModels().add(mockViewModel);

    // when
    List<String> addedProductCodes = presenter.getAddedProductCodeList();

    // then
    Assert.assertEquals(1, addedProductCodes.size());
    Assert.assertEquals("productCode", addedProductCodes.get(0));
  }

  @Test
  public void shouldGetCorrectEffectedStockCardIds() {
    // given
    BulkIssueProductViewModel mockViewModel = mock(BulkIssueProductViewModel.class);
    StockCard mockStockCard = mock(StockCard.class);
    when(mockViewModel.getStockCard()).thenReturn(mockStockCard);
    when(mockStockCard.getId()).thenReturn(2L);
    presenter.getCurrentViewModels().clear();
    presenter.getCurrentViewModels().add(mockViewModel);

    // when
    long[] effectedStockCardIds = presenter.getEffectedStockCardIds();

    // then
    Assert.assertEquals(1, effectedStockCardIds.length);
    Assert.assertEquals(2, effectedStockCardIds[0]);
  }

  @Test
  public void shouldGetCorrectEffectedStockCardIdsWhenNoViewModels() {
    // given
    presenter.getCurrentViewModels().clear();

    // when
    long[] effectedStockCardIds = presenter.getEffectedStockCardIds();

    // then
    Assert.assertEquals(0, effectedStockCardIds.length);
  }

  @Test
  public void testSaveDraftSuccess() {
    // given
    TestSubscriber<Object> testSubscriber = new TestSubscriber<>(presenter.saveDraftSubscribe);
    presenter.saveDraftSubscribe = testSubscriber;
    presenter.getCurrentViewModels().clear();

    // when
    presenter.saveDraft();

    // then
    testSubscriber.awaitTerminalEvent();
    verify(mockView, times(1)).loading();
    verify(mockView, times(1)).loaded();
    verify(mockView, times(1)).onSaveDraftFinished(true);
  }

  @Test
  public void testSaveDraftFailure() throws LMISException {
    // given
    TestSubscriber<Object> testSubscriber = new TestSubscriber<>(presenter.saveDraftSubscribe);
    presenter.saveDraftSubscribe = testSubscriber;
    LMISException toBeThrown = new LMISException("");
    doThrow(toBeThrown).when(mockBulkIssueRepository).saveDraft(any());

    // when
    presenter.saveDraft();

    // then
    testSubscriber.awaitTerminalEvent();
    Assert.assertEquals(toBeThrown, testSubscriber.getOnErrorEvents().get(0));
    verify(mockView, times(1)).loading();
    verify(mockView, times(1)).loaded();
    verify(mockView, times(1)).onSaveDraftFinished(false);
  }

  @Test
  public void shouldNotConfirmWithoutDraftAndViewModelsIsEmpty() throws LMISException {
    // given
    when(mockBulkIssueRepository.queryUsableProductAndLotDraft()).thenReturn(null);
    presenter.getCurrentViewModels().clear();

    // then
    Assert.assertFalse(presenter.needConfirm());
  }

  @Test
  public void shouldConfirmWithoutDraftAndViewModelsIsNotEmpty() throws LMISException {
    // given
    when(mockBulkIssueRepository.queryUsableProductAndLotDraft()).thenReturn(null);
    presenter.getCurrentViewModels().add(mock(BulkIssueProductViewModel.class));

    // then
    Assert.assertTrue(presenter.needConfirm());
  }

  @Test
  public void shouldConfirmWithDraftAndViewModelsHasChanged() throws LMISException {
    // given
    DraftBulkIssueProduct mockDraftProduct = mock(DraftBulkIssueProduct.class);
    when(mockBulkIssueRepository.queryUsableProductAndLotDraft())
        .thenReturn(Collections.singletonList(mockDraftProduct));

    BulkIssueProductViewModel mockProductViewModel = mock(BulkIssueProductViewModel.class);
    when(mockProductViewModel.hasChanged()).thenReturn(true);
    presenter.getCurrentViewModels().add(mockProductViewModel);

    // then
    Assert.assertTrue(presenter.needConfirm());
  }

  @Test
  public void shouldNotConfirmWithDraftsAndViewModelsHaveSameSize() throws LMISException {
    // given
    DraftBulkIssueProduct mockDraftProduct = mock(DraftBulkIssueProduct.class);
    when(mockBulkIssueRepository.queryUsableProductAndLotDraft())
        .thenReturn(Collections.singletonList(mockDraftProduct));

    BulkIssueProductViewModel mockProductViewModel = mock(BulkIssueProductViewModel.class);
    when(mockProductViewModel.hasChanged()).thenReturn(false);
    presenter.getCurrentViewModels().add(mockProductViewModel);

    // then
    Assert.assertFalse(presenter.needConfirm());
  }

  @Test
  public void shouldConfirmWithDraftsAndViewModelsHaveDifferentSize() throws LMISException {
    // given
    DraftBulkIssueProduct mockDraftProduct = mock(DraftBulkIssueProduct.class);
    when(mockBulkIssueRepository.queryUsableProductAndLotDraft())
        .thenReturn(Collections.singletonList(mockDraftProduct));

    BulkIssueProductViewModel mockProductViewModel1 = mock(BulkIssueProductViewModel.class);
    BulkIssueProductViewModel mockProductViewModel2 = mock(BulkIssueProductViewModel.class);
    when(mockProductViewModel1.hasChanged()).thenReturn(false);
    when(mockProductViewModel2.hasChanged()).thenReturn(false);
    presenter.getCurrentViewModels().add(mockProductViewModel1);
    presenter.getCurrentViewModels().add(mockProductViewModel2);

    // then
    Assert.assertTrue(presenter.needConfirm());
  }

  @Test
  public void shouldConfirmWhenCatchException() throws LMISException {
    // given
    when(mockBulkIssueRepository.queryUsableProductAndLotDraft()).thenThrow(new LMISException(""));

    // then
    Assert.assertTrue(presenter.needConfirm());
  }

  @Test
  public void shouldCorrectSaveData() throws LMISException {
    // given
    BulkIssueProductViewModel mockProductViewModel = mock(BulkIssueProductViewModel.class);
    StockMovementItem mockStockMovementItem = mock(StockMovementItem.class);
    when(mockProductViewModel.convertToMovement(any(), any())).thenReturn(mockStockMovementItem);
    presenter.getCurrentViewModels().clear();
    presenter.getCurrentViewModels().add(mockProductViewModel);
    TestSubscriber<Object> testSubscriber = new TestSubscriber<>(presenter.doIssueSubscribe);
    presenter.doIssueSubscribe = testSubscriber;

    // when
    presenter.doIssue("signature");

    testSubscriber.awaitTerminalEvent();
    // then
    verify(mockStockMovementItem, times(1)).setSignature("signature");
    verify(mockBulkIssueRepository, times(1)).deleteDraft();
    verify(mockView, times(1)).loading();
    verify(mockView, times(1)).loaded();
    verify(mockView, times(1)).onSaveMovementSuccess();
  }

  @Test
  public void shouldUpdateViewWhenError() throws LMISException {
    // given
    doThrow(new LMISException("mocked exception")).when(mockBulkIssueRepository).saveMovement(any());
    TestSubscriber<Object> testSubscriber = new TestSubscriber<>(presenter.doIssueSubscribe);
    presenter.doIssueSubscribe = testSubscriber;

    // when
    presenter.doIssue("signature");

    testSubscriber.awaitTerminalEvent();
    // then
    verify(mockView, times(1)).loading();
    verify(mockView, times(1)).loaded();
    verify(mockView, times(1)).onSaveMovementFailed(any(LMISException.class));
  }
}