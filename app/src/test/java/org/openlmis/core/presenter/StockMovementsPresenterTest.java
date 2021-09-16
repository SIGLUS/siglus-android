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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyByte;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.annotation.NonNull;
import com.google.inject.AbstractModule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.KitProduct;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.model.service.StockService;
import org.openlmis.core.view.viewmodel.StockMovementHistoryViewModel;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;
import rx.observers.TestSubscriber;

@RunWith(LMISTestRunner.class)
public class StockMovementsPresenterTest extends LMISRepositoryUnitTest {

  private StockMovementsPresenter stockMovementsPresenter;

  StockRepository mockStockRepository;
  ProductRepository productRepository;
  StockMovementsPresenter.StockMovementView view;
  StockService stockServiceMock;

  SharedPreferenceMgr sharedPreferenceMgr;
  private StockMovementRepository mockStockMovementRepository;

  @Before
  public void setup() throws Exception {
    mockStockRepository = mock(StockRepository.class);
    productRepository = mock(ProductRepository.class);
    sharedPreferenceMgr = mock(SharedPreferenceMgr.class);
    stockServiceMock = mock(StockService.class);
    mockStockMovementRepository = mock(StockMovementRepository.class);

    view = mock(StockMovementsPresenter.StockMovementView.class);
    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());

    stockMovementsPresenter = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(StockMovementsPresenter.class);
    stockMovementsPresenter.attachView(view);
    stockMovementsPresenter.stockCard = StockCardBuilder.buildStockCard();
    stockMovementsPresenter.sharedPreferenceMgr = sharedPreferenceMgr;
  }

  @Test
  public void shouldEnableUnpackMenuAndUpdateExpiryDateGroupWhenStockCardSOHIsNotZeroAndKitHasProducts()
      throws Exception {
    //given
    StockCard stockCard = createStockCard(100, true);
    Product kit = ProductBuilder.buildAdultProduct();
    kit.setKit(true);
    stockCard.setProduct(kit);
    when(mockStockRepository.queryStockCardById(200L)).thenReturn(stockCard);
    when(productRepository.queryKitProductByKitCode(kit.getCode()))
        .thenReturn(Arrays.asList(new KitProduct()));

    //when
    stockMovementsPresenter.setStockCard(200L);

    //then
    verify(view).updateUnpackKitMenu(true);
    verify(view).updateExpiryDateViewGroup();
  }

  @Test
  public void shouldDisableUnpackMenuWhenStockCardSOHIsNotZeroAndKitHasNoProduct()
      throws Exception {
    //given
    StockCard stockCard = createStockCard(100, true);
    Product kit = ProductBuilder.buildAdultProduct();
    kit.setKit(true);
    stockCard.setProduct(kit);
    when(mockStockRepository.queryStockCardById(200L)).thenReturn(stockCard);
    when(productRepository.queryKitProductByKitCode(kit.getCode()))
        .thenReturn(new ArrayList<KitProduct>());

    //when
    stockMovementsPresenter.setStockCard(200L);

    //then
    verify(view).updateUnpackKitMenu(false);
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

  @Test
  public void shouldLoadStockMovementViewModelsObserver() throws Exception {
    when(mockStockMovementRepository.listLastFiveStockMovements(anyInt())).thenReturn(new ArrayList<>());

    TestSubscriber<List<StockMovementHistoryViewModel>> subscriber = new TestSubscriber<>();
    stockMovementsPresenter.loadStockMovementViewModelsObserver().subscribe(subscriber);

    subscriber.awaitTerminalEvent();
    subscriber.assertNoErrors();
  }

  @Test
  public void shouldArchiveStockCard() {
    //given
    StockCard stockCard = stockMovementsPresenter.stockCard;
    stockCard.getProduct().setArchived(false);

    //when
    stockMovementsPresenter.archiveStockCard();

    //then
    assertThat(stockCard.getProduct().isArchived()).isTrue();
    verify(mockStockRepository).updateProductOfStockCard(stockCard.getProduct());
  }

  @Test
  public void testIsKitChildrenProduct() {
    // given
    when(productRepository.isKitChildrenProduct(anyByte())).thenReturn(true);

    // then
    assertTrue(stockMovementsPresenter.isKitChildrenProduct(1));
  }

  public class MyTestModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(StockRepository.class).toInstance(mockStockRepository);
      bind(ProductRepository.class).toInstance(productRepository);
      bind(StockService.class).toInstance(stockServiceMock);
    }
  }
}
