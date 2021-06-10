package org.openlmis.core.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Signature {

  public enum TYPE {
    SUBMITTER,
    APPROVER
  }

  public Signature(String signature, TYPE type) {
    this.signature = signature;
    this.type = type;
  }

  @DatabaseField(uniqueIndex = true, generatedId = true)
  protected long id;

  @Expose
  @SerializedName("text")
  @DatabaseField
  protected String signature;

  @Expose
  @DatabaseField
  protected TYPE type;
}
