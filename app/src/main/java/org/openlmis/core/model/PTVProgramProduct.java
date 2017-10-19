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


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.Collection;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DatabaseTable(tableName = "ptv_program_product")
public class PTVProgramProduct extends BaseModel implements Serializable {

    @DatabaseField
    int initialStock;

    @DatabaseField
    int entries;

    @DatabaseField
    int lossesAndAdjustments;

    @DatabaseField
    int requisition;

    @DatabaseField(columnName = "ptvProgramId", foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
    PTVProgram ptvProgram;

    @DatabaseField(columnName = "productId", foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)
    Product product;

    @ForeignCollectionField(eager = true)
    private Collection<DrugDispensation> drugDispensations;

}
