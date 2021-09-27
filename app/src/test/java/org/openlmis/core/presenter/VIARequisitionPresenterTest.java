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

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.assertj.core.util.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.core.model.Product.IsKit;

import android.app.Activity;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.inject.AbstractModule;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.KitProduct;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRForm.Status;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.RnrFormItemBuilder;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.builder.StockMovementItemBuilder;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.RnrFormItemRepository;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.model.repository.VIARepository;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.activity.DumpFragmentActivity;
import org.openlmis.core.view.fragment.VIARequisitionFragment;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;
import org.openlmis.core.view.viewmodel.ViaKitsViewModel;
import org.roboguice.shaded.goole.common.collect.Lists;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowToast;
import roboguice.RoboGuice;
import rx.observers.TestSubscriber;

@RunWith(LMISTestRunner.class)
public class VIARequisitionPresenterTest {

  private final String kitName = "KitName";
  private final String baseInfoItemValue = "123";
  private final String kitsReceivedHfValue = "100";
  private final String kitCode = "kit";
  private final String productCode = "code";
  private VIARequisitionPresenter presenter;
  private VIARequisitionFragment VIARequisitionFragment;
  private VIARepository mockRnrFormRepository;
  private ProductRepository mockProductRepository;
  private StockRepository mockStockRepository;
  private RnrFormItemRepository mockRnrFormItemRepository;
  private StockMovementRepository mockStockMovementRepository;

  @Before
  public void setup() throws ViewNotMatchException {
    mockRnrFormRepository = mock(VIARepository.class);
    mockProductRepository = mock(ProductRepository.class);
    mockStockRepository = mock(StockRepository.class);
    mockRnrFormItemRepository = mock(RnrFormItemRepository.class);
    mockStockMovementRepository = mock(StockMovementRepository.class);

    VIARequisitionFragment = mock(org.openlmis.core.view.fragment.VIARequisitionFragment.class);

    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
    MockitoAnnotations.initMocks(this);

    presenter = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(VIARequisitionPresenter.class);
    presenter.attachView(VIARequisitionFragment);
    Activity dumpFragmentActivity = Robolectric.buildActivity(DumpFragmentActivity.class).get();
    LMISTestApp.getInstance().SetActiveActivity((Activity) dumpFragmentActivity);
  }

  @Test
  public void shouldReturnFalseWhenRequestAmountIsNull() {

    List<RequisitionFormItemViewModel> list = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      list.add(new RequisitionFormItemViewModel(createRnrFormItem(i)));
      list.get(i).setRequestAmount("");
    }

    presenter.requisitionFormItemViewModels = list;

    assertFalse(presenter.validateRnrFormItems());
    verify(VIARequisitionFragment).showListInputError(anyInt());
  }

  @Test
  public void shouldValidateFormReturnFalseWhenConsultationNumbersInvalid() {
    presenter.rnRForm = createRnrForm(RnRForm.Emergency.NO);
    when(VIARequisitionFragment.validateConsultationNumber()).thenReturn(false);

    boolean result = presenter.validateForm();

    assertFalse(result);
  }

  @Test
  public void shouldNotValidateKitAndConsultaionNumberWhenFormIsEmergency() {
    presenter.rnRForm = createRnrForm(RnRForm.Emergency.YES);

    verify(VIARequisitionFragment, never()).validateConsultationNumber();
  }

  @Test
  public void shouldValidateFormReturnTrueWhenRnrIsEmergency() {
    presenter = spy(presenter);
    doReturn(true).when(presenter).validateRnrFormItems();
    RnRForm rnRForm = new RnRForm();
    rnRForm.setEmergency(true);
    presenter.rnRForm = rnRForm;
    when(VIARequisitionFragment.validateConsultationNumber()).thenReturn(false);
    boolean result = presenter.validateForm();
    assertTrue(result);
  }

  @Test
  public void shouldReturnTrueWhenValidateFormSuccess() {
    when(VIARequisitionFragment.validateConsultationNumber()).thenReturn(true);
    presenter.rnRForm = createRnrForm(RnRForm.Emergency.NO);

    List<RequisitionFormItemViewModel> list = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      RequisitionFormItemViewModel requisitionFormItemViewModel = new RequisitionFormItemViewModel(
          createRnrFormItem(i));
      requisitionFormItemViewModel.setRequestAmount("12");
      list.add(requisitionFormItemViewModel);
    }

    presenter.requisitionFormItemViewModels = list;
    assertTrue(presenter.validateRnrFormItems());
    assertTrue(presenter.validateForm());
  }

  @Test
  public void shouldGetRnRFormById() throws Exception {
    presenter.getRnrForm(1);
    verify(mockRnrFormRepository).queryRnRForm(anyInt());
    verify(mockRnrFormRepository, never()).queryUnAuthorized();
  }

  @Test
  public void shouldGetInitForm() throws LMISException {
    when(mockRnrFormRepository.queryUnAuthorized()).thenReturn(null);
    presenter.getRnrForm(0);
    verify(mockRnrFormRepository).queryUnAuthorized();
    verify(mockRnrFormRepository).initNormalRnrForm(null);
  }

  @Test
  public void shouldGetDraftForm() throws LMISException {
    when(mockRnrFormRepository.queryUnAuthorized()).thenReturn(new RnRForm());
    presenter.getRnrForm(0);
    verify(mockRnrFormRepository).queryUnAuthorized();
    verify(mockRnrFormRepository, never()).initNormalRnrForm(null);
  }

  @Test
  public void shouldSubmitAfterSignedAndStatusIsDraft() throws LMISException {
    //given
    RnRForm form = getRnRFormWithStatus(Status.DRAFT);
    presenter.rnRForm = form;

    //when
    presenter.processSign("userSignature");
    waitObservableToExecute();

    //then
    assertThat(Status.SUBMITTED, is(form.getStatus()));
    verify(mockRnrFormRepository).createOrUpdateWithItems(form);
  }

  @Test
  public void shouldCompleteAfterSignedAndStatusIsSubmit() throws LMISException {
    //given
    RnRForm form = getRnRFormWithStatus(Status.SUBMITTED);
    presenter.rnRForm = form;
    //when

    presenter.processSign("userSignature");
    waitObservableToExecute();

    //then
    verify(mockRnrFormRepository).createOrUpdateWithItems(form);
    assertThat(Status.AUTHORIZED, is(form.getStatus()));
  }

  private RnRForm getRnRFormWithStatus(Status status) {
    final RnRForm form = new RnRForm();
    form.setStatus(status);
    form.setRnrFormItemListWrapper(new ArrayList<>());
    form.setBaseInfoItemListWrapper(new ArrayList<BaseInfoItem>() {{
      add(new BaseInfoItem(VIARepository.ATTR_CONSULTATION, BaseInfoItem.TYPE.STRING, form, "", 0));
    }});
    return form;
  }

  private void waitObservableToExecute() {
    try {
      Thread.sleep(1500);
    } catch (InterruptedException e) {
      Log.w("waitObservableToExecute", e);
    }
  }

  @NonNull
  private RnRForm createRnrForm(RnRForm.Emergency emergency) {
    RnRForm rnRForm = new RnRForm();
    rnRForm.setEmergency(emergency.isEmergency());
    return rnRForm;
  }


  @Test
  public void shouldHighLightRequestAmountWhenFormStatusIsDraft() {
    updateFormUIWithStatus(Status.DRAFT);
    verify(VIARequisitionFragment).highLightRequestAmount();
    verify(VIARequisitionFragment, never()).highLightApprovedAmount();
  }

  @Test
  public void shouldHighLightApproveAmountWhenFormStatusIsSubmitted() {
    updateFormUIWithStatus(Status.SUBMITTED);
    verify(VIARequisitionFragment).highLightApprovedAmount();
    verify(VIARequisitionFragment, never()).highLightRequestAmount();
  }

  @Test
  public void shouldNotHighLightAnyColumnWhenFormStatusIsAuthorized() {
    updateFormUIWithStatus(Status.AUTHORIZED);
    verify(VIARequisitionFragment, never()).highLightApprovedAmount();
    verify(VIARequisitionFragment, never()).highLightRequestAmount();
  }

  @Test
  public void shouldCallSetProcessButtonNameWithSubmitWhenFormStatusIsSubmitted() {
    updateFormUIWithStatus(Status.DRAFT);
    verify(VIARequisitionFragment)
        .setProcessButtonName(LMISTestApp.getContext().getString(R.string.btn_submit));
  }

  @Test
  public void shouldCallSetProcessButtonNameWithCompleteWhenFormStatusIsAuthorized() {
    updateFormUIWithStatus(Status.SUBMITTED);
    verify(VIARequisitionFragment)
        .setProcessButtonName(LMISTestApp.getContext().getString(R.string.btn_complete));
  }

  @Test
  public void shouldNotGetConsultantNumberWhenRnRFormIsNullOrInfoItemsIsNull() {
    presenter.rnRForm = null;
    assertNull(presenter.getConsultationNumbers());

    RnRForm rnRForm = mock(RnRForm.class);
    when(rnRForm.getBaseInfoItemListWrapper()).thenReturn(new ArrayList<>());
    presenter.rnRForm = rnRForm;

    assertNull(presenter.getConsultationNumbers());
  }

  @Test
  public void shouldGetConsultantNumber() {
    BaseInfoItem baseInfoItem = new BaseInfoItem();
    baseInfoItem.setValue(baseInfoItemValue);
    ArrayList<BaseInfoItem> items = newArrayList(baseInfoItem);

    RnRForm rnRForm = mock(RnRForm.class);
    presenter.rnRForm = rnRForm;
    when(rnRForm.getBaseInfoItemListWrapper()).thenReturn(items);

    assertThat(presenter.getConsultationNumbers(), is(baseInfoItemValue));
  }

  @Test
  public void shouldShowErrorMSGWhenThereWasARequisitionInTheSamePeriod() {
    when(mockRnrFormRepository.isPeriodUnique(any(RnRForm.class))).thenReturn(false);
    when(VIARequisitionFragment.validateConsultationNumber()).thenReturn(true);
    presenter.rnRForm = createRnrForm(RnRForm.Emergency.NO);

    presenter.processRequisition(baseInfoItemValue);

    assertEquals(LMISTestApp.getContext().getResources().getString(R.string.msg_requisition_not_unique),
        ToastUtil.activityToast.getText());
  }

  @Test
  public void shouldNotShowErrorMSGWhenThereWasNoARequisitionInTheSamePeriod() {
    RnRForm rnRForm = new RnRForm();
    rnRForm.setBaseInfoItemListWrapper(newArrayList(new BaseInfoItem()));
    presenter.rnRForm = rnRForm;

    List<RequisitionFormItemViewModel> list = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      list.add(new RequisitionFormItemViewModel(createRnrFormItem(i)));
      list.get(i).setRequestAmount(String.valueOf(i));
      list.get(i).setAdjustedTotalRequest(String.valueOf(i));
    }

    ViaKitsViewModel viaKitsViewModel = buildDefaultViaKit();
    presenter.setViaKitsViewModel(viaKitsViewModel);
    presenter.requisitionFormItemViewModels = list;

    when(mockRnrFormRepository.isPeriodUnique(any(RnRForm.class))).thenReturn(true);
    when(VIARequisitionFragment.validateConsultationNumber()).thenReturn(true);

    presenter.processRequisition(baseInfoItemValue);

    assertNull(ShadowToast.getLatestToast());

    assertEquals(5, presenter.rnRForm.getRnrFormItemListWrapper().size());
    assertThat(
        presenter.getRnRForm().getRnrFormItemListWrapper().get(3).getCalculatedOrderQuantity(),
        is(1L));
  }


  @Test(expected = NumberFormatException.class)
  public void testNumberFormatException() {
    RnRForm rnRForm = new RnRForm();
    rnRForm.setBaseInfoItemListWrapper(newArrayList(new BaseInfoItem()));
    presenter.rnRForm = rnRForm;

    List<RequisitionFormItemViewModel> list = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      list.add(new RequisitionFormItemViewModel(createRnrFormItem(i)));
      list.get(i).setRequestAmount(String.valueOf(i));
      list.get(i).setAdjustedTotalRequest(String.valueOf(i));
    }

    ViaKitsViewModel viaKitsViewModel = buildDefaultViaKit();
    presenter.setViaKitsViewModel(viaKitsViewModel);
    presenter.requisitionFormItemViewModels = list;

    when(mockRnrFormRepository.isPeriodUnique(any(RnRForm.class))).thenReturn(true);
    when(VIARequisitionFragment.validateConsultationNumber()).thenReturn(true);

    // Throws NumberFormatException
    presenter.processRequisition("avd");
  }

  @Test
  public void shouldInitViaKitsViewModel() throws Exception {
    RnRForm rnRForm = mock(RnRForm.class);
    when(mockRnrFormRepository.queryRnRForm(1L)).thenReturn(rnRForm);
    when(rnRForm.getRnrItems(IsKit.NO)).thenReturn(new ArrayList<>());

    RnrFormItem rnrKitItem1 = new RnrFormItemBuilder()
        .setProduct(new ProductBuilder().setCode("26A01").build())
        .setReceived(100)
        .setIssued((long) 50)
        .build();
    RnrFormItem rnrKitItem2 = new RnrFormItemBuilder()
        .setProduct(new ProductBuilder().setCode("26A02").build())
        .setReceived(300)
        .setIssued((long) 110)
        .build();
    List<RnrFormItem> rnrFormItems = Lists.newArrayList(rnrKitItem1, rnrKitItem2);
    when(rnRForm.getRnrItems(IsKit.YES)).thenReturn(rnrFormItems);

    TestSubscriber<RnRForm> testSubscriber = new TestSubscriber<>();
    presenter.getRnrFormObservable(1L).subscribe(testSubscriber);
    testSubscriber.awaitTerminalEvent();
    testSubscriber.assertNoErrors();

    assertEquals("50", presenter.getViaKitsViewModel().getKitsOpenedHF());
    assertEquals(kitsReceivedHfValue, presenter.getViaKitsViewModel().getKitsReceivedHF());
    assertEquals("110", presenter.getViaKitsViewModel().getKitsOpenedCHW());
    assertEquals("300", presenter.getViaKitsViewModel().getKitsReceivedCHW());
  }

  @Test
  public void shouldSaveForm() throws Exception {
    RnRForm rnRForm = new RnRForm();
    rnRForm.setBaseInfoItemListWrapper(newArrayList(new BaseInfoItem()));
    presenter.rnRForm = rnRForm;

    List<RequisitionFormItemViewModel> list = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      list.add(new RequisitionFormItemViewModel(createRnrFormItem(i)));
      list.get(i).setRequestAmount("");
    }

    ViaKitsViewModel viaKitsViewModel = buildDefaultViaKit();
    presenter.setViaKitsViewModel(viaKitsViewModel);
    presenter.requisitionFormItemViewModels = list;

    TestSubscriber<RnRForm> subscriber = new TestSubscriber<>();
    presenter.getSaveFormObservable(kitsReceivedHfValue).subscribe(subscriber);
    subscriber.awaitTerminalEvent();

    verify(mockRnrFormRepository).createOrUpdateWithItems(rnRForm);
    subscriber.assertNoErrors();

    assertEquals(5, presenter.getRnRForm().getRnrFormItemListWrapper().size());
  }

  @Test
  public void shouldSetAdjustKitProductAmount() throws Exception {
    RnRForm rnRForm = new RnRForm();
    presenter.rnRForm = rnRForm;

    ArrayList<RnrFormItem> rnrFormItemListWrapper = new ArrayList<>();
    RnrFormItem rnrFormItem = createRnrFormItem(1);
    rnrFormItem.setInitialAmount((long) 1000);
    rnrFormItem.setCalculatedOrderQuantity(400L);
    rnrFormItemListWrapper.add(rnrFormItem);

    rnRForm.setRnrFormItemListWrapper(rnrFormItemListWrapper);

    ArrayList<KitProduct> kitProducts = new ArrayList<>();
    KitProduct kitProduct = new KitProduct();
    kitProduct.setQuantity(2);
    kitProduct.setKitCode(kitCode);
    kitProducts.add(kitProduct);
    when(mockProductRepository.queryKitProductByProductCode(productCode)).thenReturn(kitProducts);

    Product product = new Product();
    product.setPrimaryName(kitName);
    when(mockProductRepository.getByCode(kitCode)).thenReturn(product);

    StockCard stockCard = new StockCard();
    stockCard.setStockOnHand(100L);
    when(mockStockRepository.queryStockCardByProductId(product.getId())).thenReturn(stockCard);

    List<RequisitionFormItemViewModel> viewModelsFromRnrForm = presenter
        .getViewModelsFromRnrForm(rnRForm);

    assertThat(viewModelsFromRnrForm.size(), is(1));
    assertThat(viewModelsFromRnrForm.get(0).getAdjustedTotalRequest(), is("200"));
    assertThat(viewModelsFromRnrForm.get(0).getAdjustmentViewModels().size(), is(1));
    assertThat(viewModelsFromRnrForm.get(0).getAdjustmentViewModels().get(0).getQuantity(), is(2));
    assertThat(viewModelsFromRnrForm.get(0).getAdjustmentViewModels().get(0).getKitStockOnHand(),
        is(100L));
    assertThat(viewModelsFromRnrForm.get(0).getAdjustmentViewModels().get(0).getKitName(),
        is(kitName));
  }

  @Test
  public void shouldNotSetAdjustKitProductAmountInHistoryForm() throws Exception {
    RnRForm rnRForm = new RnRForm();
    presenter.rnRForm = rnRForm;
    presenter.isHistoryForm = true;

    ArrayList<RnrFormItem> rnrFormItemListWrapper = new ArrayList<>();
    RnrFormItem rnrFormItem = createRnrFormItem(1);
    rnrFormItem.setInitialAmount((long) 1000);
    rnrFormItem.setCalculatedOrderQuantity(400L);
    rnrFormItemListWrapper.add(rnrFormItem);

    rnRForm.setRnrFormItemListWrapper(rnrFormItemListWrapper);

    ArrayList<KitProduct> kitProducts = new ArrayList<>();
    KitProduct kitProduct = new KitProduct();
    kitProduct.setQuantity(2);
    kitProduct.setKitCode(kitCode);
    kitProducts.add(kitProduct);
    when(mockProductRepository.queryKitProductByProductCode(productCode)).thenReturn(kitProducts);

    Product product = new Product();
    product.setPrimaryName(kitName);
    when(mockProductRepository.getByCode(kitCode)).thenReturn(product);

    StockCard stockCard = new StockCard();
    stockCard.setStockOnHand(100L);
    when(mockStockRepository.queryStockCardByProductId(product.getId())).thenReturn(stockCard);

    List<RequisitionFormItemViewModel> viewModelsFromRnrForm = presenter.getViewModelsFromRnrForm(rnRForm);

    assertThat(viewModelsFromRnrForm.size(), is(1));
    assertThat(viewModelsFromRnrForm.get(0).getAdjustedTotalRequest(), is("400"));
    assertNull(viewModelsFromRnrForm.get(0).getAdjustmentViewModels());
  }

  @Test
  public void shouldNotAddAdjustItemsWhenKitIsNotFound() throws LMISException {
    RnRForm rnRForm = new RnRForm();
    presenter.rnRForm = rnRForm;

    List<RnrFormItem> rnrFormItemListWrapper = new ArrayList<>();
    RnrFormItem rnrFormItem = createRnrFormItem(1);
    rnrFormItem.setInitialAmount((long) 1000);
    rnrFormItem.setCalculatedOrderQuantity(500L);
    rnrFormItemListWrapper.add(rnrFormItem);

    rnRForm.setRnrFormItemListWrapper(rnrFormItemListWrapper);

    ArrayList<KitProduct> kitProducts = new ArrayList<>();
    KitProduct kitProduct = new KitProduct();
    kitProduct.setQuantity(2);
    kitProduct.setKitCode(kitCode);
    kitProducts.add(kitProduct);
    when(mockProductRepository.queryKitProductByProductCode(productCode)).thenReturn(kitProducts);

    Product product = new Product();
    product.setPrimaryName(kitName);
    when(mockProductRepository.getByCode(kitCode)).thenReturn(product);

    when(mockStockRepository.queryStockCardByProductId(product.getId())).thenReturn(null);

    List<RequisitionFormItemViewModel> viewModelsFromRnrForm = presenter
        .getViewModelsFromRnrForm(rnRForm);

    assertThat(viewModelsFromRnrForm.size(), is(1));
    assertThat(viewModelsFromRnrForm.get(0).getAdjustmentViewModels().size(), is(0));
  }

  @Test
  public void shouldInitEmergencyRnr() throws Exception {
    ArrayList<StockCard> stockCards = newArrayList();
    Date periodEndDate = new Date();
    RnRForm rnRForm = new RnRForm();
    when(mockRnrFormRepository.initEmergencyRnrForm(periodEndDate, stockCards)).thenReturn(rnRForm);
    ArrayList<RnrFormItem> rnrFormItems = new ArrayList<>();
    when(mockRnrFormRepository.generateRnrFormItems(rnRForm, stockCards)).thenReturn(rnrFormItems);

    RnRForm rnRForm1 = presenter.initEmergencyRnr(stockCards, periodEndDate);

    assertEquals(rnrFormItems, rnRForm1.getRnrFormItemListWrapper());
    verify(mockRnrFormRepository, never()).createRnRsWithItems(newArrayList(rnRForm));
  }

  @Test
  public void shouldOnlyUpdateUIWhenProcessEmergencyAndDraftSignature() {
    presenter = spy(presenter);
    RnRForm rnRForm = new RnRForm();
    rnRForm.setStatus(Status.DRAFT);
    rnRForm.setEmergency(true);
    presenter.rnRForm = rnRForm;

    presenter.processSign("sign");
    verify(presenter).updateUIAfterSubmit();

    reset(presenter);
    presenter.processSign("sign");
    verify(presenter, never()).updateUIAfterSubmit();
  }

  @Test
  public void shouldCreateAndUpdateRnrFormWhenAuthoriseEmergencyViaForm() throws Exception {
    RnRForm rnRForm = new RnRForm();
    rnRForm.setEmergency(true);
    presenter.rnRForm = rnRForm;
    TestSubscriber<Void> testSubscriber = new TestSubscriber<>();
    presenter.createOrUpdateRnrForm().subscribe(testSubscriber);
    testSubscriber.awaitTerminalEvent();

    testSubscriber.assertNoErrors();
    verify(mockRnrFormRepository).createOrUpdateWithItems(rnRForm);
  }

  @Test
  public void shouldNotCreateAndUpdateRnrFormWhenAuthoriseNormalViaForm() throws Exception {
    RnRForm rnRForm = new RnRForm();
    presenter.rnRForm = rnRForm;
    rnRForm.setEmergency(false);
    TestSubscriber<Void> testSubscriber = new TestSubscriber<>();
    presenter.createOrUpdateRnrForm().subscribe(testSubscriber);
    testSubscriber.awaitTerminalEvent();

    testSubscriber.assertNoErrors();
    verify(mockRnrFormRepository, never()).createAndRefresh(rnRForm);
    verify(mockRnrFormRepository).createOrUpdateWithItems(rnRForm);
  }

  @Test
  public void shouldDeleteNewRnrFormItem() throws Exception {
    Product product = new Product();
    RnrFormItem rnrFormItem = new RnrFormItemBuilder().setProduct(product).setRequestAmount(100L)
        .build();
    TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
    presenter.removeRnrItem(rnrFormItem).subscribe(testSubscriber);
    testSubscriber.awaitTerminalEvent();

    testSubscriber.assertNoErrors();
    verify(mockRnrFormItemRepository).deleteRnrItem(rnrFormItem);
  }

  @Test
  public void shouldPopulateAdditionalRnrFormItemsViewModels() throws Exception {
    presenter.requisitionFormItemViewModels = new ArrayList<>();
    Product product1 = new ProductBuilder().setCode("P1").setIsActive(true).setIsArchived(true)
        .build();
    Product product2 = new ProductBuilder().setCode("P2").setIsActive(true).setIsArchived(false)
        .build();
    RnrFormItem rnrFormItem1 = new RnrFormItemBuilder().
        setProduct(product1).
        setRequestAmount(100L).
        setCalculatedOrderQuantity(10L).
        build();
    RnrFormItem rnrFormItem2 = new RnrFormItemBuilder().
        setProduct(product2).
        setRequestAmount(200L).
        build();

    when(mockProductRepository.getByCode("P1")).thenReturn(
        new ProductBuilder().setCode("P1").setIsActive(true).setIsArchived(false).build());
    when(mockProductRepository.getByCode("P2")).thenReturn(
        new ProductBuilder().setCode("P2").setIsActive(true).setIsArchived(true).build());
    StockCard stockCard = new StockCard();
    stockCard.setId(1L);
    when(mockStockRepository.queryStockCardByProductId(anyLong())).thenReturn(stockCard);
    presenter
        .populateAdditionalDrugsViewModels(newArrayList(rnrFormItem1, rnrFormItem2), new Date());

    assertThat(presenter.requisitionFormItemViewModels.size(), is(2));
    assertThat(presenter.requisitionFormItemViewModels.get(0).getFmn(), is("P1"));
    assertThat(presenter.requisitionFormItemViewModels.get(1).getFmn(), is("P2"));
    assertThat(presenter.requisitionFormItemViewModels.get(0).getRequestAmount(), is(kitsReceivedHfValue));
    assertThat(presenter.requisitionFormItemViewModels.get(1).getRequestAmount(), is("200"));
    assertThat(presenter.requisitionFormItemViewModels.get(0).getApprovedAmount(), is(kitsReceivedHfValue));
    assertThat(presenter.requisitionFormItemViewModels.get(1).getApprovedAmount(), is("200"));
  }

  @Test
  public void shouldAssignValuesToSelectedArchivedProducts() throws Exception {
    Date periodBegin = DateUtil.parseString("2016-01-21", DateUtil.DB_DATE_FORMAT);
    Date periodEnd = DateUtil.parseString("2016-02-20", DateUtil.DB_DATE_FORMAT);

    Product product1 = new ProductBuilder().setCode("P1").setIsActive(true).setIsArchived(true)
        .build();
    Product product2 = new ProductBuilder().setCode("P2").setIsActive(true).setIsArchived(false)
        .build();
    RnrFormItem rnrFormItem1 = new RnrFormItemBuilder().setProduct(product1).setRequestAmount(100L)
        .build();
    RnrFormItem rnrFormItem2 = new RnrFormItemBuilder().setProduct(product2).setRequestAmount(200L)
        .build();

    when(mockProductRepository.getByCode("P1")).thenReturn(product1);

    when(mockProductRepository.getByCode("P2")).thenReturn(product2);

    StockCard stockCard = new StockCardBuilder().setStockOnHand(0L).setProduct(product1).build();
    StockMovementItem stockMovementItem1 = new StockMovementItemBuilder().withStockOnHand(50)
        .withQuantity(10).withMovementType(MovementReasonManager.MovementType.ISSUE)
        .withDocumentNo(baseInfoItemValue).build();
    StockMovementItem stockMovementItem2 = new StockMovementItemBuilder().build();
    StockMovementItem stockMovementItem3 = new StockMovementItemBuilder().build();

    when(mockStockRepository.queryStockCardByProductId(product1.getId())).thenReturn(stockCard);
    when(mockStockMovementRepository
        .queryStockItemsByCreatedDate(stockCard.getId(), periodBegin, periodEnd))
        .thenReturn(newArrayList(stockMovementItem1, stockMovementItem2, stockMovementItem3));

    presenter.requisitionFormItemViewModels = new ArrayList<>();
    presenter.periodEndDate = periodEnd;

    presenter
        .populateAdditionalDrugsViewModels(newArrayList(rnrFormItem1, rnrFormItem2), periodBegin);

    assertThat(presenter.requisitionFormItemViewModels.get(0).getInitAmount(), is("60"));
  }

  private ViaKitsViewModel buildDefaultViaKit() {
    ViaKitsViewModel viaKitsViewModel = new ViaKitsViewModel();
    viaKitsViewModel.setKitsOpenedCHW("10");
    viaKitsViewModel.setKitsReceivedCHW("20");
    viaKitsViewModel.setKitsOpenedHF("30");
    viaKitsViewModel.setKitsReceivedHF("40");

    Product usKit = new ProductBuilder().setCode(ViaKitsViewModel.US_KIT).build();
    Product apeKit = new ProductBuilder().setCode(ViaKitsViewModel.APE_KIT).build();
    viaKitsViewModel
        .setKitItems(Lists.newArrayList(new RnrFormItemBuilder().setProduct(usKit).build(),
            new RnrFormItemBuilder().setProduct(apeKit).build()));
    return viaKitsViewModel;
  }

  private void updateFormUIWithStatus(Status status) {
    RnRForm form = new RnRForm();
    form.setStatus(status);
    presenter.rnRForm = form;
    presenter.updateFormUI();
  }


  private RnrFormItem createRnrFormItem(int i) {
    Product product = new Product();
    product.setId(i);
    product.setCode(productCode);
    RnrFormItem rnrFormItem = new RnrFormItem();
    rnrFormItem.setInventory((long) 1000);
    rnrFormItem.setInitialAmount((long) 1000);
    rnrFormItem.setCalculatedOrderQuantity((long) 1000);
    rnrFormItem.setIssued((long) i);
    rnrFormItem.setProduct(product);
    return rnrFormItem;
  }

  public class MyTestModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(VIARepository.class).toInstance(mockRnrFormRepository);
      bind(ProductRepository.class).toInstance(mockProductRepository);
      bind(StockRepository.class).toInstance(mockStockRepository);
      bind(RnrFormItemRepository.class).toInstance(mockRnrFormItemRepository);
      bind(StockMovementRepository.class).toInstance(mockStockMovementRepository);
    }
  }
}
