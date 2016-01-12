package org.openlmis.core.view.viewmodel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.RnrFormItemBuilder;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class ViaKitsViewModelTest {

    @Test
    public void shouldConvertRnrKitLineItemToViaKitViewModel() {
        RnrFormItem rnrKitItem1 = new RnrFormItemBuilder()
                .setProduct(new ProductBuilder().setCode("SCOD10").build())
                .setReceived(100)
                .setIssued(50)
                .build();

        RnrFormItem rnrKitItem2 = new RnrFormItemBuilder()
                .setProduct(new ProductBuilder().setCode("SCOD12").build())
                .setReceived(300)
                .setIssued(110)
                .build();

        List<RnrFormItem> rnrFormItems = newArrayList(rnrKitItem1, rnrKitItem2);

        ViaKitsViewModel viaKitsViewModel = new ViaKitsViewModel();
        viaKitsViewModel.convertRnrKitItemsToViaKit(rnrFormItems);
        assertThat(viaKitsViewModel.getKitsOpenedCHW(), is("110"));
        assertThat(viaKitsViewModel.getKitsOpenedHF(), is("50"));
        assertThat(viaKitsViewModel.getKitsReceivedCHW(), is("300"));
        assertThat(viaKitsViewModel.getKitsReceivedHF(), is("100"));
    }

}