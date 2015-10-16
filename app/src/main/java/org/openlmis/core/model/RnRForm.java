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

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;


import org.openlmis.core.utils.DateUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DatabaseTable(tableName = "rnr_forms")
public class RnRForm extends BaseModel {

    public enum STATUS {
        DRAFT,
        SUBMITTED,
        AUTHORIZED
    }

    public static final int DAY_PERIOD_END = 20;

    @ForeignCollectionField()
    private ForeignCollection<RnrFormItem> rnrFormItemList;

    private ArrayList<RnrFormItem> rnrFormItemListWrapper;

    @ForeignCollectionField()
    private ForeignCollection<RegimenItem> regimenItemList;

    private ArrayList<RegimenItem> regimenItemListWrapper;

    @ForeignCollectionField()
    private ForeignCollection<BaseInfoItem> baseInfoItemList;

    private ArrayList<BaseInfoItem> baseInfoItemListWrapper;

    @DatabaseField
    private String comments;

    @DatabaseField(defaultValue = "DRAFT")
    private STATUS status;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Program program;

    @DatabaseField
    private boolean synced = false;

    @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = DateUtil.DATE_TIME_FORMAT)
    private Date periodBegin;

    @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = DateUtil.DATE_TIME_FORMAT)
    private Date periodEnd;


    public static RnRForm init(Program program, Date generateDate){
        RnRForm rnrForm = new RnRForm();
        rnrForm.program = program;

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(generateDate);

        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        if (day <= DAY_PERIOD_END + 5){
            rnrForm.periodBegin = new GregorianCalendar(year, month - 1, DAY_PERIOD_END + 1).getTime();
            rnrForm.periodEnd =  new GregorianCalendar(year, month, DAY_PERIOD_END).getTime();
        } else{
            rnrForm.periodBegin = new GregorianCalendar(year, month, DAY_PERIOD_END + 1).getTime();
            rnrForm.periodEnd = new GregorianCalendar(year, month + 1, DAY_PERIOD_END).getTime();
        }

        return rnrForm;
    }

    public static long calculateTotalRegimenAmount(Collection<RegimenItem> list) {
        long totalRegimenNumber = 0;
        for (RegimenItem item : list) {
            if (item.getAmount() != null) {
                totalRegimenNumber += item.getAmount();
            }
        }

        return totalRegimenNumber;
    }

    public ArrayList<RnrFormItem> getRnrFormItemListWrapper() {
        if (rnrFormItemListWrapper == null) {
            rnrFormItemListWrapper = new ArrayList(rnrFormItemList);
        }
        return rnrFormItemListWrapper;
    }

    public ArrayList<BaseInfoItem> getBaseInfoItemListWrapper() {
        if (baseInfoItemListWrapper == null) {
            baseInfoItemListWrapper = new ArrayList(baseInfoItemList);
        }
        return baseInfoItemListWrapper;
    }

    public ArrayList<RegimenItem> getRegimenItemListWrapper() {
        if (regimenItemListWrapper == null) {
            regimenItemListWrapper = new ArrayList(regimenItemList);
        }
        return regimenItemListWrapper;
    }
}
