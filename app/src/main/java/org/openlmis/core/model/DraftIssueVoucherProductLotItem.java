package org.openlmis.core.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.core.utils.DateUtil;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@DatabaseTable(tableName = "draft_issue_voucher_product_lot_items")
public class DraftIssueVoucherProductLotItem  extends BaseModel {

  @DatabaseField(foreign = true, foreignAutoRefresh = true)
  DraftIssueVoucherProductItem draftIssueVoucherProductItem;

  @DatabaseField
  Long shippedQuantity;

  @DatabaseField
  Long acceptedQuantity;

  @DatabaseField
  String lotNumber;

  @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = DateUtil.DB_DATE_FORMAT)
  Date expirationDate;

  @DatabaseField
  boolean newAdded;

  @DatabaseField
  boolean done;



}
