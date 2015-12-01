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

import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRFormSignature;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.repository.ProgramRepository;
import org.openlmis.core.model.repository.RnrFormRepository;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;

public class RnrFormAdapter implements JsonSerializer<RnRForm>, JsonDeserializer<RnRForm> {

    @Inject
    public ProgramRepository programRepository;
    @Inject
    RnrFormRepository rnrFormRepository;

    private final Gson gson;
    private final JsonParser jsonParser;

    public RnrFormAdapter() {
        RoboGuice.getInjector(LMISApp.getContext()).injectMembersWithoutViews(this);
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().registerTypeAdapter(Date.class, new DateAdapter()).setDateFormat(DateFormat.LONG)
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
            throw new JsonParseException("can not find Signature by rnrForm");
        } catch (NullPointerException e) {
            throw new JsonParseException("No Program associated !");
        }
    }

    @Override
    public RnRForm deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        RnRForm rnRForm = gson.fromJson(json.toString(), RnRForm.class);
        RnRForm.fillFormId(rnRForm);

        try {
            Program program = programRepository.queryByCode(json.getAsJsonObject().get("programCode").getAsString());
            rnRForm.setProgram(program);
        } catch (LMISException e) {
            e.reportToFabric();
            throw new JsonParseException("can not find Program by programCode");
        }
        rnRForm.matchPeriodEndByBegin();
        rnRForm.setStatus(RnRForm.STATUS.AUTHORIZED);
        rnRForm.setSynced(true);
        return rnRForm;
    }

    private JsonElement buildRnrFormJson(RnRForm rnRForm) throws LMISException {
        JsonObject root = gson.toJsonTree(rnRForm).getAsJsonObject();
        String programCode = rnRForm.getProgram().getProgramCode();
        List<RnRFormSignature> signatureList = rnrFormRepository.querySignaturesByRnrForm(rnRForm);

        root.add("products", jsonParser.parse(gson.toJson(rnRForm.getRnrFormItemListWrapper())));
        root.add("regimens", jsonParser.parse(gson.toJson(rnRForm.getRegimenItemListWrapper())));
        root.add("patientQuantifications", jsonParser.parse(gson.toJson(rnRForm.getBaseInfoItemListWrapper())));
        root.add("rnrSignatures", jsonParser.parse(gson.toJson(signatureList)));
        root.addProperty("agentCode", UserInfoMgr.getInstance().getUser().getFacilityCode());
        root.addProperty("programCode", programCode);
        return root;
    }
}