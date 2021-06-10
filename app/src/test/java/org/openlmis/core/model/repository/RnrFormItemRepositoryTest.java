package org.openlmis.core.model.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;
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
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class RnrFormItemRepositoryTest extends LMISRepositoryUnitTest {

  RnrFormItemRepository rnrFormItemRepository;
  private RnrFormRepository rnrFormRepository;

  ProductRepository productRepository;
  ProgramRepository programRepository;
  ProductProgramRepository productProgramRepository;

  @Before
  public void setUp() throws LMISException {
    rnrFormItemRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(RnrFormItemRepository.class);
    rnrFormRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(RnrFormRepository.class);
    productRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(ProductRepository.class);
    programRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(ProgramRepository.class);
    productProgramRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(ProductProgramRepository.class);
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

    List<RnrFormItem> rnrFormItemListFromDB = rnrFormItemRepository
        .queryListForLowStockByProductId(product);

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
    Product product1 = new ProductBuilder().setCode("P1").setIsActive(true).build();
    productRepository.createOrUpdate(product1);
    RnrFormItem rnrFormItem = new RnrFormItem();
    rnrFormItem.setProduct(product1);
    rnrFormItem.setRequestAmount(100L);
    rnrFormItem.setManualAdd(true);

    Product product2 = new ProductBuilder().setCode("P2").setIsActive(true).build();
    productRepository.createOrUpdate(product2);
    RnrFormItem rnrFormItem2 = new RnrFormItem();
    rnrFormItem2.setProduct(product2);
    rnrFormItem2.setRequestAmount(200L);
    rnrFormItemRepository.batchCreateOrUpdate(newArrayList(rnrFormItem, rnrFormItem2));

    List<RnrFormItem> rnrFormItems = rnrFormItemRepository.listAllNewRnrItems();
    assertThat(rnrFormItems.size(), is(1));
    assertThat(rnrFormItems.get(0).getProduct().getCode(), is(product1.getCode()));

    assertThat(rnrFormItemRepository.listAllNewRnrItems().size(), is(1));
  }

  @Test
  public void shouldDeleteOneRnrFormItem() throws Exception {
    Product product1 = new ProductBuilder().setCode("P1").setIsActive(true).build();
    productRepository.createOrUpdate(product1);
    RnrFormItem rnrFormItem = new RnrFormItem();
    rnrFormItem.setProduct(product1);
    rnrFormItem.setRequestAmount(100L);
    rnrFormItem.setManualAdd(true);

    Product product2 = new ProductBuilder().setCode("P2").setIsActive(true).build();
    productRepository.createOrUpdate(product2);
    RnrFormItem rnrFormItem2 = new RnrFormItem();
    rnrFormItem2.setProduct(product2);
    rnrFormItem2.setRequestAmount(200L);
    rnrFormItem2.setManualAdd(true);

    rnrFormItemRepository.batchCreateOrUpdate(newArrayList(rnrFormItem, rnrFormItem2));

    rnrFormItemRepository.deleteRnrItem(rnrFormItem);
    List<RnrFormItem> rnrFormItems = rnrFormItemRepository.listAllNewRnrItems();
    assertThat(rnrFormItems.size(), is(1));
    assertThat(rnrFormItems.get(0).getProduct().getCode(), is(product2.getCode()));

  }
}
