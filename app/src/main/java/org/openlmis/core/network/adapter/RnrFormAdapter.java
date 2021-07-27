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

import static org.openlmis.core.model.repository.VIARepository.ATTR_CONSULTATION;
import static org.openlmis.core.utils.Constants.MMIA_PROGRAM_CODE;
import static org.openlmis.core.utils.Constants.VIA_PROGRAM_CODE;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.BaseInfoItem.TYPE;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRForm.Status;
import org.openlmis.core.model.RnRFormSignature;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RnrFormSignatureRepository;
import org.openlmis.core.network.model.PatientLineItemRequest;
import org.openlmis.core.utils.DateUtil;
import roboguice.RoboGuice;

public class RnrFormAdapter implements JsonSerializer<RnRForm>, JsonDeserializer<RnRForm> {

  public static final String ACTUAL_START_DATE = "actualStartDate";
  public static final String ACTUAL_END_DATE = "actualEndDate";

  @Inject
  public ProgramRepository programRepository;
  @Inject
  RnrFormSignatureRepository signatureRepository;

  private final Gson gson;
  private final JsonParser jsonParser;

  public RnrFormAdapter() {
    RoboGuice.getInjector(LMISApp.getContext()).injectMembersWithoutViews(this);
    gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
        .registerTypeAdapter(Date.class, new DateAdapter()).setDateFormat(DateFormat.LONG)
        .registerTypeAdapter(RegimenItem.class, new RegimenItemAdapter())
        .registerTypeAdapter(RnrFormItem.class, new RnrFormItemAdapter())
        .create();
    jsonParser = new JsonParser();
  }

  @Override
  public JsonElement serialize(RnRForm rnRForm, Type typeOfSrc, JsonSerializationContext context) {
    try {
      return buildRnrFormJson(rnRForm);
    } catch (LMISException e) {
      throw new JsonParseException("can not find Signature by rnrForm", e);
    } catch (NullPointerException e) {
      throw new JsonParseException("No Program associated !", e);
    }
  }

  @Override
  public RnRForm deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    RnRForm rnRForm = gson.fromJson(json.toString(), RnRForm.class);
    RnRForm.fillFormId(rnRForm);
    try {
      Program program = programRepository.queryByCode(json.getAsJsonObject().get("programCode").getAsString());
      rnRForm.setProgram(program);
    } catch (LMISException e) {
      new LMISException(e, "RnrFormAdapter.deserialize").reportToFabric();
      throw new JsonParseException("can not find Program by programCode", e);
    }
    JsonObject jsonObject = json.getAsJsonObject();
    setPeriodTime(rnRForm, jsonObject);
    setBaseInfoForRnR(rnRForm, jsonObject);
    rnRForm.setStatus(Status.AUTHORIZED);
    rnRForm.setSynced(true);

    return rnRForm;
  }

  private void setPeriodTime(RnRForm rnRForm, JsonObject jsonObject) {
    rnRForm.setPeriodBegin(Instant.parse(jsonObject.get(ACTUAL_START_DATE).getAsString()).toDate());
    rnRForm.setPeriodEnd(Instant.parse(jsonObject.get(ACTUAL_END_DATE).getAsString()).toDate());
    rnRForm.setSubmittedTime(Instant.parse(jsonObject.get("clientSubmittedTime").getAsString()).toDate());
  }

  private void setBaseInfoForRnR(RnRForm rnRForm, JsonObject jsonObject) {
    if (rnRForm.getProgram().getProgramCode().equals(VIA_PROGRAM_CODE)) {
      Long consultationNumber = jsonObject.get("consultationNumber").getAsLong();
      BaseInfoItem baseInfoItem = new BaseInfoItem();
      baseInfoItem.setName(ATTR_CONSULTATION);
      baseInfoItem.setType(TYPE.STRING);
      baseInfoItem.setValue(String.valueOf(consultationNumber));
      baseInfoItem.setRnRForm(rnRForm);
      rnRForm.setBaseInfoItemListWrapper(Arrays.asList(baseInfoItem));
    }
  }

  private JsonElement buildRnrFormJson(RnRForm rnRForm) throws LMISException {
    JsonObject root = gson.toJsonTree(rnRForm).getAsJsonObject();
    String programCode = rnRForm.getProgram().getProgramCode();
    root.addProperty("programCode", programCode);
    root.addProperty(ACTUAL_START_DATE, DateUtil.formatDate(rnRForm.getPeriodBegin(), DateUtil.DB_DATE_FORMAT));
    root.addProperty(ACTUAL_END_DATE, DateUtil.formatDate(rnRForm.getPeriodEnd(), DateUtil.DB_DATE_FORMAT));
    DateTime submittedTime = new DateTime(rnRForm.getSubmittedTime());
    root.addProperty("clientSubmittedTime", String.valueOf(submittedTime.toInstant()));
    sectionInfo(rnRForm, root, programCode);
    List<RnRFormSignature> signatureList = signatureRepository.queryByRnrFormId(rnRForm.getId());
    root.add("signatures", jsonParser.parse(gson.toJson(signatureList)));
    return root;
  }

  private void sectionInfo(RnRForm rnRForm, JsonObject root, String programCode) {
    root.add("products", jsonParser.parse(gson.toJson(rnRForm.getRnrFormItemListWrapper())));
    root.add("regimens", jsonParser.parse(gson.toJson(rnRForm.getRegimenItemListWrapper())));
    root.add("therapeuticLines", jsonParser.parse(gson.toJson(rnRForm.getRegimenThreeLineListWrapper())));
    if (programCode.endsWith(VIA_PROGRAM_CODE)) {
      root.addProperty("consultationNumber", getConsultationNumber(rnRForm));
    }
    setPatientLineItem(programCode, root, rnRForm);
  }

  private Long getConsultationNumber(RnRForm rnRForm) {
    if (rnRForm != null && !rnRForm.getBaseInfoItemListWrapper().isEmpty()) {
      return Long.valueOf(rnRForm.getBaseInfoItemListWrapper().get(0).getValue());
    }
    return null;
  }

  private void setPatientLineItem(String programCode, JsonObject root, RnRForm rnRForm) {
    if (programCode.endsWith(MMIA_PROGRAM_CODE) && rnRForm != null
        && !rnRForm.getBaseInfoItemListWrapper().isEmpty()) {
      HashMap<String, List<BaseInfoItem>> tableNameToItems = groupPatientInfo(rnRForm);
      List<PatientLineItemRequest> patientLineItemRequests = new ArrayList<>();
      for (Map.Entry<String, List<BaseInfoItem>> map : tableNameToItems.entrySet()) {
        patientLineItemRequests.add(new PatientLineItemRequest(map.getKey(), map.getValue()));
      }
      root.add("patientLineItems", jsonParser.parse(gson.toJson(patientLineItemRequests)));
    }
  }

  @NotNull
  private HashMap<String, List<BaseInfoItem>> groupPatientInfo(RnRForm rnRForm) {
    List<BaseInfoItem> baseInfoItems = rnRForm.getBaseInfoItemListWrapper();
    HashMap<String, List<BaseInfoItem>> tableNameToItems = new HashMap<>();
    for (BaseInfoItem infoItem : baseInfoItems) {
      if (tableNameToItems.containsKey(infoItem.getTableName())) {
        List<BaseInfoItem> list = new ArrayList<>();
        list.add(infoItem);
        tableNameToItems.put(infoItem.getTableName(), list);
      } else {
        tableNameToItems.get(infoItem.getTableName()).add(infoItem);
      }
    }
    return tableNameToItems;
  }

}