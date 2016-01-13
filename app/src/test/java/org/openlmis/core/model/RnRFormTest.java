package org.openlmis.core.model;

import org.junit.Test;
import org.openlmis.core.model.Product.IsKit;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.RnrFormItemBuilder;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

public class RnRFormTest {

    @Test
    public void shouldReturnListWithDeactivatedItems() {
        RnRForm rnRForm = new RnRForm();
        Product activeProduct = new ProductBuilder().setIsActive(true).build();
        Product inactiveProduct = new ProductBuilder().setIsActive(false).build();
        RnrFormItem activeRnrProduct = new RnrFormItemBuilder().setProduct(activeProduct).build();
        RnrFormItem inactiveRnrProduct = new RnrFormItemBuilder().setProduct(inactiveProduct).build();

        rnRForm.setRnrFormItemListWrapper(newArrayList(activeRnrProduct, inactiveRnrProduct));

        List<RnrFormItem> rnrFormDeactivatedItemList = rnRForm.getDeactivatedProductItems();
        assertEquals(1, rnrFormDeactivatedItemList.size());
        assertEquals(false, rnrFormDeactivatedItemList.get(0).getProduct().isActive());
    }

    @Test
    public void shouldGetNonKitFormItemAndKitFormItem() throws Exception {
        RnRForm rnRForm = new RnRForm();
        Product kitProduct = new ProductBuilder().setIsActive(true).setIsKit(true).build();
        Product product = new ProductBuilder().setIsActive(true).setIsKit(false).build();
        RnrFormItem kitRnrProduct = new RnrFormItemBuilder().setProduct(kitProduct).build();
        RnrFormItem rnrProduct = new RnrFormItemBuilder().setProduct(product).build();

        rnRForm.setRnrFormItemListWrapper(newArrayList(kitRnrProduct, rnrProduct));

        List<RnrFormItem> rnrNonKitItems = rnRForm.getRnrItems(IsKit.No);
        assertEquals(1, rnrNonKitItems.size());
        assertFalse(rnrNonKitItems.get(0).getProduct().isKit());

        List<RnrFormItem> rnrKitItems = rnRForm.getRnrItems(IsKit.Yes);
        assertEquals(1, rnrKitItems.size());
        assertTrue(rnrKitItems.get(0).getProduct().isKit());

    }
}