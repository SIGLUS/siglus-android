package org.openlmis.core.view.viewmodel;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.utils.DateUtil;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IssueVoucherLotViewModel implements MultiItemEntity {

  public static final int TYPE_EDIT = 1;

  public static final int TYPE_DONE = 2;

  private Long shippedQuantity;

  private Long acceptedQuantity;

  private boolean done;

  private String lotNumber;

  private String expiryDate;

  public IssueVoucherLotViewModel(String lotNumber, String expiryDate) {
    this.lotNumber = lotNumber;
    this.expiryDate = expiryDate;
  }

  public static IssueVoucherLotViewModel build(LotOnHand lotOnHand) {
    return new IssueVoucherLotViewModelBuilder()
        .lotNumber(lotOnHand.getLot().getLotNumber())
        .expiryDate(DateUtil.formatDate(lotOnHand.getLot().getExpirationDate(),
            DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR))
        .build();
  }

  @Override
  public int getItemType() {
    return done ? TYPE_DONE : TYPE_EDIT;
  }


}
