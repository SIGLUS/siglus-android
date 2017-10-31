package org.openlmis.core.enums;

import lombok.Getter;

@Getter
public enum PatientDataStatusEnum {

    MISSING(0),DRAFT(1),COMPLETE(2),SYNCED(3);

    int status;

    PatientDataStatusEnum(int status) {
        this.status = status;
    }
}
