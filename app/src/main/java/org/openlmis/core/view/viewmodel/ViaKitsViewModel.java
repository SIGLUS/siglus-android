package org.openlmis.core.view.viewmodel;

import org.openlmis.core.model.RnrFormItem;

import java.util.List;

import lombok.Data;

@Data
public class ViaKitsViewModel {

    private String kitsReceivedHF;

    private String kitsReceivedCHW;

    private String kitsOpenedHF;

    private String kitsOpenedCHW;

    public static final String US_KIT = "SCOD10";

    public static final String APE_KIT = "SCOD12";

    public void convertRnrKitItemsToViaKit(List<RnrFormItem> rnrKitItems) {
        for (RnrFormItem rnrKitItem: rnrKitItems) {
            if (US_KIT.equals(rnrKitItem.getProduct().getCode())) {
                kitsOpenedHF = "" + rnrKitItem.getIssued();
                kitsReceivedHF = "" + rnrKitItem.getReceived();
            } else if (APE_KIT.equals(rnrKitItem.getProduct().getCode())) {
                kitsOpenedCHW = "" + rnrKitItem.getIssued();
                kitsReceivedCHW = "" + rnrKitItem.getReceived();
            }
        }
    }

//    public List<RnrFormItem> toRnrFormItemList() {
//
//        RnrFormItem kitHFItem= new RnrFormItem();
//
//        kitHFItem.setKitCode(RnrKitItem.US_KIT);
//
//        if (!TextUtils.isEmpty(kitsReceivedHF)) {
//            kitHFItem.setKitsReceived(Integer.parseInt(kitsReceivedHF));
//        }
//        if (!TextUtils.isEmpty(kitsOpenedHF)) {
//            kitHFItem.setKitsOpened(Integer.parseInt(kitsOpenedHF));
//        }
//
//        RnrKitItem kitCHWItem= new RnrKitItem();
//        kitCHWItem.setKitCode(RnrKitItem.APE_KIT);
//
//        if (!TextUtils.isEmpty(kitsReceivedCHW)) {
//            kitCHWItem.setKitsReceived(Integer.parseInt(kitsReceivedCHW));
//        }
//        if (!TextUtils.isEmpty(kitsOpenedCHW)) {
//            kitCHWItem.setKitsOpened(Integer.parseInt(kitsOpenedCHW));
//        }
//        return newArrayList(kitHFItem, kitCHWItem);
//    }
}
