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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

import com.google.inject.AbstractModule;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RegimenItemThreeLines;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.repository.MMIARepository.ReportType;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;


@RunWith(LMISTestRunner.class)
public class MMTBRepositoryTest extends LMISRepositoryUnitTest {
  ProductRepository mockProductRepository;
  MMTBRepository mmtbRepository;
  ProgramRepository mockProgramRepository;

  private Program program;

  @Before
  public void setup() throws LMISException {
    mockProductRepository = mock(ProductRepository.class);
    mockProgramRepository = mock(ProgramRepository.class);

    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
    mmtbRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(MMTBRepository.class);

    program = new Program("ART", "ART", null, false, null, null);
    when(mockProgramRepository.queryByCode(anyString())).thenReturn(program);
  }

  @Test
  public void shouldAssignMMTBTotalValuesAndCreateRnrFormItemByPeriod() {
    //given
    StockCard stockCard = new StockCard();
    Product product = new Product();
    List<StockMovementItem> stockMovementItemList = new ArrayList<>();

    product.setId(20);
    stockCard.setProduct(product);

    //when
    RnrFormItem resultNoLotRnrFormItem = mmtbRepository.createRnrFormItemByPeriod(
        stockCard, stockMovementItemList, DateUtil.getCurrentDate());

    //then
    assertNull(resultNoLotRnrFormItem.getValidate());
    assertEquals(0L, resultNoLotRnrFormItem.getReceived());
  }

  @Test
  public void shouldCreateRnrFormItemByPeriodWhenStockMovementItemsIsEmpty() {
    //given
    StockCard stockCard = new StockCard();
    StockCard noLotStockCard = new StockCard();
    StockCard withListStockCard = new StockCard();

    Product product = new Product();
    product.setId(20);
    Lot earliestLot = new Lot();
    earliestLot.setExpirationDate(
        DateUtil.parseString("Feb 2015", DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR));
    stockCard.setLotOnHandListWrapper(newArrayList(new LotOnHand(earliestLot, stockCard, 10L)));

    noLotStockCard.setProduct(product);
    withListStockCard.setProduct(product);
    stockCard.setProduct(product);

    Date currentDate = DateUtil.getCurrentDate();

    List<StockMovementItem> stockMovementItemList = newArrayList();
    StockMovementItem stockMovementItem = new StockMovementItem();
    stockMovementItem.setMovementType(MovementReasonManager.MovementType.RECEIVE);
    stockMovementItem.setMovementQuantity(10);
    long itemStockOnHand = 20;
    stockMovementItem.setStockOnHand(itemStockOnHand);
    stockMovementItem.setMovementDate(currentDate);
    stockMovementItemList.add(stockMovementItem);

    long expectedReceived = stockMovementItemList.stream().mapToLong(StockMovementItem::getMovementQuantity).sum();

    //when
    RnrFormItem resultRnrFormItem = mmtbRepository.createRnrFormItemByPeriod(stockCard, new ArrayList<>(), currentDate);
    RnrFormItem resultNoLotRnrFormItem = mmtbRepository.createRnrFormItemByPeriod(noLotStockCard, new ArrayList<>(), currentDate);
    RnrFormItem resultWithListRnrFormItem = mmtbRepository.createRnrFormItemByPeriod(withListStockCard, stockMovementItemList, currentDate);

    //then
    assertThat(resultRnrFormItem.getValidate(), is("01/02/2015"));
    assertEquals(0L, resultRnrFormItem.getReceived());
    assertNull(resultRnrFormItem.getInitialAmount());

    assertNull(resultNoLotRnrFormItem.getValidate());
    assertEquals(0L, resultNoLotRnrFormItem.getReceived());
    assertNull(resultNoLotRnrFormItem.getInitialAmount());

    assertNull(resultWithListRnrFormItem.getValidate());
    assertEquals(expectedReceived, resultWithListRnrFormItem.getReceived());
    assertEquals(itemStockOnHand, (long) resultWithListRnrFormItem.getInitialAmount());
  }
  
  @Test
  public void shouldGenerateRegimeThreeLineItems() {
    //given
    RnRForm rnRForm = new RnRForm();

    //when
    List<RegimenItemThreeLines> actualThreeLinesList = mmtbRepository.generateRegimeThreeLineItems(rnRForm);

    //then
    assertEquals(3, actualThreeLinesList.size());
  }

  @Test
  public void shouldGenerateBaseInfoItems() {
    //given
    RnRForm rnRForm = new RnRForm();

    //when
    List<BaseInfoItem> baseInfoItems = mmtbRepository.generateBaseInfoItems(rnRForm, ReportType.NEW);

    //then
    assertEquals(36, baseInfoItems.size());
  }

  @Test
  public void shouldUpdateInitialAmount() {
    //given
    RnrFormItem customAmountRnrFormItem = new RnrFormItem();
    RnrFormItem nonCustomAmountRnrFormItem = new RnrFormItem();

    //when
    final long initialAmount = 1L;
    mmtbRepository.updateInitialAmount(customAmountRnrFormItem, null);
    mmtbRepository.updateInitialAmount(nonCustomAmountRnrFormItem, initialAmount);

    //then
    assertTrue(customAmountRnrFormItem.getIsCustomAmount());
    assertNull(customAmountRnrFormItem.getInitialAmount());
    assertFalse(nonCustomAmountRnrFormItem.getIsCustomAmount());
    assertEquals(initialAmount, (long) nonCustomAmountRnrFormItem.getInitialAmount());
  }

  public class MyTestModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(ProductRepository.class).toInstance(mockProductRepository);
    }
  }
}
