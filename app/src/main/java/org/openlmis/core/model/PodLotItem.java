package org.openlmis.core.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DatabaseTable(tableName = "pod_lot_items")
public class PodLotItem extends BaseModel{

  @DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = false)
  private Lot lot;

  @DatabaseField
  private long shippedQuantity;

  @DatabaseField
  private long acceptedQuantity;

  @DatabaseField
  private String rejectedReason;

  @DatabaseField
  private String notes;

  @DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = false)
  private PodProduct podProduct;

}
