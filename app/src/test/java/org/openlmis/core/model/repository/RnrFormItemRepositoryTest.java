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
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.RnrFormItemBuilder;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class RnrFormItemRepositoryTest extends LMISRepositoryUnitTest {

    RnrFormItemRepository rnrFormItemRepository;
    private RnrFormRepository rnrFormRepository;

    ProductRepository productRepository;
    ProgramRepository programRepository;
    ProductProgramRepository productProgramRepository;

    @Before
    public void setUp() throws LMISException {
        rnrFormItemRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(RnrFormItemRepository.class);
        rnrFormRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(RnrFormRepository.class);
        productRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ProductRepository.class);
        programRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ProgramRepository.class);
        productProgramRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ProductProgramRepository.class);
    }

    @Test
    public void shouldQueryListForLowStockByProductId() throws LMISException {

        RnRForm form = new RnRForm();
        List<RnrFormItem> rnrFormItemList = new ArrayList<>();

        Program program = new Program();
        program.setProgramCode("1");
        Product product = new Product();
        product.setId(1);

        rnrFormItemList.add(getRnrFormItem(form, product, 1));
        rnrFormItemList.add(getRnrFormItem(form, product, 2));
        rnrFormItemList.add(getRnrFormItem(form, product, 3));
        rnrFormItemList.add(getRnrFormItem(form, product, 0));
        rnrFormItemList.add(getRnrFormItem(form, product, 5));
        rnrFormItemList.add(getRnrFormItem(form, product, 7));

        rnrFormItemRepository.batchCreateOrUpdate(rnrFormItemList);

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

    @Test
    public void shouldListAllNewRnrItems() throws Exception {
        List<RnrFormItem> rnrFormItemList = newArrayList(createRnrFormItem("P1", 100L), createRnrFormItem("P2", 200L));

        rnrFormItemRepository.batchCreateOrUpdate(rnrFormItemList);

        List<RnrFormItem> rnrFormItems = rnrFormItemRepository.listAllNewRnrItems();
        assertThat(rnrFormItems.size(), is(2));
        assertThat(rnrFormItems.get(0).getProduct().getCode(), is("P1"));
        assertThat(rnrFormItems.get(1).getProduct().getCode(), is("P2"));

        rnrFormItemRepository.deleteAllNewRnrItems();
        assertThat(rnrFormItemRepository.listAllNewRnrItems().size(), is(0));
    }

    private RnrFormItem createRnrFormItem(String productCode, Long requestedAmount) throws LMISException {
        Product product = new ProductBuilder().setCode(productCode).setIsActive(true).build();
        productRepository.createOrUpdate(product);
        return new RnrFormItemBuilder().setProduct(product).setRequestAmount(requestedAmount).build();
    }

    @Test
    public void shouldDeleteOneRnrFormItem() throws Exception {
        List<RnrFormItem> rnrFormItemList = newArrayList(createRnrFormItem("P1", 100L), createRnrFormItem("P2", 200L));

        rnrFormItemRepository.batchCreateOrUpdate(rnrFormItemList);

        rnrFormItemRepository.deleteOneNewAdditionalRnrItem(rnrFormItemList.get(0));
        List<RnrFormItem> rnrFormItems = rnrFormItemRepository.listAllNewRnrItems();
        assertThat(rnrFormItems.size(), is(1));
        assertThat(rnrFormItems.get(0).getProduct().getCode(), is("P2"));
    }

    @Test
    public void shouldQueryRnrFormItemsByFormId() throws Exception {
        Long formId = 1L;
        RnRForm rnRForm = new RnRForm();
        rnRForm.setId(formId);
        List<RnrFormItem> rnrFormItemList = newArrayList(createRnrFormItem("P1", 100L), createRnrFormItem("P2", 200L));
        rnrFormItemList.get(0).setForm(rnRForm);
        rnrFormItemList.get(1).setForm(new RnRForm());
        rnrFormItemRepository.batchCreateOrUpdate(rnrFormItemList);

        List<RnrFormItem> rnrFormItemListDB = rnrFormItemRepository.listAllRnrItemsByForm(formId);
        assertThat(rnrFormItemListDB.size(), is(1));
        assertThat(rnrFormItemListDB.get(0).getProduct().getCode(), is("P1"));

    }
}
