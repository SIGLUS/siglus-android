package org.openlmis.core.view.viewmodel;

import org.openlmis.core.model.RnrFormItem;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ViaKitsViewModel {

    private final String DEFAULT_VALUE = "0";

    private String kitsReceivedHF = DEFAULT_VALUE;

    private String kitsReceivedCHW = DEFAULT_VALUE;

    private String kitsOpenedHF = DEFAULT_VALUE;

    private String kitsOpenedCHW = DEFAULT_VALUE;

    private List<RnrFormItem> kitItems = new ArrayList<>();

    public static final String US_KIT = "SCOD10";

    public static final String APE_KIT = "SCOD12";

    public void convertRnrKitItemsToViaKit(List<RnrFormItem> rnrKitItems) {
        kitItems = rnrKitItems;

        for (RnrFormItem rnrKitItem : rnrKitItems) {

            if (US_KIT.equals(rnrKitItem.getProduct().getCode())) {
                if (rnrKitItem.getReceived() > Long.MIN_VALUE) {
                    kitsReceivedHF = String.valueOf(rnrKitItem.getReceived());
                }

                if (rnrKitItem.getIssued() > Long.MIN_VALUE) {
                    kitsOpenedHF = String.valueOf(rnrKitItem.getIssued());
                }
            } else if (APE_KIT.equals(rnrKitItem.getProduct().getCode())) {
                if (rnrKitItem.getReceived() > Long.MIN_VALUE) {
                    kitsReceivedCHW = String.valueOf(rnrKitItem.getReceived());
                }

                if (rnrKitItem.getIssued() > Long.MIN_VALUE) {
                    kitsOpenedCHW = String.valueOf(rnrKitItem.getIssued());
                }
            }
        }
    }

    public List<RnrFormItem> convertToRnrItems() {
        for (RnrFormItem rnrFormItem : kitItems) {

            if (US_KIT.equals(rnrFormItem.getProduct().getCode())) {
                if (!kitsOpenedHF.isEmpty()) {
                    rnrFormItem.setIssued(Long.parseLong(kitsOpenedHF));
                } else {
                    rnrFormItem.setIssued(Long.MIN_VALUE);
                }

                if (!kitsReceivedHF.isEmpty()) {
                    rnrFormItem.setReceived(Long.parseLong(kitsReceivedHF));
                } else {
                    rnrFormItem.setReceived(Long.MIN_VALUE);
                }

                setInventoryForItem(rnrFormItem);

            } else if (APE_KIT.equals(rnrFormItem.getProduct().getCode())) {
                if (!kitsOpenedCHW.isEmpty()) {
                    rnrFormItem.setIssued(Long.parseLong(kitsOpenedCHW));
                } else {
                    rnrFormItem.setIssued(Long.MIN_VALUE);
                }

                if (!kitsReceivedCHW.isEmpty()) {
                    rnrFormItem.setReceived(Long.parseLong(kitsReceivedCHW));
                } else {
                    rnrFormItem.setReceived(Long.MIN_VALUE);
                }

                setInventoryForItem(rnrFormItem);
            }
        }

        return kitItems;
    }

    private void setInventoryForItem(RnrFormItem rnrFormItem) {
        if (kitsOpenedHF.isEmpty() || kitsReceivedHF.isEmpty()) {
            rnrFormItem.setInventory(Long.MIN_VALUE);
        } else {
            rnrFormItem.setInventory(rnrFormItem.getReceived() - rnrFormItem.getIssued());
        }
    }
}