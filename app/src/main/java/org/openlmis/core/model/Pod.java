package org.openlmis.core.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.openlmis.core.enums.OrderStatus;
import org.openlmis.core.utils.ListUtil;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DatabaseTable(tableName = "pods")
public class Pod extends BaseModel {

  @DatabaseField
  private String shippedDate;

  @DatabaseField
  private String deliveredBy;

  @DatabaseField
  private String receivedBy;

  @DatabaseField
  private String documentNo;

  @DatabaseField
  private String receivedDate;

  @DatabaseField
  private String orderCode;

  @DatabaseField
  private String orderSupplyFacilityName;

  @DatabaseField
  private OrderStatus orderStatus;

  @DatabaseField
  private String orderCreatedDate;

  @DatabaseField
  private String  orderLastModifiedDate;

  @DatabaseField
  private String requisitionNumber;

  @DatabaseField
  private boolean requisitionIsEmergency;

  @DatabaseField
  private String requisitionProgramCode;

  @DatabaseField
  private String requisitionStartDate;

  @DatabaseField
  private String requisitionEndDate;

  @DatabaseField
  private String requisitionActualStartDate;

  @DatabaseField
  private String requisitionActualEndDate;

  @ForeignCollectionField
  private ForeignCollection<PodProduct> podProductForeignCollection;

  private List<PodProduct> podProductsWrapper;

  public List<PodProduct> getPodProductsWrapper() {
    podProductsWrapper = ListUtil.wrapOrEmpty(podProductForeignCollection, podProductsWrapper);
    return podProductsWrapper;
  }

}
