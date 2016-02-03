package org.openlmis.core.presenter;

import com.google.inject.AbstractModule;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.SyncError;
import org.openlmis.core.model.SyncType;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.model.repository.SyncErrorsRepository;
import org.openlmis.core.model.repository.VIARepository;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.RnRFormViewModel;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class RnRFormListPresenterTest {
    RnRFormListPresenter presenter;
    private List<RnRForm> rnRForms;
    private ArrayList<RnRFormViewModel> viewModels;
    SyncErrorsRepository syncErrorsRepository;
    private RnrFormRepository rnrFormRepository;
    private StockRepository stockRepository;
    private SharedPreferenceMgr sharedPreferenceMgr;
    private Period period;

    @Before
    public void setUp() {
        rnrFormRepository = mock(RnrFormRepository.class);
        stockRepository = mock(StockRepository.class);
        syncErrorsRepository = mock(SyncErrorsRepository.class);
        sharedPreferenceMgr = mock(SharedPreferenceMgr.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(SyncErrorsRepository.class).toInstance(syncErrorsRepository);
                bind(RnrFormRepository.class).toInstance(rnrFormRepository);
                bind(StockRepository.class).toInstance(stockRepository);
                bind(SharedPreferenceMgr.class).toInstance(sharedPreferenceMgr);
            }
        });

        presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(RnRFormListPresenter.class);
        period = DateUtil.generateRnRFormPeriodBy(new Date());
        rnRForms = createRnRForms();
        viewModels = new ArrayList<>();
    }

    @Test
    public void shouldBuildFormListViewModels() throws LMISException {
        presenter.setProgramCode("MMIA");
        Collections.reverse(rnRForms);
        when(rnrFormRepository.list("MMIA")).thenReturn(rnRForms);
        when(syncErrorsRepository.getBySyncTypeAndObjectId(any(SyncType.class), anyLong()))
                .thenReturn(Arrays.asList(new SyncError("Error1", SyncType.RnRForm, 1), new SyncError("Error2", SyncType.RnRForm, 1)));

        List<RnRFormViewModel> resultViewModels = presenter.buildFormListViewModels();
        assertThat(resultViewModels.size()).isEqualTo(3);
        assertThat(resultViewModels.get(0).getSyncServerErrorMessage()).isEqualTo("Error2");
    }

    @Test
    public void shouldAddCurrentPeriodFormAndRemove() {
        presenter.addCurrentPeriodViewModel(viewModels, rnRForms);

        assertThat(rnRForms.size()).isEqualTo(2);
        assertThat(viewModels.size()).isEqualTo(1);
    }

    @Test
    public void shouldAddPreviousPeriodForm() {
        rnRForms.remove(0);
        presenter.addPreviousPeriodViewModels(viewModels, rnRForms);

        assertThat(viewModels.size()).isEqualTo(2);
    }

    @Test
    public void shouldReturnOneRnrFormViewModleWhenThereIsNoRnrFormAndToggleOn() throws Exception {

        when(sharedPreferenceMgr.getLatestPhysicInventoryTime()).thenReturn(DateUtil.formatDate(period.getBegin().toDate(), DateUtil.DATE_TIME_FORMAT));
        when(rnrFormRepository.list("VIA")).thenReturn(new ArrayList<RnRForm>());
        presenter.setProgramCode(VIARepository.VIA_PROGRAM_CODE);
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_home_page_update, true);

        List<RnRFormViewModel> rnRFormViewModels = presenter.buildFormListViewModels();

        String periodString = LMISTestApp.getContext().getString(R.string.label_period_date, DateUtil.formatDate(period.getBegin().toDate()), DateUtil.formatDate(period.getEnd().toDate()));

        assertThat(rnRFormViewModels.size()).isEqualTo(1);
        assertThat(rnRFormViewModels.get(0).getPeriod()).isEqualTo(periodString);
        assertThat(rnRFormViewModels.get(0).getName()).isEqualTo(LMISTestApp.getContext().getString(R.string.label_via_name));
    }

    @Test
    public void shouldReturnUnCompleteInventoryTypeRnrFormViewModel() throws Exception {
        when(rnrFormRepository.list("VIA")).thenReturn(new ArrayList<RnRForm>());
        when(sharedPreferenceMgr.getLatestPhysicInventoryTime()).thenReturn(DateUtil.formatDate(DateUtil.generateRnRFormPeriodBy(new Date()).previous().getBegin().toDate(), DateUtil.DATE_TIME_FORMAT));
        presenter.setProgramCode(VIARepository.VIA_PROGRAM_CODE);
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_home_page_update, true);

        List<RnRFormViewModel> rnRFormViewModels = presenter.buildFormListViewModels();
        assertThat(rnRFormViewModels.size()).isEqualTo(1);
        assertThat(rnRFormViewModels.get(0).getType()).isEqualTo(RnRFormViewModel.TYPE_UNCOMPLETE_INVENTORY);
    }

    @Test
    public void shouldReturnCreateFormTypeRnrFormViewModel() throws Exception {
        when(sharedPreferenceMgr.getLatestPhysicInventoryTime()).thenReturn(DateUtil.formatDate(new Date(), DateUtil.DATE_TIME_FORMAT));
        when(rnrFormRepository.list("VIA")).thenReturn(new ArrayList<RnRForm>());
        presenter.setProgramCode(VIARepository.VIA_PROGRAM_CODE);
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_home_page_update, true);

        List<RnRFormViewModel> rnRFormViewModels = presenter.buildFormListViewModels();
        assertThat(rnRFormViewModels.size()).isEqualTo(1);
        assertThat(rnRFormViewModels.get(0).getType()).isEqualTo(RnRFormViewModel.TYPE_COMPLETED_INVENTORY);
    }


    @Test
    public void shouldReturnCreateFormTypeRnrFormViewModelWithoutRnrformInCurrentPeriod() throws Exception {
        ArrayList<RnRForm> rnRForms = new ArrayList<>();

        RnRForm rnRForm = new RnRForm();
        Program program = new Program();
        program.setProgramCode(VIARepository.VIA_PROGRAM_CODE);
        rnRForm.setProgram(program);
        rnRForm.setStatus(RnRForm.STATUS.AUTHORIZED);
        rnRForm.setPeriodBegin(period.previous().getBegin().toDate());
        rnRForm.setPeriodEnd(period.previous().getEnd().toDate());

        rnRForms.add(rnRForm);

        when(rnrFormRepository.list(VIARepository.VIA_PROGRAM_CODE)).thenReturn(rnRForms);
        when(sharedPreferenceMgr.getLatestPhysicInventoryTime()).thenReturn(DateUtil.formatDate(period.previous().getBegin().toDate(), DateUtil.DATE_TIME_FORMAT));
        presenter.setProgramCode(VIARepository.VIA_PROGRAM_CODE);
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_home_page_update, true);

        List<RnRFormViewModel> rnRFormViewModels = presenter.buildFormListViewModels();

        assertThat(rnRFormViewModels.size()).isEqualTo(2);
        assertThat(rnRFormViewModels.get(0).getType()).isEqualTo(RnRFormViewModel.TYPE_UNCOMPLETE_INVENTORY);
        assertThat(rnRFormViewModels.get(1).getType()).isEqualTo(RnRFormViewModel.TYPE_UNSYNC);
    }

    @Test
    public void shouldReturnEmptyRnrFormViewModleWhenThereIsNoRnrFormAndToggleOff() throws Exception {
        when(rnrFormRepository.list("VIA")).thenReturn(new ArrayList<RnRForm>());
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_home_page_update, false);

        List<RnRFormViewModel> rnRFormViewModels = presenter.buildFormListViewModels();

        assertThat(rnRFormViewModels.size()).isEqualTo(0);
    }

    private List<RnRForm> createRnRForms() {
        return newArrayList(createRnRForm(RnRForm.STATUS.DRAFT), createRnRForm(RnRForm.STATUS.AUTHORIZED), createRnRForm(RnRForm.STATUS.AUTHORIZED));
    }

    private RnRForm createRnRForm(RnRForm.STATUS status) {
        Program program = new Program();
        program.setProgramCode("MMIA");
        program.setProgramName("MMIA");

        RnRForm rnRForm = RnRForm.init(program, DateUtil.today());
        rnRForm.setId(1L);
        rnRForm.setStatus(status);
        rnRForm.setSynced(true);
        return rnRForm;
    }

    @Test
    public void shouldGetRightPeriodWhenLastRequisitionExists() {
        RnRForm rnRForm = new RnRForm();
        rnRForm.setPeriodBegin(new DateTime("2016-12-23").toDate());
        rnRForm.setPeriodEnd(new DateTime("2016-01-21").toDate());

        when(rnrFormRepository.queryLatestAuthorizedRnRForm()).thenReturn(rnRForm);

    }

}