package org.openlmis.core.view.viewmodel;

import android.os.Build;
import androidx.annotation.StringRes;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.roboguice.shaded.goole.common.collect.FluentIterable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IssueVoucherProductViewModel implements MultiItemEntity {

  public static final int TYPE_EDIT = 1;

  public static final int TYPE_DONE = 2;

  private boolean done;

  private Product product;

  private StockCard stockCard;

  @StringRes
  private int ErrorRes;

  private boolean shouldShowError;

  private final List<IssueVoucherLotViewModel> lotViewModels = new ArrayList<>();

  public IssueVoucherProductViewModel(Product product) {
    this.product = product;
  }

  public IssueVoucherProductViewModel(StockCard stockCard) {
    this.stockCard = stockCard;
    this.product = stockCard.getProduct();
    lotViewModels.addAll(FluentIterable.from(stockCard.getLotOnHandListWrapper())
        .filter(lotOnHand -> Objects.requireNonNull(lotOnHand).getQuantityOnHand() != null
            && lotOnHand.getQuantityOnHand() > 0)
        .transform(IssueVoucherLotViewModel::build).toList());
  }

  @Override
  public int getItemType() {
    return done ? TYPE_DONE : TYPE_EDIT;
  }

  public boolean validate() {
    setDone(true);
    return true;
  }

  public void setDone(boolean isDone) {
    this.done = isDone;
    if (lotViewModels.isEmpty()) {
      return;
    }
    for (IssueVoucherLotViewModel lotViewModel : lotViewModels) {
      lotViewModel.setDone(isDone);
    }
  }
}
