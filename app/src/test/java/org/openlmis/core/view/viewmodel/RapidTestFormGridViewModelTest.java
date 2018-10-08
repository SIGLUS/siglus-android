package org.openlmis.core.view.viewmodel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.ProgramDataColumn;
import org.openlmis.core.model.ProgramDataFormItem;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(LMISTestRunner.class)
public class RapidTestFormGridViewModelTest {
    RapidTestFormGridViewModel viewModel = new RapidTestFormGridViewModel(RapidTestFormGridViewModel.ColumnCode.Malaria);

    @Test
    public void shouldValidate() throws Exception {
        assertTrue(viewModel.validate());

        viewModel.setConsumptionValue("100");
        assertFalse(viewModel.validate());

        viewModel.setPositiveValue("100");
        viewModel.setConsumptionValue("");
        assertFalse(viewModel.validate());

        viewModel.setConsumptionValue("99");
        assertFalse(viewModel.validate());

        viewModel.setConsumptionValue("100");
        assertFalse(viewModel.validate());

        viewModel.setUnjustifiedValue("100");
        assertTrue(viewModel.validate());
    }

    @Test
    public void shouldConvertFormGridViewModelToDataModel() throws Exception {
        viewModel.setConsumptionValue("20");
        viewModel.setPositiveValue("1001");
        ProgramDataColumn consumeColumn = new ProgramDataColumn();
        consumeColumn.setCode("CONSUME_MALARIA");
        viewModel.setConsumeColumn(consumeColumn);
        ProgramDataColumn positiveColumn = new ProgramDataColumn();
        positiveColumn.setCode("POSITIVE_MALARIA");
        viewModel.setPositiveColumn(positiveColumn);

        MovementReasonManager.MovementReason reason = new MovementReasonManager.MovementReason(MovementReasonManager.MovementType.ISSUE, "ACC_EMERGENCY", "Acc emergency");
        List<ProgramDataFormItem> programDataFormItems = viewModel.convertFormGridViewModelToDataModel(reason);
        assertThat(programDataFormItems.get(0).getName(), is("ACC_EMERGENCY"));
        assertThat(programDataFormItems.get(0).getProgramDataColumn().getCode(), is("CONSUME_MALARIA"));
        assertThat(programDataFormItems.get(0).getValue(), is(20));
        assertThat(programDataFormItems.get(1).getName(), is("ACC_EMERGENCY"));
        assertThat(programDataFormItems.get(1).getProgramDataColumn().getCode(), is("POSITIVE_MALARIA"));
        assertThat(programDataFormItems.get(1).getValue(), is(1001));
    }
}