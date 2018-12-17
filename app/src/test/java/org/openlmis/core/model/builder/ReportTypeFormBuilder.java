package org.openlmis.core.model.builder;

import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.model.RnRForm;

import java.util.Date;

public class ReportTypeFormBuilder {
    private ReportTypeForm reportTypeForm;

    public ReportTypeFormBuilder() {
        this.reportTypeForm = new ReportTypeForm();
    }

    public ReportTypeFormBuilder setActive(boolean active) {
        reportTypeForm.setActive(active);
        return this;
    }

    public ReportTypeFormBuilder setStartTime(Date startTime) {
        reportTypeForm.setStartTime(startTime);
        return this;
    }

    public ReportTypeFormBuilder setName(String name) {
        reportTypeForm.setName(name);
        return this;
    }

    public ReportTypeFormBuilder setCode(String code) {
        reportTypeForm.setCode(code);
        return this;
    }

    public ReportTypeForm build() {
        return reportTypeForm;
    }
}
