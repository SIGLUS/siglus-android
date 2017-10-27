package org.openlmis.core.enums;

import lombok.Getter;

@Getter
public enum PatientDataStatusEnum {

    MISSING(1),DRAFT(2),COMPLETE(3),SYNCED(4);

    int status;

    PatientDataStatusEnum(int status) {
        this.status = status;
    }
}
