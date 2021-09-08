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
import org.openlmis.core.view.viewmodel.IssueVoucherLotViewModel;

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

  public IssueVoucherLotViewModel from() {
    return IssueVoucherLotViewModel.builder()
        .product(draftIssueVoucherProductItem.getProduct())
        .done(done)
        .shippedQuantity(shippedQuantity)
        .acceptedQuantity(acceptedQuantity)
        .lotNumber(lotNumber)
        .expiryDate(DateUtil.formatDate(expirationDate, DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR))
        .build();
  }



}
