package org.openlmis.core.network.model;


import java.util.List;

import lombok.Data;

@Data
public class FacilityInfoResponse {
    private String code;
    private String name;
    private List<SupportedProgram> supportedPrograms;
}
