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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

import com.google.inject.AbstractModule;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.ProgramBuilder;
import org.openlmis.core.model.service.RequisitionPeriodService;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class RapidTestRepositoryTest extends LMISRepositoryUnitTest {

  RapidTestRepository rapidTestRepository;

  ProductRepository mockProductRepository;
  MMIARepository mmiaRepository;
  StockRepository mockStockRepository;
  ProgramRepository mockProgramRepository;
  ProductProgramRepository productProgramRepository;
  RequisitionPeriodService mockRequisitionPeriodService;
  RegimenItemRepository regimenItemRepository;
  private StockMovementRepository mockStockMovementRepository;
  private ReportTypeFormRepository mockReportTypeFormRepository;

  @Before
  public void setup() throws LMISException {
    mockStockRepository = mock(StockRepository.class);
    mockProgramRepository = mock(ProgramRepository.class);
    mockProductRepository = mock(ProductRepository.class);
    mockRequisitionPeriodService = mock(RequisitionPeriodService.class);
    productProgramRepository = mock(ProductProgramRepository.class);
    regimenItemRepository = mock(RegimenItemRepository.class);
    mockStockMovementRepository = mock(StockMovementRepository.class);
    mockReportTypeFormRepository = mock(ReportTypeFormRepository.class);

    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
    mmiaRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(MMIARepository.class);
    rapidTestRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(RapidTestRepository.class);
  }

  @Test
  public void shouldCreateRnrFormItemByPeriod() throws LMISException {
    //given
    StockCard stockCard = new StockCard();
    StockCard noExpiryDateStockCard = new StockCard();

    Product product = new Product();
    product.setCode("01A01");
    product.setId(20);
    stockCard.setProduct(product);
    stockCard.setProduct(product);

    Lot earliestLot = new Lot();
    Date earliestLotExpiryDate = DateUtil.parseString("Feb 2015", DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR);
    earliestLot.setExpirationDate(earliestLotExpiryDate);
    stockCard.setLotOnHandListWrapper(newArrayList(new LotOnHand(earliestLot, stockCard, 10L)));

    Date currentDate = DateUtil.getCurrentDate();
    List<StockMovementItem> stockMovementItemList = newArrayList();
    StockMovementItem stockMovementItem = new StockMovementItem();
    stockMovementItem.setMovementType(MovementReasonManager.MovementType.RECEIVE);
    stockMovementItem.setMovementQuantity(10);
    stockMovementItem.setMovementDate(currentDate);
    stockMovementItemList.add(stockMovementItem);

    //when
    RnrFormItem rnrFormItemWithExpiryDate = rapidTestRepository.createRnrFormItemByPeriod(
        stockCard, stockMovementItemList, currentDate);
    RnrFormItem rnrFormItemWithoutExpiryDate = rapidTestRepository.createRnrFormItemByPeriod(
        noExpiryDateStockCard, stockMovementItemList, currentDate);

    //then
    assertNotNull(rnrFormItemWithExpiryDate);
    assertEquals(stockCard.getProduct(), rnrFormItemWithExpiryDate.getProduct());
    assertEquals(DateUtil.formatDate(earliestLotExpiryDate, DateUtil.SIMPLE_DATE_FORMAT), rnrFormItemWithExpiryDate.getValidate());

    assertNotNull(rnrFormItemWithoutExpiryDate);
    assertEquals(stockCard.getProduct(), rnrFormItemWithExpiryDate.getProduct());
    assertNull(rnrFormItemWithoutExpiryDate.getValidate());
  }

  @Test
  public void createRnrFormItemByPeriod_initAmount() {
    //given
    StockCard stockCard = new StockCard();

    Date currentDate = DateUtil.getCurrentDate();

    List<StockMovementItem> stockMovementItemList = newArrayList();
    StockMovementItem stockMovementItem = new StockMovementItem();
    stockMovementItem.setMovementType(MovementReasonManager.MovementType.RECEIVE);
    stockMovementItem.setMovementQuantity(10);
    stockMovementItem.setMovementDate(currentDate);
    long itemStockOnHand = 20;
    stockMovementItem.setStockOnHand(itemStockOnHand);
    stockMovementItemList.add(stockMovementItem);

    //when
    RnrFormItem rnrFormItemWithNullItems = rapidTestRepository.createRnrFormItemByPeriod(stockCard,
        null, currentDate);
    RnrFormItem rnrFormItemWithEmptyItems = rapidTestRepository.createRnrFormItemByPeriod(stockCard,
        newArrayList(), currentDate);
    RnrFormItem rnrFormItemWithItems = rapidTestRepository.createRnrFormItemByPeriod(stockCard,
        stockMovementItemList, currentDate);

    //then
    assertEquals(0L, (long) rnrFormItemWithNullItems.getInitialAmount());
    assertEquals(0L, (long) rnrFormItemWithEmptyItems.getInitialAmount());
    assertEquals(itemStockOnHand, (long) rnrFormItemWithItems.getInitialAmount());
  }

  @Test
  public void shouldUpdateInitialAmount() {
    //given
    RnrFormItem isCustomRnrFormItem = new RnrFormItem();
    RnrFormItem notCustomRnrFormItem = new RnrFormItem();
    long initAmount = 100L;

    //when
    rapidTestRepository.updateInitialAmount(isCustomRnrFormItem, null);
    rapidTestRepository.updateInitialAmount(notCustomRnrFormItem, initAmount);

    //then
    assertTrue(isCustomRnrFormItem.getIsCustomAmount());
    assertNull(isCustomRnrFormItem.getInitialAmount());

    assertFalse(notCustomRnrFormItem.getIsCustomAmount());
    assertEquals(initAmount, (long) notCustomRnrFormItem.getInitialAmount());
  }

  @Test
  public void shouldUpdateDefaultValue() {
    //given
    RnrFormItem rnrFormItem = new RnrFormItem();

    //when
    rapidTestRepository.updateDefaultValue(rnrFormItem);

    //then
    assertEquals(Long.valueOf(0), rnrFormItem.getIssued());
    assertEquals(Long.valueOf(0), rnrFormItem.getAdjustment());
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
      bind(ReportTypeFormRepository.class).toInstance(mockReportTypeFormRepository);
    }
  }

  private Program createProgram(String programCode) throws LMISException {
    return new ProgramBuilder().setProgramCode(programCode).setProgramName("MMIA name").build();
  }
}
