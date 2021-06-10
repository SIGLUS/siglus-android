package org.openlmis.core.model.builder;

import org.openlmis.core.model.Product;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;

public class RnrFormItemBuilder {

  private final RnrFormItem rnrFormItem;

  public RnrFormItemBuilder() {
    rnrFormItem = new RnrFormItem();
  }

  public RnrFormItemBuilder setProduct(Product product) {
    rnrFormItem.setProduct(product);
    return this;
  }

  public RnrFormItemBuilder setRnrForm(RnRForm rnrForm) {
    rnrFormItem.setForm(rnrForm);
    return this;
  }

  public RnrFormItemBuilder setInitialAmount(long initialAmount) {
    rnrFormItem.setInitialAmount(initialAmount);
    return this;
  }

  public RnrFormItemBuilder setReceived(long received) {
    rnrFormItem.setReceived(received);
    return this;
  }

  public RnrFormItemBuilder setAdjustment(Long adjustment) {
    rnrFormItem.setAdjustment(adjustment);
    return this;
  }

  public RnrFormItemBuilder setInventory(Long inventory) {
    rnrFormItem.setInventory(inventory);
    return this;
  }

  public RnrFormItemBuilder setRequestAmount(long requestAmount) {
    rnrFormItem.setRequestAmount(requestAmount);
    return this;
  }

  public RnrFormItemBuilder setApprovedAmount(long approvedAmount) {
    rnrFormItem.setApprovedAmount(approvedAmount);
    return this;
  }

  public RnrFormItemBuilder setIssued(Long issued) {
    rnrFormItem.setIssued(issued);
    return this;
  }

  public RnrFormItemBuilder setCalculatedOrderQuantity(long calculatedOrderQuantity) {
    rnrFormItem.setCalculatedOrderQuantity(calculatedOrderQuantity);
    return this;
  }

  public RnrFormItemBuilder setValidate(String validate) {
    rnrFormItem.setValidate(validate);
    return this;
  }

  public RnrFormItemBuilder setManualAdd(boolean manualAdd) {
    rnrFormItem.setManualAdd(manualAdd);
    return this;
  }

  public RnrFormItem build() {
    return rnrFormItem;
  }

}