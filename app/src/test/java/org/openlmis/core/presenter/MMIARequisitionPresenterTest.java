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

import org.junit.After;
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
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRFormSignature;
import org.openlmis.core.model.repository.MMIARepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.service.SyncManager;
import org.robolectric.RuntimeEnvironment;

import java.sql.SQLException;
import java.util.ArrayList;

import roboguice.RoboGuice;
import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class MMIARequisitionPresenterTest {

    private SyncManager syncManager;
    private MMIARequisitionPresenter presenter;
    private MMIARepository mmiaRepository;
    private RnrFormRepository rnrFormRepository;
    private ProgramRepository programRepository;
    private MMIARequisitionPresenter.MMIARequisitionView mockMMIAformView;

    @Before
    public void setup() throws ViewNotMatchException {
        mmiaRepository = mock(MMIARepository.class);
        rnrFormRepository = mock(RnrFormRepository.class);
        programRepository = mock(ProgramRepository.class);
        mockMMIAformView = mock(MMIARequisitionPresenter.MMIARequisitionView.class);
        syncManager = mock(SyncManager.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        MockitoAnnotations.initMocks(this);

        presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(MMIARequisitionPresenter.class);
        presenter.attachView(mockMMIAformView);

        RxAndroidPlugins.getInstance().reset();
        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override
            public Scheduler getMainThreadScheduler() {
                return Schedulers.immediate();
            }
        });
    }

    @After
    public void tearDown() {
        RoboGuice.Util.reset();
    }

    @Test
    public void shouldGetInitMMIAForm() throws LMISException, SQLException {
        when(mmiaRepository.queryUnAuthorized()).thenReturn(null);
        presenter.getRnrForm(0);
        verify(mmiaRepository).queryUnAuthorized();
        verify(mmiaRepository).initRnrForm();
    }

    @Test
    public void shouldGetDraftMMIAForm() throws LMISException {
        when(mmiaRepository.queryUnAuthorized()).thenReturn(new RnRForm());
        presenter.getRnrForm(0);
        verify(mmiaRepository).queryUnAuthorized();
        verify(mmiaRepository, never()).initRnrForm();
    }

    @Test
    public void shouldCompleteMMIAIfTotalsMatch() throws Exception {
        ArrayList<RegimenItem> regimenItems = generateRegimenItems();
        ArrayList<BaseInfoItem> baseInfoItems = new ArrayList<>();

        RnRForm rnRForm = new RnRForm();

        when(mmiaRepository.initRnrForm()).thenReturn(rnRForm);
        when(mmiaRepository.getTotalPatients(rnRForm)).thenReturn(100L);
        presenter.getRnrForm(0);

        presenter.processRequisition(regimenItems, baseInfoItems, "");
        verify(mockMMIAformView, never()).showValidationAlert();
    }

    @Test
    public void shouldShowSignDialogIfTotalsMatchIfFeatureToggleIsOn() throws Exception {
        ((LMISTestApp) RuntimeEnvironment.application).setFeatureToggle(true);
        ArrayList<RegimenItem> regimenItems = generateRegimenItems();
        ArrayList<BaseInfoItem> baseInfoItems = new ArrayList<>();

        RnRForm rnRForm = new RnRForm();
        rnRForm.setStatus(RnRForm.STATUS.DRAFT);

        when(mmiaRepository.initRnrForm()).thenReturn(rnRForm);
        when(mmiaRepository.getTotalPatients(rnRForm)).thenReturn(100L);

        presenter.getRnrForm(0);

        presenter.processRequisition(regimenItems, baseInfoItems, "");

        if(LMISTestApp.getInstance().getFeatureToggleFor(R.bool.display_mmia_form_signature)) {
            verify(mockMMIAformView).showSignDialog(true);
        }
    }

    @Test
    public void shouldNotShowSignDialogIfTotalsMatchIfFeatureToggleIsOff() throws Exception {
        ((LMISTestApp) RuntimeEnvironment.application).setFeatureToggle(false);
        ArrayList<RegimenItem> regimenItems = generateRegimenItems();
        ArrayList<BaseInfoItem> baseInfoItems = new ArrayList<>();

        RnRForm rnRForm = new RnRForm();

        when(mmiaRepository.initRnrForm()).thenReturn(rnRForm);
        when(mmiaRepository.getTotalPatients(rnRForm)).thenReturn(100L);
        presenter.getRnrForm(0);

        presenter.processRequisition(regimenItems, baseInfoItems, "");

        verify(mockMMIAformView, never()).showSignDialog(true);
        verify(mockMMIAformView).loading();
    }

    @Test
    public void shouldCompleteFormAfterSignSuccess() throws Exception {
        ArrayList<RegimenItem> regimenItems = generateRegimenItems();
        ArrayList<BaseInfoItem> baseInfoItems = new ArrayList<>();

        RnRForm rnRForm = new RnRForm();

        when(mmiaRepository.initRnrForm()).thenReturn(rnRForm);
        when(mmiaRepository.getTotalPatients(rnRForm)).thenReturn(100L);
        presenter.getRnrForm(0);

        presenter.processRequisition(regimenItems, baseInfoItems, "");


    }

    @Test
    public void shouldNotCompleteMMIAIfTotalsMismatchAndCommentInvalid() throws Exception {
        ArrayList<RegimenItem> regimenItems = generateRegimenItems();
        ArrayList<BaseInfoItem> baseInfoItems = new ArrayList<>();

        RnRForm rnRForm = new RnRForm();

        when(mmiaRepository.initRnrForm()).thenReturn(rnRForm);
        when(mmiaRepository.getTotalPatients(rnRForm)).thenReturn(99L);
        presenter.getRnrForm(0);

        presenter.processRequisition(regimenItems, baseInfoItems, "1234");
        verify(mockMMIAformView).showValidationAlert();
    }

    @Test
    public void shouldShowErrorWhenLoadRnRFormOnError() {
        presenter.loadDataOnErrorAction.call(new Exception("I am testing the onError action"));

        verify(mockMMIAformView).loaded();
        verify(mockMMIAformView).showErrorMessage("I am testing the onError action");
    }

    @Test
    public void shouldInitViewWhenLoadRnRFormOnNext() {
        RnRForm rnRForm = new RnRForm();
        presenter.loadDataOnNextAction.call(rnRForm);

        verify(mockMMIAformView).initView(rnRForm);
        verify(mockMMIAformView).loaded();
    }

    @Test
    public void shouldRnRFormObservableQueryRnRFormWhenFormIdIsValid() throws LMISException {
        TestSubscriber<RnRForm> subscriber = new TestSubscriber<>();
        presenter.getRnrFormObservable(100L).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        verify(mmiaRepository).queryRnRForm(100L);
    }

    @Test
    public void shouldRnRFormObservableQueryProgramWhenFormIdIsInvalid() throws LMISException {
        TestSubscriber<RnRForm> subscriber = new TestSubscriber<>();
        presenter.getRnrFormObservable(-100L).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        verify(mmiaRepository).queryUnAuthorized();
    }

    @Test
    public void shouldAuthoriseFormObservableAuthoriseForm() throws LMISException {
        RnRForm form = new RnRForm();

        presenter.rnRForm = form;

        TestSubscriber<Void> subscriber = new TestSubscriber<>();
        presenter.getAuthoriseFormObservable().subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        verify(mmiaRepository).authorise(form);
    }

    @Test
    public void shouldSubmitFormWhenTheStatusIsDraft() throws LMISException {
        ((LMISTestApp) RuntimeEnvironment.application).setFeatureToggle(true);
        RnRForm form = new RnRForm();
        form.setStatus(RnRForm.STATUS.DRAFT);
        presenter.rnRForm = form;

        String signature = "signature";
        presenter.processSign(signature);
        waitObservableToExeute();

        if(LMISTestApp.getInstance().getFeatureToggleFor(R.bool.display_mmia_form_signature)) {
            verify(rnrFormRepository).setSignature(form, signature, RnRFormSignature.TYPE.SUBMITTER);
            verify(rnrFormRepository).submit(form);
            verify(mockMMIAformView).setProcessButtonName(LMISTestApp.getContext().getString(R.string.btn_complete));
            verify(mockMMIAformView).showMessageNotifyDialog();
        }
    }

    @Test
    public void shouldAuthorizeFormWhenStatusIsSubmited() throws LMISException {
        ((LMISTestApp) RuntimeEnvironment.application).setFeatureToggle(true);
        RnRForm form = new RnRForm();
        form.setStatus(RnRForm.STATUS.SUBMITTED);
        presenter.rnRForm = form;

        String signature = "signature";
        presenter.processSign(signature);

        waitObservableToExeute();

        if(LMISTestApp.getInstance().getFeatureToggleFor(R.bool.display_mmia_form_signature)) {
            verify(rnrFormRepository).setSignature(form, signature, RnRFormSignature.TYPE.APPROVER);
            verify(rnrFormRepository).authorise(form);
        }
    }

    private void waitObservableToExeute() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<RegimenItem> generateRegimenItems() {
        ArrayList<RegimenItem> regimenItems = new ArrayList<>();
        RegimenItem regimenItem = new RegimenItem();
        regimenItem.setAmount(100L);
        regimenItems.add(regimenItem);

        return regimenItems;
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(MMIARepository.class).toInstance(mmiaRepository);
            bind(RnrFormRepository.class).toInstance(rnrFormRepository);
            bind(ProgramRepository.class).toInstance(programRepository);
            bind(MMIARequisitionPresenter.MMIARequisitionView.class).toInstance(mockMMIAformView);
            bind(SyncManager.class).toInstance(syncManager);
        }
    }
}
