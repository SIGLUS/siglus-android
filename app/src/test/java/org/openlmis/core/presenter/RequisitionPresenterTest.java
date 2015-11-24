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
import org.openlmis.core.model.repository.VIARepository;
import org.openlmis.core.view.fragment.RequisitionFragment;
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
    private RequisitionFragment requisitionFragment;
    private VIARepository mockVIARepository;

    @Before
    public void setup() throws ViewNotMatchException {
        mockVIARepository = mock(VIARepository.class);
        requisitionFragment = mock(RequisitionFragment.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        MockitoAnnotations.initMocks(this);

        presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(RequisitionPresenter.class);
        presenter.attachView(requisitionFragment);
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
        verify(requisitionFragment).showListInputError(anyInt());
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
        ((LMISTestApp) RuntimeEnvironment.application).setFeatureToggle(true);

        RnRForm form = getRnRFormWithStatus(RnRForm.STATUS.DRAFT);

        when(mockVIARepository.getDraftVIA()).thenReturn(form);

        mockVIARepository.initVIA();
        presenter.loadRnrForm(0);

        presenter.requisitionFormItemViewModelList = new ArrayList<>();
        presenter.processSign("userSignature");

        waitObservableToExeute();

        if (LMISTestApp.getInstance().getFeatureToggleFor(R.bool.display_via_form_signature_10)) {
            verify(mockVIARepository).setSignature(form, "userSignature", RnRFormSignature.TYPE.SUBMITTER);
            verify(presenter.view).showMessageNotifyDialog();
        }
        verify(mockVIARepository).submit(form);
    }

    @Test
    public void shouldCompleteAfterSignedAndStatusIsSubmit() throws LMISException{
        ((LMISTestApp) RuntimeEnvironment.application).setFeatureToggle(true);
        RnRForm form = getRnRFormWithStatus(RnRForm.STATUS.SUBMITTED);

        when(mockVIARepository.getDraftVIA()).thenReturn(form);

        mockVIARepository.initVIA();
        presenter.loadRnrForm(0);

        presenter.requisitionFormItemViewModelList = new ArrayList<>();
        presenter.processSign("userSignature");

        waitObservableToExeute();

        if (LMISTestApp.getInstance().getFeatureToggleFor(R.bool.display_via_form_signature_10)) {
            verify(mockVIARepository).setSignature(form, "userSignature", RnRFormSignature.TYPE.APPROVER);
        }
        verify(mockVIARepository).authorise(form);
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
        verify(requisitionFragment).highLightRequestAmount();
        verify(requisitionFragment, never()).highLightApprovedAmount();
    }

    @Test
    public void shouldHighLightApproveAmountWhenFormStatusIsSubmitted(){
        highLightForm(RnRForm.STATUS.SUBMITTED);
        verify(requisitionFragment).highLightApprovedAmount();
        verify(requisitionFragment, never()).highLightRequestAmount();
    }

    @Test
    public void shouldNotHighLightAnyColumnWhenFormStatusIsAuthorized() {
        highLightForm(RnRForm.STATUS.AUTHORIZED);
        verify(requisitionFragment, never()).highLightApprovedAmount();
        verify(requisitionFragment, never()).highLightRequestAmount();
    }

    @Test
    public void shouldCallSetProcessButtonNameWithSubmit()  {
        presenter.rnRForm = getRnRFormWithStatus(RnRForm.STATUS.DRAFT);

        presenter.setBtnCompleteText();

        verify(requisitionFragment).setProcessButtonName(LMISTestApp.getContext().getString(R.string.btn_submit));
    }

    @Test
    public void shouldCallSetProcessButtonNameWithComplete()  {
        presenter.rnRForm = getRnRFormWithStatus(RnRForm.STATUS.SUBMITTED);

        presenter.setBtnCompleteText();

        verify(requisitionFragment).setProcessButtonName(LMISTestApp.getContext().getString(R.string.btn_complete));
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
