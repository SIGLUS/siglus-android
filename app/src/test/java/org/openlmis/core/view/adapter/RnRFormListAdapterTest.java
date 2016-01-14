package org.openlmis.core.view.adapter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.view.viewmodel.RnRFormViewModel;
import org.robolectric.RuntimeEnvironment;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class RnRFormListAdapterTest {

    private RnRFormListAdapter rnRFormListAdapter;
    private RnRForm form;

    @Before
    public void setUp() {
        form = new RnRForm();
        Program program = new Program();
        program.setProgramCode("MMIA");
        form.setProgram(program);
        form.setPeriodBegin(new Date());
        form.setPeriodEnd(new Date());

        List<RnRFormViewModel> models = newArrayList(new RnRFormViewModel(form), new RnRFormViewModel(form));

        rnRFormListAdapter = new RnRFormListAdapter(RuntimeEnvironment.application, "VIA", models);
    }

    @Test
    public void shouldRefreshListWhenMethodCalled() {
        assertThat(rnRFormListAdapter.getItemCount()).isEqualTo(2);

        rnRFormListAdapter.refreshList(newArrayList(new RnRFormViewModel(form)));

        assertThat(rnRFormListAdapter.getItemCount()).isEqualTo(1);
    }
}