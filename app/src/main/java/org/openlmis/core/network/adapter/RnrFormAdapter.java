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

import static org.openlmis.core.constant.ReportConstants.KEY_AGE_GROUP_PROPHYLAXIS;
import static org.openlmis.core.constant.ReportConstants.KEY_AGE_GROUP_TREATMENT;
import static org.openlmis.core.constant.ReportConstants.KEY_SERVICE_ADULT;
import static org.openlmis.core.constant.ReportConstants.KEY_SERVICE_LESS_THAN_25;
import static org.openlmis.core.constant.ReportConstants.KEY_SERVICE_MORE_THAN_25;
import static org.openlmis.core.model.repository.VIARepository.ATTR_CONSULTATION;
import static org.openlmis.core.utils.Constants.AL_PROGRAM_CODE;
import static org.openlmis.core.utils.Constants.MMIA_PROGRAM_CODE;
import static org.openlmis.core.utils.Constants.MMTB_PROGRAM_CODE;
import static org.openlmis.core.utils.Constants.PARAM_PROGRAM_CODE;
import static org.openlmis.core.utils.Constants.REGIMEN_INFORMATION_TO_REGIMEN_CODE;
import static org.openlmis.core.utils.Constants.VIA_PROGRAM_CODE;

import androidx.annotation.NonNull;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
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
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.BaseInfoItem.TYPE;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RegimenItemThreeLines;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRForm.Status;
import org.openlmis.core.model.RnRFormSignature;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.TestConsumptionItem;
import org.openlmis.core.model.repository.MMIARepository;
import org.openlmis.core.model.repository.MMTBRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RegimenRepository;
import org.openlmis.core.model.repository.RnrFormSignatureRepository;
import org.openlmis.core.network.model.PatientLineItemRequest;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.ALReportViewModel.ALItemType;
import org.roboguice.shaded.goole.common.collect.FluentIterable;
import roboguice.RoboGuice;

public class RnrFormAdapter implements JsonSerializer<RnRForm>, JsonDeserializer<RnRForm> {

  public static final String ACTUAL_START_DATE = "actualStartDate";
  public static final String ACTUAL_END_DATE = "actualEndDate";
  public static final String PATIENT_LINE_ITEMS = "patientLineItems";
  public static final String AGE_GROUP_LINE_ITEMS = "ageGroupLineItems";
  public static final String NAME = "name";
  public static final String COLUMNS = "columns";
  public static final String SERVICE = "service";
  public static final String GROUP = "group";
  public static final String VALUE = "value";
  public static final String SIGNATURES = "signatures";
  public static final String CLIENT_SUBMITTED_TIME = "clientSubmittedTime";
  public static final String PRODUCTS = "products";
  public static final String REGIMEN_LINE_ITEMS = "regimenLineItems";
  public static final String REGIMEN_SUMMARY_LINE_ITEMS = "regimenSummaryLineItems";
  public static final String TEST_CONSUMPTION_LINE_ITEMS = "testConsumptionLineItems";
  public static final String USAGE_INFORMATION_LINE_ITEMS = "usageInformationLineItems";
  public static final String CONSULTATION_NUMBER = "consultationNumber";
  private final Gson gson;
  private final JsonParser jsonParser;

  @Inject
  public ProgramRepository programRepository;
  @Inject
  RnrFormSignatureRepository signatureRepository;
  @Inject
  MMIARepository mmiaRepository;
  @Inject
  RegimenRepository regimenRepository;

  public RnrFormAdapter() {
    RoboGuice.getInjector(LMISApp.getContext()).injectMembersWithoutViews(this);
    gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().registerTypeAdapter(Date.class, new DateAdapter())
        .setDateFormat(DateFormat.LONG).registerTypeAdapter(RegimenItem.class, new RegimenItemAdapter())
        .registerTypeAdapter(RnrFormItem.class, new RnrFormItemAdapter())
        .registerTypeAdapter(TestConsumptionItem.class, new TestConsumptionLineItemAdapter())
        .registerTypeAdapter(Status.class, new RnrFormStatusAdapter())
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
    setInfoForRnR(rnRForm, jsonObject);
    rnRForm.setSynced(true);

    return rnRForm;
  }

  private void setPeriodTime(RnRForm rnRForm, JsonObject jsonObject) {
    rnRForm.setPeriodBegin(Instant.parse(jsonObject.get(ACTUAL_START_DATE).getAsString()).toDate());
    rnRForm.setPeriodEnd(Instant.parse(jsonObject.get(ACTUAL_END_DATE).getAsString()).toDate());
    rnRForm.setSubmittedTime(Instant.parse(jsonObject.get(CLIENT_SUBMITTED_TIME).getAsString()).toDate());
  }

  private void setInfoForRnR(RnRForm rnRForm, JsonObject jsonObject) {
    setBaseInfoForVIA(rnRForm, jsonObject);
    setBaseInfoForMMIA(rnRForm, jsonObject);
    setRegimenLineItemsForMalaria(rnRForm, jsonObject);
    setBaseInfoForMMTB(rnRForm, jsonObject);
    setAgeGroupInfoForMMTB(rnRForm, jsonObject);
  }

  private void setBaseInfoForVIA(RnRForm rnRForm, JsonObject jsonObject) {
    if (rnRForm.getProgram().getProgramCode().equals(VIA_PROGRAM_CODE)) {
      BaseInfoItem baseInfoItem = new BaseInfoItem();
      baseInfoItem.setName(ATTR_CONSULTATION);
      baseInfoItem.setType(TYPE.STRING);
      if (jsonObject.has(CONSULTATION_NUMBER)) {
        Long consultationNumber = jsonObject.get(CONSULTATION_NUMBER).getAsLong();
        baseInfoItem.setValue(String.valueOf(consultationNumber));
      }
      baseInfoItem.setRnRForm(rnRForm);
      rnRForm.setBaseInfoItemListWrapper(Arrays.asList(baseInfoItem));
    }
  }

  private void setBaseInfoForMMIA(RnRForm rnRForm, JsonObject jsonObject) {
    if (rnRForm.getProgram().getProgramCode().equals(MMIA_PROGRAM_CODE)) {
      List<BaseInfoItem> baseInfoItems = new ArrayList<>();
      for (JsonElement jsonPatient : jsonObject.get(PATIENT_LINE_ITEMS).getAsJsonArray()) {
        JsonObject jsonObjectForPatient = jsonPatient.getAsJsonObject();
        String componentName = jsonObjectForPatient.get(NAME).getAsString();
        Map<String, Integer> tableNameToDisplayOrder = mmiaRepository.getDisplayOrderMap();
        for (JsonElement column : jsonObjectForPatient.get(COLUMNS).getAsJsonArray()) {
          JsonObject jsonColumn = column.getAsJsonObject();
          JsonElement tableNameJson = jsonColumn.get(NAME);
          if (isNullOrNullJson(tableNameJson)) {
            continue;
          }
          String tableName = tableNameJson.getAsString();
          BaseInfoItem baseInfoItem = new BaseInfoItem(tableName, TYPE.STRING, rnRForm, componentName,
              tableNameToDisplayOrder.containsKey(tableName) ? tableNameToDisplayOrder.get(tableName) : 0);

          JsonElement valueJson = jsonColumn.get(VALUE);
          if (isNullOrNullJson(valueJson)) {
            continue;
          }
          baseInfoItem.setValue(valueJson.getAsString());
          baseInfoItems.add(baseInfoItem);
        }
      }
      rnRForm.setBaseInfoItemListWrapper(baseInfoItems);
    }
  }

  private boolean isNullOrNullJson(JsonElement jsonElement) {
    return jsonElement == null || jsonElement.isJsonNull();
  }

  private void setRegimenLineItemsForMalaria(RnRForm rnRForm, JsonObject jsonObject) {
    if (AL_PROGRAM_CODE.equals(rnRForm.getProgram().getProgramCode())) {
      List<RegimenItem> regimenItems = new ArrayList<>();
      for (JsonElement jsonRegimenItem : jsonObject.get(USAGE_INFORMATION_LINE_ITEMS).getAsJsonArray()) {
        JsonObject jsonObjectForRegimenItem = jsonRegimenItem.getAsJsonObject();
        RegimenItem regimenItem = RegimenItem.builder().form(rnRForm).regimen(getRegimen(jsonObjectForRegimenItem))
            .amount(calculateAmount(jsonObjectForRegimenItem))
            .hf(jsonObjectForRegimenItem.get(ALItemType.HF.getName()).getAsLong())
            .chw(jsonObjectForRegimenItem.get(ALItemType.CHW.getName()).getAsLong()).build();
        regimenItems.add(regimenItem);
      }
      rnRForm.setRegimenItemListWrapper(regimenItems);
    }
  }

  private void setBaseInfoForMMTB(RnRForm rnRForm, JsonObject jsonObject) {
    if (!MMTB_PROGRAM_CODE.equals(rnRForm.getProgram().getProgramCode())) {
      return;
    }
    List<BaseInfoItem> baseInfoItems = new ArrayList<>();
    for (JsonElement jsonPatient : jsonObject.get(PATIENT_LINE_ITEMS).getAsJsonArray()) {
      JsonObject jsonObjectForPatient = jsonPatient.getAsJsonObject();
      String tableName = jsonObjectForPatient.get(NAME).getAsString();
      for (JsonElement column : jsonObjectForPatient.get(COLUMNS).getAsJsonArray()) {
        JsonObject jsonColumn = column.getAsJsonObject();
        JsonElement jsonValue = jsonColumn.get(VALUE);
        if (jsonValue.isJsonNull()) {
          continue;
        }
        String columnName = jsonColumn.get(NAME).getAsString();
        BaseInfoItem baseInfoItem = new BaseInfoItem(columnName, TYPE.STRING, rnRForm, tableName,
            MMTBRepository.getDisplayOrderByKey(columnName));
        baseInfoItem.setValue(jsonValue.getAsString());
        baseInfoItems.add(baseInfoItem);
      }
    }
    rnRForm.setBaseInfoItemListWrapper(baseInfoItems);
  }

  private void setAgeGroupInfoForMMTB(RnRForm rnRForm, JsonObject jsonObject) {
    if (!MMTB_PROGRAM_CODE.equals(rnRForm.getProgram().getProgramCode())) {
      return;
    }
    ArrayList<RegimenItemThreeLines> threeLines = new ArrayList<>();
    RegimenItemThreeLines adult = new RegimenItemThreeLines(KEY_SERVICE_ADULT);
    adult.setForm(rnRForm);
    threeLines.add(adult);
    RegimenItemThreeLines lessThan25 = new RegimenItemThreeLines(KEY_SERVICE_LESS_THAN_25);
    lessThan25.setForm(rnRForm);
    threeLines.add(lessThan25);
    RegimenItemThreeLines moreThan25 = new RegimenItemThreeLines(KEY_SERVICE_MORE_THAN_25);
    moreThan25.setForm(rnRForm);
    threeLines.add(moreThan25);
    for (JsonElement item : jsonObject.get(AGE_GROUP_LINE_ITEMS).getAsJsonArray()) {
      JsonObject jsonThreeLineItem = item.getAsJsonObject();
      String service = jsonThreeLineItem.get(SERVICE).getAsString();
      String group = jsonThreeLineItem.get(GROUP).getAsString();
      String value = jsonThreeLineItem.get(VALUE).getAsString();
      switch (service) {
        case KEY_SERVICE_ADULT:
          setThreeLineItemData(adult, group, value);
          break;
        case KEY_SERVICE_LESS_THAN_25:
          setThreeLineItemData(lessThan25, group, value);
          break;
        default:
          setThreeLineItemData(moreThan25, group, value);
          break;
      }
    }
    rnRForm.setRegimenThreeLinesWrapper(threeLines);
  }

  private void setThreeLineItemData(RegimenItemThreeLines threeLine, String group, String value) {
    long longValue;
    try {
      longValue = Long.parseLong(value);
    } catch (Exception e) {
      longValue = 0;
    }
    if (KEY_AGE_GROUP_TREATMENT.equals(group)) {
      threeLine.setPatientsAmount(longValue);
    } else {
      threeLine.setPharmacyAmount(longValue);
    }
  }

  private Regimen getRegimen(JsonObject jsonObjectForRegimenItem) {
    String regimenCodeKey =
        jsonObjectForRegimenItem.get("information").getAsString() + "_" + jsonObjectForRegimenItem.get("productCode")
            .getAsString();
    String regimenCode = REGIMEN_INFORMATION_TO_REGIMEN_CODE.get(regimenCodeKey);
    Regimen regimen;
    try {
      regimen = regimenRepository.getByCode(regimenCode);
    } catch (LMISException e) {
      new LMISException(e, "RnrFormAdapter.deserialize").reportToFabric();
      throw new JsonParseException("can not find Regimen by regimenCode", e);
    }
    return regimen;
  }

  private Long calculateAmount(JsonObject jsonObjectForRegimenItem) {
    return jsonObjectForRegimenItem.get(ALItemType.HF.getName()).getAsLong() + jsonObjectForRegimenItem.get(
        ALItemType.CHW.getName()).getAsLong();
  }

  private JsonElement buildRnrFormJson(RnRForm rnRForm) throws LMISException {
    JsonObject root = gson.toJsonTree(rnRForm).getAsJsonObject();
    String programCode = rnRForm.getProgram().getProgramCode();
    root.addProperty(PARAM_PROGRAM_CODE, programCode);
    root.addProperty(ACTUAL_START_DATE, DateUtil.formatDate(rnRForm.getPeriodBegin(), DateUtil.DB_DATE_FORMAT));
    root.addProperty(ACTUAL_END_DATE, DateUtil.formatDate(rnRForm.getPeriodEnd(), DateUtil.DB_DATE_FORMAT));
    DateTime submittedTime = new DateTime(rnRForm.getSubmittedTime());
    root.addProperty(CLIENT_SUBMITTED_TIME, String.valueOf(submittedTime.toInstant()));
    sectionInfo(rnRForm, root, programCode);
    List<RnRFormSignature> signatureList = signatureRepository.queryByRnrFormId(rnRForm.getId());
    root.add(SIGNATURES, jsonParser.parse(gson.toJson(signatureList)));
    return root;
  }

  private void sectionInfo(RnRForm rnRForm, JsonObject root, String programCode) {
    if (AL_PROGRAM_CODE.equals(programCode)) {
      root.add(USAGE_INFORMATION_LINE_ITEMS, buildUsageInformationLineItems(rnRForm));
      return;
    }
    if (MMTB_PROGRAM_CODE.equals(programCode)) {
      root.add(AGE_GROUP_LINE_ITEMS, buildAgeGroupLineItems(rnRForm));
    } else {
      root.add(REGIMEN_LINE_ITEMS, jsonParser.parse(gson.toJson(rnRForm.getRegimenItemListWrapper())));
      root.add(REGIMEN_SUMMARY_LINE_ITEMS, jsonParser.parse(gson.toJson(rnRForm.getRegimenThreeLineListWrapper())));
      root.add(TEST_CONSUMPTION_LINE_ITEMS, jsonParser.parse(gson.toJson(rnRForm.getTestConsumptionItemList())));
      if (programCode.endsWith(VIA_PROGRAM_CODE)) {
        root.addProperty(CONSULTATION_NUMBER, getConsultationNumber(rnRForm));
      }
    }
    root.add(PRODUCTS, jsonParser.parse(gson.toJson(rnRForm.getRnrFormItemListWrapper())));
    setPatientLineItem(programCode, root, rnRForm);
  }

  private JsonElement buildUsageInformationLineItems(RnRForm rnRForm) {
    List<RegimenItem> regimenItems = rnRForm.getRegimenItemListWrapper();
    regimenItems = FluentIterable.from(regimenItems).transform(RegimenItem::buildInformationAndProductCode).toList();
    return jsonParser.parse(gson.toJson(regimenItems));
  }

  private JsonElement buildAgeGroupLineItems(RnRForm rnrForm) {
    JsonArray ageGroup = new JsonArray();
    for (RegimenItemThreeLines item : rnrForm.getRegimenThreeLineListWrapper()) {
      switch (item.getRegimeTypes()) {
        case KEY_SERVICE_ADULT:
          addAgeGroupByGroup(ageGroup, KEY_SERVICE_ADULT, KEY_AGE_GROUP_TREATMENT, item.getPatientsAmount());
          addAgeGroupByGroup(ageGroup, KEY_SERVICE_ADULT, KEY_AGE_GROUP_PROPHYLAXIS, item.getPharmacyAmount());
          break;
        case KEY_SERVICE_LESS_THAN_25:
          addAgeGroupByGroup(ageGroup, KEY_SERVICE_LESS_THAN_25, KEY_AGE_GROUP_TREATMENT, item.getPatientsAmount());
          addAgeGroupByGroup(ageGroup, KEY_SERVICE_LESS_THAN_25, KEY_AGE_GROUP_PROPHYLAXIS, item.getPharmacyAmount());
          break;
        default:
          addAgeGroupByGroup(ageGroup, KEY_SERVICE_MORE_THAN_25, KEY_AGE_GROUP_TREATMENT, item.getPatientsAmount());
          addAgeGroupByGroup(ageGroup, KEY_SERVICE_MORE_THAN_25, KEY_AGE_GROUP_PROPHYLAXIS, item.getPharmacyAmount());
          break;
      }
    }
    return ageGroup;
  }

  private void addAgeGroupByGroup(JsonArray jsonArray, String types, String group, Long value) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty(SERVICE, types);
    jsonObject.addProperty(GROUP, group);
    jsonObject.addProperty(VALUE, String.valueOf(value));
    jsonArray.add(jsonObject);
  }

  private Long getConsultationNumber(RnRForm rnRForm) {
    if (rnRForm != null && !rnRForm.getBaseInfoItemListWrapper().isEmpty()) {
      return Long.valueOf(rnRForm.getBaseInfoItemListWrapper().get(0).getValue());
    }
    return null;
  }

  private void setPatientLineItem(String programCode, JsonObject root, RnRForm rnRForm) {
    if (rnRForm == null || rnRForm.getBaseInfoItemListWrapper().isEmpty() || !(MMIA_PROGRAM_CODE.equals(programCode)
        || MMTB_PROGRAM_CODE.equals(programCode))) {
      return;
    }
    HashMap<String, List<BaseInfoItem>> tableNameToItems = groupPatientInfo(rnRForm);
    List<PatientLineItemRequest> patientLineItemRequests = new ArrayList<>();
    for (Map.Entry<String, List<BaseInfoItem>> map : tableNameToItems.entrySet()) {
      patientLineItemRequests.add(new PatientLineItemRequest(map.getKey(), map.getValue()));
    }
    root.add(PATIENT_LINE_ITEMS, jsonParser.parse(gson.toJson(patientLineItemRequests)));
  }

  @NonNull
  private HashMap<String, List<BaseInfoItem>> groupPatientInfo(RnRForm rnRForm) {
    List<BaseInfoItem> baseInfoItems = rnRForm.getBaseInfoItemListWrapper();
    HashMap<String, List<BaseInfoItem>> tableNameToItems = new HashMap<>();
    for (BaseInfoItem infoItem : baseInfoItems) {
      if (tableNameToItems.containsKey(infoItem.getTableName())) {
        tableNameToItems.get(infoItem.getTableName()).add(infoItem);
      } else {
        List<BaseInfoItem> list = new ArrayList<>();
        list.add(infoItem);
        tableNameToItems.put(infoItem.getTableName(), list);
      }
    }
    return tableNameToItems;
  }

}