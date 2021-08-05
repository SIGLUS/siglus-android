package org.openlmis.core.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.List;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@Setter
@Getter
@Builder
@DatabaseTable(tableName = "pod_products")
public class PodProduct extends BaseModel{

  @DatabaseField
  private String code;

  @DatabaseField
  private long orderedQuantity;

  @DatabaseField
  private long partialFulfilledQuantity;

  @DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = false)
  private Pod pod;

  @ForeignCollectionField
  private ForeignCollection<PodLotItem> podLotItemForeignCollection;

  private List<PodLotItem> podLotItemsWrapper;
}
