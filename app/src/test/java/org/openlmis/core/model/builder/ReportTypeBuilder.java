package org.openlmis.core.model.builder;

import org.openlmis.core.model.ReportTypeForm;

import java.util.Date;

public class ReportTypeBuilder {
    private ReportTypeForm reportTypeForm;

    public ReportTypeBuilder() {
        reportTypeForm = new ReportTypeForm();
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

}
