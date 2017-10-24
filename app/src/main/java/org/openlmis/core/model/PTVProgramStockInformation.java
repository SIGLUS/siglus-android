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

import java.util.Collection;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DatabaseTable(tableName = "ptv_program_stock_information")
public class PTVProgramStockInformation extends BaseModel {

    @DatabaseField
    int initialStock;

    @DatabaseField
    int entries;

    @DatabaseField
    int lossesAndAdjustments;

    @DatabaseField
    int requisition;


    @DatabaseField(columnName = "ptvProgramId", foreign = true)
    PTVProgram ptvProgram;

    @DatabaseField(columnName = "productId", foreign = true)
    Product product;

    @ForeignCollectionField(eager = true)
    private Collection<ServiceDispensation> serviceDispensations;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PTVProgramStockInformation)) return false;

        PTVProgramStockInformation that = (PTVProgramStockInformation) o;

        if (initialStock != that.initialStock) return false;
        if (entries != that.entries) return false;
        if (lossesAndAdjustments != that.lossesAndAdjustments) return false;
        if (requisition != that.requisition) return false;
        return ptvProgram.equals(that.ptvProgram) && product.equals(that.product);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + initialStock;
        result = 31 * result + entries;
        result = 31 * result + lossesAndAdjustments;
        result = 31 * result + requisition;
        result = 31 * result + ptvProgram.hashCode();
        result = 31 * result + product.hashCode();
        return result;
    }
}
