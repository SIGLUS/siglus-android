/*
 * Copyright (c) 2014, Thoughtworks Inc
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
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
