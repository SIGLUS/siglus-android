package org.openlmis.core.presenter;


import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.constant.IntentConstants;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.presenter.IssueVoucherDraftPresenter.IssueVoucherDraftView;
import org.openlmis.core.view.viewmodel.IssueVoucherProductViewModel;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;
import rx.observers.TestSubscriber;

@RunWith(LMISTestRunner.class)
public class IssueVoucherDraftPresenterTest {

  private IssueVoucherDraftPresenter presenter;

  private StockRepository mockStockRepository;

  private IssueVoucherDraftView mockView;


  private Product product;


  @Before
  public void setUp() throws Exception {
    mockStockRepository = mock(StockRepository.class);
    mockView = mock(IssueVoucherDraftView.class);

    product = Product.builder()
        .primaryName("products")
        .code("22A07")
        .build();

    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(StockRepository.class).toInstance(mockStockRepository);
        bind(IssueVoucherDraftView.class).toInstance(mockView);
      }
    });
    presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(IssueVoucherDraftPresenter.class);
    presenter.attachView(mockView);
  }

  @Test
  public void shouldCorrectInitViewModelsFromGivenProducts() throws LMISException {
    // given
    StockCard stockCard = StockCardBuilder.buildStockCard();
    Intent intent = new Intent();
    intent.putExtra(SELECTED_PRODUCTS, (Serializable) Collections.singletonList(product));
    intent.putExtra(MOVEMENT_REASON_CODE, "movementReasonCode");
    intent.putExtra(IntentConstants.PARAM_ORDER_NUMBER, "orderNumber");
    when(mockStockRepository.listStockCardsByProductIds(any())).thenReturn(Collections.singletonList(stockCard));
    TestSubscriber<List<IssueVoucherProductViewModel>> subscriber = new TestSubscriber<>(presenter.viewModelsSubscribe);
    presenter.viewModelsSubscribe = subscriber;

    // when
    presenter.initialViewModels(intent);
    subscriber.awaitTerminalEvent();

    // then
    assertEquals(1, presenter.getCurrentViewModels().size());
    verify(mockView, times(1)).onRefreshViewModels();
  }

  @Test
  public void shouldCorrectAddProduct() throws LMISException {
    // given
    StockCard stockCard = StockCardBuilder.buildStockCard();
    when(mockStockRepository.listStockCardsByProductIds(any())).thenReturn(Collections.singletonList(stockCard));
    TestSubscriber<List<IssueVoucherProductViewModel>> subscriber = new TestSubscriber<>(presenter.viewModelsSubscribe);
    presenter.viewModelsSubscribe = subscriber;

    // when
    presenter.addProducts(Collections.singletonList(product));
    subscriber.awaitTerminalEvent();
    // then

    assertEquals(1, presenter.getCurrentViewModels().size());
  }

  @Test
  public void shouldCorrectCovertPod() {
    // given
    presenter.setOrderNumber("orderNumber");
    presenter.setMovementReasonCode("movementReasonCode");

    // when
    Pod pod =presenter.coverToPodFromIssueVoucher("VC", true);

    // then
    assertEquals("orderNumber", pod.getOrderCode());
  }

}