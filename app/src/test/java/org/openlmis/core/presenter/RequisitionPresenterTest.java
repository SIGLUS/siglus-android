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
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRFormSignature;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.repository.VIARepository;
import org.openlmis.core.view.activity.RequisitionActivity;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;
import org.robolectric.RuntimeEnvironment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class RequisitionPresenterTest {

    private RequisitionPresenter presenter;
    private RequisitionActivity mockActivity;
    private VIARepository mockVIARepository;

    @Before
    public void setup() throws ViewNotMatchException {
        mockVIARepository = mock(VIARepository.class);
        mockActivity = mock(RequisitionActivity.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        MockitoAnnotations.initMocks(this);

        presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(RequisitionPresenter.class);
        presenter.attachView(mockActivity);
    }

    @Test
    public void shouldReturnFalseWhenRequestAmountIsNull() throws Exception {

        List<RequisitionFormItemViewModel> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            list.add(new RequisitionFormItemViewModel(createRnrFormItem(i)));
            list.get(i).setRequestAmount("");
        }

        presenter.requisitionFormItemViewModelList = list;
        assertFalse(presenter.validateFormInput());
        verify(mockActivity).showListInputError(anyInt());
    }

    @Test
    public void shouldReturnTrueWhenRequestAmountIsNotNull() throws Exception {

        List<RequisitionFormItemViewModel> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            RequisitionFormItemViewModel requisitionFormItemViewModel = new RequisitionFormItemViewModel(createRnrFormItem(i));
            requisitionFormItemViewModel.setRequestAmount("12");
            list.add(requisitionFormItemViewModel);
        }

        presenter.requisitionFormItemViewModelList = list;
        assertTrue(presenter.validateFormInput());
    }

    @Test
    public void shouldGetRnRFormById() throws Exception {
        presenter.loadRnrForm(1);
        verify(mockVIARepository).queryRnRForm(anyInt());
        verify(mockVIARepository, never()).getDraftVIA();
    }

    @Test
    public void shouldGetInitForm() throws LMISException, SQLException {
        when(mockVIARepository.getDraftVIA()).thenReturn(null);
        presenter.loadRnrForm(0);
        verify(mockVIARepository).getDraftVIA();
        verify(mockVIARepository).initVIA();
    }

    @Test
    public void shouldGetDraftForm() throws LMISException {
        when(mockVIARepository.getDraftVIA()).thenReturn(new RnRForm());
        presenter.loadRnrForm(0);
        verify(mockVIARepository).getDraftVIA();
        verify(mockVIARepository, never()).initVIA();
    }

    @Test
    public void shouldSubmitAfterSignedAndStatusIsDraft() throws LMISException{
        final RnRForm form = new RnRForm();
        form.setStatus(RnRForm.STATUS.DRAFT);
        form.setRnrFormItemListWrapper(new ArrayList<RnrFormItem>());
        form.setBaseInfoItemListWrapper(new ArrayList<BaseInfoItem>() {{
            add(new BaseInfoItem(VIARepository.ATTR_CONSULTATION, BaseInfoItem.TYPE.STRING, form));
        }});

        when(mockVIARepository.getDraftVIA()).thenReturn(form);

        mockVIARepository.initVIA();
        presenter.loadRnrForm(0);

        presenter.requisitionFormItemViewModelList = new ArrayList<>();
        presenter.processSign("username");

        waitObservableToExeute();

        verify(mockVIARepository).setSignature(form, "userSignature", RnRFormSignature.TYPE.SUBMITTER);

        form.setStatus(RnRForm.STATUS.SUBMITTED);
        presenter.processRequisition("123");

        waitObservableToExeute();

        verify(mockVIARepository).authorise(form);
    }

    private void waitObservableToExeute() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void shouldHighLightRequestAmountWhenFormStatusIsDraft(){
        highLightForm(RnRForm.STATUS.DRAFT);
        verify(mockActivity).highLightRequestAmount();
        verify(mockActivity, never()).highLightApprovedAmount();
    }

    @Test
    public void shouldHighLightApproveAmountWhenFormStatusIsSubmitted(){
        highLightForm(RnRForm.STATUS.SUBMITTED);
        verify(mockActivity).highLightApprovedAmount();
        verify(mockActivity, never()).highLightRequestAmount();
    }

    @Test
    public void shouldNotHighLightAnyColumnWhenFormStatusIsAuthorized() {
        highLightForm(RnRForm.STATUS.AUTHORIZED);
        verify(mockActivity, never()).highLightApprovedAmount();
        verify(mockActivity, never()).highLightRequestAmount();
    }

    private void highLightForm(RnRForm.STATUS status) {
        RnRForm form = new RnRForm();
        form.setStatus(status);
        presenter.rnRForm = form;
        presenter.updateRequisitionFormUI();
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
            bind(VIARepository.class).toInstance(mockVIARepository);
        }
    }

}
