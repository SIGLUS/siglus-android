package org.openlmis.core.model.builder;

import org.openlmis.core.model.Program;

public class ProgramBuilder {

  private final Program program;

  public ProgramBuilder() {
    program = new Program();
  }

  public ProgramBuilder setProgramCode(String programCode) {
    program.setProgramCode(programCode);
    return this;
  }

  public ProgramBuilder setProgramName(String programName) {
    program.setProgramName(programName);
    return this;
  }

  public ProgramBuilder setParentCode(String parentCode) {
    program.setParentCode(parentCode);
    return this;
  }

  public ProgramBuilder setSupportEmergency(boolean isSupportProgram) {
    program.setSupportEmergency(isSupportProgram);
    return this;
  }

  public ProgramBuilder setProgramId(long programId) {
    program.setId(programId);
    return this;
  }

  public Program build() {
    return program;
  }

}
