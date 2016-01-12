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
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.KitProduct;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.KitProductBuilder;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.repository.ProductRepository.IsKit;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

import roboguice.RoboGuice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class ProductRepositoryTest extends LMISRepositoryUnitTest {

    private ProductRepository productRepository;

    private SharedPreferenceMgr sharedPreferenceMgr;

    private StockRepository stockRepository;

    @Before
    public void setUp() throws Exception {
        sharedPreferenceMgr = mock(SharedPreferenceMgr.class);
        stockRepository = mock(StockRepository.class);
        productRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ProductRepository.class);
        productRepository.sharedPreferenceMgr = sharedPreferenceMgr;
        productRepository.stockRepository = stockRepository;
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
    public void shouldGetKits() throws Exception {
        Product kitProduct = ProductBuilder.create().setCode("kitCode1").setIsActive(true).setIsKit(true).build();

        productRepository.createOrUpdate(kitProduct);

        List<Product> kits = productRepository.listActiveProducts(IsKit.Yes);

        assertEquals(1, kits.size());
        assertEquals("kitCode1", kits.get(0).getCode());
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
    public void shouldUpdateNotifyBannerListWhenSOHIsZeroAndProductIsDeactiveAndSOHIsZero() throws Exception {
        //given
        Product existingProduct = ProductBuilder.create().setCode("code").setIsActive(true).setIsArchived(true).build();
        productRepository.createOrUpdate(existingProduct);

        Product product = new Product();
        product.setPrimaryName("name");
        product.setActive(false);
        product.setCode("code");

        StockCard stockCard = new StockCard();
        stockCard.setStockOnHand(0);
        when(stockRepository.queryStockCardByProductId(anyLong())).thenReturn(stockCard);

        //when
        productRepository.createOrUpdate(product);

        //then
        verify(sharedPreferenceMgr).setIsNeedShowProductsUpdateBanner(true, "name");
    }

    @Test
    public void shouldRemoveNotifyBannerListWhenReactiveProduct() throws Exception {
        //given
        Product existingProduct = ProductBuilder.create().setCode("code").setIsActive(false).setPrimaryName("name").build();
        productRepository.createOrUpdate(existingProduct);

        Product product = new Product();
        product.setPrimaryName("new name");
        product.setActive(true);
        product.setCode("code");

        StockCard stockCard = new StockCard();
        stockCard.setStockOnHand(0);
        when(stockRepository.queryStockCardByProductId(anyLong())).thenReturn(stockCard);

        //when
        productRepository.createOrUpdate(product);

        //then
        verify(sharedPreferenceMgr).removeShowUpdateBannerTextWhenReactiveProduct("name");
    }

}