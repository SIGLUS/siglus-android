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

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.KitProduct;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRFormSignature;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.RnrFormItemBuilder;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.model.repository.VIARepository;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;
import org.openlmis.core.view.viewmodel.ViaKitsViewModel;
import org.roboguice.shaded.goole.common.collect.Lists;
import org.robolectric.RuntimeEnvironment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;
import rx.Observer;
import rx.observers.TestSubscriber;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.assertj.core.util.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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

    @Before
    public void setup() throws ViewNotMatchException {
        mockRnrFormRepository = mock(VIARepository.class);
        mockProductRepository = mock(ProductRepository.class);
        mockStockRepository = mock(StockRepository.class);

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
        when(VIARequisitionFragment.validateConsultationNumber()).thenReturn(false);
        boolean result = presenter.validateForm();
        assertFalse(result);
    }

    @Test
    public void shouldValidateFormReturnFalseWhenKitDataInvalid() throws Exception {
        when(VIARequisitionFragment.validateConsultationNumber()).thenReturn(true);
        when(VIARequisitionFragment.validateKitData()).thenReturn(false);
        boolean result = presenter.validateForm();
        assertFalse(result);
    }

    @Test
    public void shouldReturnTrueWhenValidateFormSuccess() throws Exception {
        when(VIARequisitionFragment.validateConsultationNumber()).thenReturn(true);
        when(VIARequisitionFragment.validateKitData()).thenReturn(true);

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
        verify(mockRnrFormRepository).initRnrForm(null);
    }

    @Test
    public void shouldGetDraftForm() throws LMISException {
        when(mockRnrFormRepository.queryUnAuthorized()).thenReturn(new RnRForm());
        presenter.getRnrForm(0);
        verify(mockRnrFormRepository).queryUnAuthorized();
        verify(mockRnrFormRepository, never()).initRnrForm(null);
    }

    @Test
    public void shouldSubmitAfterSignedAndStatusIsDraft() throws LMISException {
        RnRForm form = testSignatureStatus(RnRForm.STATUS.DRAFT, RnRFormSignature.TYPE.SUBMITTER);
        verify(mockRnrFormRepository).submit(form);
    }

    @Test
    public void shouldCompleteAfterSignedAndStatusIsSubmit() throws LMISException {
        RnRForm form = testSignatureStatus(RnRForm.STATUS.SUBMITTED, RnRFormSignature.TYPE.APPROVER);
        verify(mockRnrFormRepository).authorise(form);
    }

    private RnRForm testSignatureStatus(RnRForm.STATUS formStatus, RnRFormSignature.TYPE signatureType)
            throws LMISException {
        //given
        RnRForm form = getRnRFormWithStatus(formStatus);

        //when
        presenter.processSign("userSignature", form);
        waitObservableToExecute();

        //then
        verify(mockRnrFormRepository).setSignature(form, "userSignature", signatureType);
        return form;
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
        verify(mockRnrFormRepository).save(presenter.getRnRForm());
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
        verify(VIARequisitionFragment).showSaveErrorMessage();
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

        presenter.processRequisition("123");
        verify(VIARequisitionFragment).showErrorMessage(LMISTestApp.getContext().getResources().getString(R.string.msg_requisition_not_unique));
    }

    @Test
    public void shouldNotShowErrorMSGWhenThereWasNoARequisitionInTheSamePeriodAndKitIsOff() throws Exception {
        ((LMISTestApp) LMISTestApp.getInstance()).setFeatureToggle(R.bool.feature_kit, false);

        RnRForm rnRForm = new RnRForm();
        rnRForm.setBaseInfoItemListWrapper(newArrayList(new BaseInfoItem()));
        presenter.rnRForm = rnRForm;
        when(VIARequisitionFragment.validateKitData()).thenReturn(true);

        List<RequisitionFormItemViewModel> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            list.add(new RequisitionFormItemViewModel(createRnrFormItem(i)));
            list.get(i).setRequestAmount(String.valueOf(i));
        }

        presenter.requisitionFormItemViewModels = list;

        when(mockRnrFormRepository.isPeriodUnique(any(RnRForm.class))).thenReturn(true);
        when(VIARequisitionFragment.validateConsultationNumber()).thenReturn(true);

        presenter.processRequisition("123");

        verify(VIARequisitionFragment, never()).showErrorMessage(anyString());
        Assert.assertEquals(3, presenter.rnRForm.getRnrFormItemListWrapper().size());
    }

    @Test
    public void shouldNotShowErrorMSGWhenThereWasNoARequisitionInTheSamePeriod() throws Exception {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_kit, true);

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

        verify(VIARequisitionFragment, never()).showErrorMessage(anyString());
        Assert.assertEquals(5, presenter.rnRForm.getRnrFormItemListWrapper().size());
        assertThat(presenter.getRnRForm().getRnrFormItemListWrapper().get(1).getCalculatedOrderQuantity(), is(1L));
    }

    @Test
    public void shouldInitViaKitsViewModel() throws Exception {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_kit, true);

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
    public void shouldIncludeKitItemsWhenSaving() throws Exception {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_kit, true);

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

        presenter.saveVIAForm("100");
        assertEquals(5, presenter.getRnRForm().getRnrFormItemListWrapper().size());
    }

    @Test
    public void shouldSetAdjustKitProductAmount() throws Exception {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_requisition_theoretical, true);
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

        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_requisition_theoretical, true);
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
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_requisition_theoretical, true);
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
        Program program = new Program();
        program.setProgramCode("1");
        Product product = new Product();
        product.setProgram(program);
        product.setId(1);
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
        }
    }
}
