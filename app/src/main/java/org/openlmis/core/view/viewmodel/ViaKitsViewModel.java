package org.openlmis.core.view.viewmodel;

import org.openlmis.core.model.RnrFormItem;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ViaKitsViewModel {

    private String kitsReceivedHF = "";

    private String kitsReceivedCHW = "";

    private String kitsOpenedHF = "";

    private String kitsOpenedCHW = "";

    private List<RnrFormItem> kitItems = new ArrayList<>();

    public static final String US_KIT = "SCOD10";

    public static final String APE_KIT = "SCOD12";

    public void convertRnrKitItemsToViaKit(List<RnrFormItem> rnrKitItems) {
        kitItems = rnrKitItems;

        for (RnrFormItem rnrKitItem : rnrKitItems) {

            if (rnrKitItem.getIssued() < 0) continue;

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
                if (!kitsOpenedHF.isEmpty()) {
                    rnrFormItem.setIssued(Long.parseLong(kitsOpenedHF));
                }
                if (!kitsReceivedHF.isEmpty()) {
                    rnrFormItem.setReceived(Long.parseLong(kitsReceivedHF));
                }
                if (!kitsOpenedHF.isEmpty() && !kitsReceivedHF.isEmpty()) {
                    rnrFormItem.setInventory(rnrFormItem.getReceived() - rnrFormItem.getIssued());
                }
            } else if (APE_KIT.equals(rnrFormItem.getProduct().getCode())) {
                if (!kitsOpenedCHW.isEmpty()) {
                    rnrFormItem.setIssued(Long.parseLong(kitsOpenedCHW));
                }
                if (!kitsReceivedCHW.isEmpty()) {
                    rnrFormItem.setReceived(Long.parseLong(kitsReceivedCHW));
                }
                if (!kitsOpenedCHW.isEmpty() && !kitsReceivedCHW.isEmpty()) {
                    rnrFormItem.setInventory(rnrFormItem.getReceived() - rnrFormItem.getIssued());
                }
            }
        }

        return kitItems;
    }
}