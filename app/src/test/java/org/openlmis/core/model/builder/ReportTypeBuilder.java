package org.openlmis.core.model.builder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.joda.time.DateTime;
import org.openlmis.core.LMISApp;
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.JsonFileReader;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

public class ReportTypeBuilder {
    private ReportTypeForm reportTypeForm;

    public ReportTypeBuilder() {
        reportTypeForm = new ReportTypeForm();
    }

    public ReportTypeForm getMMIAReportTypeForm() {
        DateTime dateTime = new DateTime(LMISApp.getInstance().getCurrentTimeMillis());
        return this
                .setActive(true)
                .setCode(Constants.MMIA_PROGRAM_CODE)
                .setName(Constants.MMIA_REPORT)
                .setStartTime(dateTime.toDate())
                .build();
    }

    public ReportTypeBuilder setActive(Boolean active) {
        reportTypeForm.setActive(active);
        return this;
    }

    public ReportTypeBuilder setCode(String code) {
        reportTypeForm.setCode(code);
        return this;
    }

    public ReportTypeBuilder setName(String name) {
        reportTypeForm.setName(name);
        return this;
    }

    public ReportTypeBuilder setStartTime(Date date) {
        reportTypeForm.setStartTime(date);
        return this;
    }

    public ReportTypeForm build() {
        return reportTypeForm;
    }


    public static List<ReportTypeForm> getReportTypeForms(Class<?> clazz, String fileName) {
        String json = JsonFileReader.readJson(clazz, "data", fileName);
        Gson gson = new GsonBuilder().setDateFormat(DateUtil.DATE_TIME_FORMAT).create();
        Type type = new TypeToken<List<ReportTypeForm>>() {
        }.getType();
        return gson.fromJson(json, type);

    }

}
