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

import com.google.inject.AbstractModule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockItem;
import org.robolectric.Robolectric;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(LMISTestRunner.class)
public class MMIARepositoryTest extends LMISRepositoryUnitTest {

    ProductRepository productRepository;
    MMIARepository MMIARepository;
    StockRepository mockStockRepository;
    ProgramRepository mockProgramRepository;

    Product product;

    @Before
    public void setup() throws LMISException {
        mockStockRepository = mock(StockRepository.class);
        mockProgramRepository = mock(ProgramRepository.class);

        RoboGuice.overrideApplicationInjector(Robolectric.application, new MyTestModule());

        MMIARepository = RoboGuice.getInjector(Robolectric.application).getInstance(MMIARepository.class);
        productRepository = RoboGuice.getInjector(Robolectric.application).getInstance(ProductRepository.class);

        product = new Product();
        product.setPrimaryName("Test Product");
        product.setStrength("200");

        productRepository.create(product);

        when(mockProgramRepository.queryByCode(anyString())).thenReturn(new Program("ART", "ART", null));
    }

    @Test
    public void shouldCalculateInfoFromStockCardByPeriod() throws Exception {
        ArrayList<StockCard> stockCards = new ArrayList<>();
        StockCard stockCard = new StockCard();
        stockCard.setProduct(product);
        stockCard.setStockOnHand(10);
        stockCards.add(stockCard);

        ArrayList<StockItem> stockItems = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            StockItem stockItem = new StockItem();
            stockItem.setStockOnHand(100);
            stockItem.setAmount(i);
            stockItem.setStockCard(stockCard);
            if (i % 2 == 0) {
                stockItem.setMovementType(StockItem.MovementType.ISSUE);
            } else {
                stockItem.setMovementType(StockItem.MovementType.RECEIVE);
            }
            stockItems.add(stockItem);
        }

        when(mockStockRepository.list(anyString())).thenReturn(stockCards);
        when(mockStockRepository.queryStockItems(any(StockCard.class), any(Date.class), any(Date.class))).thenReturn(stockItems);

        RnRForm form = MMIARepository.initMIMIA();
        assertThat(form.getRnrFormItemList().size(), is(1));

        for (RnrFormItem item : form.getRnrFormItemList()) {
            assertThat(item.getReceived(), is(25L));
            assertThat(item.getIssued(), is(20L));
        }
    }


    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(StockRepository.class).toInstance(mockStockRepository);
            bind(ProgramRepository.class).toInstance(mockProgramRepository);
        }
    }

    @Test
    public void shouldSaveSuccess() throws Exception {
        RnRForm initForm = MMIARepository.initMIMIA();
        ArrayList<RegimenItem> regimenItemListWrapper = initForm.getRegimenItemListWrapper();

        for (int i = 0; i < regimenItemListWrapper.size(); i++) {
            RegimenItem item = regimenItemListWrapper.get(i);
            item.setAmount((long)i);
        }

        ArrayList<BaseInfoItem> baseInfoItemListWrapper = initForm.getBaseInfoItemListWrapper();
        for (int i = 0; i < baseInfoItemListWrapper.size(); i++) {
            BaseInfoItem item = baseInfoItemListWrapper.get(i);
            item.setValue(String.valueOf(i));
        }
        MMIARepository.save(initForm);

        List<RnRForm> list = MMIARepository.list();
        RnRForm DBForm = list.get(list.size() - 1);

        long expectRegimeTotal = RnRForm.getRegimenItemListAmount(initForm.getRegimenItemListWrapper());
        long regimenTotal = RnRForm.getRegimenItemListAmount(DBForm.getRegimenItemListWrapper());
        assertThat(expectRegimeTotal, is(regimenTotal));

        assertThat(MMIARepository.getTotalPatients(initForm), is(MMIARepository.getTotalPatients(DBForm)));
    }
}
