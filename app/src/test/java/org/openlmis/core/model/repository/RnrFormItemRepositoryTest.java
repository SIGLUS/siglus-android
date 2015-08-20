package org.openlmis.core.model.repository;

import android.support.annotation.NonNull;

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
        List<RnrFormItem> rnrFormItemList = new ArrayList<>();

        Program program = new Program();
        program.setProgramCode("1");
        Product product = new Product();
        product.setProgram(program);
        product.setId(1);

        rnrFormItemList.add(getRnrFormItem(form, product, 1));
        rnrFormItemList.add(getRnrFormItem(form, product, 2));
        rnrFormItemList.add(getRnrFormItem(form, product, 3));
        rnrFormItemList.add(getRnrFormItem(form, product, 0));
        rnrFormItemList.add(getRnrFormItem(form, product, 5));
        rnrFormItemList.add(getRnrFormItem(form, product, 7));

        rnrFormItemRepository.create(rnrFormItemList);

        rnrFormRepository.create(form);

        List<RnrFormItem> rnrFormItemListFromDB = rnrFormItemRepository.queryListForLowStockByProductId(product);

        assertThat(rnrFormItemListFromDB.size(), is(3));

        assertThat(rnrFormItemListFromDB.get(0).getInventory(), is(7L));
        assertThat(rnrFormItemListFromDB.get(1).getInventory(), is(5L));
        assertThat(rnrFormItemListFromDB.get(2).getInventory(), is(3L));
    }

    @NonNull
    private RnrFormItem getRnrFormItem(RnRForm form, Product product, long inventory) {
        RnrFormItem rnrFormItem = new RnrFormItem();
        rnrFormItem.setForm(form);
        rnrFormItem.setProduct(product);
        rnrFormItem.setInventory(inventory);
        return rnrFormItem;
    }


}
