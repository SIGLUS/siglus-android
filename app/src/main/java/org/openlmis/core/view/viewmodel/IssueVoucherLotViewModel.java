package org.openlmis.core.view.viewmodel;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.PodProductLotItem;
import org.openlmis.core.model.Product;
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

  @Getter
  private Product product;

  private boolean isNewAdd;

  private boolean valid;

  private boolean shouldShowError;

  private Lot lot;

  public IssueVoucherLotViewModel(String lotNumber, String expiryDate, Product product) {
    this.lotNumber = lotNumber;
    this.expiryDate = expiryDate;
    this.product = product;
    this.isNewAdd = true;
    this.valid = true;
    this.shouldShowError = false;
    this.lot = Lot.builder()
        .lotNumber(lotNumber)
        .expirationDate(DateUtil.parseString(expiryDate,DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR))
        .product(product)
        .build();
  }

  public static IssueVoucherLotViewModel build(LotOnHand lotOnHand) {
    return new IssueVoucherLotViewModelBuilder()
        .lotNumber(lotOnHand.getLot().getLotNumber())
        .expiryDate(DateUtil.formatDate(lotOnHand.getLot().getExpirationDate(),
            DateUtil.DATE_FORMAT_ONLY_MONTH_AND_YEAR))
        .product(lotOnHand.getStockCard().getProduct())
        .lot(lotOnHand.getLot())
        .valid(true)
        .isNewAdd(false)
        .shouldShowError(false)
        .build();
  }

  @Override
  public int getItemType() {
    return done ? TYPE_DONE : TYPE_EDIT;
  }

  public boolean isLotAllBlank() {
    return shippedQuantity == null && acceptedQuantity == null;
  }

  public boolean validateLot() {
    return !isAcceptedQuantityMoreThanShippedQuantity()
        && !isNewAddedLotHasBlank()
        && !existedLotHasBlank()
        && !isShippedQuantityZero();
  }

  public boolean isAcceptedQuantityMoreThanShippedQuantity() {
    return shippedQuantity != null
        && acceptedQuantity != null
        && acceptedQuantity > shippedQuantity;
  }

  public boolean isNewAddedLotHasBlank() {
    return isNewAdd
        && (shippedQuantity == null || acceptedQuantity == null);
  }

  public boolean existedLotHasBlank() {
    return !isNewAdd
        && ((shippedQuantity != null && acceptedQuantity == null)
        || (shippedQuantity == null && acceptedQuantity != null));
  }

  public boolean isShippedQuantityZero() {
    return shippedQuantity != null && shippedQuantity == 0;
  }

  public PodProductLotItem from() {
    return PodProductLotItem.builder()
        .shippedQuantity(shippedQuantity)
        .acceptedQuantity(acceptedQuantity)
        .lot(lot)
        .build();
  }
}
