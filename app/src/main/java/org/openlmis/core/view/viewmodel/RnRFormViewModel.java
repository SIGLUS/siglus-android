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

package org.openlmis.core.view.viewmodel;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;

import java.util.Date;

import lombok.Data;

@Data
public class RnRFormViewModel {

    public static final int TYPE_UNCOMPLETE_INVENTORY_IN_CURRENT_PERIOD = 10;
    public static final int TYPE_INVENTORY_DONE = 20;
    public static final int TYPE_CLOSE_OF_PERIOD_SELECTED = 30; //Before period logic change
    public static final int TYPE_CREATED_BUT_UNCOMPLETED = 40;
    public static final int TYPE_UNSYNCED_HISTORICAL = 50;
    public static final int TYPE_SYNCED_HISTORICAL = 60;
    public static final int TYPE_CANNOT_DO_MONTHLY_INVENTORY = 70;
    public static final int TYPE_FIRST_MISSED_PERIOD = 80;
    public static final int TYPE_MISSED_PERIOD = 90;

    int type;
    String syncedDate;
    String period;
    DateTime periodEndMonth;
    String title;
    String name;
    long id;
    String syncServerErrorMessage;
    private RnRForm form;
    private String programCode;

    public RnRFormViewModel() {
    }

    public RnRFormViewModel(RnRForm form) {
        this.form = form;
        Date submittedTime = form.getSubmittedTime();
        if (submittedTime != null) {
            this.syncedDate = DateUtil.formatDate(submittedTime);
        } else {
            this.syncedDate = StringUtils.EMPTY;
        }
        this.period = generatePeriod(form.getPeriodBegin(), form.getPeriodEnd());
        this.id = form.getId();
        this.programCode = form.getProgram().getProgramCode();
        periodEndMonth = new DateTime(form.getPeriodEnd());
        setName(programCode);
        setType(form);
    }

    public RnRFormViewModel(Period period, String programCode, int type) {
        this.period = generatePeriod(period.getBegin().toDate(), period.getEnd().toDate());
        this.type = type;
        this.programCode = programCode;
//        this.periodEndMonth = DateUtil.getMonthAbbrByDate(period.getEnd().toDate());
        periodEndMonth = period.getEnd();
        setName(programCode);
    }

    private String generatePeriod(Date begin, Date end) {
        return LMISApp.getContext().getString(R.string.label_period_date, DateUtil.formatDate(begin), DateUtil.formatDate(end));
    }

    public void setType(RnRForm form) {
        if (form.getStatus() == RnRForm.STATUS.AUTHORIZED) {
            this.type = form.isSynced() ? TYPE_SYNCED_HISTORICAL : TYPE_UNSYNCED_HISTORICAL;
        } else {
            this.type = TYPE_CREATED_BUT_UNCOMPLETED;
        }
    }

    public void setSyncServerErrorMessage(String syncServerErrorMessage) {
        this.syncServerErrorMessage = syncServerErrorMessage;
    }

    private void setName(String programCode) {
        switch (programCode) {
            case Constants.MMIA_PROGRAM_CODE:
                this.name = LMISApp.getContext().getString(R.string.label_mmia_name);
                break;
            case Constants.VIA_PROGRAM_CODE:
                this.name = LMISApp.getContext().getString(R.string.label_via_name);
                break;
            case Constants.ESS_PROGRAM_CODE:
                this.name = LMISApp.getContext().getString(R.string.label_via_name);
                break;
            default:
                this.name = StringUtils.EMPTY;
                break;
        }
    }

    public static RnRFormViewModel buildMissedPeriod(Date startDate, Date endDate) {
        RnRFormViewModel rnRFormViewModel = new RnRFormViewModel();
        rnRFormViewModel.type = TYPE_MISSED_PERIOD;
        rnRFormViewModel.periodEndMonth = new DateTime(endDate);
        rnRFormViewModel.period = LMISApp.getContext().getString(R.string.label_period_date, DateUtil.formatDateWithoutDay(startDate), DateUtil.formatDateWithoutDay(endDate));
        return rnRFormViewModel;
    }

    public static RnRFormViewModel buildFirstMissedPeriod(String programCode, Date startDate, Date endDate) {
        RnRFormViewModel rnRFormViewModel = new RnRFormViewModel();
        rnRFormViewModel.setProgramCode(programCode);
        rnRFormViewModel.type = TYPE_FIRST_MISSED_PERIOD;
        rnRFormViewModel.periodEndMonth = new DateTime(endDate);
        rnRFormViewModel.period = LMISApp.getContext().getString(R.string.label_period_date, DateUtil.formatDateWithoutYear(startDate), DateUtil.formatDateWithoutDay(endDate));
        return rnRFormViewModel;
    }
}
