package org.openlmis.core.model;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@DatabaseTable(tableName = "sync_errors")
public class SyncError extends BaseModel {

  @DatabaseField
  private SyncType syncType;

  @Getter
  @DatabaseField
  private String errorMessage;

  @DatabaseField
  private long syncObjectId;

  public SyncError(String message, SyncType syncType, long syncObjectId) {
    this.errorMessage = message;
    this.syncType = syncType;
    this.syncObjectId = syncObjectId;
  }
}
