package org.openlmis.core.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@DatabaseTable(tableName = "program_data_form_signatures")
public class ProgramDataFormSignature extends Signature {

  @DatabaseField(foreign = true, foreignAutoRefresh = true)
  ProgramDataForm form;

  public ProgramDataFormSignature(ProgramDataForm form, String signature, TYPE type) {
    super(signature, type);
    this.form = form;
  }
}
