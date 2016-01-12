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
import org.mockito.MockitoAnnotations;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRFormSignature;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.RnrKitItem;
import org.openlmis.core.model.repository.VIARepository;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;
import org.robolectric.RuntimeEnvironment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;
import rx.Observer;
import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class VIARequisitionPresenterTest {

    private VIARequisitionPresenter presenter;
    private org.openlmis.core.view.fragment.VIARequisitionFragment VIARequisitionFragment;
    private VIARepository mockRnrFormRepository;

    @Before
    public void setup() throws ViewNotMatchException {
        mockRnrFormRepository = mock(VIARepository.class);

        VIARequisitionFragment = mock(org.openlmis.core.view.fragment.VIARequisitionFragment.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        MockitoAnnotations.initMocks(this);

        presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(VIARequisitionPresenter.class);
        presenter.attachView(VIARequisitionFragment);

        RxAndroidPlugins.getInstance().reset();
        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override
            public Scheduler getMainThreadScheduler() {
                return Schedulers.immediate();
            }
        });
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
        verify(mockRnrFormRepository).initRnrForm();
    }

    @Test
    public void shouldGetDraftForm() throws LMISException {
        when(mockRnrFormRepository.queryUnAuthorized()).thenReturn(new RnRForm());
        presenter.getRnrForm(0);
        verify(mockRnrFormRepository).queryUnAuthorized();
        verify(mockRnrFormRepository, never()).initRnrForm();
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
        assertThat(presenter.getConsultationNumbers()).isNull();

        RnRForm rnRForm = mock(RnRForm.class);
        when(rnRForm.getBaseInfoItemListWrapper()).thenReturn(null);
        presenter.rnRForm = rnRForm;

        assertThat(presenter.getConsultationNumbers()).isNull();
    }

    @Test
    public void shouldGetConsultantNumber() {
        BaseInfoItem baseInfoItem = new BaseInfoItem();
        baseInfoItem.setValue("123");
        ArrayList<BaseInfoItem> items = newArrayList(baseInfoItem);

        RnRForm rnRForm = mock(RnRForm.class);
        presenter.rnRForm = rnRForm;
        when(rnRForm.getBaseInfoItemListWrapper()).thenReturn(items);

        assertThat(presenter.getConsultationNumbers()).isEqualTo("123");
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
    public void shouldNotShowErrorMSGWhenThereWasNoARequisitionInTheSamePeriod() throws Exception {
        BaseInfoItem baseInfoItem = new BaseInfoItem();
        baseInfoItem.setValue("123");
        ArrayList<BaseInfoItem> items = newArrayList(baseInfoItem);

        RnRForm rnRForm = mock(RnRForm.class);
        presenter.rnRForm = rnRForm;
        when(rnRForm.getBaseInfoItemListWrapper()).thenReturn(items);

        presenter.rnRForm = rnRForm;
        when(mockRnrFormRepository.isPeriodUnique(any(RnRForm.class))).thenReturn(true);
        presenter.processRequisition("123");
        verify(VIARequisitionFragment, never()).showErrorMessage(anyString());
    }

    @Test
    public void shouldInitViaKitsViewModel() throws Exception {
        RnRForm rnRForm = mock(RnRForm.class);
        when(rnRForm.getRnrFormItemListWrapper()).thenReturn(new ArrayList<RnrFormItem>());
        List<RnrKitItem> rnrKits = new ArrayList<>();
        RnrKitItem kit1 = new RnrKitItem();
        kit1.setKitCode(RnrKitItem.US_KIT);
        kit1.setKitsOpened(10);
        kit1.setKitsReceived(11);
        kit1.setForm(rnRForm);
        rnrKits.add(kit1);

        RnrKitItem kit2 = new RnrKitItem();
        kit2.setKitCode(RnrKitItem.APE_KIT);
        kit2.setKitsOpened(15);
        kit2.setKitsReceived(18);
        kit2.setForm(rnRForm);
        rnrKits.add(kit2);

        when(rnRForm.getRnrKitItems()).thenReturn(rnrKits);
        when(mockRnrFormRepository.queryRnRForm(1L)).thenReturn(rnRForm);

        presenter.getRnrFormObservable(1L).subscribe();
        Thread.sleep(1000);

        assertEquals("10", presenter.getViaKitsViewModel().getKitsOpenedHF());
        assertEquals("11", presenter.getViaKitsViewModel().getKitsReceivedHF());
        assertEquals("15", presenter.getViaKitsViewModel().getKitsOpenedCHW());
        assertEquals("18", presenter.getViaKitsViewModel().getKitsReceivedCHW());
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
        }
    }
}
