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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.core.manager.MovementReasonManager.MovementType.ISSUE;
import static org.openlmis.core.manager.MovementReasonManager.MovementType.NEGATIVE_ADJUST;
import static org.openlmis.core.manager.MovementReasonManager.MovementType.PHYSICAL_INVENTORY;
import static org.openlmis.core.manager.MovementReasonManager.MovementType.POSITIVE_ADJUST;
import static org.openlmis.core.manager.MovementReasonManager.MovementType.RECEIVE;
import static org.openlmis.core.utils.Constants.MMIA_PROGRAM_CODE;
import static org.openlmis.core.utils.Constants.VIA_PROGRAM_CODE;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

import androidx.annotation.NonNull;
import com.google.inject.AbstractModule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;
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
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.ProductProgram;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRForm.Status;
import org.openlmis.core.model.RnRFormSignature;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.ProgramBuilder;
import org.openlmis.core.model.builder.ReportTypeBuilder;
import org.openlmis.core.model.builder.RnRFormBuilder;
import org.openlmis.core.model.builder.RnrFormItemBuilder;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.builder.StockMovementItemBuilder;
import org.openlmis.core.model.service.RequisitionPeriodService;
import org.openlmis.core.utils.DateUtil;
import org.roboguice.shaded.goole.common.collect.Lists;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class RnrFormRepositoryTest extends LMISRepositoryUnitTest {

  RnrFormRepository rnrFormRepository;
  private StockRepository mockStockRepository;

  private RnrFormItemRepository mockRnrFormItemRepository;

  private ProgramRepository mockProgramRepository;

  private ProductProgramRepository mockProductProgramRepository;

  private RequisitionPeriodService mockRequisitionPeriodService;
  private StockMovementRepository mockStockMovementRepository;

  private ReportTypeFormRepository mockReportTypeFormRepository;

  static final String comment = "DRAFT Form";

  @Before
  public void setup() throws LMISException {
    mockProgramRepository = mock(ProgramRepository.class);
    mockStockRepository = mock(StockRepository.class);
    mockRnrFormItemRepository = mock(RnrFormItemRepository.class);
    mockRequisitionPeriodService = mock(RequisitionPeriodService.class);
    mockProductProgramRepository = mock(ProductProgramRepository.class);
    mockStockMovementRepository = mock(StockMovementRepository.class);
    mockReportTypeFormRepository = mock(ReportTypeFormRepository.class);

    RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());

    rnrFormRepository = RoboGuice.getInjector(RuntimeEnvironment.application)
        .getInstance(RnrFormRepository.class);

    Program programMMIA = createProgram(MMIA_PROGRAM_CODE);
    programMMIA.setId(1L);

    when(mockProgramRepository.queryByCode(anyString())).thenReturn(programMMIA);
    ReportTypeForm reportTypeForm = new ReportTypeBuilder()
        .setActive(true)
        .setStartTime(DateUtil.dateMinusMonth(new Date(), 2))
        .build();

    when(mockReportTypeFormRepository.getReportType(anyString())).thenReturn(reportTypeForm);
  }

  @Test
  public void shouldGetAllUnsyncedMMIAForms() throws LMISException {
    for (int i = 0; i < 10; i++) {
      RnRForm form = new RnRFormBuilder().setComments("Rnr Form" + i)
          .setStatus(Status.AUTHORIZED)
          .setProgram(createProgram("MMIA" + i))
          .setSynced(i % 2 == 0)
          .build();
      rnrFormRepository.create(form);
    }

    List<RnRForm> unsyncedForms = rnrFormRepository.listUnsynced();
    assertThat(unsyncedForms.size(), is(5));
  }

  private Program createProgram(String programCode) throws LMISException {
    return new ProgramBuilder().setProgramCode(programCode).setProgramName("MMIA name").build();
  }

  @Test
  public void shouldGetAllMMIAForms() throws LMISException {
    Program programMMIA = new Program();
    programMMIA.setId(1l);
    Program programVIA = new Program();
    programVIA.setId(2l);

    for (int i = 0; i < 11; i++) {
      RnRForm form = new RnRFormBuilder().setComments("Rnr Form" + i)
          .setStatus(Status.AUTHORIZED)
          .setProgram(i % 2 == 0 ? programMMIA : programVIA)
          .build();
      form.setPeriodBegin(new Date());
      rnrFormRepository.create(form);
    }

    List<RnRForm> list = rnrFormRepository
        .listInclude(RnRForm.Emergency.NO, MMIA_PROGRAM_CODE);
    assertThat(list.size(), is(6));
  }

  @Test
  public void shouldGetDraftForm() throws LMISException {
    Program program = new Program();
    program.setId(1l);
    program.setProgramCode(MMIA_PROGRAM_CODE);

    ReportTypeForm reportTypeForm = new ReportTypeBuilder()
        .setActive(true)
        .setStartTime(DateUtil.dateMinusMonth(new Date(), 2))
        .build();

    when(mockReportTypeFormRepository.getReportType(anyString())).thenReturn(reportTypeForm);

    RnRForm form = new RnRFormBuilder().setComments(comment)
        .setStatus(Status.DRAFT).setProgram(program).build();
    form.setPeriodBegin(DateUtil.dateMinusMonth(new Date(), 1));
    when(mockProgramRepository.queryByCode(anyString())).thenReturn(program);
    mockProgramRepository.createOrUpdate(program);
    rnrFormRepository.create(form);

    RnRForm rnRForm = rnrFormRepository.queryUnAuthorized();

    assertThat(rnRForm.getComments(), is(comment));
  }

  @Test
  public void shouldGetSubmittedForm() throws LMISException {
    Program program = new Program();

    RnRForm form = new RnRFormBuilder().setComments("Submitted Form")
        .setStatus(Status.SUBMITTED)
        .setProgram(program).build();
    form.setPeriodBegin(DateUtil.dateMinusMonth(new Date(), 1));
    when(mockProgramRepository.queryByCode(anyString())).thenReturn(program);

    rnrFormRepository.create(form);
    RnRForm rnRForm = rnrFormRepository.queryUnAuthorized();

    assertThat(rnRForm.getComments(), is("Submitted Form"));
  }


  @Test
  public void shouldReturnFalseIfThereIsAAuthorizedFormExisted() throws Exception {
    Program program = new Program();
    program.setId(123);

    Date generateDate = DateUtil.parseString("05/07/2015", DateUtil.SIMPLE_DATE_FORMAT);

    RnRForm form = RnRForm.init(program, generateDate);
    form.setStatus(Status.AUTHORIZED);
    rnrFormRepository.create(form);

    generateDate = DateUtil.parseString("20/07/2015", DateUtil.SIMPLE_DATE_FORMAT);

    RnRForm rnRForm2 = RnRForm.init(program, generateDate);

    assertThat(rnrFormRepository.isPeriodUnique(rnRForm2), is(false));
  }

  @Test
  public void shouldReturnTrueIfThereIsNoAuthorizedFormExisted() throws Exception {
    Program program = new Program();
    program.setId(123);

    Date generateDate = DateUtil.parseString("05/07/2015", DateUtil.SIMPLE_DATE_FORMAT);

    RnRForm form = RnRForm.init(program, generateDate);
    form.setStatus(Status.DRAFT);
    rnrFormRepository.create(form);

    generateDate = DateUtil.parseString("20/07/2015", DateUtil.SIMPLE_DATE_FORMAT);

    RnRForm rnRForm2 = RnRForm.init(program, generateDate);

    assertThat(rnrFormRepository.isPeriodUnique(rnRForm2), is(true));
  }

  @Test
  public void shouldGenerateRnrFormItemWithCorrectAttributes() throws Exception {
    //given
    int stockExistence = 100;
    int issueQuantity = 10;
    int receiveQuantity = 20;
    int positiveQuantity = 30;
    int negativeQuantity = 40;
    Program program = createProgram(MMIA_PROGRAM_CODE);

    ArrayList<StockMovementItem> stockMovementItems = new ArrayList<>();
    StockMovementItemBuilder stockMovementItemBuilder = new StockMovementItemBuilder();

    StockMovementItem inventoryItem =
        createStockMovementItemBySOH(stockMovementItemBuilder, PHYSICAL_INVENTORY, stockExistence);
    StockMovementItem issueItem = createStockMovementItemByQuality(stockMovementItemBuilder, ISSUE, issueQuantity);
    StockMovementItem receiveItem =
        createStockMovementItemByQuality(stockMovementItemBuilder, RECEIVE, receiveQuantity);
    StockMovementItem positiveItem =
        createStockMovementItemByQuality(stockMovementItemBuilder, POSITIVE_ADJUST, positiveQuantity);
    StockMovementItem negativeItem =
        createStockMovementItemByQuality(stockMovementItemBuilder, NEGATIVE_ADJUST, negativeQuantity);
    stockMovementItems.addAll(Arrays.asList(inventoryItem, issueItem, receiveItem, positiveItem, negativeItem));

    ArrayList<StockCard> stockCards = new ArrayList<>();
    Product product = new Product();
    String productCode = "01A01";
    product.setCode(productCode);

    StockCard stockCard = new StockCardBuilder()
        .setCreateDate(new Date())
        .setProduct(product)
        .build();
    stockCard.setCreatedAt(DateUtil.parseString("10/10/2015", DateUtil.SIMPLE_DATE_FORMAT));
    stockCards.add(stockCard);

    RnRForm form = new RnRForm();
    form.setPeriodBegin(DateUtil.parseString("9/21/2015", DateUtil.SIMPLE_DATE_FORMAT));
    form.setPeriodEnd(DateUtil.parseString("10/20/2015", DateUtil.SIMPLE_DATE_FORMAT));
    form.setProgram(program);

    DateTime dateTime = new DateTime();
    dateTime.millisOfDay();
    StockMovementItem stockMovementItem = new StockMovementItem();
    stockMovementItem.setMovementDate(dateTime.toDate());
    stockMovementItem.setCreatedTime(new Date());
    when(mockStockMovementRepository.queryFirstStockMovementByStockCardId(anyLong()))
        .thenReturn(stockMovementItem);
    Map<String, List<StockMovementItem>> idToStockItem = new HashMap<>();
    idToStockItem.put(String.valueOf(stockCard.getId()), stockMovementItems);
    when(mockStockMovementRepository
        .queryStockMovement(new HashSet<>(Arrays.asList(String.valueOf(stockCard.getId()))), form.getPeriodBegin(),
            form.getPeriodEnd())).thenReturn(idToStockItem);

    ProductProgram productProgram = new ProductProgram();
    productProgram.setCategory("Adult");
    productProgram.setProductCode(productCode);

    when(mockProductProgramRepository.listActiveProductProgramsByProgramCodes(anyList()))
        .thenReturn(Arrays.asList(productProgram));
    when(mockProgramRepository.queryByCode(anyString())).thenReturn(program);

    // when
    List<RnrFormItem> rnrFormItemList = rnrFormRepository.generateRnrFormItems(form, stockCards);

    // then
    RnrFormItem rnrFormItem = rnrFormItemList.get(0);
    int expectAdjustment = positiveQuantity - negativeQuantity;
    int expectInventoryQuantity =
        stockExistence + receiveQuantity + positiveQuantity - issueQuantity - negativeQuantity;
    int expectOrderQuantity = 2 * issueQuantity - expectInventoryQuantity;
    expectOrderQuantity = Math.max(expectOrderQuantity, 0);
    assertThat(rnrFormItem.getProduct(), is(product));
    assertThat(rnrFormItem.getCategory(), is("Adult"));
    assertEquals(stockExistence, rnrFormItem.getInitialAmount().longValue());
    assertEquals(issueQuantity, rnrFormItem.getIssued().longValue());
    assertEquals(receiveQuantity, rnrFormItem.getReceived());
    assertEquals(expectAdjustment, rnrFormItem.getAdjustment().longValue());
    assertEquals(expectInventoryQuantity, rnrFormItem.getInventory().longValue());
    assertEquals(expectOrderQuantity, rnrFormItem.getCalculatedOrderQuantity().longValue());
  }

  @Test
  public void shouldReturnRnRForm() throws LMISException {
    Program program = new Program();

    RnRForm form = new RnRForm();
    form.setProgram(program);
    form.setId(1);
    form.setComments(comment);

    rnrFormRepository.create(form);

    RnRForm rnRForm = rnrFormRepository.queryRnRForm(1);
    assertThat(rnRForm.getComments(), is(comment));
  }

  @Test
  public void shouldRemoveRnrForm() throws LMISException {
    Program program = new Program();

    RnRForm form = new RnRForm();
    form.setProgram(program);
    form.setId(1);
    form.setComments(comment);

    rnrFormRepository.create(form);
    List<RnRForm> rnRForm = rnrFormRepository.list();
    assertThat(rnRForm.size(), is(1));
    assertThat(rnRForm.get(0).getComments(), is(comment));

    rnrFormRepository.removeRnrForm(form);

    assertThat(rnrFormRepository.hasRequisitionData(), is(false));
  }


  @Test
  public void shouldHasZeroData() throws LMISException {
    assertThat(rnrFormRepository.hasOldDate(), is(false));
  }

  @Test
  public void shouldListNotSynchronizedFromStarTime() throws LMISException {
    assertThat(rnrFormRepository.listNotSynchronizedFromStarTime().size(), is(0));
    rnrFormRepository.deleteOldData();
  }

  @Test
  public void shouldQueryAllUnsyncedForms() throws LMISException {
    assertThat(rnrFormRepository.queryAllUnsyncedForms().size(), is(0));
  }

  @Test
  public void shouldRecordUpdateTimeWhenAuthorizeRnrForm() throws Exception {
    Program program = buildViaProgram();

    RnRForm form = RnRForm
        .init(program, DateUtil.parseString("01/01/2015", DateUtil.SIMPLE_DATE_FORMAT));
    form.setId(1);
    form.setComments(comment);

    form.setRnrFormItemListWrapper(new ArrayList<RnrFormItem>());
    form.setRegimenItemListWrapper(new ArrayList<RegimenItem>());
    form.setBaseInfoItemListWrapper(new ArrayList<BaseInfoItem>());
    form.getSignaturesWrapper()
        .add(new RnRFormSignature(form, "sign", RnRFormSignature.TYPE.SUBMITTER));
    rnrFormRepository.createAndRefresh(form);
    rnrFormRepository.createOrUpdateWithItems(form);

    RnRForm rnRForm = rnrFormRepository.queryRnRForm(1);
    assertThat(DateUtil.formatDate(rnRForm.getUpdatedAt(), DateUtil.SIMPLE_DATE_FORMAT),
        is(DateUtil.formatDate(DateUtil.today(), DateUtil.SIMPLE_DATE_FORMAT)));
  }


  @Test
  public void shouldCreateSuccess() throws Exception {
    Program program = Program.builder().programCode(MMIA_PROGRAM_CODE).build();
    RnRForm form = RnRForm
        .init(program, DateUtil.parseString("01/01/2015", DateUtil.SIMPLE_DATE_FORMAT));
    ArrayList<RnRForm> rnRForms = new ArrayList<>();
    rnRForms.add(form);

    rnrFormRepository.createRnRsWithItems(rnRForms);

    RnRForm form2 = RnRForm
        .init(program, DateUtil.parseString("01/01/2015", DateUtil.SIMPLE_DATE_FORMAT));
    ArrayList<BaseInfoItem> baseInfoItems = new ArrayList<>();
    BaseInfoItem baseInfoItem = new BaseInfoItem();
    baseInfoItem.setValue("Value1");
    baseInfoItem.setName("Name1");
    baseInfoItems.add(baseInfoItem);
    form2.setBaseInfoItemListWrapper(baseInfoItems);
    form2.setComments("Comments");
    RnRForm.fillFormId(form2);
    ArrayList<RnRForm> rnRForms2 = new ArrayList<>();
    rnRForms2.add(form2);
    rnrFormRepository.createRnRsWithItems(rnRForms2);
    assertThat(form.getId(), is(1L));
    assertThat(form2.getId(), is(2L));
    RnRForm rnRForm = rnrFormRepository.queryRnRForm(2L);
    assertThat(rnRForm.getBaseInfoItemListWrapper().get(0).getName(), is("Name1"));
    assertThat(rnRForm.getComments(), is("Comments"));
  }

  @Test
  public void shouldInitRnrFormItemWithoutMovement() throws Exception {
    rnrFormRepository = spy(rnrFormRepository);

    Product product = new Product();
    RnRForm form = new RnRForm();
    RnrFormItem rnrFormItem = new RnrFormItem();
    rnrFormItem.setInventory(100L);
    rnrFormItem.setProduct(product);
    form.setRnrFormItemListWrapper(newArrayList(rnrFormItem));
    doReturn(newArrayList(form)).when(rnrFormRepository)
        .listInclude(any(RnRForm.Emergency.class), anyString());

    StockCard stockCard = new StockCard();
    product.setId(20);
    stockCard.setProduct(product);
    Lot lot = new Lot();
    lot.setExpirationDate(
        DateUtil.parseString("Feb 2015", DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR));
    stockCard.setLotOnHandListWrapper(newArrayList(new LotOnHand(lot, stockCard, 10L)));
    when(mockStockMovementRepository
        .queryStockMovementsByMovementDate(anyLong(), any(Date.class), any(Date.class)))
        .thenReturn(new ArrayList<StockMovementItem>());

    RnrFormItem rnrFormItemByPeriod = rnrFormRepository
        .createRnrFormItemByPeriod(stockCard, Collections.emptyList());

    assertThat(rnrFormItemByPeriod.getReceived(), is(0L));
    assertThat(rnrFormItemByPeriod.getCalculatedOrderQuantity(), is(0L));
    assertThat(rnrFormItemByPeriod.getInventory(), is(0L));
    assertThat(rnrFormItemByPeriod.getInitialAmount(), is(0L));

    stockCard.setLotOnHandListWrapper(Lists.newArrayList());
    rnrFormItemByPeriod = rnrFormRepository
        .createRnrFormItemByPeriod(stockCard, Collections.emptyList());
    assertNull(rnrFormItemByPeriod.getValidate());
  }

  @Test
  public void shouldInitRnrFormItemWithoutMovementAndMovementIsNull() throws Exception {
    rnrFormRepository = spy(rnrFormRepository);
    StockCard stockCard = new StockCard();
    when(mockStockMovementRepository
        .queryStockMovementsByMovementDate(anyLong(), any(Date.class), any(Date.class)))
        .thenReturn(new ArrayList<StockMovementItem>());
    doReturn(new ArrayList<>()).when(rnrFormRepository)
        .listInclude(any(RnRForm.Emergency.class), anyString());

    RnrFormItem rnrFormItemByPeriod = rnrFormRepository
        .createRnrFormItemByPeriod(stockCard, Collections.emptyList());

    assertThat(rnrFormItemByPeriod.getReceived(), is(0L));
    assertThat(rnrFormItemByPeriod.getCalculatedOrderQuantity(), is(0L));
    assertThat(rnrFormItemByPeriod.getInventory(), is(0L));
    assertThat(rnrFormItemByPeriod.getInitialAmount(), is(0L));
  }

  @Test
  public void shouldDeleteDeactivatedItemsFromRnrForms() throws Exception {
    RnRForm form = new RnRFormBuilder().setComments("Submitted Form")
        .setStatus(Status.AUTHORIZED)
        .setSynced(false).setProgram(createProgram("MMIA")).build();

    RnrFormItem deactivatedProductItem = new RnrFormItemBuilder()
        .setProduct(new ProductBuilder().setCode("P1").setIsActive(false).build()).build();
    form.setRnrFormItemListWrapper(newArrayList(deactivatedProductItem));

    rnrFormRepository.deleteDeactivatedAndUnsupportedProductItems(Arrays.asList(form));

    verify(mockRnrFormItemRepository).deleteFormItems(anyList());
  }

  @Test
  public void shouldInitRnrUsingPeriodReturnedByPeriodService() throws Exception {
    when(mockRequisitionPeriodService.generateNextPeriod(anyString(), any(Date.class)))
        .thenReturn(new Period(new DateTime(), new DateTime()));
    rnrFormRepository.programCode = "MMIA";

    rnrFormRepository.initNormalRnrForm(null);
    verify(mockRequisitionPeriodService).generateNextPeriod(rnrFormRepository.programCode, null);
  }

  @Test
  public void shouldInitRnrEmergencyService() throws Exception {
    when(mockRequisitionPeriodService.generateNextPeriod(anyString(), any(Date.class)))
        .thenReturn(new Period(new DateTime(), new DateTime()));
    rnrFormRepository.programCode = VIA_PROGRAM_CODE;

    rnrFormRepository.initEmergencyRnrForm(null, Collections.EMPTY_LIST);
    verify(mockRequisitionPeriodService).generateNextPeriod(rnrFormRepository.programCode, null);
  }

  @Test
  public void shouldListFormsForProgramAndSubprograms() throws Exception {
    Program programEss = new Program();
    programEss.setId(1L);
    programEss.setProgramCode("ESS_MEDS");

    Program programVIA = new Program();
    programVIA.setId(2L);
    programVIA.setProgramCode(VIA_PROGRAM_CODE);
    programEss.setParentCode(VIA_PROGRAM_CODE);

    when(mockProgramRepository.queryByCode(VIA_PROGRAM_CODE)).thenReturn(programVIA);

    RnRForm formEss = new RnRForm();
    formEss.setProgram(programEss);
    formEss.setStatus(Status.AUTHORIZED);
    formEss.setEmergency(false);
    formEss.setPeriodBegin(DateUtil.dateMinusMonth(new Date(), 1));
    formEss.setPeriodEnd(new Date());

    RnRForm formVIA = new RnRForm();
    formVIA.setProgram(programVIA);
    formVIA.setStatus(Status.AUTHORIZED);
    formVIA.setEmergency(false);
    formVIA.setPeriodBegin(DateUtil.dateMinusMonth(new Date(), 1));
    formVIA.setPeriodEnd(new Date());

    RnRForm formVIAEmergency = new RnRForm();
    formVIAEmergency.setProgram(programVIA);
    formVIAEmergency.setStatus(Status.AUTHORIZED);
    formVIAEmergency.setEmergency(true);
    formVIAEmergency.setPeriodBegin(DateUtil.dateMinusMonth(new Date(), 1));
    formVIAEmergency.setPeriodEnd(new Date());

    rnrFormRepository.create(formEss);
    rnrFormRepository.create(formVIA);
    rnrFormRepository.create(formVIAEmergency);

    List<RnRForm> list = rnrFormRepository
        .listInclude(RnRForm.Emergency.NO, VIA_PROGRAM_CODE);
    assertThat(list.size(), is(1));

    //I'm not sure why programCode is higher priority than Emergency...
    List<RnRForm> listWithEmergency = rnrFormRepository
        .listInclude(RnRForm.Emergency.YES, VIA_PROGRAM_CODE);
    assertThat(listWithEmergency.size(), is(2));
  }

  @Test
  public void shouldReturnOldestPeriodBeginRnrFormWhenQueryOldestSyncedRnRFormGroupByProgramWithData()
      throws LMISException {
    // given
    Date periodBegin = new Date();
    RnRForm rnRForm = generateRnrForm(periodBegin);

    Date oldestPeriodBegin = DateUtil.dateMinusMonth(periodBegin, 1);
    RnRForm rnRForm2 = generateRnrForm(oldestPeriodBegin);

    rnrFormRepository.create(rnRForm);
    rnrFormRepository.create(rnRForm2);
    // when
    RnRForm actualRnrForm = rnrFormRepository.queryOldestSyncedRnRFormGroupByProgram();
    // then
    assertEquals(
        DateUtil.parseString(DateUtil.formatDate(oldestPeriodBegin, DateUtil.DB_DATE_FORMAT),
            DateUtil.DB_DATE_FORMAT),
        actualRnrForm.getPeriodBegin()
    );
  }

  @Test
  public void shouldReturnNullWhenQueryOldestSyncedRnRFormGroupByProgramWithoutData()
      throws LMISException {
    // when
    RnRForm actualRnrForm = rnrFormRepository.queryOldestSyncedRnRFormGroupByProgram();
    // then
    assertNull(actualRnrForm);
  }

  @Test
  public void shouldQueryFormByPeriodAndProgramCodeWhenSaveAndDeleteDuplicatedPeriodRequisitionsAndListIsNotEmpty()
      throws LMISException {
    // given
    String programCode = "programCode1";

    Program program = new Program();
    program.setProgramCode(programCode);
    program.setId(1);

    when(mockProgramRepository.queryByCode(programCode)).thenReturn(program);

    RnRForm rnRForm = generateRnRForm(program, new Date());

    ArrayList<RnRForm> forms = newArrayList(rnRForm);
    // when
    rnrFormRepository.saveAndDeleteDuplicatedPeriodRequisitions(forms);
    // then
    verify(mockProgramRepository).queryByCode(programCode);
  }

  @Test
  public void shouldSaveFormWhenSaveAndDeleteDuplicatedPeriodRequisitionsAndListIsNotEmptyAndNoExistForm()
      throws LMISException {
    // given
    Program program = new Program();
    program.setProgramCode("programCode2");
    program.setId(1);

    Date periodBegin = new Date();
    RnRForm rnRForm = generateRnRForm(program, periodBegin);

    when(mockProgramRepository.queryByCode(anyString())).thenReturn(null);

    ArrayList<RnRForm> inputForms = newArrayList(rnRForm);
    // when
    rnrFormRepository.saveAndDeleteDuplicatedPeriodRequisitions(inputForms);
    // then
    List<RnRForm> actualForms = rnrFormRepository.list();
    assertEquals(1, actualForms.size());
    RnRForm actualForm = actualForms.get(0);
    assertEquals(
        DateUtil.formatDate(periodBegin),
        DateUtil.formatDate(actualForm.getPeriodBegin())
    );
  }

  @NonNull
  private RnRForm generateRnRForm(Program program, Date periodBegin) {
    RnRForm rnRForm = new RnRForm();
    rnRForm.setProgram(program);
    rnRForm.setPeriodBegin(periodBegin);

    return rnRForm;
  }

  @Test
  public void shouldDeleteExistFormWhenSaveAndDeleteDuplicatedPeriodRequisitionsAndListIsNotEmptyAndHasExistForm()
      throws LMISException {
    // given
    Program program = new Program();
    program.setProgramCode("programCode3");
    program.setId(1);

    Calendar calendar = Calendar.getInstance();
    calendar.set(2024, 5, 20);
    Date periodBegin = calendar.getTime();
    RnRForm rnRForm = generateRnRForm(program);

    Program shouldBeDeletedProgram = new Program();
    shouldBeDeletedProgram.setProgramCode("programCode4");
    shouldBeDeletedProgram.setId(2);
    Date shouldBeDeletedPeriodBegin = DateUtil.getFirstDayForCurrentMonthByDate(periodBegin);
    RnRForm shouldBeDeletedRnRForm = generateRnRForm(shouldBeDeletedProgram, shouldBeDeletedPeriodBegin);

    when(mockProgramRepository.queryByCode(anyString())).thenReturn(shouldBeDeletedProgram, program);

    rnrFormRepository.create(shouldBeDeletedRnRForm);
    rnrFormRepository.create(rnRForm);
    ArrayList<RnRForm> inputForms = newArrayList(shouldBeDeletedRnRForm);
    // when
    rnrFormRepository.saveAndDeleteDuplicatedPeriodRequisitions(inputForms);
    // then
    List<RnRForm> rnRForms = rnrFormRepository.list();
    assertEquals(2, rnRForms.size());

    RnRForm actualRnRForm1 = rnRForms.get(0);
    assertNotEquals(
        DateUtil.formatDate(shouldBeDeletedPeriodBegin, DateUtil.DB_DATE_FORMAT),
        DateUtil.formatDate(actualRnRForm1.getPeriodBegin(), DateUtil.DB_DATE_FORMAT)
    );
  }

  @NonNull
  private static RnRForm generateRnRForm(Program program) {
    RnRForm rnRForm = new RnRForm();
    rnRForm.setProgram(program);
    Date periodBegin = new Date();
    rnRForm.setPeriodBegin(periodBegin);
    return rnRForm;
  }

  private RnRForm generateRnrForm(Date current) {
    RnRForm form = new RnRForm();
    form.setSynced(true);
    form.setPeriodBegin(current);
    form.setPeriodEnd(DateUtil.dateMinusMonth(current, -1));

    return form;
  }

  public class MyTestModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(ProgramRepository.class).toInstance(mockProgramRepository);
      bind(StockRepository.class).toInstance(mockStockRepository);
      bind(RnrFormItemRepository.class).toInstance(mockRnrFormItemRepository);
      bind(RequisitionPeriodService.class).toInstance(mockRequisitionPeriodService);
      bind(ProductProgramRepository.class).toInstance(mockProductProgramRepository);
      bind(StockMovementRepository.class).toInstance(mockStockMovementRepository);
      bind(ReportTypeFormRepository.class).toInstance(mockReportTypeFormRepository);
    }
  }

  @NonNull
  private RnrFormItem getRnrFormItem(RnRForm form, Product product, long inventory) {
    RnrFormItem rnrFormItem = new RnrFormItem();
    rnrFormItem.setForm(form);
    rnrFormItem.setProduct(product);
    rnrFormItem.setInventory(inventory);
    return rnrFormItem;
  }

  private StockMovementItem createStockMovementItemBySOH(StockMovementItemBuilder stockMovementItemBuilder,
      MovementReasonManager.MovementType type, int stockExistence) {
   return createStockMovementItem(stockMovementItemBuilder, type, stockExistence, 12);
  }

  private StockMovementItem createStockMovementItemByQuality(StockMovementItemBuilder stockMovementItemBuilder,
      MovementReasonManager.MovementType type, int quantity) {
    return createStockMovementItem(stockMovementItemBuilder, type, 100, quantity);
  }

  private StockMovementItem createStockMovementItem(StockMovementItemBuilder stockMovementItemBuilder,
      MovementReasonManager.MovementType type, int stockExistence, int quantity) {
    StockMovementItem inventoryItem = stockMovementItemBuilder
        .withDocumentNo("1")
        .withMovementReason("reason")
        .withMovementDate("10/10/2015")
        .withMovementType(type)
        .withStockOnHand(stockExistence)
        .withQuantity(quantity)
        .build();
    return inventoryItem;
  }

  private Program buildViaProgram() {
    Program program = new Program();
    program.setProgramCode(VIA_PROGRAM_CODE);
    return program;
  }
}
