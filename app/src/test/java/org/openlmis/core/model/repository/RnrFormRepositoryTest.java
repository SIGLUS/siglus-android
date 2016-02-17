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
import org.openlmis.core.model.Inventory;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRFormSignature;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.ProgramBuilder;
import org.openlmis.core.model.builder.RnrFormItemBuilder;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.builder.StockMovementItemBuilder;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class RnrFormRepositoryTest extends LMISRepositoryUnitTest {

    RnrFormRepository rnrFormRepository;
    private StockRepository mockStockRepository;

    private RnrFormItemRepository mockRnrFormItemRepository;

    private ProgramRepository mockProgramRepository;

    private InventoryRepository mockInventoryRepository;

    @Before
    public void setup() throws LMISException {
        mockProgramRepository = mock(ProgramRepository.class);
        mockStockRepository = mock(StockRepository.class);
        mockRnrFormItemRepository = mock(RnrFormItemRepository.class);
        mockInventoryRepository = mock(InventoryRepository.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());

        rnrFormRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(RnrFormRepository.class);

        Program programMMIA = new Program("MMIA", "MMIA", null);
        programMMIA.setId(1l);

        when(mockProgramRepository.queryByCode(anyString())).thenReturn(programMMIA);
    }

    @Test
    public void shouldGetAllUnsyncedMMIAForms() throws LMISException {
        for (int i = 0; i < 10; i++) {
            RnRForm form = new RnRForm();
            form.setComments("Rnr Form" + i);
            form.setStatus(RnRForm.STATUS.AUTHORIZED);
            if (i % 2 == 0) {
                form.setSynced(true);
            }
            rnrFormRepository.create(form);
        }

        List<RnRForm> list = rnrFormRepository.deleteDeactivatedProductItemsFromUnsyncedForms();
        assertThat(list.size(), is(5));
    }

    @Test
    public void shouldGetAllMMIAForms() throws LMISException {
        Program programMMIA = new Program();
        programMMIA.setId(1l);
        Program programVIA = new Program();
        programVIA.setId(2l);

        for (int i = 0; i < 11; i++) {
            RnRForm form = new RnRForm();
            form.setComments("Rnr Form" + i);
            form.setStatus(RnRForm.STATUS.AUTHORIZED);
            if (i % 2 == 0) {
                form.setProgram(programMMIA);
            } else {
                form.setProgram(programVIA);
            }
            rnrFormRepository.create(form);
        }

        List<RnRForm> list = rnrFormRepository.listMMIA();
        assertThat(list.size(), is(6));
    }

    @Test
    public void shouldGetDraftForm() throws LMISException {
        Program program = new Program();

        RnRForm form = new RnRForm();
        form.setProgram(program);
        form.setComments("DRAFT Form");
        form.setStatus(RnRForm.STATUS.DRAFT);

        when(mockProgramRepository.queryByCode(anyString())).thenReturn(program);

        rnrFormRepository.create(form);

        RnRForm rnRForm = rnrFormRepository.queryUnAuthorized();

        assertThat(rnRForm.getComments(), is("DRAFT Form"));
    }

    @Test
    public void shouldGetSubmittedForm() throws LMISException {
        Program program = new Program();

        RnRForm form = new RnRForm();
        form.setProgram(program);
        form.setComments("Submitted Form");
        form.setStatus(RnRForm.STATUS.SUBMITTED);
        when(mockProgramRepository.queryByCode(anyString())).thenReturn(program);

        rnrFormRepository.create(form);
        RnRForm rnRForm = rnrFormRepository.queryUnAuthorized();

        assertThat(rnRForm.getComments(), is("Submitted Form"));
    }

    @Test
    public void shouldGetSignatureByRnrForm() throws LMISException {
        Program program = new Program();
        RnRForm form = new RnRForm();

        form.setProgram(program);
        form.setComments("Submitted Form");
        form.setStatus(RnRForm.STATUS.SUBMITTED);

        rnrFormRepository.create(form);

        rnrFormRepository.setSignature(form, "Submitter Signature", RnRFormSignature.TYPE.SUBMITTER);
        rnrFormRepository.setSignature(form, "Approver Signature", RnRFormSignature.TYPE.APPROVER);

        List<RnRFormSignature> signatures = rnrFormRepository.querySignaturesByRnrForm(form);

        assertThat(signatures.size(), is(2));
        assertThat(signatures.get(0).getSignature(), is("Submitter Signature"));
        assertThat(signatures.get(1).getSignature(), is("Approver Signature"));
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
    public void shouldGetStockCardsExistedInPeriodLastDay() throws Exception {
        Program program = new Program();
        program.setId(123);
        program.setProgramCode(MMIARepository.MMIA_PROGRAM_CODE);

        Date generateDate = DateUtil.parseString("20/07/2015", DateUtil.SIMPLE_DATE_FORMAT);
        Date movementDate = DateUtil.parseString("20/07/2015", DateUtil.SIMPLE_DATE_FORMAT);

        RnRForm form = RnRForm.init(program, generateDate);

        form.setPeriodBegin(DateUtil.parseString("21/06/2015", DateUtil.SIMPLE_DATE_FORMAT));
        form.setPeriodEnd(generateDate);

        List<StockCard> stockCardList = new ArrayList<>();
        StockCard stockCard = new StockCard();

        stockCardList.add(stockCard);

        when(mockStockRepository.listActiveStockCardsByProgramId(anyLong())).thenReturn(stockCardList);
        StockMovementItem stockMovementItem = new StockMovementItem();
        stockMovementItem.setMovementDate(movementDate);
        when(mockStockRepository.queryFirstStockMovementItem(stockCard)).thenReturn(stockMovementItem);

        assertThat(rnrFormRepository.getStockCardsBeforePeriodEnd(form).size(), is(1));
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
                .withMovementType(StockMovementItem.MovementType.PHYSICAL_INVENTORY)
                .withStockOnHand(stockExistence)
                .build();
        StockMovementItem issueItem = stockMovementItemBuilder
                .withDocumentNo("1")
                .withMovementReason("reason")
                .withMovementDate("10/10/2015")
                .withMovementType(StockMovementItem.MovementType.ISSUE)
                .withQuantity(issueQuantity)
                .build();
        StockMovementItem receiveItem = stockMovementItemBuilder
                .withDocumentNo("1")
                .withMovementReason("reason")
                .withMovementDate("10/11/2015")
                .withMovementType(StockMovementItem.MovementType.RECEIVE)
                .withQuantity(receiveQuantity)
                .build();
        StockMovementItem positiveItem = stockMovementItemBuilder
                .withDocumentNo("1")
                .withMovementReason("reason")
                .withMovementDate("10/12/2015")
                .withMovementType(StockMovementItem.MovementType.POSITIVE_ADJUST)
                .withQuantity(positiveQuantity)
                .build();
        StockMovementItem negativeItem = stockMovementItemBuilder
                .withDocumentNo("1")
                .withMovementReason("reason")
                .withMovementDate("10/12/2015")
                .withMovementType(StockMovementItem.MovementType.NEGATIVE_ADJUST)
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

        StockCardBuilder stockCardBuilder = new StockCardBuilder();
        StockCard stockCard = stockCardBuilder
                .setCreateDate(new Date())
                .setProduct(product)
                .setExpireDates("10/10/2016, 11/10/2016, 12/10/2017")
                .build();
        stockCard.setCreatedAt(DateUtil.parseString("10/10/2015", DateUtil.SIMPLE_DATE_FORMAT));
        stockCards.add(stockCard);

        RnRForm form = new RnRForm();
        form.setPeriodBegin(DateUtil.parseString("9/21/2015", DateUtil.SIMPLE_DATE_FORMAT));
        form.setPeriodEnd(DateUtil.parseString("10/20/2015", DateUtil.SIMPLE_DATE_FORMAT));
        form.setProgram(new Program("mmia", "mmia", null));

        when(mockStockRepository.listActiveStockCardsByProgramId(anyLong())).thenReturn(stockCards);
        DateTime dateTime = new DateTime();
        dateTime.millisOfDay();
        StockMovementItem stockMovementItem = new StockMovementItem();
        stockMovementItem.setMovementDate(dateTime.toDate());
        when(mockStockRepository.queryFirstStockMovementItem(any(StockCard.class))).thenReturn(stockMovementItem);
        when(mockStockRepository.queryStockItems(stockCard, form.getPeriodBegin(), form.getPeriodEnd())).thenReturn(stockMovementItems);

        List<RnrFormItem> rnrFormItemList = rnrFormRepository.generateRnrFormItems(form);

        RnrFormItem rnrFormItem = rnrFormItemList.get(0);
        int expectAdjustment = positiveQuantity - negativeQuantity;
        int expectInventoryQuantity = stockExistence + receiveQuantity + positiveQuantity - issueQuantity - negativeQuantity;
        int expectOrderQuantity = 2 * issueQuantity - expectInventoryQuantity;
        expectOrderQuantity = expectOrderQuantity > 0 ? expectOrderQuantity : 0;

        assertThat(rnrFormItem.getProduct(), is(product));
        assertEquals(stockExistence, rnrFormItem.getInitialAmount());
        assertEquals(issueQuantity, rnrFormItem.getIssued());
        assertEquals(receiveQuantity, rnrFormItem.getReceived());
        assertEquals(expectAdjustment, rnrFormItem.getAdjustment());
        assertEquals(expectInventoryQuantity, rnrFormItem.getInventory());
        assertEquals("10/10/2016", rnrFormItem.getValidate());
        assertEquals(expectOrderQuantity, rnrFormItem.getCalculatedOrderQuantity());
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
    public void shouldRecordUpdateTimeWhenAuthorizeRnrForm() throws Exception {
        Program program = new Program();

        RnRForm form = RnRForm.init(program, DateUtil.parseString("01/01/2015", DateUtil.SIMPLE_DATE_FORMAT));
        form.setId(1);
        form.setComments("DRAFT Form");

        form.setRnrFormItemListWrapper(new ArrayList<RnrFormItem>());
        form.setRegimenItemListWrapper(new ArrayList<RegimenItem>());
        form.setBaseInfoItemListWrapper(new ArrayList<BaseInfoItem>());

        rnrFormRepository.create(form);
        rnrFormRepository.authorise(form);

        RnRForm rnRForm = rnrFormRepository.queryRnRForm(1);
        assertThat(DateUtil.formatDate(rnRForm.getUpdatedAt(), DateUtil.SIMPLE_DATE_FORMAT), is(DateUtil.formatDate(DateUtil.today(), DateUtil.SIMPLE_DATE_FORMAT)));
    }


    @Test
    public void shouldCreateSuccess() throws Exception {
        Program program = new Program();
        RnRForm form = RnRForm.init(program, DateUtil.parseString("01/01/2015", DateUtil.SIMPLE_DATE_FORMAT));
        ArrayList<RnRForm> rnRForms = new ArrayList<>();
        rnRForms.add(form);

        rnrFormRepository.createFormAndItems(rnRForms);

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
        rnrFormRepository.createFormAndItems(rnRForms2);
        assertThat(form.getId(), is(1L));
        assertThat(form2.getId(), is(2L));
        RnRForm rnRForm = rnrFormRepository.queryRnRForm(2L);
        assertThat(rnRForm.getBaseInfoItemListWrapper().get(0).getName(), is("Name1"));
        assertThat(rnRForm.getComments(), is("Comments"));
    }

    @Test
    public void shouldGetAllSignaturesByRnrFormId() throws LMISException {
        Program program = new Program();
        RnRForm form = new RnRForm();

        form.setProgram(program);
        form.setComments("Submitted Form");
        form.setStatus(RnRForm.STATUS.SUBMITTED);

        rnrFormRepository.create(form);

        rnrFormRepository.setSignature(form, "Submitter Signature", RnRFormSignature.TYPE.SUBMITTER);
        rnrFormRepository.setSignature(form, "Approver Signature", RnRFormSignature.TYPE.APPROVER);

        List<RnRFormSignature> signatures = rnrFormRepository.querySignaturesByRnrForm(form);

        assertThat(signatures.size(), is(2));
        assertThat(signatures.get(0).getSignature(), is("Submitter Signature"));
        assertThat(signatures.get(1).getSignature(), is("Approver Signature"));
    }

    @Test
    public void shouldInitRnrFormItemWithoutMovement() throws Exception {
        StockCard stockCard = new StockCard();
        stockCard.setStockOnHand(100L);
        when(mockStockRepository.queryStockItems(any(StockCard.class), any(Date.class), any(Date.class))).thenReturn(new ArrayList<StockMovementItem>());

        RnrFormItem rnrFormItemByPeriod = rnrFormRepository.createRnrFormItemByPeriod(stockCard, new Date(), new Date());

        assertThat(rnrFormItemByPeriod.getReceived(), is(0L));
        assertThat(rnrFormItemByPeriod.getCalculatedOrderQuantity(), is(0L));
        assertThat(rnrFormItemByPeriod.getInventory(), is(100L));
        assertThat(rnrFormItemByPeriod.getInitialAmount(), is(100L));
    }

    @Test
    public void shouldDeleteDeactivatedItemsFromRnrForms() throws Exception {
        Program program = new Program();
        RnRForm form = new RnRForm();
        form.setProgram(program);
        form.setComments("Submitted Form");
        form.setStatus(RnRForm.STATUS.AUTHORIZED);
        form.setSynced(false);

        RnrFormItem deactivatedProductItem = new RnrFormItemBuilder().setProduct(new ProductBuilder().setCode("P1").setIsActive(false).build()).build();

        form.setRnrFormItemListWrapper(newArrayList(deactivatedProductItem));

        rnrFormRepository.create(form);

        rnrFormRepository.deleteDeactivatedProductItemsFromUnsyncedForms();

        verify(mockRnrFormItemRepository).delete(anyList());
    }

    @Test
    public void shouldUseTodayToInitializeRnrFormIfFeatureToggleIsOff() throws LMISException {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_requisition_period_logic_change, false);

        rnrFormRepository.programCode = "P1";
        when(mockProgramRepository.queryByCode("P1")).thenReturn(new Program());
        RnRForm rnRForm = rnrFormRepository.initRnrForm(null);
        assertThat(new DateTime(rnRForm.getPeriodBegin()).getDayOfMonth(), is(21));
    }

    @Test
    public void shouldUseInitialInventoryDateAsPeriodBeginIfNoPreviousRnrData() throws LMISException {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_requisition_period_logic_change, true);

        Program viaProgram = new ProgramBuilder().setProgramCode("ESS_MEDS").build();
        Inventory initialInventory = new Inventory();
        initialInventory.setCreatedAt(DateUtil.parseString("2020-10-20", DateUtil.DB_DATE_FORMAT));
        when(mockInventoryRepository.queryInitialInventory()).thenReturn(initialInventory);
        when(mockProgramRepository.queryByCode(viaProgram.getProgramCode())).thenReturn(viaProgram);

        Date periodEndDate = DateUtil.parseString("2020-11-24", DateUtil.DB_DATE_FORMAT);
        RnRForm rnrForm = rnrFormRepository.initRnrForm(periodEndDate);
        assertThat(rnrForm.getPeriodBegin(), is(initialInventory.getCreatedAt()));
        assertThat(rnrForm.getPeriodEnd(), is(periodEndDate));
    }

    @Test
    public void shouldUseLastRnrPeriodEndWhenPreviousRnrExists() throws Exception {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_requisition_period_logic_change, true);

        Program viaProgram = new ProgramBuilder().setProgramId(1L).setProgramCode("ESS_MEDS").build();

        when(mockProgramRepository.queryByCode(viaProgram.getProgramCode())).thenReturn(viaProgram);
        RnRForm previousRnrForm = new RnRForm();
        previousRnrForm.setProgram(viaProgram);
        previousRnrForm.setPeriodEnd(DateUtil.parseString("2020-10-20", DateUtil.DB_DATE_FORMAT));

        rnrFormRepository.create(previousRnrForm);

        RnRForm rnRForm = rnrFormRepository.initRnrForm(null);
        assertThat(rnRForm.getPeriodBegin(), is(previousRnrForm.getPeriodEnd()));
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ProgramRepository.class).toInstance(mockProgramRepository);
            bind(StockRepository.class).toInstance(mockStockRepository);
            bind(RnrFormItemRepository.class).toInstance(mockRnrFormItemRepository);
            bind(InventoryRepository.class).toInstance(mockInventoryRepository);
        }
    }
}
