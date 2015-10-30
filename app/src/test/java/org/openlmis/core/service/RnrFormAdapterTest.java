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
package org.openlmis.core.service;

import com.google.gson.JsonElement;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.User;
import org.openlmis.core.model.repository.MMIARepository;
import org.openlmis.core.network.adapter.RnrFormAdapter;
import org.openlmis.core.utils.DateUtil;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;

import roboguice.RoboGuice;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(LMISTestRunner.class)
public class RnrFormAdapterTest {
    private RnrFormAdapter rnrFormAdapter;
    private RnRForm rnRForm;
    private Program program;

    @Before
    public void setUp() throws LMISException {
        rnrFormAdapter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(RnrFormAdapter.class);
        rnRForm = new RnRForm();
        program = new Program();
        program.setProgramCode(MMIARepository.MMIA_PROGRAM_CODE);
        rnRForm.setProgram(program);
    }

    @Test
    public void shouldSerializeRnrFormWithCommentsToJsonObject() throws LMISException {

        UserInfoMgr.getInstance().setUser(new User("user", "password"));
        rnRForm.setComments("XYZ");
        rnRForm.setSubmittedTime(DateUtil.today());

        JsonElement rnrJson = rnrFormAdapter.serialize(rnRForm, RnRForm.class, null);
        assertEquals("\"XYZ\"", rnrJson.getAsJsonObject().get("clientSubmittedNotes").toString());
    }

    @Test
    public void shouldSerializeRnrFormWithSubmittedTime() throws Exception {
        UserInfoMgr.getInstance().setUser(new User("user", "password"));

        rnRForm.setSubmittedTime(DateUtil.parseString("2015-10-14 01:01:11", "yyyy-MM-dd HH:mm:ss"));

        JsonElement rnrJson = rnrFormAdapter.serialize(rnRForm, RnRForm.class, null);
        assertThat(rnrJson.getAsJsonObject().get("clientSubmittedTime").toString(), is("\"2015-10-14 01:01:11\""));
    }

    @Test
    public void shouldSerializeRnrFormWithExpirationDate() throws Exception {
        UserInfoMgr.getInstance().setUser(new User("user", "password"));

        ArrayList<RnrFormItem> rnrFormItemListWrapper=new ArrayList<>();
        RnrFormItem rnrFormItem = new RnrFormItem();
        rnrFormItem.setProduct(new Product());
        rnrFormItem.setValidate("10/11/2015");
        rnrFormItemListWrapper.add(rnrFormItem);
        rnRForm.setRnrFormItemListWrapper(rnrFormItemListWrapper);

        rnRForm.setSubmittedTime(DateUtil.parseString("2015-10-14 01:01:11", "yyyy-MM-dd HH:mm:ss"));

        JsonElement rnrJson = rnrFormAdapter.serialize(rnRForm, RnRForm.class, null);
        assertThat(rnrJson.getAsJsonObject().get("products").getAsJsonArray().get(0).getAsJsonObject().get("expirationDate").toString(), is("\"10/11/2015\""));
    }
}
