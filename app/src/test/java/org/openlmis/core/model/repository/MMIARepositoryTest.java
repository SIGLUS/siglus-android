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

import android.support.annotation.NonNull;

import com.google.inject.AbstractModule;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.service.PeriodService;
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
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;


@RunWith(LMISTestRunner.class)
public class MMIARepositoryTest extends LMISRepositoryUnitTest {

    ProductRepository mockProductRepository;
    MMIARepository mmiaRepository;
    StockRepository mockStockRepository;
    ProgramRepository mockProgramRepository;
    RnrFormRepository mockRnrFormRepository;
    PeriodService mockPeriodService;
    private Program program;

    @Before
    public void setup() throws LMISException {
        mockStockRepository = mock(StockRepository.class);
        mockProgramRepository = mock(ProgramRepository.class);
        mockRnrFormRepository = mock(RnrFormRepository.class);
        mockProductRepository = mock(ProductRepository.class);
        mockPeriodService = mock(PeriodService.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        mmiaRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(MMIARepository.class);

        program = new Program("ART", "ART", null);
        when(mockProgramRepository.queryByCode(anyString())).thenReturn(program);
        when(mockProductRepository.queryProducts(anyLong())).thenReturn(createProducts());
    }

    @Test
    public void shouldCalculateInfoFromStockCardByPeriod() throws Exception {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_requisition_period_logic_change, true);
        Date mockDay1 = DateUtil.parseString("2017-01-10", DateUtil.DB_DATE_FORMAT);
        Date mockDay2 = DateUtil.parseString("2017-01-15", DateUtil.DB_DATE_FORMAT);
        Date mockDay3 = DateUtil.parseString("2017-01-20", DateUtil.DB_DATE_FORMAT);
        Date mockToday = DateUtil.parseString("2017-01-22", DateUtil.DB_DATE_FORMAT);
        LMISTestApp.getInstance().setCurrentTimeMillis(mockToday.getTime());

        Product product = generateProduct();
        StockCard stockCard = generateStockCard(product);

        List<StockCard> stockCards = new ArrayList<>();
        stockCards.add(stockCard);

        StockMovementItem stockMovementItem1 = createMovementItem(StockMovementItem.MovementType.ISSUE, 10, stockCard, mockDay1, mockDay1);
        StockMovementItem stockMovementItem2 = createMovementItem(StockMovementItem.MovementType.RECEIVE, 20, stockCard, mockDay2, mockDay2);
        StockMovementItem stockMovementItem3 = createMovementItem(StockMovementItem.MovementType.POSITIVE_ADJUST, 30, stockCard, mockDay3, mockDay3);


        when(mockStockRepository.listActiveStockCardsByProgramId(anyLong())).thenReturn(stockCards);
        when(mockStockRepository.queryFirstStockMovementItem(any(StockCard.class))).thenReturn(stockMovementItem1);
        when(mockPeriodService.generateNextPeriod(anyString(), any(Date.class))).thenReturn(new Period(new DateTime("2016-12-27"), new DateTime("2017-01-20")));
        when(mockStockRepository.queryStockItemsByPeriodDates(any(StockCard.class), any(Date.class), any(Date.class)))
                .thenReturn(newArrayList(stockMovementItem1, stockMovementItem2, stockMovementItem3));

        RnRForm form = mmiaRepository.initRnrForm(null);
        assertThat(form.getRnrFormItemList().size(), is(24));

        RnrFormItem item = form.getRnrFormItemListWrapper().get(1);
        assertThat(item.getReceived(), is(20L));
        assertThat(item.getIssued(), is(10L));
        assertThat(item.getAdjustment(), is(30L));
        assertThat(item.getInitialAmount(), is(10L));
        assertThat(item.getInventory(), is(50L));
    }

    @NonNull
    private Product generateProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setPrimaryName("Test Product");
        product.setStrength("200");
        return product;
    }

    @Test
    public void shouldCalculateInfoFromStockCardByPeriodWhenFeatureToggleOff() throws Exception {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_requisition_period_logic_change, false);

        Product product = generateProduct();

        StockCard stockCard = generateStockCard(product);

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
        stockMovementItem.setCreatedTime(new Date());
        stockMovementItem.setMovementDate(DateUtil.generateRnRFormPeriodBy(new Date()).getBegin().toDate());
        when(mockStockRepository.queryFirstStockMovementItem(any(StockCard.class))).thenReturn(stockMovementItem);
        when(mockStockRepository.queryStockItems(any(StockCard.class), any(Date.class), any(Date.class))).thenReturn(stockMovementItems);

        RnRForm form = mmiaRepository.initRnrForm(null);
        assertThat(form.getRnrFormItemList().size(), is(24));

        RnrFormItem item = form.getRnrFormItemListWrapper().get(1);
        assertThat(item.getReceived(), is(25L));
        assertThat(item.getIssued(), is(20L));
    }

    @NonNull
    private StockCard generateStockCard(Product product) {
        StockCard stockCard = new StockCard();
        stockCard.setProduct(product);
        stockCard.setStockOnHand(10);
        stockCard.setCreatedAt(RnRForm.init(program, DateUtil.today()).getPeriodEnd());
        return stockCard;
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

    private StockMovementItem createMovementItem(StockMovementItem.MovementType type, long quantity, StockCard stockCard, Date createdTime, Date movementDate) throws LMISException {
        StockMovementItem stockMovementItem = new StockMovementItem();
        stockMovementItem.setMovementQuantity(quantity);
        stockMovementItem.setMovementType(type);
        stockMovementItem.setMovementDate(movementDate);
        stockMovementItem.setStockCard(stockCard);
        stockMovementItem.setCreatedTime(createdTime);

        if (stockMovementItem.isPositiveMovement()) {
            stockMovementItem.setStockOnHand(stockCard.getStockOnHand() + quantity);
        } else {
            stockMovementItem.setStockOnHand(stockCard.getStockOnHand() - quantity);
        }

        stockCard.setStockOnHand(stockMovementItem.getStockOnHand());

        return stockMovementItem;
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ProductRepository.class).toInstance(mockProductRepository);
            bind(RnrFormRepository.class).toInstance(mockRnrFormRepository);
            bind(StockRepository.class).toInstance(mockStockRepository);
            bind(ProgramRepository.class).toInstance(mockProgramRepository);
            bind(PeriodService.class).toInstance(mockPeriodService);
        }
    }
}
