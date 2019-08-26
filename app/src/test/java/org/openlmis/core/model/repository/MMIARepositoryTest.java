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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.ProductProgram;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.service.RequisitionPeriodService;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.widget.MMIARegimeList;
import org.roboguice.shaded.goole.common.collect.Lists;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;


@RunWith(LMISTestRunner.class)
public class MMIARepositoryTest extends LMISRepositoryUnitTest {

    ProductRepository mockProductRepository;
    MMIARepository mmiaRepository;
    StockRepository mockStockRepository;
    ProgramRepository mockProgramRepository;
    ProductProgramRepository productProgramRepository;
    RequisitionPeriodService mockRequisitionPeriodService;
    private Program program;
    RegimenItemRepository regimenItemRepository;
    private StockMovementRepository mockStockMovementRepository;

    @Before
    public void setup() throws LMISException {
        mockStockRepository = mock(StockRepository.class);
        mockProgramRepository = mock(ProgramRepository.class);
        mockProductRepository = mock(ProductRepository.class);
        mockRequisitionPeriodService = mock(RequisitionPeriodService.class);
        productProgramRepository = mock(ProductProgramRepository.class);
        regimenItemRepository = mock(RegimenItemRepository.class);
        mockStockMovementRepository = mock(StockMovementRepository.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        mmiaRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(MMIARepository.class);

        program = new Program("ART", "ART", null, false, null, null);
        when(mockProgramRepository.queryByCode(anyString())).thenReturn(program);
    }

    @Ignore
    @Test
    public void shouldCalculateInfoFromStockCardByPeriod() throws Exception {
        Date mockDay1 = DateUtil.parseString("2017-01-10", DateUtil.DB_DATE_FORMAT);
        Date mockDay2 = DateUtil.parseString("2017-01-15", DateUtil.DB_DATE_FORMAT);
        Date mockDay3 = DateUtil.parseString("2017-01-20", DateUtil.DB_DATE_FORMAT);
        Date mockToday = DateUtil.parseString("2017-01-22", DateUtil.DB_DATE_FORMAT);
        LMISTestApp.getInstance().setCurrentTimeMillis(mockToday.getTime());

        Product product = generateProduct();
        StockCard stockCard = generateStockCard(product);

        List<StockCard> stockCards = new ArrayList<>();
        stockCards.add(stockCard);

        StockMovementItem stockMovementItem1 = createMovementItem(MovementReasonManager.MovementType.ISSUE, 10, stockCard, mockDay1, mockDay1);
        StockMovementItem stockMovementItem2 = createMovementItem(MovementReasonManager.MovementType.RECEIVE, 20, stockCard, mockDay2, mockDay2);
        StockMovementItem stockMovementItem3 = createMovementItem(MovementReasonManager.MovementType.POSITIVE_ADJUST, 30, stockCard, mockDay3, mockDay3);

        when(mockRequisitionPeriodService.generateNextPeriod(anyString(), any(Date.class))).thenReturn(new Period(new DateTime("2016-12-27"), new DateTime("2017-01-20")));
        when(mockStockMovementRepository.queryStockItemsByCreatedDate(anyLong(), any(Date.class), any(Date.class)))
                .thenReturn(newArrayList(stockMovementItem1, stockMovementItem2, stockMovementItem3));
        when(mockStockRepository.getStockCardsBeforePeriodEnd(any(RnRForm.class))).thenReturn(stockCards);

        ProductProgram productProgram = new ProductProgram();
        productProgram.setCategory("Adult");
        when(productProgramRepository.queryByCode(anyString(), anyList())).thenReturn(productProgram);
        List<String> mmiaCodes = newArrayList("MMIA");
        when(mockProgramRepository.queryProgramCodesByProgramCodeOrParentCode(anyString())).thenReturn(mmiaCodes);
        List<Long> mmiaProductIds = new ArrayList<>();
        when(productProgramRepository.queryActiveProductIdsByProgramsWithKits(mmiaCodes, false)).thenReturn(mmiaProductIds);
        Product someProduct = ProductBuilder.buildAdultProduct();
        when(mockProductRepository.queryProductsByProductIds(mmiaProductIds)).thenReturn(newArrayList(product, someProduct));

        RnRForm form = mmiaRepository.initNormalRnrForm(null);
        assertThat(form.getRnrFormItemList().size(), is(2));
        RnrFormItem item = form.getRnrFormItemListWrapper().get(0);
        assertThat(item.getReceived(), is(20L));
        assertThat(item.getInitialAmount(), is(0L));
    }

    @NonNull
    private Product generateProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setCode("ABC");
        product.setPrimaryName("Test Product");
        product.setStrength("200");
        return product;
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
        when(mockRequisitionPeriodService.generateNextPeriod(anyString(), any(Date.class))).thenReturn(new Period(new DateTime("2016-12-27"), new DateTime("2017-01-20")));

        RnRForm initForm = mmiaRepository.initNormalRnrForm(null);
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
        mmiaRepository.createOrUpdateWithItems(initForm);

        List<RnRForm> list = mmiaRepository.list();
        RnRForm DBForm = list.get(list.size() - 1);

        long expectRegimeTotal = RnRForm.calculateTotalRegimenAmount(initForm.getRegimenItemListWrapper(), MMIARegimeList.COUNTTYPE.AMOUNT);
        long regimenTotal = RnRForm.calculateTotalRegimenAmount(DBForm.getRegimenItemListWrapper(), MMIARegimeList.COUNTTYPE.AMOUNT);
        assertThat(expectRegimeTotal, is(regimenTotal));

        assertThat(mmiaRepository.getTotalPatients(initForm), is(mmiaRepository.getTotalPatients(DBForm)));
    }

    @Test
    public void shouldGenerateBaseInfoItems() throws Exception {
        RnRForm rnRForm = new RnRForm();
        List<BaseInfoItem> baseInfoItems = mmiaRepository.generateBaseInfoItems(rnRForm, MMIARepository.ReportType.OLD);
        assertThat(baseInfoItems.size(), is(7));
        assertThat(baseInfoItems.get(0).getName(), is(mmiaRepository.ATTR_NEW_PATIENTS));
        assertThat(baseInfoItems.get(3).getName(), is(mmiaRepository.ATTR_PTV));
        assertThat(baseInfoItems.get(baseInfoItems.size() - 1).getName(), is(mmiaRepository.ATTR_TOTAL_PATIENTS));
    }

    @Test
    public void shouldInitRnrFormItemWithoutMovement() throws Exception {
        mmiaRepository = spy(mmiaRepository);

        Product product = new Product();
        RnRForm form = new RnRForm();
        RnrFormItem rnrFormItem = new RnrFormItem();
        rnrFormItem.setInventory(100L);
        rnrFormItem.setProduct(product);
        form.setRnrFormItemListWrapper(newArrayList(rnrFormItem));
        doReturn(newArrayList(form)).when(mmiaRepository).listInclude(any(RnRForm.Emergency.class), anyString());

        StockCard stockCard = new StockCard();
        product.setId(20);
        stockCard.setProduct(product);
        Lot lot = new Lot();
        lot.setExpirationDate(DateUtil.parseString("Feb 2015", DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR));
        stockCard.setLotOnHandListWrapper(newArrayList(new LotOnHand(lot, stockCard, 10L)));
        when(mockStockMovementRepository.queryStockMovementsByMovementDate(anyLong(), any(Date.class), any(Date.class))).thenReturn(new ArrayList<StockMovementItem>());

        RnrFormItem rnrFormItemByPeriod = mmiaRepository.createRnrFormItemByPeriod(stockCard, new Date(), new Date());

        assertThat(rnrFormItemByPeriod.getValidate(), is("01/02/2015"));
        assertThat(rnrFormItemByPeriod.getCalculatedOrderQuantity(), is(0L));
        assertThat(rnrFormItemByPeriod.getInitialAmount(), is(0L));

        stockCard.setLotOnHandListWrapper(Lists.<LotOnHand>newArrayList());
        rnrFormItemByPeriod = mmiaRepository.createRnrFormItemByPeriod(stockCard, new Date(), new Date());
        assertNull(rnrFormItemByPeriod.getValidate());
    }

    private StockMovementItem createMovementItem(MovementReasonManager.MovementType type, long quantity, StockCard stockCard, Date createdTime, Date movementDate) throws LMISException {
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
            bind(StockRepository.class).toInstance(mockStockRepository);
            bind(ProgramRepository.class).toInstance(mockProgramRepository);
            bind(RequisitionPeriodService.class).toInstance(mockRequisitionPeriodService);
            bind(ProductProgramRepository.class).toInstance(productProgramRepository);
            bind(RegimenItemRepository.class).toInstance(regimenItemRepository);
            bind(StockMovementRepository.class).toInstance(mockStockMovementRepository);
        }
    }
}
