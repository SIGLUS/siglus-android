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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;

import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(LMISTestRunner.class)
public class RnrFormRepositoryTest extends LMISRepositoryUnitTest {

    RnrFormRepository rnrFormRepository;

    @Before
    public void setup() throws LMISException {
        rnrFormRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(RnrFormRepository.class);
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
    public void shouldGetDraftMMIAForms() throws LMISException {
        Program program = new Program();

        RnRForm form = new RnRForm();
        form.setProgram(program);
        form.setComments("DRAFT Form");
        form.setStatus(RnRForm.STATUS.DRAFT);

        rnrFormRepository.create(form);

        RnRForm rnRForm = rnrFormRepository.queryDraft(program);

        assertThat(rnRForm.getComments(), is("DRAFT Form"));
    }


    @Test
    public void shouldGenerateRnRFromByLastPeriod() throws Exception{
        Date generateDate = DateUtil.parseString("10/06/2015", DateUtil.DEFAULT_DATE_FORMAT);
        RnRForm rnRForm = RnRForm.init(new Program(), generateDate);

        assertThat(DateUtil.formatDate(rnRForm.getPeriodBegin()), is("21/05/2015"));
        assertThat(DateUtil.formatDate(rnRForm.getPeriodEnd()), is("20/06/2015"));

        generateDate = DateUtil.parseString("30/05/2015", DateUtil.DEFAULT_DATE_FORMAT);
        rnRForm = RnRForm.init(new Program(), generateDate);

        assertThat(DateUtil.formatDate(rnRForm.getPeriodBegin()), is("21/05/2015"));
        assertThat(DateUtil.formatDate(rnRForm.getPeriodEnd()), is("20/06/2015"));
    }

    @Test
    public void shouldGenerateRnRFromByCurrentPeriod() throws Exception{
        Date generateDate = DateUtil.parseString("30/06/2015", DateUtil.DEFAULT_DATE_FORMAT);
        RnRForm rnRForm = RnRForm.init(new Program(), generateDate);

        assertThat(DateUtil.formatDate(rnRForm.getPeriodBegin()), is("21/06/2015"));
        assertThat(DateUtil.formatDate(rnRForm.getPeriodEnd()), is("20/07/2015"));

        generateDate = DateUtil.parseString("05/07/2015", DateUtil.DEFAULT_DATE_FORMAT);
        rnRForm = RnRForm.init(new Program(), generateDate);

        assertThat(DateUtil.formatDate(rnRForm.getPeriodBegin()), is("21/06/2015"));
        assertThat(DateUtil.formatDate(rnRForm.getPeriodEnd()), is("20/07/2015"));
    }

    @Test
    public void shouldReturnFalseIfThereIsAAuthorizedFormExisted() throws Exception{
        Program program = new Program();
        program.setId(123);

        Date generateDate = DateUtil.parseString("05/07/2015", DateUtil.DEFAULT_DATE_FORMAT);

        RnRForm form = RnRForm.init(program, generateDate);
        form.setStatus(RnRForm.STATUS.AUTHORIZED);
        rnrFormRepository.create(form);

        generateDate = DateUtil.parseString("20/07/2015", DateUtil.DEFAULT_DATE_FORMAT);

        RnRForm rnRForm2 = RnRForm.init(program, generateDate);

        assertThat(rnrFormRepository.isPeriodUnique(rnRForm2), is(false));
    }

    @Test
    public void shouldReturnTrueIfThereIsNoAuthorizedFormExisted() throws Exception{
        Program program = new Program();
        program.setId(123);

        Date generateDate = DateUtil.parseString("05/07/2015", DateUtil.DEFAULT_DATE_FORMAT);

        RnRForm form = RnRForm.init(program, generateDate);
        form.setStatus(RnRForm.STATUS.DRAFT);
        rnrFormRepository.create(form);

        generateDate = DateUtil.parseString("20/07/2015", DateUtil.DEFAULT_DATE_FORMAT);

        RnRForm rnRForm2 = RnRForm.init(program, generateDate);

        assertThat(rnrFormRepository.isPeriodUnique(rnRForm2), is(true));
    }
}
