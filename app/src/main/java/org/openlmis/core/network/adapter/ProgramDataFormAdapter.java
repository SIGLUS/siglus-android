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
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.inject.Inject;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Date;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ProgramDataForm;
import org.openlmis.core.model.ProgramDataForm.Status;
import org.openlmis.core.model.ProgramDataFormBasicItem;
import org.openlmis.core.model.ProgramDataFormItem;
import org.openlmis.core.model.ProgramDataFormSignature;
import org.openlmis.core.model.repository.ProgramDataFormRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import roboguice.RoboGuice;

public class ProgramDataFormAdapter implements JsonSerializer<ProgramDataForm>,
    JsonDeserializer<ProgramDataForm> {

  @Inject
  public ProgramRepository programRepository;

  @Inject
  public ProgramDataFormRepository programDataFormRepository;

  private final Gson gson;
  private final JsonParser jsonParser;

  public ProgramDataFormAdapter() {
    RoboGuice.getInjector(LMISApp.getContext()).injectMembersWithoutViews(this);
    gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
        .registerTypeAdapter(Date.class, new DateAdapter()).setDateFormat(DateFormat.LONG)
        .registerTypeAdapter(ProgramDataFormItem.class, new ProgramDataFormItemAdapter())
        .registerTypeAdapter(ProgramDataFormBasicItem.class, new ProgramDataFormBasicItemAdapter())
        .create();
    jsonParser = new JsonParser();
  }


  @Override
  public ProgramDataForm deserialize(JsonElement json, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {
    ProgramDataForm programDataForm = gson.fromJson(json.toString(), ProgramDataForm.class);

    try {
      String programCode = json.getAsJsonObject().get("programCode").getAsString();
      if (programCode.equals(Constants.RAPID_TEST_OLD_CODE)) {
        programCode = Constants.RAPID_TEST_CODE;
      }

      Program program = programRepository.queryByCode(programCode);
      programDataForm.setProgram(program);
    } catch (LMISException e) {
      new LMISException(e, "ProgramDataFormAdapter.deserialize").reportToFabric();
      throw new JsonParseException("can not find Program by programCode");
    }
    programDataForm.setStatus(Status.AUTHORIZED);
    programDataForm.setSynced(true);
    for (ProgramDataFormItem item : programDataForm.getProgramDataFormItemListWrapper()) {
      item.setForm(programDataForm);
    }

    for (ProgramDataFormSignature signature : programDataForm.getSignaturesWrapper()) {
      signature.setForm(programDataForm);
    }

    for (ProgramDataFormBasicItem basicItem : programDataForm.getFormBasicItemListWrapper()) {
      basicItem.setForm(programDataForm);
    }

    return programDataForm;
  }

  @Override
  public JsonElement serialize(ProgramDataForm src, Type typeOfSrc,
      JsonSerializationContext context) {
    try {
      return buildProgramDataFormJson(src);
    } catch (LMISException e) {
      throw new JsonParseException("can not find Signature by programDataForm");
    } catch (NullPointerException e) {
      throw new JsonParseException("No Program associated !");
    }

  }

  private JsonElement buildProgramDataFormJson(ProgramDataForm programDataForm)
      throws LMISException {
    JsonObject root = gson.toJsonTree(programDataForm).getAsJsonObject();
    String facilityId = UserInfoMgr.getInstance().getUser().getFacilityId();
    String programCode = programDataForm.getProgram().getProgramCode();

    root.addProperty("facilityId", facilityId);
    if (programCode.equals(Constants.RAPID_TEST_CODE)) {
      programCode = Constants.RAPID_TEST_OLD_CODE;
    }
    String periodBegin = DateUtil
        .formatDate(programDataForm.getPeriodBegin(), DateUtil.DB_DATE_FORMAT);
    String periodEnd = DateUtil.formatDate(programDataForm.getPeriodEnd(), DateUtil.DB_DATE_FORMAT);
    String submittedTime = DateUtil
        .formatDate(programDataForm.getSubmittedTime(), DateUtil.ISO_BASIC_DATE_TIME_FORMAT);
    root.addProperty("programCode", programCode);
    root.addProperty("periodBegin", periodBegin);
    root.addProperty("periodEnd", periodEnd);
    root.addProperty("submittedTime", submittedTime);
    root.add("programDataFormItems",
        jsonParser.parse(gson.toJson(programDataForm.getProgramDataFormItemListWrapper())));
    root.add("programDataFormBasicItems",
        jsonParser.parse(gson.toJson(programDataForm.getFormBasicItemListWrapper())));
    root.add("programDataFormSignatures",
        jsonParser.parse(gson.toJson(programDataForm.getSignaturesWrapper())));
    return root;
  }
}