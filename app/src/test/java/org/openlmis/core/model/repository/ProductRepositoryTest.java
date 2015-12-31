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
import org.openlmis.core.model.Product;
import org.openlmis.core.model.builder.ProductBuilder;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

import roboguice.RoboGuice;

import static org.junit.Assert.*;

@RunWith(LMISTestRunner.class)
public class ProductRepositoryTest extends LMISRepositoryUnitTest {

    private ProductRepository productRepository;

    @Before
    public void setUp() throws Exception {
        productRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(ProductRepository.class);
    }

    @Test
    public void shouldGetActiveProducts() throws Exception {
        Product product1 = ProductBuilder.create().setCode("P1").setActive(true).build();
        Product product2 = ProductBuilder.create().setCode("P2").setActive(false).build();
        Product product3 = ProductBuilder.create().setCode("P3").setActive(true).build();

        productRepository.createOrUpdate(product1);
        productRepository.createOrUpdate(product2);
        productRepository.createOrUpdate(product3);

        List<Product> activeProducts = productRepository.listActiveProducts();

        assertEquals(2, activeProducts.size());
    }
}