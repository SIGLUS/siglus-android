package org.openlmis.core.view.adapter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.view.viewmodel.RnRFormViewModel;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class RnRFormListAdapterTest {

    private RnRFormListAdapter rnRFormListAdapter;

    @Before
    public void setUp() {
        List<RnRFormViewModel> models = newArrayList(new RnRFormViewModel("form1"), new RnRFormViewModel("form2"));

        rnRFormListAdapter = new RnRFormListAdapter(RuntimeEnvironment.application, "VIA", models);
    }

    @Test
    public void shouldRefreshListWhenMethodCalled() {
        assertThat(rnRFormListAdapter.getItemCount()).isEqualTo(2);

        rnRFormListAdapter.refreshList(newArrayList(new RnRFormViewModel("form3")));

        assertThat(rnRFormListAdapter.getItemCount()).isEqualTo(1);
    }
}