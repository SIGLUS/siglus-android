package org.openlmis.core.model.builder;

import org.joda.time.DateTime;
import org.openlmis.core.LMISApp;
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.utils.Constants;

import java.util.Date;

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

    public ReportTypeForm getVIAReportTypeForm() {
        DateTime dateTime = new DateTime(LMISApp.getInstance().getCurrentTimeMillis());
        return this
                .setActive(true)
                .setCode(Constants.VIA_PROGRAM_CODE)
                .setName(Constants.VIA_REPORT)
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

}
