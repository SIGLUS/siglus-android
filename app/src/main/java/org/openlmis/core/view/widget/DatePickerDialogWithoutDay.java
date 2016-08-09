package org.openlmis.core.view.widget;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import org.openlmis.core.exceptions.LMISException;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class DatePickerDialogWithoutDay extends DatePickerDialog {

    private OnDateSetListener mDateSetListener;

    public DatePickerDialogWithoutDay(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
        super(context, AlertDialog.THEME_HOLO_LIGHT, null, year, monthOfYear, dayOfMonth);
        init(callBack);
    }

    private void init(OnDateSetListener listener) {
        mDateSetListener = listener;
        hideDay(getDatePicker());
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case BUTTON_POSITIVE:
                if (mDateSetListener != null) {
                    DatePicker datePicker = getDatePicker();
                    mDateSetListener.onDateSet(datePicker, datePicker.getYear(),
                            datePicker.getMonth(), getLastDayOfMonth(datePicker));
                }
                break;
            case BUTTON_NEGATIVE:
                cancel();
                break;
        }
    }

    public static int getLastDayOfMonth(DatePicker datePicker) {
        return new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth()).getActualMaximum(Calendar.DATE);
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int month, int day) {
        int defaultDay = new GregorianCalendar(year, month, day).getActualMaximum(Calendar.DATE);
        super.onDateChanged(view, year, month, defaultDay);
    }

    private void hideDay(DatePicker datePicker) {
        if (datePicker == null) {
            return;
        }
        ViewGroup datePickerLayout = (ViewGroup) datePicker.getChildAt(0);
        if (datePickerLayout == null) {
            return;
        }

        try {
            //hide CalendarView
            datePickerLayout.getChildAt(1).setVisibility(View.GONE);

            //hide day
            int dayIdentifier = Resources.getSystem().getIdentifier("day", "id", "android");
            ViewGroup pickers = (ViewGroup) datePickerLayout.getChildAt(0);
            for (int i = 0; i < pickers.getChildCount(); i++) {
                View childView = pickers.getChildAt(i);
                if (childView.getId() == dayIdentifier) {
                    childView.setVisibility(View.GONE);
                    return;
                }

            }
        } catch (NullPointerException e) {
            new LMISException(e).reportToFabric();
        }
    }
}
