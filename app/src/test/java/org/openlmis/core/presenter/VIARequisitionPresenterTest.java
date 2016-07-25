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

import android.support.annotation.NonNull;

import com.google.inject.AbstractModule;

import junit.framework.Assert;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.KitProduct;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.RnrFormItemBuilder;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.RnrFormItemRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.model.repository.VIARepository;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;
import org.openlmis.core.view.viewmodel.ViaKitsViewModel;
import org.roboguice.shaded.goole.common.collect.Lists;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowToast;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;
import rx.Observer;
import rx.observers.TestSubscriber;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.assertj.core.util.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.core.model.Product.IsKit;

@RunWith(LMISTestRunner.class)
public class VIARequisitionPresenterTest {

    private VIARequisitionPresenter presenter;
    private org.openlmis.core.view.fragment.VIARequisitionFragment VIARequisitionFragment;
    private VIARepository mockRnrFormRepository;
    private ProductRepository mockProductRepository;
    private StockRepository mockStockRepository;
    private RnrFormItemRepository mockRnrFormItemRepository;

    @Before
    public void setup() throws ViewNotMatchException {
        mockRnrFormRepository = mock(VIARepository.class);
        mockProductRepository = mock(ProductRepository.class);
        mockStockRepository = mock(StockRepository.class);
        mockRnrFormItemRepository = mock(RnrFormItemRepository.class);

        VIARequisitionFragment = mock(org.openlmis.core.view.fragment.VIARequisitionFragment.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        MockitoAnnotations.initMocks(this);

        presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(VIARequisitionPresenter.class);
        presenter.attachView(VIARequisitionFragment);
    }

    @Test
    public void shouldReturnFalseWhenRequestAmountIsNull() throws Exception {

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
    public void shouldValidateFormReturnFalseWhenConsultationNumbersInvalid() throws Exception {
        presenter.rnRForm = createRnrForm(RnRForm.Emergency.No);
        when(VIARequisitionFragment.validateConsultationNumber()).thenReturn(false);

        boolean result = presenter.validateForm();

        assertFalse(result);
    }

    @Test
    public void shouldNotValidateKitAndConsultaionNumberWhenFormIsEmergency() throws Exception {
        presenter.rnRForm = createRnrForm(RnRForm.Emergency.Yes);

        verify(VIARequisitionFragment, never()).validateConsultationNumber();
        verify(VIARequisitionFragment, never()).validateKitData();
    }

    @Test
    public void shouldValidateFormReturnFalseWhenKitDataInvalid() throws Exception {
        when(VIARequisitionFragment.validateConsultationNumber()).thenReturn(true);
        when(VIARequisitionFragment.validateKitData()).thenReturn(false);
        presenter.rnRForm = createRnrForm(RnRForm.Emergency.No);

        boolean result = presenter.validateForm();

        assertFalse(result);
    }

    @Test
    public void shouldValidateFormReturnTrueWhenRnrIsEmergency() throws Exception {
        presenter = spy(presenter);
        doReturn(true).when(presenter).validateRnrFormItems();
        RnRForm rnRForm = new RnRForm();
        rnRForm.setEmergency(true);
        presenter.rnRForm = rnRForm;
        when(VIARequisitionFragment.validateConsultationNumber()).thenReturn(false);
        when(VIARequisitionFragment.validateKitData()).thenReturn(false);
        boolean result = presenter.validateForm();
        assertTrue(result);
    }

    @Test
    public void shouldReturnTrueWhenValidateFormSuccess() throws Exception {
        when(VIARequisitionFragment.validateConsultationNumber()).thenReturn(true);
        when(VIARequisitionFragment.validateKitData()).thenReturn(true);
        presenter.rnRForm = createRnrForm(RnRForm.Emergency.No);

        List<RequisitionFormItemViewModel> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            RequisitionFormItemViewModel requisitionFormItemViewModel = new RequisitionFormItemViewModel(createRnrFormItem(i));
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
    public void shouldGetInitForm() throws LMISException, SQLException {
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
        RnRForm form = getRnRFormWithStatus(RnRForm.STATUS.DRAFT);

        //when
        presenter.processSign("userSignature", form);
        waitObservableToExecute();

        //then
        assertThat(RnRForm.STATUS.SUBMITTED, is(form.getStatus()));
        verify(mockRnrFormRepository).createOrUpdateWithItems(form);
    }

    @Test
    public void shouldCompleteAfterSignedAndStatusIsSubmit() throws LMISException {
        //given
        RnRForm form = getRnRFormWithStatus(RnRForm.STATUS.SUBMITTED);

        //when
        presenter.processSign("userSignature", form);
        waitObservableToExecute();

        //then
        verify(mockRnrFormRepository).createOrUpdateWithItems(form);
        assertThat(RnRForm.STATUS.AUTHORIZED, is(form.getStatus()));
    }

    private RnRForm getRnRFormWithStatus(RnRForm.STATUS status) {
        final RnRForm form = new RnRForm();
        form.setStatus(status);
        form.setRnrFormItemListWrapper(new ArrayList<RnrFormItem>());
        form.setBaseInfoItemListWrapper(new ArrayList<BaseInfoItem>() {{
            add(new BaseInfoItem(VIARepository.ATTR_CONSULTATION, BaseInfoItem.TYPE.STRING, form));
        }});
        return form;
    }

    private void waitObservableToExecute() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    private RnRForm createRnrForm(RnRForm.Emergency emergency) {
        RnRForm rnRForm = new RnRForm();
        rnRForm.setEmergency(emergency.Emergency());
        return rnRForm;
    }


    @Test
    public void shouldHighLightRequestAmountWhenFormStatusIsDraft() {
        updateFormUIWithStatus(RnRForm.STATUS.DRAFT);
        verify(VIARequisitionFragment).highLightRequestAmount();
        verify(VIARequisitionFragment, never()).highLightApprovedAmount();
    }

    @Test
    public void shouldHighLightApproveAmountWhenFormStatusIsSubmitted() {
        updateFormUIWithStatus(RnRForm.STATUS.SUBMITTED);
        verify(VIARequisitionFragment).highLightApprovedAmount();
        verify(VIARequisitionFragment, never()).highLightRequestAmount();
    }

    @Test
    public void shouldNotHighLightAnyColumnWhenFormStatusIsAuthorized() {
        updateFormUIWithStatus(RnRForm.STATUS.AUTHORIZED);
        verify(VIARequisitionFragment, never()).highLightApprovedAmount();
        verify(VIARequisitionFragment, never()).highLightRequestAmount();
    }

    @Test
    public void shouldCallSetProcessButtonNameWithSubmitWhenFormStatusIsSubmitted() {
        updateFormUIWithStatus(RnRForm.STATUS.DRAFT);
        verify(VIARequisitionFragment).setProcessButtonName(LMISTestApp.getContext().getString(R.string.btn_submit));
    }

    @Test
    public void shouldCallSetProcessButtonNameWithCompleteWhenFormStatusIsAuthorized() {
        updateFormUIWithStatus(RnRForm.STATUS.SUBMITTED);
        verify(VIARequisitionFragment).setProcessButtonName(LMISTestApp.getContext().getString(R.string.btn_complete));
    }

    @Test
    public void shouldSaveRnRFrom() throws Exception {
        TestSubscriber<Void> subscriber = new TestSubscriber<>();
        presenter.getSaveFormObservable().subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        verify(mockRnrFormRepository).createOrUpdateWithItems(presenter.getRnRForm());
    }

    @Test
    public void shouldGoToHomePageAfterSaveRequisitionComplete() {
        Observer<Void> subscriber = presenter.getSaveFormSubscriber();

        subscriber.onCompleted();

        subscriber.onNext(null);
        verify(VIARequisitionFragment).loaded();
        verify(VIARequisitionFragment).saveSuccess();
    }

    @Test
    public void shouldGoToHomePageAfterSaveRequisitionCompleteOnError() {
        Observer<Void> subscriber = presenter.getSaveFormSubscriber();

        subscriber.onError(new Throwable("msg"));
        verify(VIARequisitionFragment).loaded();

        assertNotNull(ShadowToast.getLatestToast());
    }

    @Test
    public void shouldNotGetConsultantNumberWhenRnRFormIsNullOrInfoItemsIsNull() {
        presenter.rnRForm = null;
        assertNull(presenter.getConsultationNumbers());

        RnRForm rnRForm = mock(RnRForm.class);
        when(rnRForm.getBaseInfoItemListWrapper()).thenReturn(new ArrayList<BaseInfoItem>());
        presenter.rnRForm = rnRForm;

        assertNull(presenter.getConsultationNumbers());
    }

    @Test
    public void shouldGetConsultantNumber() {
        BaseInfoItem baseInfoItem = new BaseInfoItem();
        baseInfoItem.setValue("123");
        ArrayList<BaseInfoItem> items = newArrayList(baseInfoItem);

        RnRForm rnRForm = mock(RnRForm.class);
        presenter.rnRForm = rnRForm;
        when(rnRForm.getBaseInfoItemListWrapper()).thenReturn(items);

        assertThat(presenter.getConsultationNumbers(), is("123"));
    }

    @Test
    public void shouldShowErrorMSGWhenThereWasARequisitionInTheSamePeriod() throws Exception {
        when(mockRnrFormRepository.isPeriodUnique(any(RnRForm.class))).thenReturn(false);
        when(VIARequisitionFragment.validateConsultationNumber()).thenReturn(true);
        when(VIARequisitionFragment.validateKitData()).thenReturn(true);
        presenter.rnRForm = createRnrForm(RnRForm.Emergency.No);

        presenter.processRequisition("123");

        Assert.assertEquals(LMISTestApp.getContext().getResources().getString(R.string.msg_requisition_not_unique),
                ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void shouldNotShowErrorMSGWhenThereWasNoARequisitionInTheSamePeriod() throws Exception {
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
        when(VIARequisitionFragment.validateKitData()).thenReturn(true);

        presenter.processRequisition("123");

        assertNull(ShadowToast.getLatestToast());

        Assert.assertEquals(5, presenter.rnRForm.getRnrFormItemListWrapper().size());
        assertThat(presenter.getRnRForm().getRnrFormItemListWrapper().get(3).getCalculatedOrderQuantity(), is(1L));
    }

    @Test
    public void shouldInitViaKitsViewModel() throws Exception {
        RnRForm rnRForm = mock(RnRForm.class);
        when(mockRnrFormRepository.queryRnRForm(1L)).thenReturn(rnRForm);
        when(rnRForm.getRnrItems(IsKit.No)).thenReturn(new ArrayList<RnrFormItem>());

        RnrFormItem rnrKitItem1 = new RnrFormItemBuilder()
                .setProduct(new ProductBuilder().setCode("SCOD10").build())
                .setReceived(100)
                .setIssued(50)
                .build();
        RnrFormItem rnrKitItem2 = new RnrFormItemBuilder()
                .setProduct(new ProductBuilder().setCode("SCOD12").build())
                .setReceived(300)
                .setIssued(110)
                .build();
        List<RnrFormItem> rnrFormItems = Lists.newArrayList(rnrKitItem1, rnrKitItem2);
        when(rnRForm.getRnrItems(IsKit.Yes)).thenReturn(rnrFormItems);

        TestSubscriber<RnRForm> testSubscriber = new TestSubscriber<>();
        presenter.getRnrFormObservable(1L).subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertNoErrors();

        assertEquals("50", presenter.getViaKitsViewModel().getKitsOpenedHF());
        assertEquals("100", presenter.getViaKitsViewModel().getKitsReceivedHF());
        assertEquals("110", presenter.getViaKitsViewModel().getKitsOpenedCHW());
        assertEquals("300", presenter.getViaKitsViewModel().getKitsReceivedCHW());
    }

    @Test
    public void shouldAddAdditionalProductsToVIA() throws LMISException {
        RnRForm rnRForm = mock(RnRForm.class);
        when(mockRnrFormRepository.queryRnRForm(1L)).thenReturn(rnRForm);

        RnrFormItem rnrFormItem1 = new RnrFormItemBuilder()
                .setProduct(new ProductBuilder().setCode("P1").build()).setRequestAmount(100L)
                .build();
        RnrFormItem rnrFormItem2 = new RnrFormItemBuilder()
                .setProduct(new ProductBuilder().setCode("P2").build()).setRequestAmount(200L)
                .build();
        List<RnrFormItem> rnrFormItems = Lists.newArrayList(rnrFormItem1, rnrFormItem2);
        when(rnRForm.getRnrItems(IsKit.Yes)).thenReturn(new ArrayList<RnrFormItem>());
        when(mockRnrFormItemRepository.listAllNewRnrItems()).thenReturn(rnrFormItems);

        TestSubscriber<RnRForm> testSubscriber = new TestSubscriber<>();
        presenter.getRnrFormObservable(1L).subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertNoErrors();

        assertEquals(2, presenter.getRequisitionFormItemViewModels().size());
        assertEquals("100", presenter.getRequisitionFormItemViewModels().get(0).getRequestAmount());
        assertEquals("200", presenter.getRequisitionFormItemViewModels().get(1).getRequestAmount());
    }

    @Test
    public void shouldIncludeKitItemsWhenSaving() throws Exception {
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

        presenter.saveVIAForm("100", true);
        assertEquals(5, presenter.getRnRForm().getRnrFormItemListWrapper().size());
    }

    @Test
    public void shouldSetAdjustKitProductAmount() throws Exception {
        RnRForm rnRForm = new RnRForm();
        presenter.rnRForm = rnRForm;

        ArrayList<RnrFormItem> rnrFormItemListWrapper = new ArrayList<>();
        RnrFormItem rnrFormItem = createRnrFormItem(1);
        rnrFormItem.setInitialAmount(1000);
        rnrFormItem.setCalculatedOrderQuantity(400L);
        rnrFormItemListWrapper.add(rnrFormItem);

        rnRForm.setRnrFormItemListWrapper(rnrFormItemListWrapper);

        ArrayList<KitProduct> kitProducts = new ArrayList<>();
        KitProduct kitProduct = new KitProduct();
        kitProduct.setQuantity(2);
        kitProduct.setKitCode("kit");
        kitProducts.add(kitProduct);
        when(mockProductRepository.queryKitProductByProductCode("code")).thenReturn(kitProducts);

        Product product = new Product();
        product.setPrimaryName("KitName");
        when(mockProductRepository.getByCode("kit")).thenReturn(product);

        StockCard stockCard = new StockCard();
        stockCard.setStockOnHand(100L);
        when(mockStockRepository.queryStockCardByProductId(product.getId())).thenReturn(stockCard);

        List<RequisitionFormItemViewModel> viewModelsFromRnrForm = presenter.getViewModelsFromRnrForm(rnRForm);

        assertThat(viewModelsFromRnrForm.size(), is(1));
        assertThat(viewModelsFromRnrForm.get(0).getAdjustedTotalRequest(), is("200"));
        assertThat(viewModelsFromRnrForm.get(0).getAdjustmentViewModels().size(), is(1));
        assertThat(viewModelsFromRnrForm.get(0).getAdjustmentViewModels().get(0).getQuantity(), is(2));
        assertThat(viewModelsFromRnrForm.get(0).getAdjustmentViewModels().get(0).getKitStockOnHand(), is(100L));
        assertThat(viewModelsFromRnrForm.get(0).getAdjustmentViewModels().get(0).getKitName(), is("KitName"));
    }

    @Test
    public void shouldNotSetAdjustKitProductAmountInHistoryForm() throws Exception {
        when(VIARequisitionFragment.isHistoryForm()).thenReturn(true);

        RnRForm rnRForm = new RnRForm();
        presenter.rnRForm = rnRForm;

        ArrayList<RnrFormItem> rnrFormItemListWrapper = new ArrayList<>();
        RnrFormItem rnrFormItem = createRnrFormItem(1);
        rnrFormItem.setInitialAmount(1000);
        rnrFormItem.setCalculatedOrderQuantity(400L);
        rnrFormItemListWrapper.add(rnrFormItem);

        rnRForm.setRnrFormItemListWrapper(rnrFormItemListWrapper);

        ArrayList<KitProduct> kitProducts = new ArrayList<>();
        KitProduct kitProduct = new KitProduct();
        kitProduct.setQuantity(2);
        kitProduct.setKitCode("kit");
        kitProducts.add(kitProduct);
        when(mockProductRepository.queryKitProductByProductCode("code")).thenReturn(kitProducts);

        Product product = new Product();
        product.setPrimaryName("KitName");
        when(mockProductRepository.getByCode("kit")).thenReturn(product);

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
        rnrFormItem.setInitialAmount(1000);
        rnrFormItem.setCalculatedOrderQuantity(500L);
        rnrFormItemListWrapper.add(rnrFormItem);

        rnRForm.setRnrFormItemListWrapper(rnrFormItemListWrapper);

        ArrayList<KitProduct> kitProducts = new ArrayList<>();
        KitProduct kitProduct = new KitProduct();
        kitProduct.setQuantity(2);
        kitProduct.setKitCode("kit");
        kitProducts.add(kitProduct);
        when(mockProductRepository.queryKitProductByProductCode("code")).thenReturn(kitProducts);

        Product product = new Product();
        product.setPrimaryName("KitName");
        when(mockProductRepository.getByCode("kit")).thenReturn(product);

        when(mockStockRepository.queryStockCardByProductId(product.getId())).thenReturn(null);

        List<RequisitionFormItemViewModel> viewModelsFromRnrForm = presenter.getViewModelsFromRnrForm(rnRForm);

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

        org.junit.Assert.assertThat(rnRForm1.getRnrFormItemListWrapper(), Is.<List<RnrFormItem>>is(rnrFormItems));
        verify(mockRnrFormRepository, never()).createRnRsWithItems(newArrayList(rnRForm));
    }

    @Test
    public void shouldOnlyUpdateUIWhenProcessEmergencyAndDraftSignature() throws Exception {
        presenter = spy(presenter);
        RnRForm rnRForm = new RnRForm();
        rnRForm.setStatus(RnRForm.STATUS.DRAFT);
        rnRForm.setEmergency(true);

        presenter.processSign("sign", rnRForm);
        verify(presenter).updateUIAfterSubmit();

        reset(presenter);
        presenter.processSign("sign", rnRForm);
        verify(presenter, never()).updateUIAfterSubmit();
    }

    @Test
    public void shouldCreateAndUpdateRnrFormWhenAuthoriseEmergencyViaForm() throws Exception {
        RnRForm rnRForm = new RnRForm();
        rnRForm.setEmergency(true);
        TestSubscriber<Void> testSubscriber = new TestSubscriber<>();
        presenter.createOrUpdateRnrForm(rnRForm).subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();

        testSubscriber.assertNoErrors();
        verify(mockRnrFormRepository).createOrUpdateWithItems(rnRForm);
    }

    @Test
    public void shouldNotCreateAndUpdateRnrFormWhenAuthoriseNormalViaForm() throws Exception {
        RnRForm rnRForm = new RnRForm();
        rnRForm.setEmergency(false);
        TestSubscriber<Void> testSubscriber = new TestSubscriber<>();
        presenter.createOrUpdateRnrForm(rnRForm).subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();

        testSubscriber.assertNoErrors();
        verify(mockRnrFormRepository, never()).createAndRefresh(rnRForm);
        verify(mockRnrFormRepository).createOrUpdateWithItems(rnRForm);
    }

    @Test
    public void shouldAddNewlyAddedProductsOnVIAFormAsStockCards() throws Exception {
        RnRForm rnRForm = new RnRForm();
        rnRForm.setStatus(RnRForm.STATUS.AUTHORIZED);
        rnRForm.setRnrFormItemListWrapper(newArrayList(createRnrFormItem(1)));

        RnrFormItem item1 = createRnrFormItem(2);
        RnrFormItem item2 = createRnrFormItem(3);
        RnrFormItem item3 = createRnrFormItem(4);
        when(mockRnrFormItemRepository.listAllNewRnrItems()).thenReturn(newArrayList(item1, item2, item3));

        presenter.createStockCardsOrUnarchiveAndAddToFormForAdditionalRnrItems(rnRForm);

        ArgumentCaptor<StockCard> captor = ArgumentCaptor.forClass(StockCard.class);
        verify(mockStockRepository, times(3)).createOrUpdateStockCardWithStockMovement(captor.capture());
        List<StockCard> captorAllValues = captor.getAllValues();
        assertThat((captorAllValues.get(0)).getProduct().getId(), is(2L));
        assertThat((captorAllValues.get(1)).getProduct().getId(), is(3L));
        assertThat((captorAllValues.get(2)).getProduct().getId(), is(4L));
        assertThat(rnRForm.getRnrFormItemListWrapper().size(), is(4));
    }

    @Test
    public void shouldDeleteNewRnrFormItem() throws Exception {
        Product product = new Product();
        RnrFormItem rnrFormItem = new RnrFormItemBuilder().setProduct(product).setRequestAmount(100L).build();
        TestSubscriber<Void> testSubscriber = new TestSubscriber<>();
        presenter.removeOneNewRnrItems(rnrFormItem).subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();

        testSubscriber.assertNoErrors();
        verify(mockRnrFormItemRepository).deleteOneNewAdditionalRnrItem(rnrFormItem);
    }

    private ViaKitsViewModel buildDefaultViaKit() {
        ViaKitsViewModel viaKitsViewModel = new ViaKitsViewModel();
        viaKitsViewModel.setKitsOpenedCHW("10");
        viaKitsViewModel.setKitsReceivedCHW("20");
        viaKitsViewModel.setKitsOpenedHF("30");
        viaKitsViewModel.setKitsReceivedHF("40");

        Product usKit = new ProductBuilder().setCode(ViaKitsViewModel.US_KIT).build();
        Product apeKit = new ProductBuilder().setCode(ViaKitsViewModel.APE_KIT).build();
        viaKitsViewModel.setKitItems(Lists.newArrayList(new RnrFormItemBuilder().setProduct(usKit).build(),
                new RnrFormItemBuilder().setProduct(apeKit).build()));
        return viaKitsViewModel;
    }

    private void updateFormUIWithStatus(RnRForm.STATUS status) {
        RnRForm form = new RnRForm();
        form.setStatus(status);
        presenter.rnRForm = form;
        presenter.updateFormUI();
    }


    private RnrFormItem createRnrFormItem(int i) {
        Product product = new Product();
        product.setId(i);
        product.setCode("code");
        RnrFormItem rnrFormItem = new RnrFormItem();
        rnrFormItem.setInventory(1000);
        rnrFormItem.setIssued(i);
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
        }
    }
}
