/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

package org.openlmis.core.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.Locale;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.openlmis.core.exceptions.SyncServerException;
import org.openlmis.core.utils.Constants;

@NoArgsConstructor
@DatabaseTable(tableName = "sync_errors")
public class SyncError extends BaseModel {

  @DatabaseField
  private SyncType syncType;

  @DatabaseField
  private String errorMessage;

  @DatabaseField
  private String errorMessageInPortuguese;

  @DatabaseField
  private long syncObjectId;

  public SyncError(String message, SyncType syncType, long syncObjectId) {
    this.errorMessage = message;
    this.errorMessageInPortuguese = message;
    this.syncType = syncType;
    this.syncObjectId = syncObjectId;
  }

  public SyncError(@NotNull Exception e, SyncType syncType, long syncObjectId) {
    this.syncType = syncType;
    this.syncObjectId = syncObjectId;
    if (e instanceof SyncServerException) {
      SyncServerException serverException = (SyncServerException) e;
      this.errorMessage = serverException.getMessageInEnglish();
      this.errorMessageInPortuguese = serverException.getMessageInPortuguese();
    }else {
      this.errorMessage = Constants.SERVER_FAILED_MESSAGE_IN_ENGLISH;
      this.errorMessageInPortuguese = Constants.SERVER_FAILED_MESSAGE_IN_PORTUGUESE;
    }
  }

  public String getErrorMessage() {
    if (Locale.getDefault().getLanguage().equals(new Locale("pt").getLanguage())) {
      return errorMessageInPortuguese;
    } else {
      return errorMessage;
    }
  }
}
