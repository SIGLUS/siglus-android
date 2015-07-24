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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    public static SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MMM-dd");

    public static Date getMonthStartDate(Date date) {
        Calendar calendar = calendarDate(date);

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        Date startDate = calendar.getTime();
        return startDate;
    }

    public static Date getMonthEndDate(Date date) {
        Calendar calendar = calendarDate(date);

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date endDate = calendar.getTime();
        return endDate;
    }

    public static int maxMonthDate(Date date) {
        Calendar calendar = calendarDate(date);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static Date addDayOfMonth(Date date) {
        Calendar calendar = calendarDate(date);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTime();
    }

    public static Date addDayOfMonth(Date date, int difference) {
        Calendar calendar = calendarDate(date);
        calendar.add(Calendar.DAY_OF_MONTH, difference);
        return calendar.getTime();
    }

    public static Date addMonth(Date date, int difference){
        Calendar calendar = calendarDate(date);
        calendar.add(Calendar.MONTH, difference);
        return calendar.getTime();
    }

    public static Calendar calendarDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    public static boolean equal(Date date1, Date date2) {
        return dateFormater.format(date1).equals(dateFormater.format(date2));
    }

    public static int dayNumber(Date date) {
        Calendar calender = calendarDate(date);
        return calender.get(Calendar.DAY_OF_MONTH);
    }

    public static String formatDate(Date date) {
        return dateFormater.format(date);
    }

    public static String formatDate(Date date, String format) {
        return new SimpleDateFormat(format).format(date);
    }

    public static Date parseString(String string, String format) throws ParseException {
        return new SimpleDateFormat(format).parse(string);
    }

    public static Date today() {
        return Calendar.getInstance().getTime();
    }

    public static int numDaysToEndOfMonth() {
        return maxMonthDate(new Date()) - dayNumber(new Date());
    }

    public static int monthNumber(Date date) {
        Calendar calendar = calendarDate(date);
        return calendar.get(Calendar.MONTH);
    }

    public static int monthNumber() {
        return monthNumber(new Date());
    }
}
