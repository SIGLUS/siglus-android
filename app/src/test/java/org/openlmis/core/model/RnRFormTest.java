package org.openlmis.core.model;

import org.junit.Test;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.RnrFormItemBuilder;

import java.util.List;

import static org.junit.Assert.assertEquals;
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
}