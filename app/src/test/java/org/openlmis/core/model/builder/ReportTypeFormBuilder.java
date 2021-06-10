package org.openlmis.core.model.builder;

import java.util.Date;
import org.openlmis.core.model.ReportTypeForm;

public class ReportTypeFormBuilder {

  private final ReportTypeForm reportTypeForm;

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

  public ReportTypeFormBuilder setLastReportEndTime(String time) {
    reportTypeForm.setLastReportEndTime(time);
    return this;
  }

  public ReportTypeForm build() {
    return reportTypeForm;
  }
}
