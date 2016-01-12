package org.openlmis.core.presenter;

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.SyncError;
import org.openlmis.core.model.SyncType;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.SyncErrorsRepository;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.RnRFormViewModel;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    @Before
    public void setUp() {
        syncErrorsRepository = mock(SyncErrorsRepository.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(SyncErrorsRepository.class).toInstance(syncErrorsRepository);
            }
        });

        presenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(RnRFormListPresenter.class);
        rnRForms = createRnRForms();
        viewModels = new ArrayList<>();
    }

    @Test
    public void shouldBuildFormListViewModels() throws LMISException {
        presenter.setProgramCode("MMIA");
        presenter.repository = mock(RnrFormRepository.class);
        Collections.reverse(rnRForms);
        when(presenter.repository.list("MMIA")).thenReturn(rnRForms);
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

    private List<RnRForm> createRnRForms() {
        return newArrayList(createRnRForm(RnRForm.STATUS.DRAFT), createRnRForm(RnRForm.STATUS.AUTHORIZED), createRnRForm(RnRForm.STATUS.AUTHORIZED));
    }

    private RnRForm createRnRForm(RnRForm.STATUS statu) {
        Program program = new Program();
        program.setProgramCode("MMIA");
        program.setProgramName("MMIA");

        RnRForm rnRForm = RnRForm.init(program, DateUtil.today());
        rnRForm.setId(1L);
        rnRForm.setStatus(statu);
        rnRForm.setSynced(true);
        return rnRForm;
    }

}