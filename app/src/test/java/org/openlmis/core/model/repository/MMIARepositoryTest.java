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
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(LMISTestRunner.class)
public class MMIARepositoryTest extends LMISRepositoryUnitTest {

    ProductRepository mockProductRepository;
    MMIARepository mmiaRepository;
    StockRepository mockStockRepository;
    ProgramRepository mockProgramRepository;
    RnrFormRepository mockRnrFormRepository;
    private Program program;

    @Before
    public void setup() throws LMISException {
        mockStockRepository = mock(StockRepository.class);
        mockProgramRepository = mock(ProgramRepository.class);
        mockRnrFormRepository = mock(RnrFormRepository.class);
        mockProductRepository = mock(ProductRepository.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        mmiaRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(MMIARepository.class);

        program = new Program("ART", "ART", null);
        when(mockProgramRepository.queryByCode(anyString())).thenReturn(program);
        when(mockProductRepository.queryProducts(anyLong())).thenReturn(createProducts());
    }

    @Test
    public void shouldCalculateInfoFromStockCardByPeriod() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setPrimaryName("Test Product");
        product.setStrength("200");

        StockCard stockCard = new StockCard();
        stockCard.setProduct(product);
        stockCard.setStockOnHand(10);
        stockCard.setCreatedAt(RnRForm.init(program, DateUtil.today()).getPeriodEnd());

        ArrayList<StockCard> stockCards = new ArrayList<>();
        stockCards.add(stockCard);

        ArrayList<StockMovementItem> stockMovementItems = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            StockMovementItem stockMovementItem = new StockMovementItem();
            stockMovementItem.setStockOnHand(100);
            stockMovementItem.setMovementQuantity(i);
            stockMovementItem.setStockCard(stockCard);
            if (i % 2 == 0) {
                stockMovementItem.setMovementType(StockMovementItem.MovementType.ISSUE);
            } else {
                stockMovementItem.setMovementType(StockMovementItem.MovementType.RECEIVE);
            }
            stockMovementItems.add(stockMovementItem);
        }

        when(mockStockRepository.listActiveStockCardsByProgramId(anyLong())).thenReturn(stockCards);
        StockMovementItem stockMovementItem = new StockMovementItem();
        stockMovementItem.setMovementDate(DateUtil.generateRnRFormPeriodBy(new Date()).getBegin().toDate());
        when(mockStockRepository.queryFirstStockMovementItem(any(StockCard.class))).thenReturn(stockMovementItem);
        when(mockStockRepository.queryStockItems(any(StockCard.class), any(Date.class), any(Date.class))).thenReturn(stockMovementItems);

        RnRForm form = mmiaRepository.initRnrForm(null);
        assertThat(form.getRnrFormItemList().size(), is(24));

        RnrFormItem item = form.getRnrFormItemListWrapper().get(1);
        assertThat(item.getReceived(), is(25L));
        assertThat(item.getIssued(), is(20L));
    }

    @Test
    public void shouldSaveSuccess() throws Exception {
        RnRForm initForm = mmiaRepository.initRnrForm(null);
        List<RegimenItem> regimenItemListWrapper = initForm.getRegimenItemListWrapper();

        for (int i = 0; i < regimenItemListWrapper.size(); i++) {
            RegimenItem item = regimenItemListWrapper.get(i);
            item.setAmount((long) i);
        }

        List<BaseInfoItem> baseInfoItemListWrapper = initForm.getBaseInfoItemListWrapper();
        for (int i = 0; i < baseInfoItemListWrapper.size(); i++) {
            BaseInfoItem item = baseInfoItemListWrapper.get(i);
            item.setValue(String.valueOf(i));
        }
        mmiaRepository.save(initForm);

        List<RnRForm> list = mmiaRepository.list();
        RnRForm DBForm = list.get(list.size() - 1);

        long expectRegimeTotal = RnRForm.calculateTotalRegimenAmount(initForm.getRegimenItemListWrapper());
        long regimenTotal = RnRForm.calculateTotalRegimenAmount(DBForm.getRegimenItemListWrapper());
        assertThat(expectRegimeTotal, is(regimenTotal));

        assertThat(mmiaRepository.getTotalPatients(initForm), is(mmiaRepository.getTotalPatients(DBForm)));
    }

    @Test
    public void shouldInflateMMIAProducts() throws Exception {
        Program program = new Program();
        program.setProgramCode(org.openlmis.core.model.repository.MMIARepository.MMIA_PROGRAM_CODE);

        RnRForm rnRForm = new RnRForm();
        rnRForm.setProgram(program);

        when(mockRnrFormRepository.initRnrForm(null)).thenReturn(rnRForm);

        RnRForm rnRFormTest = mmiaRepository.initRnrForm(null);

        assertThat(rnRFormTest.getRnrFormItemListWrapper().size(), is(24));
    }

    @Test
    public void shouldGenerateBaseInfoItems() throws Exception {
        RnRForm rnRForm = new RnRForm();
        List<BaseInfoItem> baseInfoItems = mmiaRepository.generateBaseInfoItems(rnRForm);
        assertThat(baseInfoItems.size(), is(7));
        assertThat(baseInfoItems.get(0).getName(), is(mmiaRepository.ATTR_NEW_PATIENTS));
        assertThat(baseInfoItems.get(3).getName(), is(mmiaRepository.ATTR_PTV));
        assertThat(baseInfoItems.get(baseInfoItems.size() - 1).getName(), is(mmiaRepository.ATTR_TOTAL_PATIENTS));
    }

    private ArrayList<Product> createProducts() {
        ArrayList<Product> products = new ArrayList<>();

        for (int i = 0; i < 24; i++) {
            Product product = new Product();
            product.setId(i);
            product.setProgram(program);
            product.setPrimaryName("mockProduct");
            products.add(product);
        }
        return products;
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ProductRepository.class).toInstance(mockProductRepository);
            bind(RnrFormRepository.class).toInstance(mockRnrFormRepository);
            bind(StockRepository.class).toInstance(mockStockRepository);
            bind(ProgramRepository.class).toInstance(mockProgramRepository);
        }
    }
}
