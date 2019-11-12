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


import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import org.openlmis.core.utils.DateUtil;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DatabaseTable(tableName = "ptv_program")
public class PTVProgram extends BaseModel implements Serializable {

    @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = DateUtil.DATE_TIME_FORMAT)
    private Date startPeriod;

    @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = DateUtil.DATE_TIME_FORMAT)
    private Date endPeriod;

    @DatabaseField
    String createdBy;

    @DatabaseField
    String verifiedBy;

    @DatabaseField(dataType = DataType.ENUM_INTEGER)
    ViaReportStatus status;

    @ForeignCollectionField(eager = true)
    private Collection<PTVProgramStockInformation> ptvProgramStocksInformation;

    @ForeignCollectionField(eager = true)
    private Collection<PatientDispensation> patientDispensations;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PTVProgram)) return false;

        PTVProgram that = (PTVProgram) o;

        return Objects.equals(startPeriod, that.startPeriod)
                && Objects.equals(endPeriod, that.endPeriod)
                && Objects.equals(createdBy, that.createdBy)
                && Objects.equals(verifiedBy, that.verifiedBy)
                && Objects.equals(status, that.status)
                && Objects.equals(ptvProgramStocksInformation, that.ptvProgramStocksInformation)
                && Objects.equals(patientDispensations, that.patientDispensations);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (startPeriod != null ? startPeriod.hashCode() : 0);
        result = 31 * result + (endPeriod != null ? endPeriod.hashCode() : 0);
        result = 31 * result + (createdBy != null ? createdBy.hashCode() : 0);
        result = 31 * result + (verifiedBy != null ? verifiedBy.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (ptvProgramStocksInformation != null ? ptvProgramStocksInformation.hashCode() : 0);
        result = 31 * result + (patientDispensations != null ? patientDispensations.hashCode() : 0);
        return result;
    }
}
