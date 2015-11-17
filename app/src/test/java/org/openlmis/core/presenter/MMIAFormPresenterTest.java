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
import org.mockito.Matchers;
import org.mockito.MockitoAnnotations;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.exceptions.PeriodNotUniqueException;
import org.openlmis.core.exceptions.ViewNotMatchException;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.repository.MMIARepository;
import org.openlmis.core.model.repository.ProgramRepository;
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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class MMIAFormPresenterTest {

    private SyncManager syncManager;
    private MMIAFormPresenter presenter;
    private MMIARepository mmiaRepository;
    private ProgramRepository programRepository;
    private MMIAFormPresenter.MMIAFormView mockMMIAformView;

    @Before
    public void setup() throws ViewNotMatchException {
        mmiaRepository = mock(MMIARepository.class);
        programRepository = mock(ProgramRepository.class);
        mockMMIAformView = mock(MMIAFormPresenter.MMIAFormView.class);
        syncManager = mock(SyncManager.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        MockitoAnnotations.initMocks(this);

        presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(MMIAFormPresenter.class);
        presenter.attachView(mockMMIAformView);

        RxAndroidPlugins.getInstance().reset();
        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override
            public Scheduler getMainThreadScheduler() {
                return Schedulers.immediate();
            }
        });
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(MMIARepository.class).toInstance(mmiaRepository);
            bind(ProgramRepository.class).toInstance(programRepository);
            bind(MMIAFormPresenter.MMIAFormView.class).toInstance(mockMMIAformView);
            bind(SyncManager.class).toInstance(syncManager);
        }
    }

    @After
    public void tearDown() {
        RoboGuice.Util.reset();
    }

    @Test
    public void shouldGetInitMMIAForm() throws LMISException, SQLException {
        when(mmiaRepository.getUnCompletedMMIA(Matchers.<Program>anyObject())).thenReturn(null);
        presenter.getRnrForm(0);
        verify(mmiaRepository).getUnCompletedMMIA(Matchers.<Program>anyObject());
        verify(mmiaRepository).initMMIA(Matchers.<Program>anyObject());
    }

    @Test
    public void shouldGetDraftMMIAForm() throws LMISException {
        when(mmiaRepository.getUnCompletedMMIA(Matchers.<Program>anyObject())).thenReturn(new RnRForm());
        presenter.getRnrForm(0);
        verify(mmiaRepository).getUnCompletedMMIA(Matchers.<Program>anyObject());
        verify(mmiaRepository, never()).initMMIA(Matchers.<Program>anyObject());
    }

    @Test
    public void shouldCompleteMMIAIfTotalsMatch() throws Exception {
        ArrayList<RegimenItem> regimenItems = generateRegimenItems();
        ArrayList<BaseInfoItem> baseInfoItems = new ArrayList<>();

        RnRForm rnRForm = new RnRForm();

        when(mmiaRepository.initMMIA(Matchers.<Program>anyObject())).thenReturn(rnRForm);
        when(mmiaRepository.getTotalPatients(rnRForm)).thenReturn(100L);
        presenter.getRnrForm(0);

        presenter.completeMMIA(regimenItems, baseInfoItems, "");
        verify(mockMMIAformView, never()).showValidationAlert();
    }

    @Test
    public void shouldShowSignDialogIfTotalsMatchIfFeatureToggleIsOn() throws Exception {
        ((LMISTestApp) RuntimeEnvironment.application).setFeatureToggle(true);
        ArrayList<RegimenItem> regimenItems = generateRegimenItems();
        ArrayList<BaseInfoItem> baseInfoItems = new ArrayList<>();

        RnRForm rnRForm = new RnRForm();

        when(mmiaRepository.initMMIA(Matchers.<Program>anyObject())).thenReturn(rnRForm);
        when(mmiaRepository.getTotalPatients(rnRForm)).thenReturn(100L);
        presenter.getRnrForm(0);

        presenter.completeMMIA(regimenItems, baseInfoItems, "");

        if(LMISTestApp.getInstance().getFeatureToggleFor(R.bool.display_form_signature)) {
            verify(mockMMIAformView).showSignDialog();
        }
    }

    @Test
    public void shouldNotShowSignDialogIfTotalsMatchIfFeatureToggleIsOff() throws Exception {
        ((LMISTestApp) RuntimeEnvironment.application).setFeatureToggle(false);
        ArrayList<RegimenItem> regimenItems = generateRegimenItems();
        ArrayList<BaseInfoItem> baseInfoItems = new ArrayList<>();

        RnRForm rnRForm = new RnRForm();

        when(mmiaRepository.initMMIA(Matchers.<Program>anyObject())).thenReturn(rnRForm);
        when(mmiaRepository.getTotalPatients(rnRForm)).thenReturn(100L);
        presenter.getRnrForm(0);

        presenter.completeMMIA(regimenItems, baseInfoItems, "");

        verify(mockMMIAformView, never()).showSignDialog();
        verify(mockMMIAformView).loading();
    }

    @Test
    public void shouldCompleteFormAfterSignSuccess() throws Exception {
        ArrayList<RegimenItem> regimenItems = generateRegimenItems();
        ArrayList<BaseInfoItem> baseInfoItems = new ArrayList<>();

        RnRForm rnRForm = new RnRForm();

        when(mmiaRepository.initMMIA(Matchers.<Program>anyObject())).thenReturn(rnRForm);
        when(mmiaRepository.getTotalPatients(rnRForm)).thenReturn(100L);
        presenter.getRnrForm(0);

        presenter.completeMMIA(regimenItems, baseInfoItems, "");


    }

    @Test
    public void shouldNotCompleteMMIAIfTotalsMismatchAndCommentInvalid() throws Exception {
        ArrayList<RegimenItem> regimenItems = generateRegimenItems();
        ArrayList<BaseInfoItem> baseInfoItems = new ArrayList<>();

        RnRForm rnRForm = new RnRForm();

        when(mmiaRepository.initMMIA(Matchers.<Program>anyObject())).thenReturn(rnRForm);
        when(mmiaRepository.getTotalPatients(rnRForm)).thenReturn(99L);
        presenter.getRnrForm(0);

        presenter.completeMMIA(regimenItems, baseInfoItems, "1234");
        verify(mockMMIAformView).showValidationAlert();
    }

    @Test
    public void shouldShowErrorWhenLoadRnRFormOnError() {
        presenter.rnRFormOnErrorAction.call(new Exception("I am testing the onError action"));

        verify(mockMMIAformView).loaded();
        verify(mockMMIAformView).showErrorMessage("I am testing the onError action");
    }

    @Test
    public void shouldInitViewWhenLoadRnRFormOnNext() {
        RnRForm rnRForm = new RnRForm();

        presenter.rnRFormOnNextAction.call(rnRForm);

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

        verify(programRepository).queryByCode(MMIARepository.MMIA_PROGRAM_CODE);
        verify(mmiaRepository).getUnCompletedMMIA(any(Program.class));
    }

    @Test
    public void shouldAuthoriseFormObservableAuthoriseForm() throws LMISException {
        RnRForm form = new RnRForm();

        presenter.form = form;

        TestSubscriber<Void> subscriber = new TestSubscriber<>();
        presenter.getAuthoriseFormObservable().subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        verify(mmiaRepository).authorise(form);
    }

    @Test
    public void shouldCompleteSuccessAndRequestSyncWhenAuthoriseFormOnNext() {
        presenter.authoriseFormOnNextAction.call(null);

        verify(mockMMIAformView).loaded();
        verify(mockMMIAformView).completeSuccess();
        verify(syncManager).requestSyncImmediately();
    }

    @Test
    public void shouldShowAuthoriseFormErrorMessageWhenPeriodNotUnique() {
        presenter.authorizeFormOnErrorAction.call(new PeriodNotUniqueException("Period not unique"));

        verify(mockMMIAformView).loaded();
        verify(mockMMIAformView).showErrorMessage("Cannot submit MMIA twice in a period!");
    }

    @Test
    public void shouldShowAuthoriseFormErrorMessageWhenCompleteFailed() {
        presenter.authorizeFormOnErrorAction.call(new LMISException("Period not unique"));

        verify(mockMMIAformView).loaded();
        verify(mockMMIAformView).showErrorMessage("Complete Failed");
    }

    private ArrayList<RegimenItem> generateRegimenItems() {
        ArrayList<RegimenItem> regimenItems = new ArrayList<>();
        RegimenItem regimenItem = new RegimenItem();
        regimenItem.setAmount(100L);
        regimenItems.add(regimenItem);

        return regimenItems;
    }
}
