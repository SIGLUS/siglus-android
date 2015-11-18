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
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRFormSignature;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class RnrFormRepositoryTest extends LMISRepositoryUnitTest {

    RnrFormRepository rnrFormRepository;
    private StockRepository mockStockRepository;

    private ProgramRepository mockProgramRepository;

    @Before
    public void setup() throws LMISException {
        mockProgramRepository = mock(ProgramRepository.class);
        mockStockRepository = mock(StockRepository.class);
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

        List<RnRForm> list = rnrFormRepository.listUnSynced();
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

        rnrFormRepository.create(form);

        RnRForm rnRForm = rnrFormRepository.queryUnAuthorized(program);

        assertThat(rnRForm.getComments(), is("DRAFT Form"));
    }

    @Test
    public void shouldGetSubmittedForm() throws LMISException {
        Program program = new Program();

        RnRForm form = new RnRForm();
        form.setProgram(program);
        form.setComments("Submitted Form");
        form.setStatus(RnRForm.STATUS.SUBMITTED);

        rnrFormRepository.create(form);

        RnRForm rnRForm = rnrFormRepository.queryUnAuthorized(program);

        assertThat(rnRForm.getComments(), is("Submitted Form"));
    }

    @Test
    public void shouldGetSubmitterSign() throws LMISException {
        Program program = new Program();
        RnRForm form = new RnRForm();

        form.setProgram(program);
        form.setComments("Submitted Form");
        form.setStatus(RnRForm.STATUS.SUBMITTED);

        rnrFormRepository.create(form);

        rnrFormRepository.setSignature(form, "Submitter Signature", RnRFormSignature.TYPE.SUBMITTER);
        rnrFormRepository.setSignature(form, "Approver Signature", RnRFormSignature.TYPE.APPROVER);

        RnRFormSignature SubmitterSign = rnrFormRepository.querySignature(form, RnRFormSignature.TYPE.SUBMITTER);
        RnRFormSignature ApproverSign = rnrFormRepository.querySignature(form, RnRFormSignature.TYPE.APPROVER);

        assertThat(SubmitterSign.getSignature(), is("Submitter Signature"));
        assertThat(ApproverSign.getSignature(), is("Approver Signature"));
    }

    @Test
    public void shouldGenerateRnRFromByLastPeriod() throws Exception {
        Date generateDate = DateUtil.parseString("10/06/2015", DateUtil.SIMPLE_DATE_FORMAT);
        RnRForm rnRForm = RnRForm.init(new Program(), generateDate);

        assertThat(DateUtil.formatDate(rnRForm.getPeriodBegin(), DateUtil.SIMPLE_DATE_FORMAT), is("21/05/2015"));
        assertThat(DateUtil.formatDate(rnRForm.getPeriodEnd(), DateUtil.SIMPLE_DATE_FORMAT), is("20/06/2015"));

        generateDate = DateUtil.parseString("30/05/2015", DateUtil.SIMPLE_DATE_FORMAT);
        rnRForm = RnRForm.init(new Program(), generateDate);

        assertThat(DateUtil.formatDate(rnRForm.getPeriodBegin(), DateUtil.SIMPLE_DATE_FORMAT), is("21/05/2015"));
        assertThat(DateUtil.formatDate(rnRForm.getPeriodEnd(), DateUtil.SIMPLE_DATE_FORMAT), is("20/06/2015"));


        generateDate = DateUtil.parseString("25/01/2015", DateUtil.SIMPLE_DATE_FORMAT);
        rnRForm = RnRForm.init(new Program(), generateDate);

        assertThat(DateUtil.formatDate(rnRForm.getPeriodBegin(), DateUtil.SIMPLE_DATE_FORMAT), is("21/12/2014"));
        assertThat(DateUtil.formatDate(rnRForm.getPeriodEnd(), DateUtil.SIMPLE_DATE_FORMAT), is("20/01/2015"));
    }

    @Test
    public void shouldGenerateRnRFromByCurrentPeriod() throws Exception {
        Date generateDate = DateUtil.parseString("30/06/2015", DateUtil.SIMPLE_DATE_FORMAT);
        RnRForm rnRForm = RnRForm.init(new Program(), generateDate);

        assertThat(DateUtil.formatDate(rnRForm.getPeriodBegin(), DateUtil.SIMPLE_DATE_FORMAT), is("21/06/2015"));
        assertThat(DateUtil.formatDate(rnRForm.getPeriodEnd(), DateUtil.SIMPLE_DATE_FORMAT), is("20/07/2015"));

        generateDate = DateUtil.parseString("05/07/2015", DateUtil.SIMPLE_DATE_FORMAT);
        rnRForm = RnRForm.init(new Program(), generateDate);

        assertThat(DateUtil.formatDate(rnRForm.getPeriodBegin(), DateUtil.SIMPLE_DATE_FORMAT), is("21/06/2015"));
        assertThat(DateUtil.formatDate(rnRForm.getPeriodEnd(), DateUtil.SIMPLE_DATE_FORMAT), is("20/07/2015"));


        generateDate = DateUtil.parseString("28/12/2015", DateUtil.SIMPLE_DATE_FORMAT);
        rnRForm = RnRForm.init(new Program(), generateDate);

        assertThat(DateUtil.formatDate(rnRForm.getPeriodBegin(), DateUtil.SIMPLE_DATE_FORMAT), is("21/12/2015"));
        assertThat(DateUtil.formatDate(rnRForm.getPeriodEnd(), DateUtil.SIMPLE_DATE_FORMAT), is("20/01/2016"));
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
    public void shouldGetStockCardsExistedInPeriod() throws Exception {
        Program program = new Program();
        program.setId(123);
        program.setProgramCode(MMIARepository.MMIA_PROGRAM_CODE);

        Date generateDate1 = DateUtil.parseString("05/07/2015", DateUtil.SIMPLE_DATE_FORMAT);
        Date generateDate2 = DateUtil.parseString("20/06/2015", DateUtil.SIMPLE_DATE_FORMAT);
        Date generateDate3 = DateUtil.parseString("21/07/2015", DateUtil.SIMPLE_DATE_FORMAT);
        RnRForm form = RnRForm.init(program, generateDate1);

        List<StockCard> stockCardList = new ArrayList<>();
        StockCard stockCard1 = new StockCard();
        stockCard1.setCreatedAt(generateDate1);

        StockCard stockCard2 = new StockCard();
        stockCard2.setCreatedAt(generateDate2);

        StockCard stockCard3 = new StockCard();
        stockCard3.setCreatedAt(generateDate3);

        stockCardList.add(stockCard1);
        stockCardList.add(stockCard2);
        stockCardList.add(stockCard3);

        when(mockStockRepository.list(program.getProgramCode())).thenReturn(stockCardList);
        assertThat(rnrFormRepository.generateRnrFormItems(form).size(), is(2));
    }

    @Test
    public void shouldGetStockCardsExistedInPeriodLastDay() throws Exception {
        Program program = new Program();
        program.setId(123);
        program.setProgramCode(MMIARepository.MMIA_PROGRAM_CODE);

        Date generateDate = DateUtil.parseString("20/07/2015", DateUtil.SIMPLE_DATE_FORMAT);
        Date generateDateWithHour = DateUtil.parseString("2015-07-20 11:33:44", DateUtil.DATE_TIME_FORMAT);

        RnRForm form = RnRForm.init(program, generateDate);

        form.setPeriodBegin(DateUtil.parseString("21/06/2015", DateUtil.SIMPLE_DATE_FORMAT));
        form.setPeriodEnd(generateDate);

        List<StockCard> stockCardList = new ArrayList<>();
        StockCard stockCard = new StockCard();
        stockCard.setCreatedAt(generateDateWithHour);

        stockCardList.add(stockCard);

        when(mockStockRepository.list(program.getProgramCode())).thenReturn(stockCardList);

        assertThat(rnrFormRepository.generateRnrFormItems(form).size(), is(1));
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

        rnrFormRepository.createFormAndItems(form);

        RnRForm form2 = RnRForm.init(program, DateUtil.parseString("01/01/2015", DateUtil.SIMPLE_DATE_FORMAT));
        ArrayList<BaseInfoItem> baseInfoItems = new ArrayList<>();
        BaseInfoItem baseInfoItem = new BaseInfoItem();
        baseInfoItem.setValue("Value1");
        baseInfoItem.setName("Name1");
        baseInfoItems.add(baseInfoItem);
        form2.setBaseInfoItemListWrapper(baseInfoItems);
        form2.setComments("Comments");
        RnRForm.fillFormId(form2);
        rnrFormRepository.createFormAndItems(form2);
        assertThat(form.getId(), is(1L));
        assertThat(form2.getId(), is(2L));
        RnRForm rnRForm = rnrFormRepository.queryRnRForm(2L);
        assertThat(rnRForm.getBaseInfoItemListWrapper().get(0).getName(),is("Name1"));
        assertThat(rnRForm.getComments(),is("Comments"));
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
        assertThat(signatures.get(0).getSignature(),is("Submitter Signature"));
        assertThat(signatures.get(1).getSignature(),is("Approver Signature"));
    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ProgramRepository.class).toInstance(mockProgramRepository);
            bind(StockRepository.class).toInstance(mockStockRepository);
        }
    }
}
