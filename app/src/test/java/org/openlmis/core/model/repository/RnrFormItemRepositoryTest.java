package org.openlmis.core.model.repository;

import android.support.annotation.NonNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.ProductProgram;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.ProductProgramBuilder;
import org.openlmis.core.model.builder.ProgramBuilder;
import org.openlmis.core.model.builder.RnRFormBuilder;
import org.openlmis.core.utils.Constants;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

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
        product.setProgram(program);
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
    public void testListAllProductIdsInCurrentVIAForm() throws Exception {

        Program program = new ProgramBuilder().setProgramCode(Constants.VIA_PROGRAM_CODE).build();
        program.setProgramCode(Constants.VIA_PROGRAM_CODE);
        programRepository.createOrUpdate(program);

        Product product1 = new ProductBuilder().setCode("P1").setIsActive(true).build();
        productRepository.createOrUpdate(product1);
        Product product2 = new ProductBuilder().setCode("P2").setIsActive(true).build();
        productRepository.createOrUpdate(product2);

        ProductProgram productProgram1 = new ProductProgramBuilder().setProductCode("P1").setProgramCode(Constants.VIA_PROGRAM_CODE).build();
        ProductProgram productProgram2 = new ProductProgramBuilder().setProductCode("P2").setProgramCode(Constants.VIA_PROGRAM_CODE).build();
        productProgramRepository.createOrUpdate(productProgram1);
        productProgramRepository.createOrUpdate(productProgram2);

        RnRForm form = new RnRFormBuilder().setStatus(RnRForm.STATUS.DRAFT).setProgram(program).build();
        form.setId(1L);
        List<RnrFormItem> rnrFormItemList = new ArrayList<>();

        rnrFormItemList.add(getRnrFormItem(form, product1, 1));
        rnrFormItemList.add(getRnrFormItem(form, product2, 2));

        rnrFormItemRepository.batchCreateOrUpdate(rnrFormItemList);
        rnrFormRepository.create(form);

        List<Long> productIds = rnrFormItemRepository.listAllProductIdsInCurrentVIADraft();
        assertThat(productIds.size(), is(2));
        assertThat(productIds.get(0), is(product1.getId()));
        assertThat(productIds.get(1), is(product2.getId()));
    }
}
