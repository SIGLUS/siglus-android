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
import com.google.gson.JsonParseException;
import com.google.inject.Inject;

import org.openlmis.core.LMISApp;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.Service;
import org.openlmis.core.model.repository.ProgramRepository;

import java.lang.reflect.Type;

import roboguice.RoboGuice;

public class ServiceAdapter implements JsonDeserializer<Service> {
    @Inject
    public ProgramRepository programRepository;

    private final Gson gson;

    public ServiceAdapter() {
        RoboGuice.getInjector(LMISApp.getContext()).injectMembersWithoutViews(this);
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    }

    @Override
    public Service deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Service service = gson.fromJson(json.toString(), Service.class);
        try {
            Program program = programRepository.queryByCode(json.getAsJsonObject().get("programCode").getAsString());
            service.setProgram(program);
        } catch (LMISException e) {
            e.reportToFabric();
            throw new JsonParseException("can not find Program by programCode");
        }
        return service;
    }
}