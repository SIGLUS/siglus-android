package org.openlmis.core.model;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.openlmis.core.enums.OrderStatus;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@DatabaseTable(tableName = "pods")
public class Pod extends BaseModel{

  @DatabaseField(canBeNull = false, dataType = DataType.STRING)
  private LocalDate shippedDate;

  @DatabaseField
  private String deliveredBy;

  @DatabaseField
  private String receivedBy;

  @SerializedName("documentNo")
  @DatabaseField
  private String documentNumber;

  @DatabaseField(dataType = DataType.STRING)
  private LocalDate receivedDate;

  @DatabaseField
  private String orderCode;

  @DatabaseField
  private String orderSupplyFacilityName;

  @DatabaseField
  private OrderStatus orderStatus;

  @DatabaseField(dataType = DataType.STRING)
  private LocalDateTime orderCreatedDate;

  @DatabaseField
  private LocalDateTime orderLastModifiedDate;

  @DatabaseField
  private String requisitionNumber;

  @DatabaseField
  private boolean requisitionIsEmergency;

  @DatabaseField(dataType = DataType.STRING)
  private LocalDate requisitionStartDate;

  @DatabaseField(dataType = DataType.STRING)
  private LocalDate requisitionEndDate;

  @DatabaseField(dataType = DataType.STRING)
  private LocalDate requisitionActualStartDate;

  @DatabaseField(dataType = DataType.STRING)
  private LocalDate requisitionActualEndDate;

  @ForeignCollectionField
  private ForeignCollection<PodProduct> podProductForeignCollection;

}
