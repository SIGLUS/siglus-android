package org.openlmis.core.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.openlmis.core.utils.ListUtil;
import org.openlmis.core.view.viewmodel.IssueVoucherProductViewModel;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@DatabaseTable(tableName = "draft_issue_voucher_product_items")
public class DraftIssueVoucherProductItem extends BaseModel{

  @DatabaseField(foreign = true, foreignAutoRefresh = true)
  Pod pod;

  @DatabaseField(foreign = true, foreignAutoRefresh = true)
  Product product;

  @DatabaseField(defaultValue = "false")
  boolean done;

  @ForeignCollectionField(eager = true)
  private ForeignCollection<DraftIssueVoucherProductLotItem> foreignDraftLotItems;

  private List<DraftIssueVoucherProductLotItem> draftLotItemListWrapper;

  public List<DraftIssueVoucherProductLotItem> getDraftLotItemListWrapper() {
    draftLotItemListWrapper = ListUtil.wrapOrEmpty(foreignDraftLotItems, draftLotItemListWrapper);
    return draftLotItemListWrapper;
  }

  public IssueVoucherProductViewModel from() {
    return IssueVoucherProductViewModel.builder()
        .product(product)
        .done(done)
        .productItem(this)
        .lotViewModels(FluentIterable.from(getDraftLotItemListWrapper())
            .transform(DraftIssueVoucherProductLotItem::from)
            .toList())
        .build();
  }

}
