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

package org.openlmis.core.network.adapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.inject.Inject;
import java.lang.reflect.Type;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.TestConsumptionItem;
import org.openlmis.core.model.UsageColumnsMap;
import org.openlmis.core.model.repository.UsageColumnsMapRepository;
import roboguice.RoboGuice;

public class TestConsumptionLineItemAdapter implements JsonSerializer<TestConsumptionItem>,
    JsonDeserializer<TestConsumptionItem> {

  public static final String TEST_OUTCOME = "testOutcome";
  public static final String TEST_PROJECT = "testProject";

  private final Gson gson;

  @Inject
  public UsageColumnsMapRepository usageColumnsMapRepository;

  public TestConsumptionLineItemAdapter() {
    RoboGuice.getInjector(LMISApp.getContext()).injectMembersWithoutViews(this);
    gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
  }

  @Override
  public JsonElement serialize(TestConsumptionItem testConsumptionLineItem, Type typeOfSrc,
                               JsonSerializationContext context) {
    JsonObject result = gson.toJsonTree(testConsumptionLineItem).getAsJsonObject();
    UsageColumnsMap usageColumnsMap = testConsumptionLineItem.getUsageColumnsMap();
    result.addProperty(TEST_OUTCOME, usageColumnsMap.getTestOutcome());
    result.addProperty(TEST_PROJECT, usageColumnsMap.getTestProject());
    return result;
  }

  @Override
  public TestConsumptionItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
          throws JsonParseException {
    TestConsumptionItem testConsumptionLineItem = gson.fromJson(json, TestConsumptionItem.class);

    try {
      JsonObject jsonObject = json.getAsJsonObject();
      String testOutcome = jsonObject.get(TEST_OUTCOME).getAsString();
      String testProject = jsonObject.get(TEST_PROJECT).getAsString();
      UsageColumnsMap usageColumnsMap = usageColumnsMapRepository
              .getByCode(testOutcome.concat("_").concat(testProject));
      testConsumptionLineItem.setUsageColumnsMap(usageColumnsMap);
    } catch (LMISException e) {
      new LMISException(e, "testConsumptionLineItem.deserialize").reportToFabric();
      throw new JsonParseException("can not find testConsumptionLineItem by name and category", e);
    }
    return testConsumptionLineItem;
  }
}
