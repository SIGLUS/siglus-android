package org.openlmis.core.event;

import lombok.Getter;

@Getter
public class InitialDirtyDataCheckEvent {

  private final boolean isChecking;
  private final boolean existingDirtyData;

  public InitialDirtyDataCheckEvent(boolean isChecking, boolean existingDirtyData) {
    this.isChecking = isChecking;
    this.existingDirtyData = existingDirtyData;
  }
}
