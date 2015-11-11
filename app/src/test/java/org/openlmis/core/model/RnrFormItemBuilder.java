package org.openlmis.core.model;

public class RnrFormItemBuilder {
    private RnrFormItem rnrFormItem;

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

    public RnrFormItemBuilder setAdjustment(long adjustment) {
        rnrFormItem.setAdjustment(adjustment);
        return this;
    }

    public RnrFormItemBuilder setInventory(long inventory) {
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

    public RnrFormItemBuilder setCalculatedOrderQuantity(long calculatedOrderQuantity) {
        rnrFormItem.setCalculatedOrderQuantity(calculatedOrderQuantity);
        return this;
    }

    public RnrFormItemBuilder setValidate(String validate) {
        rnrFormItem.setValidate(validate);
        return this;
    }

    public RnrFormItem build() {
        return rnrFormItem;
    }

}