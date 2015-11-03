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

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.inject.Inject;

import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.repository.MMIARepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.VIARepository;
import org.openlmis.core.network.model.SyncBackRequisitionsResponse;
import org.openlmis.core.utils.DateUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

import roboguice.RoboGuice;

public class RnrFormAdapter implements JsonSerializer<RnRForm>, JsonDeserializer<SyncBackRequisitionsResponse> {
    @Inject
    public ProductRepository productRepository;

    @Inject
    public ProgramRepository programRepository;

    public RnrFormAdapter() {
        RoboGuice.getInjector(LMISApp.getContext()).injectMembersWithoutViews(this);
    }

    @Override
    public JsonElement serialize(RnRForm rnRForm, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject root = new JsonObject();
        String programCode = rnRForm.getProgram().getProgramCode();
        root.addProperty("agentCode", UserInfoMgr.getInstance().getUser().getFacilityCode());
        try {
            root.addProperty("programCode", programCode);
        } catch (NullPointerException e) {
            Log.e(RnrFormAdapter.class.getSimpleName(), "No Program associated !");
        }

        if (rnRForm.getRnrFormItemListWrapper() != null && rnRForm.getRnrFormItemListWrapper().size() > 0) {
            root.add("products", serializeProductItems(rnRForm.getRnrFormItemListWrapper(), programCode));
        }

        if (rnRForm.getRegimenItemList() != null && programCode.equals(MMIARepository.MMIA_PROGRAM_CODE)) {
            root.add("regimens", serializeRegimens(rnRForm.getRegimenItemList()));
        }

        if (rnRForm.getBaseInfoItemList() != null) {
            root.add("patientQuantifications", serializePatientInfo(rnRForm.getBaseInfoItemList()));
        }

        if (rnRForm.getComments() != null) {
            root.addProperty("clientSubmittedNotes", rnRForm.getComments());
        }

        root.addProperty("clientSubmittedTime", DateUtil.formatDate(rnRForm.getSubmittedTime(), "yyyy-MM-dd HH:mm:ss"));
        return root;
    }

    private JsonArray serializeProductItems(Iterable<RnrFormItem> productItems, String programCode) {
        JsonArray products = new JsonArray();
        for (RnrFormItem item : productItems) {
            JsonObject product = new JsonObject();
            product.addProperty("productCode", item.getProduct().getCode());
            product.addProperty("beginningBalance", item.getInitialAmount());
            product.addProperty("quantityReceived", item.getReceived());
            product.addProperty("quantityDispensed", item.getIssued());
            product.addProperty("stockInHand", item.getInventory());
            product.addProperty("quantityRequested", item.getRequestAmount());
            product.addProperty("reasonForRequestedQuantity", "reason");
            if (programCode.equals(VIARepository.VIA_PROGRAM_CODE)) {
                product.addProperty("calculatedOrderQuantity", item.getCalculatedOrderQuantity());
            }
            product.addProperty("totalLossesAndAdjustments", item.getAdjustment());
            product.addProperty("expirationDate", item.getValidate());
            products.add(product);
        }
        return products;
    }

    private JsonArray serializeRegimens(Iterable<RegimenItem> regimenItems) {
        JsonArray regimens = new JsonArray();
        for (RegimenItem item : regimenItems) {
            JsonObject regimenItem = new JsonObject();
            regimenItem.addProperty("code", item.getRegimen().getCode());
            regimenItem.addProperty("name", item.getRegimen().getName());

            regimenItem.addProperty("patientsOnTreatment", item.getAmount());
            regimens.add(regimenItem);
        }

        return regimens;
    }

    private JsonArray serializePatientInfo(Iterable<BaseInfoItem> patientInfoItems) {
        JsonArray patientInfos = new JsonArray();
        for (BaseInfoItem item : patientInfoItems) {
            JsonObject patientInfo = new JsonObject();
            patientInfo.addProperty("category", item.getName());
            patientInfo.addProperty("total", item.getValue());

            patientInfos.add(patientInfo);
        }
        return patientInfos;
    }

    @Override
    public SyncBackRequisitionsResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return convertRnrForms(json);
    }

    public SyncBackRequisitionsResponse convertRnrForms(JsonElement json) {
        ArrayList<RnRForm> rnRForms = new ArrayList<>();
        JsonElement requisitions = json.getAsJsonObject().get("requisitions");
        JsonArray asJsonArray = requisitions.getAsJsonArray();
        for (JsonElement element : asJsonArray) {
            JsonObject asJsonObject = element.getAsJsonObject();
            long periodStartDate = asJsonObject.get("periodStartDate").getAsLong();

            RnRForm rnRForm = new RnRForm();
            Program program = null;
            try {
                program = programRepository.queryByCode(asJsonObject.get("programCode").getAsString());
            } catch (LMISException e) {
                e.printStackTrace();
            }
            rnRForm.setProgram(program);
            RnRForm.setPeriodByPeriodBegin(new Date(periodStartDate), rnRForm);

            rnRForm.setStatus(RnRForm.STATUS.AUTHORIZED);
            rnRForm.setSynced(true);

            JsonArray products = asJsonObject.get("products").getAsJsonArray();
            rnRForm.setRnrFormItemListWrapper(deserializeProductItems(products));
            JsonArray regimens = asJsonObject.get("regimens").getAsJsonArray();
            rnRForm.setRegimenItemListWrapper(deserializeRegimens(regimens));
            JsonArray patientQuantifications = asJsonObject.get("patientQuantifications").getAsJsonArray();
            rnRForm.setBaseInfoItemListWrapper(deserializePatientInfo(patientQuantifications));
            JsonElement submittedNotes = asJsonObject.get("clientSubmittedNotes");

            if (submittedNotes != null) {
                rnRForm.setComments(submittedNotes.getAsString());
            }

            rnRForm.setSubmittedTime(new Date(asJsonObject.get("clientSubmittedTime").getAsLong()));

            rnRForms.add(rnRForm);
        }
        SyncBackRequisitionsResponse response = new SyncBackRequisitionsResponse();
        response.setRequisitions(rnRForms);
        return response;
    }

    @NonNull
    private ArrayList<RnrFormItem> deserializeProductItems(JsonArray products) {
        ArrayList<RnrFormItem> items = new ArrayList<>();
        for (JsonElement productJson : products) {
            JsonObject productJsonAsJsonObject = productJson.getAsJsonObject();
            RnrFormItem item = new RnrFormItem();

            if (productJsonAsJsonObject.has("beginningBalance")) {
                item.setInitialAmount(productJsonAsJsonObject.get("beginningBalance").getAsLong());
            }
            if (productJsonAsJsonObject.has("quantityReceived")) {
                item.setReceived(productJsonAsJsonObject.get("quantityReceived").getAsLong());
            }
            if (productJsonAsJsonObject.has("quantityDispensed")) {
                item.setIssued(productJsonAsJsonObject.get("quantityDispensed").getAsLong());
            }
            if (productJsonAsJsonObject.has("stockInHand")) {
                item.setInventory(productJsonAsJsonObject.get("stockInHand").getAsLong());
            }
            if (productJsonAsJsonObject.has("quantityRequested")) {
                item.setRequestAmount(productJsonAsJsonObject.get("quantityRequested").getAsLong());
            }
            item.setAdjustment(productJsonAsJsonObject.get("totalLossesAndAdjustments").getAsLong());
            if (productJsonAsJsonObject.has("expirationDate")) {
                item.setValidate(productJsonAsJsonObject.get("expirationDate").getAsString());
            }
            if (productJsonAsJsonObject.has("calculatedOrderQuantity")) {
                item.setCalculatedOrderQuantity(productJsonAsJsonObject.get("calculatedOrderQuantity").getAsLong());
            }
            try {
                item.setProduct(productRepository.getByCode(productJsonAsJsonObject.get("productCode").getAsString()));
            } catch (LMISException e) {
                e.printStackTrace();
            }
            items.add(item);
        }
        return items;
    }

    private ArrayList<RegimenItem> deserializeRegimens(JsonArray regimenItems) {
        ArrayList<RegimenItem> regimens = new ArrayList<>();
        for (JsonElement regimenJson : regimenItems.getAsJsonArray()) {
            RegimenItem regimenItem = new RegimenItem();
            JsonObject asJsonObject = regimenJson.getAsJsonObject();
            regimenItem.setAmount(asJsonObject.get("patientsOnTreatment").getAsLong());
            regimens.add(regimenItem);
        }
        return regimens;
    }

    private ArrayList<BaseInfoItem> deserializePatientInfo(JsonArray patientInfoItems) {
        ArrayList<BaseInfoItem> patientInfos = new ArrayList<>();
        for (JsonElement element : patientInfoItems.getAsJsonArray()) {
            BaseInfoItem baseInfoItem = new BaseInfoItem();
            JsonObject asJsonObject = element.getAsJsonObject();
            baseInfoItem.setName(asJsonObject.get("category").getAsString());
            baseInfoItem.setValue(asJsonObject.get("total").getAsString());
            patientInfos.add(baseInfoItem);
        }
        return patientInfos;
    }

}
