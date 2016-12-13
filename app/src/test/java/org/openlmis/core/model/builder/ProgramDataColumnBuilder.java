package org.openlmis.core.model.builder;

import org.openlmis.core.model.ProgramDataColumn;

public class ProgramDataColumnBuilder {
    ProgramDataColumn column;

    public ProgramDataColumnBuilder() {
        this.column = new ProgramDataColumn();
    }

    public ProgramDataColumnBuilder setCode(String code) {
        column.setCode(code);
        return this;
    }

    public ProgramDataColumn build() {
        return column;
    }
}
