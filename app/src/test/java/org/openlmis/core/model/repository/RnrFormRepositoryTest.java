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
import org.openlmis.core.model.RnRFormSignature;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.ProgramBuilder;
import org.openlmis.core.model.builder.RnRFormBuilder;
import org.openlmis.core.model.builder.RnrFormItemBuilder;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.builder.StockMovementItemBuilder;
import org.openlmis.core.model.service.RequisitionPeriodService;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.roboguice.shaded.goole.common.collect.Lists;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
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
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class RnrFormRepositoryTest extends LMISRepositoryUnitTest {

    RnrFormRepository rnrFormRepository;
    private StockRepository mockStockRepository;

    private RnrFormItemRepository mockRnrFormItemRepository;

    private ProgramRepository mockProgramRepository;

    private ProductProgramRepository mockProductProgramRepository;

    private RequisitionPeriodService mockRequisitionPeriodService;
    private StockMovementRepository mockStockMovementRepository;

    @Before
    public void setup() throws LMISException {
        mockProgramRepository = mock(ProgramRepository.class);
        mockStockRepository = mock(StockRepository.class);
        mockRnrFormItemRepository = mock(RnrFormItemRepository.class);
        mockRequisitionPeriodService = mock(RequisitionPeriodService.class);
        mockProductProgramRepository = mock(ProductProgramRepository.class);
        mockStockMovementRepository = mock(StockMovementRepository.class);

        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());

        rnrFormRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(RnrFormRepository.class);

        Program programMMIA = new Program("MMIA", "MMIA", null, false, null, null);
        programMMIA.setId(1l);

        when(mockProgramRepository.queryByCode(anyString())).thenReturn(programMMIA);
    }

    @Test
    public void shouldGetAllUnsyncedMMIAForms() throws LMISException {
        for (int i = 0; i < 10; i++) {
            RnRForm form = new RnRFormBuilder().setComments("Rnr Form" + i)
                    .setStatus(RnRForm.STATUS.AUTHORIZED)
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

    @Ignore
    @Test
    public void shouldGetAllMMIAForms() throws LMISException {
        Program programMMIA = new Program();
        programMMIA.setId(1l);
        Program programVIA = new Program();
        programVIA.setId(2l);
        when(mockProgramRepository.queryProgramIdsByProgramCodeOrParentCode(Constants.MMIA_PROGRAM_CODE)).thenReturn(newArrayList(1L));

        for (int i = 0; i < 11; i++) {
            RnRForm form = new RnRFormBuilder().setComments("Rnr Form" + i)
                    .setStatus(RnRForm.STATUS.AUTHORIZED)
                    .setProgram(i % 2 == 0 ? programMMIA : programVIA)
                    .build();
            rnrFormRepository.create(form);
        }

        List<RnRForm> list = rnrFormRepository.listInclude(RnRForm.Emergency.No, Constants.MMIA_PROGRAM_CODE);
        assertThat(list.size(), is(6));
    }

    @Ignore
    @Test
    public void shouldGetDraftForm() throws LMISException {
        Program program = new Program();

        RnRForm form = new RnRFormBuilder().setComments("DRAFT Form")
                .setStatus(RnRForm.STATUS.DRAFT).setProgram(program).build();

        when(mockProgramRepository.queryByCode(anyString())).thenReturn(program);

        rnrFormRepository.create(form);

        RnRForm rnRForm = rnrFormRepository.queryUnAuthorized();

        assertThat(rnRForm.getComments(), is("DRAFT Form"));
    }

    @Ignore
    @Test
    public void shouldGetSubmittedForm() throws LMISException {
        Program program = new Program();

        RnRForm form = new RnRFormBuilder().setComments("Submitted Form")
                .setStatus(RnRForm.STATUS.SUBMITTED)
                .setProgram(program).build();

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
        form.setStatus(RnRForm.STATUS.AUTHORIZED);
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
        form.setStatus(RnRForm.STATUS.DRAFT);
        rnrFormRepository.create(form);

        generateDate = DateUtil.parseString("20/07/2015", DateUtil.SIMPLE_DATE_FORMAT);

        RnRForm rnRForm2 = RnRForm.init(program, generateDate);

        assertThat(rnrFormRepository.isPeriodUnique(rnRForm2), is(true));
    }

    @Test
    public void shouldGenerateRnrFormItemWithCorrectAttributes() throws Exception {
        int stockExistence = 100;
        int issueQuantity = 10;
        int receiveQuantity = 20;
        int positiveQuantity = 30;
        int negativeQuantity = 40;

        ArrayList<StockMovementItem> stockMovementItems = new ArrayList<>();
        StockMovementItemBuilder stockMovementItemBuilder = new StockMovementItemBuilder();

        StockMovementItem inventoryItem = stockMovementItemBuilder
                .withDocumentNo("1")
                .withMovementReason("reason")
                .withMovementDate("10/10/2015")
                .withMovementType(MovementReasonManager.MovementType.PHYSICAL_INVENTORY)
                .withStockOnHand(stockExistence)
                .build();
        StockMovementItem issueItem = stockMovementItemBuilder
                .withDocumentNo("1")
                .withMovementReason("reason")
                .withMovementDate("10/10/2015")
                .withMovementType(MovementReasonManager.MovementType.ISSUE)
                .withQuantity(issueQuantity)
                .build();
        StockMovementItem receiveItem = stockMovementItemBuilder
                .withDocumentNo("1")
                .withMovementReason("reason")
                .withMovementDate("10/11/2015")
                .withMovementType(MovementReasonManager.MovementType.RECEIVE)
                .withQuantity(receiveQuantity)
                .build();
        StockMovementItem positiveItem = stockMovementItemBuilder
                .withDocumentNo("1")
                .withMovementReason("reason")
                .withMovementDate("10/12/2015")
                .withMovementType(MovementReasonManager.MovementType.POSITIVE_ADJUST)
                .withQuantity(positiveQuantity)
                .build();
        StockMovementItem negativeItem = stockMovementItemBuilder
                .withDocumentNo("1")
                .withMovementReason("reason")
                .withMovementDate("10/12/2015")
                .withMovementType(MovementReasonManager.MovementType.NEGATIVE_ADJUST)
                .withQuantity(negativeQuantity)
                .build();

        stockMovementItems.add(inventoryItem);
        stockMovementItems.add(issueItem);
        stockMovementItems.add(receiveItem);
        stockMovementItems.add(positiveItem);
        stockMovementItems.add(negativeItem);

        ArrayList<StockCard> stockCards = new ArrayList<>();
        Product product = new Product();
        product.setCode("01A01");

        StockCard stockCard = new StockCardBuilder()
                .setCreateDate(new Date())
                .setProduct(product)
                .build();
        stockCard.setCreatedAt(DateUtil.parseString("10/10/2015", DateUtil.SIMPLE_DATE_FORMAT));
        stockCards.add(stockCard);

        RnRForm form = new RnRForm();
        form.setPeriodBegin(DateUtil.parseString("9/21/2015", DateUtil.SIMPLE_DATE_FORMAT));
        form.setPeriodEnd(DateUtil.parseString("10/20/2015", DateUtil.SIMPLE_DATE_FORMAT));
        form.setProgram(new Program("mmia", "mmia", null, false, null, null));

        DateTime dateTime = new DateTime();
        dateTime.millisOfDay();
        StockMovementItem stockMovementItem = new StockMovementItem();
        stockMovementItem.setMovementDate(dateTime.toDate());
        stockMovementItem.setCreatedTime(new Date());
        when(mockStockMovementRepository.queryFirstStockMovementByStockCardId(anyLong())).thenReturn(stockMovementItem);
        when(mockStockMovementRepository.queryStockItemsByCreatedDate(stockCard.getId(), form.getPeriodBegin(), form.getPeriodEnd())).thenReturn(stockMovementItems);

        ProductProgram productProgram = new ProductProgram();
        productProgram.setCategory("Adult");
        when(mockProductProgramRepository.queryByCode(anyString(), anyList())).thenReturn(productProgram);
        when(mockProgramRepository.queryProgramCodesByProgramCodeOrParentCode(anyString())).thenReturn(new ArrayList<String>());

        List<RnrFormItem> rnrFormItemList = rnrFormRepository.generateRnrFormItems(form, stockCards);

        RnrFormItem rnrFormItem = rnrFormItemList.get(0);
        int expectAdjustment = positiveQuantity - negativeQuantity;
        int expectInventoryQuantity = stockExistence + receiveQuantity + positiveQuantity - issueQuantity - negativeQuantity;
        int expectOrderQuantity = 2 * issueQuantity - expectInventoryQuantity;
        expectOrderQuantity = expectOrderQuantity > 0 ? expectOrderQuantity : 0;

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
        form.setComments("DRAFT Form");

        rnrFormRepository.create(form);

        RnRForm rnRForm = rnrFormRepository.queryRnRForm(1);
        assertThat(rnRForm.getComments(), is("DRAFT Form"));
    }

    @Test
    public void shouldRemoveRnrForm() throws LMISException {
        Program program = new Program();

        RnRForm form = new RnRForm();
        form.setProgram(program);
        form.setId(1);
        form.setComments("DRAFT Form");

        rnrFormRepository.create(form);
        List<RnRForm> rnRForm = rnrFormRepository.list();
        assertThat(rnRForm.size(), is(1));
        assertThat(rnRForm.get(0).getComments(), is("DRAFT Form"));

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
        Program program = new Program();

        RnRForm form = RnRForm.init(program, DateUtil.parseString("01/01/2015", DateUtil.SIMPLE_DATE_FORMAT));
        form.setId(1);
        form.setComments("DRAFT Form");

        form.setRnrFormItemListWrapper(new ArrayList<RnrFormItem>());
        form.setRegimenItemListWrapper(new ArrayList<RegimenItem>());
        form.setBaseInfoItemListWrapper(new ArrayList<BaseInfoItem>());
        form.getSignaturesWrapper().add(new RnRFormSignature(form, "sign", RnRFormSignature.TYPE.SUBMITTER));
        rnrFormRepository.createAndRefresh(form);
        rnrFormRepository.createOrUpdateWithItems(form);

        RnRForm rnRForm = rnrFormRepository.queryRnRForm(1);
        assertThat(DateUtil.formatDate(rnRForm.getUpdatedAt(), DateUtil.SIMPLE_DATE_FORMAT), is(DateUtil.formatDate(DateUtil.today(), DateUtil.SIMPLE_DATE_FORMAT)));
    }


    @Test
    public void shouldCreateSuccess() throws Exception {
        Program program = new Program();
        RnRForm form = RnRForm.init(program, DateUtil.parseString("01/01/2015", DateUtil.SIMPLE_DATE_FORMAT));
        ArrayList<RnRForm> rnRForms = new ArrayList<>();
        rnRForms.add(form);

        rnrFormRepository.createRnRsWithItems(rnRForms);

        RnRForm form2 = RnRForm.init(program, DateUtil.parseString("01/01/2015", DateUtil.SIMPLE_DATE_FORMAT));
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
        doReturn(newArrayList(form)).when(rnrFormRepository).listInclude(any(RnRForm.Emergency.class), anyString());

        StockCard stockCard = new StockCard();
        product.setId(20);
        stockCard.setProduct(product);
        Lot lot = new Lot();
        lot.setExpirationDate(DateUtil.parseString("Feb 2015", DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR));
        stockCard.setLotOnHandListWrapper(newArrayList(new LotOnHand(lot, stockCard, 10L)));
        when(mockStockMovementRepository.queryStockMovementsByMovementDate(anyLong(), any(Date.class), any(Date.class))).thenReturn(new ArrayList<StockMovementItem>());

        RnrFormItem rnrFormItemByPeriod = rnrFormRepository.createRnrFormItemByPeriod(stockCard, new Date(), new Date());

        assertThat(rnrFormItemByPeriod.getReceived(), is(0L));
        assertThat(rnrFormItemByPeriod.getCalculatedOrderQuantity(), is(0L));
        assertThat(rnrFormItemByPeriod.getInventory(), is(0L));
        assertThat(rnrFormItemByPeriod.getInitialAmount(), is(0L));

        stockCard.setLotOnHandListWrapper(Lists.<LotOnHand>newArrayList());
        rnrFormItemByPeriod = rnrFormRepository.createRnrFormItemByPeriod(stockCard, new Date(), new Date());
        assertNull(rnrFormItemByPeriod.getValidate());
    }

    @Test
    public void shouldInitRnrFormItemWithoutMovementAndMovementIsNull() throws Exception {
        rnrFormRepository = spy(rnrFormRepository);
        StockCard stockCard = new StockCard();
        when(mockStockMovementRepository.queryStockMovementsByMovementDate(anyLong(), any(Date.class), any(Date.class))).thenReturn(new ArrayList<StockMovementItem>());
        doReturn(new ArrayList<>()).when(rnrFormRepository).listInclude(any(RnRForm.Emergency.class), anyString());

        RnrFormItem rnrFormItemByPeriod = rnrFormRepository.createRnrFormItemByPeriod(stockCard, new Date(), new Date());

        assertThat(rnrFormItemByPeriod.getReceived(), is(0L));
        assertThat(rnrFormItemByPeriod.getCalculatedOrderQuantity(), is(0L));
        assertThat(rnrFormItemByPeriod.getInventory(), is(0L));
        assertThat(rnrFormItemByPeriod.getInitialAmount(), is(0L));
    }

    @Test
    public void shouldDeleteDeactivatedItemsFromRnrForms() throws Exception {
        RnRForm form = new RnRFormBuilder().setComments("Submitted Form")
                .setStatus(RnRForm.STATUS.AUTHORIZED)
                .setSynced(false).setProgram(createProgram("MMIA")).build();

        RnrFormItem deactivatedProductItem = new RnrFormItemBuilder().setProduct(new ProductBuilder().setCode("P1").setIsActive(false).build()).build();
        form.setRnrFormItemListWrapper(newArrayList(deactivatedProductItem));

        rnrFormRepository.deleteDeactivatedAndUnsupportedProductItems(Arrays.asList(form));

        verify(mockRnrFormItemRepository).deleteFormItems(anyList());
    }

    @Test
    public void shouldInitRnrUsingPeriodReturnedByPeriodService() throws Exception {
        when(mockRequisitionPeriodService.generateNextPeriod(anyString(), any(Date.class))).thenReturn(new Period(new DateTime(), new DateTime()));
        rnrFormRepository.programCode = "MMIA";

        rnrFormRepository.initNormalRnrForm(null);
        verify(mockRequisitionPeriodService).generateNextPeriod(rnrFormRepository.programCode, null);
    }

    @Test
    public void shouldInitRnrEmergencyService() throws Exception {
        when(mockRequisitionPeriodService.generateNextPeriod(anyString(), any(Date.class))).thenReturn(new Period(new DateTime(), new DateTime()));
        rnrFormRepository.programCode = "MMIA";

        rnrFormRepository.initEmergencyRnrForm(null, Collections.EMPTY_LIST);
        verify(mockRequisitionPeriodService).generateNextPeriod(rnrFormRepository.programCode, null);
    }

    @Ignore
    @Test
    public void shouldListFormsForProgramAndSubprograms() throws Exception {
        Program programEss = new Program();
        programEss.setId(1L);
        programEss.setProgramCode("ESS_MEDS");
        Program programVIA = new Program();
        programVIA.setId(2L);
        programVIA.setProgramCode(Constants.VIA_PROGRAM_CODE);
        programEss.setParentCode(Constants.VIA_PROGRAM_CODE);
        when(mockProgramRepository.queryProgramIdsByProgramCodeOrParentCode(Constants.VIA_PROGRAM_CODE)).thenReturn(newArrayList(1L, 2L));
        RnRForm form = new RnRForm();
        form.setProgram(programEss);
        form.setStatus(RnRForm.STATUS.AUTHORIZED);
        form.setEmergency(false);
        RnRForm form2 = new RnRForm();
        form2.setProgram(programVIA);
        form2.setStatus(RnRForm.STATUS.AUTHORIZED);
        form2.setEmergency(false);

        RnRForm form3 = new RnRForm();
        form3.setProgram(programVIA);
        form3.setStatus(RnRForm.STATUS.AUTHORIZED);
        form3.setEmergency(true);

        rnrFormRepository.create(form);
        rnrFormRepository.create(form2);
        rnrFormRepository.create(form3);

        List<RnRForm> list = rnrFormRepository.listInclude(RnRForm.Emergency.No, Constants.VIA_PROGRAM_CODE);
        assertThat(list.size(), is(2));


        List<RnRForm> listWithEmergency = rnrFormRepository.listInclude(RnRForm.Emergency.Yes, Constants.VIA_PROGRAM_CODE);
        assertThat(listWithEmergency.size(), is(3));
    }

    @Ignore
    @Test
    public void shouldDeleteOldRnrFormData() throws Exception {
        Program programEss = new Program();
        programEss.setId(1L);
        programEss.setProgramCode("ESS_MEDS");

        Program programVIA = new Program();
        programVIA.setId(2L);
        programVIA.setProgramCode(Constants.VIA_PROGRAM_CODE);
        programEss.setParentCode(Constants.VIA_PROGRAM_CODE);

        Program programMMIA = new Program();
        programVIA.setId(3L);
        programVIA.setProgramCode(Constants.MMIA_PROGRAM_CODE);


        when(mockProgramRepository.queryProgramIdsByProgramCodeOrParentCode(Constants.VIA_PROGRAM_CODE)).thenReturn(newArrayList(1L, 2L));
        RnRForm form = new RnRForm();
        form.setProgram(programEss);
        form.setStatus(RnRForm.STATUS.AUTHORIZED);
        form.setEmergency(false);
        form.setPeriodEnd(new Period(new DateTime()).getEnd().toDate());

        RnRForm form2 = new RnRForm();
        form2.setProgram(programVIA);
        form2.setStatus(RnRForm.STATUS.AUTHORIZED);
        form2.setEmergency(false);
        form2.setPeriodEnd(DateUtil.parseString("2015-09-01", DateUtil.DB_DATE_FORMAT));

        RnRForm form3 = new RnRForm();
        form3.setProgram(programMMIA);
        form3.setStatus(RnRForm.STATUS.AUTHORIZED);
        form3.setEmergency(true);
        form3.setPeriodEnd(DateUtil.parseString("2015-09-01", DateUtil.DB_DATE_FORMAT));


        List<RnrFormItem> rnrFormItemList = new ArrayList<>();

        Program program = new Program();
        program.setProgramCode("1");
        Product product = new Product();
        product.setId(1);

        rnrFormItemList.add(getRnrFormItem(form3, product, 1));

        RnrFormItemRepository rnrFormItemRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(RnrFormItemRepository.class);
        rnrFormItemRepository.batchCreateOrUpdate(rnrFormItemList);

        form3.setRegimenItemListWrapper(new ArrayList<RegimenItem>());
        form3.setBaseInfoItemListWrapper(new ArrayList<BaseInfoItem>());

        rnrFormRepository.create(form);
        rnrFormRepository.create(form2);
        rnrFormRepository.create(form3);

        rnrFormRepository.deleteOldData();

        List<RnRForm> rnRFormsQueried = rnrFormRepository.queryAllUnsyncedForms();
        List<RnrFormItem> rnrFormItemListFromDB = rnrFormItemRepository.listAllNewRnrItems();

        assertEquals(1, rnRFormsQueried.size());
        assertEquals(new Period(new DateTime()).getEnd().toDate(), rnRFormsQueried.get(0).getPeriodEnd());
        assertEquals(0, rnrFormItemListFromDB.size());

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
}
