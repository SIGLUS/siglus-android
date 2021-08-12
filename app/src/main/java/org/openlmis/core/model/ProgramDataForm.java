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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import org.openlmis.core.utils.ListUtil;

@Deprecated
@Data
@DatabaseTable(tableName = "program_data_forms")
public class ProgramDataForm extends BaseModel {

  @Getter
  @Expose
  @DatabaseField
  public Date periodBegin;
  @Getter
  @Expose
  @DatabaseField
  public Date periodEnd;
  @Getter
  @DatabaseField(defaultValue = "DRAFT")
  private Status status;
  @Getter
  @DatabaseField(foreign = true, foreignAutoRefresh = true)
  private Program program;
  @Getter
  @DatabaseField
  private boolean synced = false;
  @Getter
  @Expose
  @DatabaseField
  private Date submittedTime;
  @Getter
  @Expose
  @DatabaseField(defaultValue = "")
  private String observation;
  @ForeignCollectionField
  private ForeignCollection<ProgramDataFormItem> programDataFormItemList;
  @Expose
  @SerializedName("programDataFormItems")
  private List<ProgramDataFormItem> programDataFormItemListWrapper;
  @ForeignCollectionField()
  private ForeignCollection<ProgramDataFormBasicItem> formBasicItemList;
  @Expose
  @SerializedName("programDataFormBasicItems")
  private List<ProgramDataFormBasicItem> formBasicItemListWrapper;
  @ForeignCollectionField()
  private ForeignCollection<ProgramDataFormSignature> signatures;
  @Expose
  @SerializedName("programDataFormSignatures")
  private List<ProgramDataFormSignature> signaturesWrapper;

  public List<ProgramDataFormSignature> getSignaturesWrapper() {
    signaturesWrapper = ListUtil.wrapOrEmpty(signatures, signaturesWrapper);
    return signaturesWrapper;
  }

  public List<ProgramDataFormItem> getProgramDataFormItemListWrapper() {
    programDataFormItemListWrapper = ListUtil
        .wrapOrEmpty(programDataFormItemList, programDataFormItemListWrapper);
    return programDataFormItemListWrapper;
  }

  public List<ProgramDataFormBasicItem> getFormBasicItemListWrapper() {
    formBasicItemListWrapper = ListUtil.wrapOrEmpty(formBasicItemList, formBasicItemListWrapper);
    return formBasicItemListWrapper;
  }

  public enum Status {
    DRAFT,
    SUBMITTED,
    AUTHORIZED
  }

}
