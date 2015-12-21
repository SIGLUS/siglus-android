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
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.Period;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

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

    @ForeignCollectionField()
    private ForeignCollection<RnrFormItem> rnrFormItemList;

    @Expose
    @SerializedName("products")
    private ArrayList<RnrFormItem> rnrFormItemListWrapper;

    @ForeignCollectionField()
    private ForeignCollection<RegimenItem> regimenItemList;

    @Expose
    @SerializedName("regimens")
    private ArrayList<RegimenItem> regimenItemListWrapper;

    @ForeignCollectionField()
    private ForeignCollection<BaseInfoItem> baseInfoItemList;

    @Expose
    @SerializedName("patientQuantifications")
    private ArrayList<BaseInfoItem> baseInfoItemListWrapper;

    @Expose
    @SerializedName("clientSubmittedNotes")
    @DatabaseField
    private String comments;

    @DatabaseField(defaultValue = "DRAFT")
    private STATUS status;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Program program;

    @DatabaseField
    private boolean synced = false;

    @Expose(serialize = false)
    @SerializedName("periodStartDate")
    @DatabaseField
    private Date periodBegin;

    @DatabaseField
    private Date periodEnd;

    @Expose
    @SerializedName("clientSubmittedTime")
    @DatabaseField
    private Date submittedTime;

    public boolean isDraft() {
        return getStatus() == STATUS.DRAFT;
    }

    public boolean isSubmitted() {
        return getStatus() == STATUS.SUBMITTED;
    }

    public boolean isAuthorized() {
        return getStatus() == STATUS.AUTHORIZED;
    }

    public static RnRForm init(Program program, Date generateDate) {
        RnRForm rnrForm = new RnRForm();
        rnrForm.program = program;
        Period period = DateUtil.generateRnRFormPeriodBy(generateDate);
        rnrForm.periodBegin = period.getBegin().toDate();
        rnrForm.periodEnd = period.getEnd().toDate();
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
        rnrFormItemListWrapper = wrapOrEmpty(rnrFormItemList, rnrFormItemListWrapper);
        return rnrFormItemListWrapper;
    }

    public ArrayList<BaseInfoItem> getBaseInfoItemListWrapper() {
        baseInfoItemListWrapper = wrapOrEmpty(baseInfoItemList, baseInfoItemListWrapper);
        return baseInfoItemListWrapper;
    }

    public ArrayList<RegimenItem> getRegimenItemListWrapper() {
        regimenItemListWrapper = wrapOrEmpty(regimenItemList, regimenItemListWrapper);
        return regimenItemListWrapper;
    }

    public static void fillFormId(RnRForm rnRForm) {
        for (RnrFormItem item : rnRForm.getRnrFormItemListWrapper()) {
            item.setForm(rnRForm);
        }
        for (RegimenItem regimenItem : rnRForm.getRegimenItemListWrapper()) {
            regimenItem.setForm(rnRForm);
        }
        for (BaseInfoItem item : rnRForm.getBaseInfoItemListWrapper()) {
            item.setRnRForm(rnRForm);
        }
    }

    private <T> ArrayList<T> wrapOrEmpty(ForeignCollection<T> origin, ArrayList<T> target) {
        if (target == null) {
            return (origin == null ? new ArrayList<T>() : new ArrayList<>(origin));
        }
        return target;
    }
}
