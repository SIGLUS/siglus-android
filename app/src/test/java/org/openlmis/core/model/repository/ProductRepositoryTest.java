/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

package org.openlmis.core.model.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.KitProduct;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Product.IsKit;
import org.openlmis.core.model.ProductProgram;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.KitProductBuilder;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.ProductProgramBuilder;
import org.openlmis.core.model.builder.ProgramBuilder;
import org.openlmis.core.model.builder.RnRFormBuilder;
import org.openlmis.core.model.builder.RnrFormItemBuilder;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;
import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class ProductRepositoryTest extends LMISRepositoryUnitTest {

    private ProductRepository productRepository;

    private ProductProgramRepository productProgramRepository;

    private ProgramRepository programRepository;

    private RnrFormItemRepository rnrFormItemRepository;

    private RnrFormRepository rnrFormRepository;

    private StockRepository stockRepository;

    @Before
    public void setUp() throws Exception {
        stockRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(StockRepository.class);
        productRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ProductRepository.class);
        productProgramRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ProductProgramRepository.class);
        programRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ProgramRepository.class);
        rnrFormItemRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(RnrFormItemRepository.class);
        rnrFormRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(RnrFormRepository.class);
    }

    @Test
    public void shouldGetActiveProducts() throws Exception {
        Product product1 = ProductBuilder.create().setCode("P1").setIsActive(true).build();
        Product product2 = ProductBuilder.create().setCode("P2").setIsActive(false).build();
        Product product3 = ProductBuilder.create().setCode("P3").setIsActive(true).build();

        productRepository.createOrUpdate(product1);
        productRepository.createOrUpdate(product2);
        productRepository.createOrUpdate(product3);

        List<Product> activeProducts = productRepository.listActiveProducts(IsKit.No);

        assertEquals(2, activeProducts.size());
    }

    @Test
    public void shouldGetProductsNotArchivedOrNotInStockCard() throws Exception {
        Product product1 = ProductBuilder.create().setCode("P1").setIsActive(true).setIsKit(true).setIsArchived(true).build();
        Product product2 = ProductBuilder.create().setCode("P2").setIsActive(false).setIsKit(false).setIsArchived(true).build();
        Product product3 = ProductBuilder.create().setCode("P3").setIsActive(true).setIsKit(false).setIsArchived(true).build();
        productRepository.createOrUpdate(product1);
        productRepository.createOrUpdate(product2);
        productRepository.createOrUpdate(product3);
        List<Product> products1 = productRepository.listProductsArchivedOrNotInStockCard();

        assertTrue(products1.get(0).isArchived());
        assertEquals(1, products1.size());

        Product product4 = ProductBuilder.create().setCode("P4").setIsActive(true).setIsKit(false).setIsArchived(false).build();
        productRepository.createOrUpdate(product4);
        List<Product> products2 = productRepository.listProductsArchivedOrNotInStockCard();

        assertEquals(2, products2.size());

        StockCard stockCard = new StockCard();
        stockCard.setProduct(product4);
        stockRepository.createOrUpdate(stockCard);
        List<Product> products3 = productRepository.listProductsArchivedOrNotInStockCard();

        assertEquals(1, products3.size());
    }

    @Test
    public void shouldGetKits() throws Exception {
        Product kitProduct = ProductBuilder.create().setCode("kitCode1").setIsActive(true).setIsKit(true).build();

        productRepository.createOrUpdate(kitProduct);

        List<Product> kits = productRepository.listActiveProducts(IsKit.Yes);

        assertEquals(1, kits.size());
        assertEquals("kitCode1", kits.get(0).getCode());
    }

    @Test
    public void shouldGetProductsByKitCode() throws Exception {
        Product kit = ProductBuilder.create().setCode("KIT_Code").setIsKit(true).build();

        KitProduct kitProduct1 = KitProductBuilder.create().setKitCode("KIT_Code").setProductCode("P1_Code").build();
        KitProduct kitProduct2 = KitProductBuilder.create().setKitCode("KIT_Code").setProductCode("P2_Code").build();
        KitProduct kitProduct3 = KitProductBuilder.create().setKitCode("KIT_Code").setProductCode("P3_Code").build();

        List<KitProduct> kitProducts = Arrays.asList(kitProduct1, kitProduct2, kitProduct3);
        kit.setKitProductList(kitProducts);

        productRepository.batchCreateOrUpdateProducts(Arrays.asList(kit));

        List<KitProduct> result = productRepository.queryKitProductByKitCode(kit.getCode());
        assertEquals(result.size(), 3);
        assertEquals(result.get(0).getProductCode(), kitProduct1.getProductCode());
        assertEquals(result.get(1).getProductCode(), kitProduct2.getProductCode());
        assertEquals(result.get(2).getProductCode(), kitProduct3.getProductCode());
    }

    @Test
    public void shouldUpdateWithExistingArchivedStatusForProduct() throws LMISException {
        Product existingProduct = ProductBuilder.create().setCode("P1").setIsActive(true).setIsArchived(true).build();
        productRepository.createOrUpdate(existingProduct);

        Product updatedProduct = ProductBuilder.create().setCode("P1").setIsActive(true).setIsArchived(false).build();
        productRepository.createOrUpdate(updatedProduct);
        assertTrue(productRepository.getByCode("P1").isArchived());
    }

    @Test
    public void shouldCreateKitProductsIfTheyDontExist() throws LMISException {
        ProductBuilder.create().setCode("P1").setIsActive(true).setIsArchived(true).build();
        Product kit = ProductBuilder.create().setCode("KIT").setIsActive(true).setIsArchived(true).build();
        KitProduct kitProduct1 = new KitProductBuilder().setProductCode("P1").setKitCode("KIT").setQuantity(100).build();
        kit.setKitProductList(newArrayList(kitProduct1));
        productRepository.createOrUpdate(kit);

        assertNotNull(productRepository.queryKitProductByCode("KIT", "P1"));
    }

    @Test
    public void shouldCreateProductIfNotExistYetButExistInKit() throws LMISException {
        Product kit = ProductBuilder.create().setCode("KIT").setIsActive(true).setIsArchived(true).build();
        KitProduct kitProduct1 = new KitProductBuilder().setProductCode("P1").setKitCode("KIT").setQuantity(100).build();
        kit.setKitProductList(newArrayList(kitProduct1));
        productRepository.createOrUpdate(kit);

        assertNotNull(productRepository.getByCode("P1"));
        assertNotNull(productRepository.queryKitProductByCode("KIT", "P1"));
    }

    @Test
    public void shouldGetKitProductByProductCode() throws Exception {
        ProductBuilder.create().setCode("P1").setIsActive(true).setIsArchived(true).build();
        Product kit = ProductBuilder.create().setCode("KIT").setIsActive(true).setIsArchived(true).build();
        KitProduct kitProduct1 = new KitProductBuilder().setProductCode("P1").setKitCode("KIT").setQuantity(100).build();
        kit.setKitProductList(newArrayList(kitProduct1));
        productRepository.createOrUpdate(kit);

        List<KitProduct> kitProducts = productRepository.queryKitProductByProductCode("P1");

        assertThat(kitProducts.size(), is(1));
    }

    @Test
    public void shouldGetArchivedProducts() throws Exception {
        Product product1 = ProductBuilder.create().setCode("P1").setIsArchived(true).build();
        Product product2 = ProductBuilder.create().setCode("P2").setIsArchived(false).build();
        Product product3 = ProductBuilder.create().setCode("P3").setIsArchived(true).build();

        productRepository.createOrUpdate(product1);
        productRepository.createOrUpdate(product2);
        productRepository.createOrUpdate(product3);

        List<String> activeProductCodes = productRepository.listArchivedProductCodes();

        assertEquals(2, activeProductCodes.size());
    }

    @Test
    public void shouldQueryActiveProductsByCodesWithKits() throws Exception {
        createSeveralProducts();

        List<Product> queriedProducts = productRepository.queryActiveProductsByCodesWithKits(Arrays.asList("08A01", "08A02", "08A03", "08A04"), true);

        assertEquals(3, queriedProducts.size());
    }

    private void createSeveralProducts() throws LMISException {
        productRepository.createOrUpdate(ProductBuilder.create().setCode("08A01").setIsActive(true).setIsKit(true).setIsBasic(true).build());
        productRepository.createOrUpdate(ProductBuilder.create().setCode("08A02").setIsActive(true).setIsKit(false).setIsBasic(false).build());
        productRepository.createOrUpdate(ProductBuilder.create().setCode("08A03").setIsActive(false).setIsKit(true).setIsBasic(true).build());
        productRepository.createOrUpdate(ProductBuilder.create().setCode("08A04").setIsActive(true).setIsKit(false).setIsBasic(false).build());
    }

    @Test
    public void shouldQueryActiveProductsByCodesWithoutKits() throws Exception {

        createSeveralProducts();

        List<Product> queriedProducts = productRepository.queryActiveProductsByCodesWithKits(Arrays.asList("08A01", "08A02", "08A03", "08A04"), false);

        assertEquals(2, queriedProducts.size());
    }

    @Test
    public void shouldQueryProductsByProductIds() throws Exception {
        productRepository.createOrUpdate(ProductBuilder.create().setCode("08A01").setIsActive(true).setIsKit(true).build());
        productRepository.createOrUpdate(ProductBuilder.create().setCode("08A02").setIsActive(true).setIsKit(false).build());
        productRepository.createOrUpdate(ProductBuilder.create().setCode("08A03").setIsActive(false).setIsKit(true).build());
        productRepository.createOrUpdate(ProductBuilder.create().setCode("08A04").setIsActive(true).setIsKit(false).build());

        Product product1 = productRepository.getByCode("08A01");
        Product product2 = productRepository.getByCode("08A03");


        List<Product> queriedProducts = productRepository.queryProductsByProductIds(Arrays.asList(product1.getId(), product2.getId()));

        assertEquals(2, queriedProducts.size());
    }

    @Test
    public void shouldQueryActiveProductsInVIAProgramButNotInDraftVIAForm() throws Exception {
        Program parentProgram = new ProgramBuilder().setProgramCode("VIA").setParentCode(null).build();
        programRepository.createOrUpdate(parentProgram);
        Program program1 = new ProgramBuilder().setProgramCode("PR1").setParentCode("VIA").build();
        programRepository.createOrUpdate(program1);
        Program program2 = new ProgramBuilder().setProgramCode("PR2").setParentCode(null).build();
        programRepository.createOrUpdate(program2);

        RnRForm rnRForm = new RnRFormBuilder().setProgram(parentProgram).setStatus(RnRForm.STATUS.DRAFT).build();
        rnrFormRepository.create(rnRForm);

        //should not be in list
        Product productInVIA = createProduct("P1", true, false, true);
        ProductProgram productProgram = new ProductProgramBuilder().setProductCode(productInVIA.getCode()).setProgramCode(program1.getProgramCode()).setActive(true).build();
        productProgramRepository.createOrUpdate(productProgram);
        RnrFormItem rnrFormItem = new RnrFormItemBuilder().setProduct(productInVIA).setRnrForm(rnRForm).build();

        //should not be in list
        Product kitProduct = createProduct("P2", false, true, true);
        ProductProgram productProgram2 = new ProductProgramBuilder().setProductCode(kitProduct.getCode()).setProgramCode(program1.getProgramCode()).setActive(true).build();
        productProgramRepository.createOrUpdate(productProgram2);

        //should not be in list
        Product inactiveProduct = createProduct("P3", false, true, false);
        ProductProgram productProgram3 = new ProductProgramBuilder().setProductCode(inactiveProduct.getCode()).setProgramCode(program1.getProgramCode()).setActive(true).build();
        productProgramRepository.createOrUpdate(productProgram3);

        //should not be in list
        Product mmiaProduct = createProduct("P4", false, false, true);
        ProductProgram productProgram4 = new ProductProgramBuilder().setProductCode(mmiaProduct.getCode()).setProgramCode(program2.getProgramCode()).setActive(true).build();
        productProgramRepository.createOrUpdate(productProgram4);

        //should be in list
        Product activeVIAProductNotInForm = createProduct("P5", false, false, true);
        ProductProgram productProgram5 = new ProductProgramBuilder().setProductCode(activeVIAProductNotInForm.getCode()).setProgramCode(program1.getProgramCode()).setActive(true).build();
        productProgramRepository.createOrUpdate(productProgram5);

        //should not be in list
        Product productAddedAsAdditional = createProduct("P6", false, false, true);
        ProductProgram productProgram6 = new ProductProgramBuilder().setProductCode(productAddedAsAdditional.getCode()).setProgramCode(program1.getProgramCode()).setActive(true).build();
        productProgramRepository.createOrUpdate(productProgram6);
        RnrFormItem rnrFormItem2 = new RnrFormItemBuilder().setProduct(productAddedAsAdditional).setRnrForm(rnRForm).build();

        //should be in list
        Product archivedVIAProductNotInForm = createProduct("P7", true, false, true);
        ProductProgram productProgram7 = new ProductProgramBuilder().setProductCode(archivedVIAProductNotInForm.getCode()).setProgramCode(program1.getProgramCode()).setActive(true).build();
        productProgramRepository.createOrUpdate(productProgram7);

        rnrFormItemRepository.batchCreateOrUpdate(newArrayList(rnrFormItem, rnrFormItem2));

        List<Product> products = productRepository.queryActiveProductsInVIAProgramButNotInDraftVIAForm();
        assertThat(products.size(), is(2));
        assertThat(products.get(0).getCode(), is("P5"));
        assertThat(products.get(0).isArchived(), is(false));
        assertThat(products.get(0).getId(), is(activeVIAProductNotInForm.getId()));
        assertThat(products.get(1).getCode(), is("P7"));
        assertThat(products.get(1).isArchived(), is(true));
        assertThat(products.get(1).getId(), is(archivedVIAProductNotInForm.getId()));

    }

    @Test
    public void shouldReturnAListOfBasicProducts() throws LMISException {

        createSeveralProducts();

        List<Product> basicProducts = productRepository.listBasicProducts();

        assertEquals(1, basicProducts.size());
        assertEquals("08A01", basicProducts.get(0).getCode());
    }

    @Test
    public void shouldReturnAListOfNonBasicProducts() throws Exception {
        createSeveralProducts();

        List<Product> nonBasicProducts = productRepository.listNonBasicProducts();

        assertThat(nonBasicProducts.size(), is(2));
        assertThat(nonBasicProducts.get(0).getCode(), is("08A02"));
        assertThat(nonBasicProducts.get(1).getCode(), is("08A04"));
    }

    private Product createProduct(String code, boolean archived, boolean isKit, boolean active) throws LMISException {
        Product productInVIA = new ProductBuilder().setCode(code)
                .setPrimaryName("product 1").setIsArchived(archived).setIsKit(isKit)
                .setStrength("100").setType("type").setIsActive(active).build();
        productRepository.createOrUpdate(productInVIA);
        return productInVIA;
    }
}