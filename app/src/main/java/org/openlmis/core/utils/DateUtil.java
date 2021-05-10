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

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.StockCard;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public final class DateUtil {

    public static final String DEFAULT_DATE_FORMAT = "dd MMM yyyy";
    public static final String DATE_FORMAT_ONLY_MONTH_AND_YEAR = "MMM yyyy";
    public static final String DATE_FORMAT_ONLY_MONTH_AND_YEAR_SHORT = "MM/yy";
    public static final String DATE_FORMAT_ONLY_MONTH_AND_YEAR_LONG = "MM/yyyy";
    public static final String SIMPLE_DATE_FORMAT = "dd/MM/yyyy";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String TIME_FORMAT = "HH:mm";
    public static final String DATE_FORMAT_ONLY_DAY_AND_MONTH = "dd MMM";
    public static final String TIME_FORMAT_WITHOUT_SECOND = "dd MMM yyyy HH:mm";
    public static final String DB_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_DIGIT_FORMAT_ONLY_MONTH_AND_YEAR = "MMyyyy";
    public static final String ISO_BASIC_DATE_TIME_FORMAT = "yyyyMMdd'T'HHmmss.SSSZ";
    public static final int DAY_PERIOD_END = 20;
    public static final String MOZ_TIME_ZONE = "Africa/Maputo";

    public static final long MILLISECONDS_MINUTE = 60000;
    public static final long MILLISECONDS_HOUR = 3600000;
    public static final long MILLISECONDS_DAY = 86400000;

    private DateUtil() {
    }

    public static Date getCurrentDate() {
        if (LMISApp.getInstance() != null && LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(2021, 0, 18);
            return calendar.getTime();
        }
        return new Date();
    }

    public static Date today() {
        if (LMISApp.getInstance() != null && LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(2021, 0, 18);
            return calendar.getTime();
        }
        return Calendar.getInstance().getTime();
    }

    public static  Calendar getCurrentCalendar() {
        if (LMISApp.getInstance() != null && LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_training)) {
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.set(2021, 0, 18);
            return calendar;
        }
        return GregorianCalendar.getInstance();

    }

    public static Date truncateTimeStampInDate(Date date) {
        String formattedDateStr = formatDate(date, SIMPLE_DATE_FORMAT);
        return parseString(formattedDateStr, SIMPLE_DATE_FORMAT);
    }

    public static Date addDayOfMonth(Date date, int difference) {
        Calendar calendar = calendarDate(date);
        calendar.add(Calendar.DAY_OF_MONTH, difference);
        return calendar.getTime();
    }

    public static Date minusDayOfMonth(Date date, int difference) {
        return addDayOfMonth(date, -difference);
    }

    public static String formatDate(Date date) {
        return new SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.getDefault()).format(date);
    }

    public static String formatDateTime(Date date) {
        return new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault()).format(date);
    }

    public static String formatDateWithoutYear(Date date) {
        return new SimpleDateFormat(DATE_FORMAT_ONLY_DAY_AND_MONTH, Locale.getDefault()).format(date);
    }

    public static String formatDateWithoutDay(Date date) {
        return new SimpleDateFormat(DATE_FORMAT_ONLY_MONTH_AND_YEAR, Locale.getDefault()).format(date);
    }

    public static String formatDateWithShortMonthAndYear(Date date) {
        return new SimpleDateFormat(DATE_FORMAT_ONLY_MONTH_AND_YEAR_SHORT, Locale.getDefault()).format(date);
    }

    public static String formatDateWithLongMonthAndYear(Date date) {
        return new SimpleDateFormat(DATE_FORMAT_ONLY_MONTH_AND_YEAR_LONG, Locale.getDefault()).format(date);
    }

    public static String formatDate(Date date, String format) {
        return new SimpleDateFormat(format, Locale.getDefault()).format(date);
    }

    public static Date parseString(String string, String format) {
        try {
            return new SimpleDateFormat(format, Locale.getDefault()).parse(string);
        } catch (ParseException e) {
            new LMISException(e, "DateUtil,parseString").reportToFabric();
            return null;
        }
    }

    public static Date parseString(String string, String format, String timeZone) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
            return simpleDateFormat.parse(string);
        } catch (ParseException e) {
            new LMISException(e, "DateUtil,parseString").reportToFabric();
            return null;
        }
    }

    public static String convertDate(String date, String currentFormat, String expectFormat) throws ParseException {
        return formatDate(parseString(date, currentFormat), expectFormat);
    }

    public static Date dateMinusMonth(Date current, int months) {
        return new DateTime(current).minusMonths(months).toDate();
    }

    public static void sortByDate(List<String> expiryDates) {
        Collections.sort(expiryDates, (lhs, rhs) -> {
            Date date = parseString(lhs, SIMPLE_DATE_FORMAT);
            if (date != null) {
                return date.compareTo(parseString(rhs, SIMPLE_DATE_FORMAT));
            } else {
                return 0;
            }
        });
    }

    public static Period generateRnRFormPeriodBy(Date generateDate) {
        DateTime dateTime = new DateTime(generateDate);
        Period period = new Period(dateTime);
        if (isInSubmitDates(dateTime.getDayOfMonth())) {
            return period.previous();
        }
        return period;
    }

    public static Date generatePreviousMonthDateBy(Date date) {
        return dateMinusMonth(date, 1);
    }

    public static int calculateDateMonthOffset(Date earlierDate, Date laterDate) {
        Calendar startCalendar = new GregorianCalendar();
        startCalendar.setTime(earlierDate);
        Calendar endCalendar = new GregorianCalendar();
        endCalendar.setTime(laterDate);

        int diffYear = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
        return diffYear * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);
    }

    private static Calendar calendarDate(Date date) {
        final Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        return instance;
    }

    public static DateTime cutTimeStamp(DateTime dateTime) {
        DateTimeFormatter format = DateTimeFormat.forPattern(DB_DATE_FORMAT);
        String formatDate = format.print(dateTime);
        return format.parseDateTime(formatDate);
    }


    private static boolean isInSubmitDates(int day) {
        return day >= DAY_PERIOD_END + 1 && day <= DAY_PERIOD_END + 5;
    }

    public static long calculateTimeIntervalFromNow(long lastSyncedTimestamp) {
        return DateUtil.getCurrentDate().getTime() - lastSyncedTimestamp;
//        return new Date().getTime() - lastSyncedTimestamp;
    }

    public static String formatExpiryDateString(List<String> expiryDates) {
        if (expiryDates == null) {
            return StringUtils.EMPTY;
        }
        sortByDate(expiryDates);
        return StringUtils.join(expiryDates, StockCard.DIVIDER);
    }

    public static String uniqueExpiryDates(List<String> expiryDates, String existingDates) {
        return formatExpiryDateString(addExpiryDates(expiryDates, existingDates));
    }

    public static List<String> addExpiryDates(List<String> expiryDates, String date) {
        if (StringUtils.isEmpty(date)) {
            return expiryDates;
        }

        if (expiryDates == null || expiryDates.isEmpty()) {
            expiryDates = new ArrayList<>();
        }

        String[] existingExpiryDates = date.split(StockCard.DIVIDER);
        for (String expiryDate : existingExpiryDates) {
            if (!expiryDates.contains(expiryDate)) {
                expiryDates.add(expiryDate);
            }
        }
        return expiryDates;
    }

    public static String getMonthAbbrByDate(Date date) {
        final Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        return new SimpleDateFormat("MMM", Locale.getDefault()).format(instance.getTime());
    }

    public static int calculateMonthOffset(DateTime biggerTime, DateTime smallerTime) {
        return biggerTime.getYear() * 12 + biggerTime.getMonthOfYear() - (smallerTime.getYear() * 12 + smallerTime.getMonthOfYear());
    }

    public static Date getActualMaximumDate(Date date) {
        final Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        instance.set(Calendar.DAY_OF_MONTH, instance.getActualMaximum(Calendar.DAY_OF_MONTH));
        return instance.getTime();
    }
}
