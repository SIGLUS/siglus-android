package org.openlmis.core.model.builder;

import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRForm.Status;

public class RnRFormBuilder {

  private final RnRForm rnRForm;

  public RnRFormBuilder() {
    rnRForm = new RnRForm();
  }

  public RnRFormBuilder setComments(String comments) {
    rnRForm.setComments(comments);
    return this;
  }

  public RnRFormBuilder setStatus(Status status) {
    rnRForm.setStatus(status);
    return this;
  }

  public RnRFormBuilder setProgram(Program program) {
    rnRForm.setProgram(program);
    return this;
  }

  public RnRFormBuilder setSynced(boolean synced) {
    rnRForm.setSynced(synced);
    return this;
  }

  public RnRForm build() {
    return rnRForm;
  }
}
