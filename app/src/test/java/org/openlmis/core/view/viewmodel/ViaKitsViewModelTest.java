package org.openlmis.core.view.viewmodel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.RnrFormItemBuilder;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class ViaKitsViewModelTest {

    @Test
    public void shouldConvertRnrKitLineItemToViaKitViewModel() {
        RnrFormItem rnrKitItem1 = new RnrFormItemBuilder()
                .setProduct(new ProductBuilder().setCode("26A01").build())
                .setReceived(100)
                .setIssued((long) 50)
                .build();

        RnrFormItem rnrKitItem2 = new RnrFormItemBuilder()
                .setProduct(new ProductBuilder().setCode("26A02").build())
                .setReceived(300)
                .setIssued((long) 110)
                .build();

        List<RnrFormItem> rnrFormItems = newArrayList(rnrKitItem1, rnrKitItem2);

        ViaKitsViewModel viaKitsViewModel = new ViaKitsViewModel();
        viaKitsViewModel.convertRnrKitItemsToViaKit(rnrFormItems);
        assertThat(viaKitsViewModel.getKitsOpenedCHW(), is("110"));
        assertThat(viaKitsViewModel.getKitsOpenedHF(), is("50"));
        assertThat(viaKitsViewModel.getKitsReceivedCHW(), is("300"));
        assertThat(viaKitsViewModel.getKitsReceivedHF(), is("100"));
    }

    @Test
    public void shouldNotPopulateAnyValueIfAmountIsBelowZero() {
        ViaKitsViewModel viaKitsViewModel = new ViaKitsViewModel();

        Product usKit = new ProductBuilder().setCode(ViaKitsViewModel.US_KIT).build();
        Product apeKit = new ProductBuilder().setCode(ViaKitsViewModel.APE_KIT).build();
        List<RnrFormItem> rnrFormItems = newArrayList(new RnrFormItemBuilder().setIssued(Long.MIN_VALUE).setProduct(usKit).build(),
                new RnrFormItemBuilder().setIssued(Long.MIN_VALUE).setProduct(apeKit).build());

        viaKitsViewModel.convertRnrKitItemsToViaKit(rnrFormItems);

        assertThat(viaKitsViewModel.getKitsOpenedCHW(), is("0"));
        assertThat(viaKitsViewModel.getKitsOpenedHF(), is("0"));
    }
}