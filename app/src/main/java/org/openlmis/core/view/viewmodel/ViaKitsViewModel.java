package org.openlmis.core.view.viewmodel;

import org.openlmis.core.model.RnrFormItem;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ViaKitsViewModel {

    private String kitsReceivedHF;

    private String kitsReceivedCHW;

    private String kitsOpenedHF;

    private String kitsOpenedCHW;

    private List<RnrFormItem> kitItems = new ArrayList<>();

    public static final String US_KIT = "SCOD10";

    public static final String APE_KIT = "SCOD12";

    public void convertRnrKitItemsToViaKit(List<RnrFormItem> rnrKitItems) {
        kitItems = rnrKitItems;

        for (RnrFormItem rnrKitItem : rnrKitItems) {
            if (US_KIT.equals(rnrKitItem.getProduct().getCode())) {
                kitsOpenedHF = String.valueOf(rnrKitItem.getIssued());
                kitsReceivedHF = String.valueOf(rnrKitItem.getReceived());
            } else if (APE_KIT.equals(rnrKitItem.getProduct().getCode())) {
                kitsOpenedCHW = String.valueOf(rnrKitItem.getIssued());
                kitsReceivedCHW = String.valueOf(rnrKitItem.getReceived());
            }
        }
    }

    public List<RnrFormItem> convertToRnrItems() {
        for (RnrFormItem rnrFormItem : kitItems) {
            if (US_KIT.equals(rnrFormItem.getProduct().getCode())) {
                rnrFormItem.setIssued(Long.parseLong(kitsOpenedHF));
                rnrFormItem.setReceived(Long.parseLong(kitsReceivedHF));
                rnrFormItem.setInventory(rnrFormItem.getReceived() - rnrFormItem.getIssued());
            } else if (APE_KIT.equals(rnrFormItem.getProduct().getCode())) {
                rnrFormItem.setIssued(Long.parseLong(kitsOpenedCHW));
                rnrFormItem.setReceived(Long.parseLong(kitsReceivedCHW));
                rnrFormItem.setInventory(rnrFormItem.getReceived() - rnrFormItem.getIssued());
            }
        }

        return kitItems;
    }
}