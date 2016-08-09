package org.openlmis.core.view.viewmodel;

import java.io.Serializable;

import lombok.Data;

@Data
public class LotMovementViewModel implements Serializable {

    private String lotNumber;
    private String expiryDate;
    private String quantity;
    private String lotSoh;
    private String stockMovementId;
}
