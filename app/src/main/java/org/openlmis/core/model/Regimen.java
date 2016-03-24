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

package org.openlmis.core.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;

import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@Getter
@Setter
@DatabaseTable(tableName = "regimes")
public class Regimen extends BaseModel {

    public enum RegimeType {
        Adults,
        Paediatrics,
    }

    @Expose
    @SerializedName("name")
    @DatabaseField
    private String name;

    @Expose
    @SerializedName("code")
    @DatabaseField
    private String code;

    @Expose
    @SerializedName("categoryName")
    @DatabaseField
    private RegimeType type;

    public static final ArrayList<String> DEFAULT_REGIMES_NAME = newArrayList(
            "AZT+3TC+NVP",
            "TDF+3TC+EFV",
            "AZT+3TC+EFV",
            "d4T 30+3TC+NVP",
            "d4T 30+3TC+EFV",
            "AZT+3TC+LPV/r",
            "TDF+3TC+LPV/r",
            "ABC+3TC+LPV/r",
            "d4T+3TC+NVP(3DFC Baby)",
            "d4T+3TC+LPV/r(2DFC Baby + LPV/r)",
            "d4T+3TC+ABC(2DFC Baby + ABC)",
            "d4T+3TC+EFV(2DFC Baby + EFV)",
            "AZT60+3TC+NVP(3DFC)",
            "AZT60+3TC+EFV(2DFC + EFV)",
            "AZT60+3TC+ABC(2DFC + ABC)",
            "AZT60+3TC+LPV/r(2DFC + LPV/r)",
            "ABC+3TC+LPV/r",
            "ABC+3TC+EFZ");

    public boolean isCustom() {
        return !DEFAULT_REGIMES_NAME.contains(name);
    }
}
