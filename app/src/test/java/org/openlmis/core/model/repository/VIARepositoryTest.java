package org.openlmis.core.model.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(LMISTestRunner.class)
public class VIARepositoryTest {

    private VIARepository viaRepository;
    private Program viaProgram;

    @Before
    public void setup() throws LMISException {
        viaRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(VIARepository.class);

        viaProgram = new Program("VIA", "VIA", null, false, null,null);
        viaProgram.setId(1l);
    }

    @Test
    public void shouldGenerateBaseInfoItems() throws Exception {
        RnRForm form = new RnRForm();
        form.setProgram(viaProgram);
        form.setComments("Submitted Form");
        form.setStatus(RnRForm.STATUS.AUTHORIZED);
        form.setSynced(false);

        List<BaseInfoItem> baseInfoItems = viaRepository.generateBaseInfoItems(form,MMIARepository.REPORT_TYPE.OLD);

        assertThat(baseInfoItems.size(), is(1));
        assertThat(baseInfoItems.get(0).getName(), is(VIARepository.ATTR_CONSULTATION));
    }
}