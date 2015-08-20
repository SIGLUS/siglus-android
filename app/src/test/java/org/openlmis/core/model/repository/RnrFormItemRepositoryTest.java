package org.openlmis.core.model.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.robolectric.Robolectric;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(LMISTestRunner.class)
public class RnrFormItemRepositoryTest extends LMISRepositoryUnitTest {
    RnrFormItemRepository rnrFormItemRepository;
    private RnrFormRepository rnrFormRepository;

    @Before
    public void setUp() throws LMISException {
        rnrFormItemRepository = RoboGuice.getInjector(Robolectric.application).getInstance(RnrFormItemRepository.class);
        rnrFormRepository = RoboGuice.getInjector(Robolectric.application).getInstance(RnrFormRepository.class);
    }

    @Test
    public void shouldQueryListForLowStockByProductId() throws LMISException {

        RnRForm form = new RnRForm();
        List<RnrFormItem> rnrFormItemList = new ArrayList();

        Program program = new Program();
        program.setProgramCode("1");
        Product product = new Product();
        product.setProgram(program);
        product.setId(1);

        for (int i = 0; i < 20; i++) {
            RnrFormItem rnrFormItem = new RnrFormItem();
            rnrFormItem.setForm(form);
            rnrFormItem.setProduct(product);
            if (i % 2 == 0) {
                rnrFormItem.setInventory(0);
            } else {
                rnrFormItem.setInventory(1);
            }
            rnrFormItemList.add(rnrFormItem);
        }

        rnrFormItemRepository.create(rnrFormItemList);

        rnrFormRepository.create(form);

        List<RnrFormItem> rnrFormItemListFromDB = rnrFormItemRepository.queryListForLowStockByProductId(product);

        assertThat(rnrFormItemListFromDB.size(), is(3));

        for (RnrFormItem item : rnrFormItemListFromDB) {
            assertThat(item.getInventory(), is(1L));
        }
    }


}
