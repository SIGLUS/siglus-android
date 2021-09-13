package org.openlmis.core.presenter;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doNothing;
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
import org.assertj.core.util.DateUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.constant.IntentConstants;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.DraftIssueVoucherProductItem;
import org.openlmis.core.model.DraftIssueVoucherProductLotItem;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.Pod;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.repository.IssueVoucherDraftRepository;
import org.openlmis.core.model.repository.PodRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.presenter.IssueVoucherDraftPresenter.IssueVoucherDraftView;
import org.openlmis.core.view.viewmodel.IssueVoucherLotViewModel;
import org.openlmis.core.view.viewmodel.IssueVoucherProductViewModel;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;
import rx.observers.TestSubscriber;

@RunWith(LMISTestRunner.class)
public class IssueVoucherDraftPresenterTest {

  public static final String LOT_NUMBER = "lotNumber";

  private IssueVoucherDraftPresenter presenter;

  private StockRepository mockStockRepository;

  private PodRepository mockPodRepository;

  private IssueVoucherDraftRepository mockIssueVoucherDraftRepository;

  private IssueVoucherDraftView mockView;

  private Product product;

  @Before
  public void setUp() throws Exception {
    mockPodRepository = mock(PodRepository.class);
    mockStockRepository = mock(StockRepository.class);
    mockIssueVoucherDraftRepository = mock(IssueVoucherDraftRepository.class);
    mockView = mock(IssueVoucherDraftView.class);

    product = Product.builder()
        .primaryName("products")
        .code("22A07")
        .build();

    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(StockRepository.class).toInstance(mockStockRepository);
        bind(IssueVoucherDraftRepository.class).toInstance(mockIssueVoucherDraftRepository);
        bind(PodRepository.class).toInstance(mockPodRepository);
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
  public void shouldCorrectInitViewModelsFromDraft() throws LMISException {
    // given
    Pod pod = new Pod();
    pod.setId(1L);
    Intent intent = new Intent();
    intent.putExtra(IntentConstants.PARAM_DRAFT_ISSUE_VOUCHER, pod);
    DraftIssueVoucherProductItem productItem = DraftIssueVoucherProductItem.builder()
        .product(ProductBuilder.buildAdultProduct())
        .pod(pod)
        .done(true)
        .build();

    List<DraftIssueVoucherProductLotItem> lotItems = Collections.singletonList(DraftIssueVoucherProductLotItem.builder()
        .lotNumber(LOT_NUMBER)
        .acceptedQuantity(1L)
        .shippedQuantity(1L)
        .done(true)
        .expirationDate(DateUtil.now())
        .newAdded(true)
        .draftIssueVoucherProductItem(productItem)
        .build());

    productItem.setDraftLotItemListWrapper(lotItems);
    StockCard stockCard = StockCardBuilder.buildStockCard();
    List<DraftIssueVoucherProductItem> mockProductItems = Collections.singletonList(productItem);
    List<StockCard> stockCards = Collections.singletonList(stockCard);
    TestSubscriber<List<IssueVoucherProductViewModel>> subscriber = new TestSubscriber<>(presenter.viewModelsSubscribe);
    when(mockIssueVoucherDraftRepository.queryByPodId(anyLong())).thenReturn(mockProductItems);
    when(mockStockRepository.listStockCardsByProductIds(anyList())).thenReturn(stockCards);
    when(mockPodRepository.queryById(anyLong())).thenReturn(pod);
    presenter.viewModelsSubscribe = subscriber;

    // when
    presenter.initialViewModels(intent);
    subscriber.awaitTerminalEvent();

    // then
    verify(mockView, times(1)).loaded();
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

  @Test
  public void shouldSaveDraftSuccessfully() throws LMISException {
    // given
    StockCard stockCard = StockCardBuilder.buildStockCard();
    TestSubscriber<Object> subscriber = new TestSubscriber<>(presenter.saveDraftSubscribe);
    presenter.saveDraftSubscribe = subscriber;
    doNothing().when(mockIssueVoucherDraftRepository).saveDraft(any(),any());
    presenter.getCurrentViewModels().addAll(Collections.singletonList(new IssueVoucherProductViewModel(stockCard)));
    // when
    presenter.saveIssueVoucherDraft("VC");
    subscriber.awaitTerminalEvent();
    // then
    verify(mockView, times(1)).onSaveDraftFinished(true);
  }

  @Test
  public void shouldDeleteDraftSuccessfully() throws LMISException {
    // given
    TestSubscriber<Object> subscriber = new TestSubscriber<>(presenter.deleteDraftSubscribe);
    presenter.deleteDraftSubscribe = subscriber;
    Pod pod = new Pod();
    pod.setId(1L);
    presenter.setPod(pod);
    // when
    presenter.deleteDraftPod();
    subscriber.awaitTerminalEvent();
    // then
    verify(mockView, times(1)).loaded();
  }

  @Test
  public void shouldCorrectJudgeNeedConfirm() throws LMISException {
    // given
    Pod pod = new Pod();
    pod.setId(1L);

    DraftIssueVoucherProductItem productItem = DraftIssueVoucherProductItem.builder()
        .product(ProductBuilder.buildAdultProduct())
        .pod(pod)
        .done(true)
        .build();

    List<DraftIssueVoucherProductLotItem> lotItems = Collections.singletonList(DraftIssueVoucherProductLotItem.builder()
        .lotNumber(LOT_NUMBER)
        .acceptedQuantity(1L)
        .shippedQuantity(1L)
        .done(true)
        .expirationDate(DateUtil.now())
        .newAdded(true)
        .draftIssueVoucherProductItem(productItem)
        .build());

    List<IssueVoucherLotViewModel> lotViewModels = Collections.singletonList(IssueVoucherLotViewModel.builder()
    .lot(Lot.builder()
        .lotNumber(LOT_NUMBER)
        .expirationDate(DateUtil.now())
        .product(ProductBuilder.buildAdultProduct())
        .build())
    .lotNumber(LOT_NUMBER)
    .acceptedQuantity(1L)
    .shippedQuantity(1L)
    .done(true)
    .expiryDate(DateUtil.now().toString())
    .isNewAdd(true)
    .product(ProductBuilder.buildAdultProduct())
    .productLotItem(lotItems.get(0))
    .build());

    presenter.setPod(pod);
    presenter.getCurrentViewModels().addAll(Collections.singletonList(IssueVoucherProductViewModel.builder()
        .product(ProductBuilder.buildAdultProduct())
        .productItem(productItem)
        .done(true)
        .build()
    ));
    presenter.getCurrentViewModels().get(0).getLotViewModels().addAll(lotViewModels);
    List<DraftIssueVoucherProductItem> productItems = Collections.singletonList(productItem);
    productItem.setDraftLotItemListWrapper(lotItems);
    when(mockIssueVoucherDraftRepository.queryByPodId(anyLong())).thenReturn(productItems);

    // when
    boolean result = presenter.needConfirm();

    // then
    assertFalse(result);
  }

}