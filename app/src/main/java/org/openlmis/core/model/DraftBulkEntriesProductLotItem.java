package org.openlmis.core.model;


import static org.openlmis.core.utils.DateUtil.DB_DATE_FORMAT;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;

@Setter
@Getter
@NoArgsConstructor
@DatabaseTable(tableName = "draft_bulk_entries_product_lot_item")
public class DraftBulkEntriesProductLotItem extends BaseModel{

  @DatabaseField
  Long quantity;

  @DatabaseField(foreign = true, foreignAutoRefresh = true)
  Product product;

  @DatabaseField
  String lotNumber;

  @DatabaseField
  String documentNumber;

  @DatabaseField
  String reason;

  @DatabaseField(canBeNull = false, dataType = DataType.DATE_STRING, format = DB_DATE_FORMAT)
  Date expirationDate;

  @DatabaseField(foreign = true, foreignAutoRefresh = true)
  private DraftBulkEntriesProduct draftBulkEntriesProduct;

  @DatabaseField
  boolean newAdded;

  public DraftBulkEntriesProductLotItem(LotMovementViewModel lotMovementViewModel, Product product,
      boolean isNewAdded) {
    try {
      quantity = Long.parseLong(lotMovementViewModel.getQuantity());
    } catch (Exception e) {
      quantity = null;
    }
    setExpirationDate(DateUtil.getActualMaximumDate(DateUtil
        .parseString(lotMovementViewModel.getExpiryDate(),
            DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR)));
    setLotNumber(lotMovementViewModel.getLotNumber());
    setProduct(product);
    newAdded = isNewAdded;
  }
}
