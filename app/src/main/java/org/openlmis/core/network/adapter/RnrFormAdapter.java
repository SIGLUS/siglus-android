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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.BaseInfoItem;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;

import java.lang.reflect.Type;

public class RnrFormAdapter implements JsonSerializer<RnRForm> {
    @Override
    public JsonElement serialize(RnRForm rnRForm, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject root = new JsonObject();
        root.addProperty("agentCode", UserInfoMgr.getInstance().getUser().getFacilityCode());
        try {
            root.addProperty("programCode", rnRForm.getProgram().getProgramCode());
        }catch (NullPointerException e){
            Log.e(RnrFormAdapter.class.getSimpleName(), "No Program associated !");
        }

        if (rnRForm.getRnrFormItemList() != null){
            root.add("products", serializeProductItems(rnRForm.getRnrFormItemList()));
        }

        if (rnRForm.getRegimenItemList() != null){
            root.add("regimens", serializeRegimens(rnRForm.getRegimenItemList()));
        }

        if (rnRForm.getBaseInfoItemList() !=null){
            root.add("patientQuantifications", serializePatientInfo(rnRForm.getBaseInfoItemList()));
        }

        return root;
    }

    private JsonArray serializeProductItems(Iterable<RnrFormItem> productItems){
        JsonArray products = new JsonArray();
        for (RnrFormItem item : productItems){
            JsonObject product = new JsonObject();
            product.addProperty("productCode", item.getProduct().getCode());
            product.addProperty("beginningBalance", item.getInitialAmount());
            product.addProperty("quantityReceived", item.getReceived());
            product.addProperty("quantityDispensed", item.getIssued());
            product.addProperty("totalLossesAndAdjustments", item.getAdjustment());
            product.addProperty("stockInHand", item.getInventory());
            product.addProperty("quantityRequested", 0);
            product.addProperty("reasonForRequestedQuantity", "reason");

            products.add(product);
        }
        return products;
    }

    private JsonArray serializeRegimens(Iterable<RegimenItem> regimenItems){
        JsonArray regimens = new JsonArray();
        for (RegimenItem item : regimenItems){
            JsonObject regimenItem = new JsonObject();
            regimenItem.addProperty("code", item.getRegimen().getCode());
            regimenItem.addProperty("name", item.getRegimen().getName());
            regimenItem.addProperty("patientsOnTreatmentAdult", item.getAmount());

            regimens.add(regimenItem);
        }

        return regimens;
    }

    private JsonArray serializePatientInfo(Iterable<BaseInfoItem> patientInfoItems){
        JsonArray patientInfos = new JsonArray();
        for (BaseInfoItem item : patientInfoItems){
            JsonObject patientInfo = new JsonObject();
            patientInfo.addProperty("category", item.getName());
            patientInfo.addProperty("total", item.getValue());
        }
        return patientInfos;
    }

}
