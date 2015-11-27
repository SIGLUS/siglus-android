package org.openlmis.core.presenter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.RnRFormViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class RnRFormListPresenterTest {
    RnRFormListPresenter presenter;
    private List<RnRForm> rnRForms;
    private ArrayList<RnRFormViewModel> viewModels;

    @Before
    public void setUp() {
        presenter = new RnRFormListPresenter();
        rnRForms = getRnRForms();
        viewModels = new ArrayList<>();
    }

    @Test
    public void shouldBuildFormListViewModels() throws LMISException {
        presenter.setProgramCode("MMIA");
        presenter.repository = mock(RnrFormRepository.class);
        Collections.reverse(rnRForms);
        when(presenter.repository.list("MMIA")).thenReturn(rnRForms);
        List<RnRFormViewModel> resultViewModels = presenter.buildFormListViewModels();
        assertThat(resultViewModels.size()).isEqualTo(5);
    }

    @Test
    public void shouldAddCurrentPeriodFormAndRemove() {
        presenter.addCurrentPeriodViewModel(viewModels, rnRForms);

        assertThat(rnRForms.size()).isEqualTo(2);
        assertThat(viewModels.size()).isEqualTo(2);
        assertThat(viewModels.get(0).getTitle()).isEqualTo("Current period");
    }

    @Test
    public void shouldAddPreviousPeriodForm() {
        rnRForms.remove(0);
        presenter.addPreviousPeriodViewModels(viewModels, rnRForms);

        assertThat(viewModels.size()).isEqualTo(3);
        assertThat(viewModels.get(0).getTitle()).isEqualTo("Previous periods");
    }

    private List<RnRForm> getRnRForms() {
        return newArrayList(getRnRForm(RnRForm.STATUS.DRAFT), getRnRForm(RnRForm.STATUS.AUTHORIZED), getRnRForm(RnRForm.STATUS.AUTHORIZED));
    }

    private RnRForm getRnRForm(RnRForm.STATUS statu) {
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