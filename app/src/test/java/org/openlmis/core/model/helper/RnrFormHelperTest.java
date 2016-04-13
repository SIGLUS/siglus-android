package org.openlmis.core.model.helper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRFormSignature;
import org.openlmis.core.persistence.DbUtil;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(LMISTestRunner.class)
public class RnrFormHelperTest {

    private RnrFormHelper rnrFormHelper;

    @Before
    public void setUp() throws Exception {
        rnrFormHelper = new RnrFormHelper();
    }

    @Test
    public void shouldUpdateWrapperList() throws Exception {
        RnRForm rnRForm = new RnRForm();
        DbUtil.initialiseDao(RnRForm.class).assignEmptyForeignCollection(rnRForm, "signatures");

        rnRForm.getSignatures().add(new RnRFormSignature(rnRForm, "sign", RnRFormSignature.TYPE.SUBMITTER));

        rnrFormHelper.updateWrapperList(rnRForm);
        assertThat(rnRForm.getSignaturesWrapper().size(), is(rnRForm.getSignatures().size()));

        rnRForm.getSignaturesWrapper().add(new RnRFormSignature(rnRForm, "sign", RnRFormSignature.TYPE.APPROVER));
        rnrFormHelper.updateWrapperList(rnRForm);
        assertThat(rnRForm.getSignaturesWrapper().size(), is(rnRForm.getSignatures().size()));
    }
}