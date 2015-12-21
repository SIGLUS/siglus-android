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

package org.openlmis.core.utils;

import android.support.annotation.NonNull;

import org.openlmis.core.exceptions.LMISException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public final class DateUtil {

    public static final String DEFAULT_DATE_FORMAT = "dd MMM yyyy";
    public static final String DATE_FORMAT_ONLY_MONTH_AND_YEAR = "MMM yyyy";
    public static final String SIMPLE_DATE_FORMAT = "dd/MM/yyyy";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT_ONLY_DAY_AND_MONTH = "dd MMM";

    public static final String DB_DATE_FORMAT = "yyyy-MM-dd";
    public static final int DAY_PERIOD_END = 20;


    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
    public static final SimpleDateFormat DATE_FORMAT_NOT_DISPLAY_YEAR = new SimpleDateFormat(DATE_FORMAT_ONLY_DAY_AND_MONTH);
    private static Locale locale = Locale.getDefault();

    public static final long MILLISECONDS_MINUTE = 60000;
    public static final long MILLISECONDS_HOUR = 3600000;
    public static final long MILLISECONDS_DAY = 86400000;
    public static final Calendar CALENDAR_NOW = Calendar.getInstance();

    private DateUtil() {

    }

    public static Date truncateTimeStampInDate(Date date) {
        String formattedDateStr = formatDate(date, SIMPLE_DATE_FORMAT);
        Date formattedDate;
        formattedDate = parseString(formattedDateStr, SIMPLE_DATE_FORMAT);

        return formattedDate;
    }

    public static Date addDayOfMonth(Date date, int difference) {
        Calendar calendar = calendarDate(date);
        calendar.add(Calendar.DAY_OF_MONTH, difference);
        return calendar.getTime();
    }

    public static Date minusDayOfMonth(Date date, int difference) {
        return addDayOfMonth(date, -difference);
    }

    public static Calendar calendarDate(Date date) {
        CALENDAR_NOW.setTime(date);
        return CALENDAR_NOW;
    }

    public static String formatDate(Date date) {
        return DATE_FORMATTER.format(date);
    }

    public static String formatDateWithoutYear(Date date) {
        return DATE_FORMAT_NOT_DISPLAY_YEAR.format(date);
    }

    public static String formatDate(Date date, String format) {
        return new SimpleDateFormat(format, locale).format(date);
    }

    public static Date parseString(String string, String format) {
        try {
            return new SimpleDateFormat(format, locale).parse(string);
        } catch (ParseException e) {
            new LMISException(e).reportToFabric();
            return null;
        }
    }

    public static Date today() {
        return Calendar.getInstance().getTime();
    }

    public static String convertDate(String date, String currentFormat, String expectFormat) throws ParseException {
        return formatDate(parseString(date, currentFormat), expectFormat);
    }

    public static String formatDateFromIntToString(int year, int monthOfYear, int dayOfMonth) {
        return new StringBuilder().append(dayOfMonth).append("/").append(monthOfYear + 1).append("/").append(year).toString();
    }

    public static Date dateMinusMonth(Date current, int months) {
        Calendar now = calendarDate(current);
        int currentMonth = now.get(Calendar.MONTH);
        now.set(Calendar.MONTH, currentMonth - months);
        return now.getTime();
    }

    public static void sortByDate(List<String> expiryDates) {
        Collections.sort(expiryDates, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return parseString(lhs, SIMPLE_DATE_FORMAT).compareTo(parseString(rhs, SIMPLE_DATE_FORMAT));
            }
        });
    }

    public static Date generateRnRFormPeriodBeginBy(Date generateDate) {
        return getPeriodBeginDate(generateDate, DAY_PERIOD_END + 5);
    }

    @NonNull
    private static Date getPeriodBeginDate(Date generateDate, int thresholdDate) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(generateDate);

        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        if (day <= thresholdDate) {
            return new GregorianCalendar(year, month - 1, DAY_PERIOD_END + 1).getTime();
        } else {
            return new GregorianCalendar(year, month, DAY_PERIOD_END + 1).getTime();
        }
    }

    public static Date getPeriodBeginBy(Date generateDate) {
        return getPeriodBeginDate(generateDate, DAY_PERIOD_END);
    }

    public static Date generatePeriodEndByBegin(Date periodBegin) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(periodBegin);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        return new GregorianCalendar(year, month + 1, DAY_PERIOD_END).getTime();
    }

    public static Date generatePreviousMonthDateBy(Date periodDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(periodDate);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        return new GregorianCalendar(year, month - 1, day).getTime();
    }

    public static int calculateDateMonthOffset(Date earlierDate, Date laterDate) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(laterDate);
        int laterTotalMonth = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH);

        calendar.setTime(earlierDate);
        int earlierTotalMonth = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH);

        return laterTotalMonth - earlierTotalMonth;
    }
}
