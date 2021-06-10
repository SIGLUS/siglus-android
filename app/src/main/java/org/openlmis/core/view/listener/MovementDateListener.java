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

package org.openlmis.core.view.listener;

import android.app.DatePickerDialog;
import android.widget.DatePicker;
import android.widget.TextView;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import org.openlmis.core.R;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;

public class MovementDateListener implements DatePickerDialog.OnDateSetListener {

  private final Date previousMovementDate;
  private final StockMovementViewModel model;
  TextView movementDateField;

  public MovementDateListener(StockMovementViewModel model, Date previousMovementDate,
      TextView movementDateField) {
    this.previousMovementDate = previousMovementDate;
    this.model = model;
    this.movementDateField = movementDateField;
  }

  @Override
  public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

    Date chosenDate = new GregorianCalendar(year, monthOfYear, dayOfMonth).getTime();
    if (validateStockMovementDate(previousMovementDate, chosenDate)) {
      movementDateField.setText(DateUtil.formatDate(chosenDate));
      model.setMovementDate(DateUtil.formatDate(chosenDate));
    } else {
      ToastUtil.show(R.string.msg_invalid_stock_movement_date);
    }
  }

  private boolean validateStockMovementDate(Date previousMovementDate, Date chosenDate) {
    Calendar today = DateUtil.getCurrentCalendar();

    return (previousMovementDate == null || !previousMovementDate.after(chosenDate)) && !chosenDate
        .after(today.getTime());
  }
}