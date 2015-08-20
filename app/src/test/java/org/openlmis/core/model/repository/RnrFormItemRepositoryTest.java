package org.openlmis.core.model.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.RnrFormItem;
import org.robolectric.Robolectric;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(LMISTestRunner.class)
public class RnrFormItemRepositoryTest {
    RnrFormItemRepository rnrFormItemRepository;

    @Before
    public void setUp() throws LMISException {
        rnrFormItemRepository = RoboGuice.getInjector(Robolectric.application).getInstance(RnrFormItemRepository.class);
    }

    @Test
    public void shouldQueryListForLowStockByProductId() throws LMISException {
        List<RnrFormItem> rnrFormItemList = new ArrayList();
        Product product = new Product();
        product.setId(10);
        for (int i = 0; i < 7; i++) {
            RnrFormItem rnrFormItem = new RnrFormItem();
            rnrFormItem.setProduct(product);
            if (i % 2 == 0) {
                rnrFormItem.setInventory(0);
            } else {
                rnrFormItem.setInventory(1);
            }
            rnrFormItemList.add(rnrFormItem);
        }

        rnrFormItemRepository.create(rnrFormItemList);

        List<RnrFormItem> rnrFormItemListFromDB = rnrFormItemRepository.queryListForLowStockByProductId(product);

        assertThat(rnrFormItemListFromDB.size(), is(3));

        for (RnrFormItem item : rnrFormItemListFromDB) {
            assertThat(item.getInventory(), is(1L));
        }
    }
}
