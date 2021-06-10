/*
 *
 *  * This program is part of the OpenLMIS logistics management information
 *  * system platform software.
 *  *
 *  * Copyright © 2015 ThoughtWorks, Inc.
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU Affero General Public License as published
 *  * by the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version. This program is distributed in the
 *  * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 *  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  * See the GNU Affero General Public License for more details. You should
 *  * have received a copy of the GNU Affero General Public License along with
 *  * this program. If not, see http://www.gnu.org/licenses. For additional
 *  * information contact info@OpenLMIS.org
 *
 */

package org.openlmis.core.network.adapter;

import com.google.gson.JsonParser;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Program;
import org.openlmis.core.network.ProgramCacheManager;
import org.openlmis.core.network.model.SyncDownLatestProductsResponse;
import org.openlmis.core.utils.JsonFileReader;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(LMISTestRunner.class)
public class ProductsResponseAdapterTest {

    @Test
    public void addPrograms() {
        final Program program = new Program();
        program.setProgramCode("VC");
        final ArrayList<Program> programs = new ArrayList<>();
        programs.add(program);
        ProgramCacheManager.addPrograms(programs);
        assertThat(ProgramCacheManager.getPrograms("VC"), Matchers.is(program));
    }

    @Test
    public void deserialize() {
        ProductsResponseAdapter stockMovementItemAdapter = new ProductsResponseAdapter();
        String json = JsonFileReader.readJson(getClass(), "V3ProductsResponseAdapterTest.json");
        SyncDownLatestProductsResponse syncDownLatestProductsResponse = stockMovementItemAdapter.deserialize(new JsonParser().parse(json), null, null);

        assertThat(syncDownLatestProductsResponse.getLastSyncTime(),Matchers.is("1623115749445"));
        assertThat(syncDownLatestProductsResponse.getLatestProducts().get(0).getProduct().getCode(),Matchers.is("22A07"));
        assertThat(syncDownLatestProductsResponse.getLatestProducts().get(1).getProduct().getCode(),Matchers.is("26B01"));
    }
}