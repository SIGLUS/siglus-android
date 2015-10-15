package org.openlmis.core.model;

public class ProgramBuilder {
    private Program program;

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

    public Program build() {
        return program;
    }

}
