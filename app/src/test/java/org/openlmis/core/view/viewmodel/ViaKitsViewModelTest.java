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

    @Test
    public void shouldConvertToRnrItemFromViaKitViewModel() throws Exception {
        ViaKitsViewModel viaKitsViewModel = new ViaKitsViewModel();
        viaKitsViewModel.setKitsOpenedCHW("10");
        viaKitsViewModel.setKitsReceivedCHW("20");
        viaKitsViewModel.setKitsOpenedHF("30");
        viaKitsViewModel.setKitsReceivedHF("40");

        Product usKit = new ProductBuilder().setCode(ViaKitsViewModel.US_KIT).build();
        Product apeKit = new ProductBuilder().setCode(ViaKitsViewModel.APE_KIT).build();
        viaKitsViewModel.setKitItems(newArrayList(new RnrFormItemBuilder().setProduct(usKit).build(),
                new RnrFormItemBuilder().setProduct(apeKit).build()));

        List<RnrFormItem> rnrFormItems = viaKitsViewModel.convertToRnrItems();

        assertEquals(2, rnrFormItems.size());
        assertEquals(10, rnrFormItems.get(1).getIssued());
        assertEquals(20, rnrFormItems.get(1).getReceived());
        assertEquals(10, rnrFormItems.get(1).getInventory());
        assertEquals(ViaKitsViewModel.APE_KIT, rnrFormItems.get(1).getProduct().getCode());

        assertEquals(30, rnrFormItems.get(0).getIssued());
        assertEquals(40, rnrFormItems.get(0).getReceived());
        assertEquals(10, rnrFormItems.get(0).getInventory());
        assertEquals(ViaKitsViewModel.US_KIT, rnrFormItems.get(0).getProduct().getCode());
    }

    @Test
    public void shouldConvertToRnrItemFromViaKitViewModelWhenQUantityIsEmpty() throws Exception {
        ViaKitsViewModel viaKitsViewModel = new ViaKitsViewModel();
        viaKitsViewModel.setKitsOpenedCHW("10");
        viaKitsViewModel.setKitsReceivedCHW("");
        viaKitsViewModel.setKitsOpenedHF("");
        viaKitsViewModel.setKitsReceivedHF("40");

        Product usKit = new ProductBuilder().setCode(ViaKitsViewModel.US_KIT).build();
        Product apeKit = new ProductBuilder().setCode(ViaKitsViewModel.APE_KIT).build();
        viaKitsViewModel.setKitItems(newArrayList(new RnrFormItemBuilder().setProduct(usKit).build(),
                new RnrFormItemBuilder().setProduct(apeKit).build()));

        List<RnrFormItem> rnrFormItems = viaKitsViewModel.convertToRnrItems();

        assertEquals(2, rnrFormItems.size());
        assertEquals(10, rnrFormItems.get(1).getIssued());
        assertEquals(Long.MIN_VALUE, rnrFormItems.get(1).getReceived());
        assertEquals(Long.MIN_VALUE, rnrFormItems.get(1).getInventory());
        assertEquals(ViaKitsViewModel.APE_KIT, rnrFormItems.get(1).getProduct().getCode());

        assertEquals(Long.MIN_VALUE, rnrFormItems.get(0).getIssued());
        assertEquals(40, rnrFormItems.get(0).getReceived());
        assertEquals(Long.MIN_VALUE, rnrFormItems.get(0).getInventory());
        assertEquals(ViaKitsViewModel.US_KIT, rnrFormItems.get(0).getProduct().getCode());
    }

    @Test
    public void shouldNotPopulateAnyValueIfAmountIsBelowZero() {
        ViaKitsViewModel viaKitsViewModel = new ViaKitsViewModel();

        Product usKit = new ProductBuilder().setCode(ViaKitsViewModel.US_KIT).build();
        Product apeKit = new ProductBuilder().setCode(ViaKitsViewModel.APE_KIT).build();
        List<RnrFormItem> rnrFormItems = newArrayList(new RnrFormItemBuilder().setIssued(Long.MIN_VALUE).setProduct(usKit).build(),
                new RnrFormItemBuilder().setIssued(Long.MIN_VALUE).setProduct(apeKit).build());

        viaKitsViewModel.convertRnrKitItemsToViaKit(rnrFormItems);

        assertThat(viaKitsViewModel.getKitsOpenedCHW(), is(""));
        assertThat(viaKitsViewModel.getKitsOpenedHF(), is(""));
    }

    @Test
    public void shouldNotThrowExceptionIfKitValuesAreEmpty() {
        Product usKit = new ProductBuilder().setCode(ViaKitsViewModel.US_KIT).build();
        Product apeKit = new ProductBuilder().setCode(ViaKitsViewModel.APE_KIT).build();
        List<RnrFormItem> rnrFormItems = newArrayList(new RnrFormItemBuilder().setIssued(-1).setProduct(usKit).build(),
                new RnrFormItemBuilder().setIssued(-1).setProduct(apeKit).build());

        ViaKitsViewModel viaKitsViewModel = new ViaKitsViewModel();
        viaKitsViewModel.setKitItems(rnrFormItems);

        viaKitsViewModel.convertToRnrItems();
    }
}