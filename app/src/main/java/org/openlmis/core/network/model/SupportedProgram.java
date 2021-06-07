package org.openlmis.core.network.model;


import lombok.Data;

@Data
public class SupportedProgram {
    private String code;
    private String name;
    private boolean supportActive;
    private String supportStartDate;
}
