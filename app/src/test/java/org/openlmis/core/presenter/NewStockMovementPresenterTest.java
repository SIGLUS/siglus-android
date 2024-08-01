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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.annotation.NonNull;
import com.google.inject.AbstractModule;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.KitProduct;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModelBuilder;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;
import androidx.test.core.app.ApplicationProvider;
import roboguice.RoboGuice;
import rx.observers.TestSubscriber;

@RunWith(LMISTestRunner.class)
public class NewStockMovementPresenterTest {

  private NewStockMovementPresenter newStockMovementPresenter;
  private StockRepository stockRepositoryMock;
  NewStockMovementPresenter.NewStockMovementView view;

  @Before
  public void setup() throws Exception {
    stockRepositoryMock = mock(StockRepository.class);
    view = mock(NewStockMovementPresenter.NewStockMovementView.class);
    RoboGuice.overrideApplicationInjector(ApplicationProvider.getApplicationContext(), new MyTestModule());

    newStockMovementPresenter = RoboGuice.getInjector(ApplicationProvider.getApplicationContext())
        .getInstance(NewStockMovementPresenter.class);
    newStockMovementPresenter.attachView(view);
  }

  @Test
  public void shouldSaveStockItemWhenSaving() throws Exception {
    StockCard stockCard = createStockCard(0, true);
    StockMovementViewModel stockMovementViewModel = newStockMovementPresenter.getViewModel();
    when(stockRepositoryMock.queryStockCardById(anyLong())).thenReturn(stockCard);
    newStockMovementPresenter.loadData(1L, MovementReasonManager.MovementType.RECEIVE, false);
    stockMovementViewModel.getTypeQuantityMap()
        .put(MovementReasonManager.MovementType.RECEIVE, "10");
    stockMovementViewModel.setMovementType(MovementReasonManager.MovementType.RECEIVE);
    stockMovementViewModel.setMovementReason("code");
    stockMovementViewModel.setMovementDate("10/02/2016");
    LotMovementViewModel lotMovementViewModel = new LotMovementViewModelBuilder()
        .setExpiryDate("Jan 2016").setQuantity("50").build();
    stockMovementViewModel.getNewLotMovementViewModelList().add(lotMovementViewModel);

    TestSubscriber<StockMovementViewModel> subscriber = new TestSubscriber<>();
    newStockMovementPresenter.getSaveMovementObservable().subscribe(subscriber);

    subscriber.awaitTerminalEvent();
    subscriber.assertNoErrors();

    ArgumentCaptor<StockMovementItem> captor = ArgumentCaptor.forClass(StockMovementItem.class);
    verify(stockRepositoryMock).addStockMovementAndUpdateStockCard(captor.capture());
    StockMovementItem stockMovementItemSaved = captor.getAllValues().get(0);
    assertThat(stockMovementItemSaved.getStockOnHand(), is(50L));
    assertThat(stockMovementItemSaved.getStockCard(), is(stockCard));
    assertFalse(SharedPreferenceMgr.getInstance().isNeedShowProductsUpdateBanner());
  }

  @Test
  public void shouldUpdateBannerPreferenceWhenSaving() throws Exception {
    StockCard stockCard = createStockCard(0, true);
    stockCard.getProduct().setActive(false);
    when(stockRepositoryMock.queryStockCardById(anyLong())).thenReturn(stockCard);

    StockMovementViewModel stockMovementViewModel = newStockMovementPresenter.getViewModel();
    newStockMovementPresenter.loadData(1L, MovementReasonManager.MovementType.RECEIVE, false);
    stockMovementViewModel.getTypeQuantityMap().put(MovementReasonManager.MovementType.RECEIVE, "0");
    stockMovementViewModel.setMovementType(MovementReasonManager.MovementType.RECEIVE);
    stockMovementViewModel.setMovementReason("code");
    stockMovementViewModel.setMovementDate("10/02/2016");
    TestSubscriber<StockMovementViewModel> subscriber = new TestSubscriber<>();
    newStockMovementPresenter.getSaveMovementObservable().subscribe(subscriber);

    subscriber.awaitTerminalEvent();
    subscriber.assertNoErrors();
    assertTrue(SharedPreferenceMgr.getInstance().isNeedShowProductsUpdateBanner());
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
    product.setCode("productCode");
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