package org.openlmis.core.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@DatabaseTable(tableName = "draft_lot_items")
@NoArgsConstructor
public class DraftLotItem extends BaseModel{

    @DatabaseField
    Long quantity;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    Lot lot;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private DraftInventory draftInventory;

    @DatabaseField
    boolean newAdded;

    public DraftLotItem(LotMovementViewModel lotMovementViewModel, Product product, boolean isNewAdded) {
        try {
            quantity = Long.parseLong(lotMovementViewModel.getQuantity());
        } catch (Exception e) {
            quantity = 0L;
        }
        lot = new Lot();
        lot.setExpirationDate(DateUtil.getActualMaximumDate(DateUtil.parseString(lotMovementViewModel.getExpiryDate(), DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR)));
        lot.setLotNumber(lotMovementViewModel.getLotNumber());
        lot.setProduct(product);
        newAdded = isNewAdded;
    }
}
