package org.openlmis.core.view.listener;

import android.app.DatePickerDialog;
import android.widget.DatePicker;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MovementDateListener implements DatePickerDialog.OnDateSetListener {

    private Date previousMovementDate;
    private StockMovementViewModel model;
    TextView movementDateField;

    public MovementDateListener(StockMovementViewModel model, Date previousMovementDate, TextView movementDateField) {
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
        Calendar today = GregorianCalendar.getInstance();

        return previousMovementDate == null || !previousMovementDate.after(chosenDate) && !chosenDate.after(today.getTime());
    }
}