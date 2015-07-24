package org.openlmis.core.model;

import lombok.Data;

@Data
public class AdjustmentReason {
    String id;
    String reason;
    boolean positive;
}
