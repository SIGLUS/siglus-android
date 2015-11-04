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

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.repository.MMIARepository;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.VIARepository;
import org.openlmis.core.utils.DateUtil;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Date;

import roboguice.RoboGuice;

public class RnrFormAdapter implements JsonSerializer<RnRForm>, JsonDeserializer<RnRForm> {
    @Inject
    public ProductRepository productRepository;

    @Inject
    public ProgramRepository programRepository;
    private final Gson gson;

    public RnrFormAdapter() {
        RoboGuice.getInjector(LMISApp.getContext()).injectMembersWithoutViews(this);
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().registerTypeAdapter(Date.class, new DateAdapter()).setDateFormat(DateFormat.LONG)
                .registerTypeAdapter(Product.class, new ProductAdapter())
                .create();
    }

    class ProductAdapter implements JsonDeserializer<Product> {

        @Override
        public Product deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                return productRepository.getByCode(json.getAsString());
            } catch (LMISException e) {
                e.printStackTrace();
                return null;
            }
        }
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
    public RnRForm deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return convertRnrForms(json);
    }

    public RnRForm convertRnrForms(JsonElement json) {

        RnRForm rnRForm = gson.fromJson(json.toString(), RnRForm.class);
        JsonObject asJsonObject = json.getAsJsonObject();
        try {
            Program program = programRepository.queryByCode(asJsonObject.get("programCode").getAsString());
            rnRForm.setProgram(program);
        } catch (LMISException e) {
            e.printStackTrace();
        }
        RnRForm.setPeriodByPeriodBegin(rnRForm);
        rnRForm.setStatus(RnRForm.STATUS.AUTHORIZED);
        rnRForm.setSynced(true);
        return rnRForm;
    }

}
