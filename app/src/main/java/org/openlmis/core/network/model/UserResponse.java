package org.openlmis.core.network.model;

import org.openlmis.core.model.Program;
import org.openlmis.core.model.User;

import java.util.List;

import lombok.Data;

@Data
public class UserResponse {
    private User userInformation;
    private List<Program> facilitySupportedPrograms;
}
