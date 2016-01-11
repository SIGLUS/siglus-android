package org.openlmis.core.view.viewmodel;

import android.text.TextUtils;

import org.openlmis.core.model.RnrKitItem;

import java.util.List;

import lombok.Data;

import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@Data
public class ViaKitsViewModel {

    private String kitsReceivedHF;

    private String kitsReceivedCHW;

    private String kitsOpenedHF;

    private String kitsOpenedCHW;

    public List<RnrKitItem> toRnrKitItemList() {

        RnrKitItem kitHFItem= new RnrKitItem();
        kitHFItem.setKitCode(RnrKitItem.US_KIT);

        if (!TextUtils.isEmpty(kitsReceivedHF)) {
            kitHFItem.setKitsReceived(Integer.parseInt(kitsReceivedHF));
        }
        if (!TextUtils.isEmpty(kitsOpenedHF)) {
            kitHFItem.setKitsOpened(Integer.parseInt(kitsOpenedHF));
        }

        RnrKitItem kitCHWItem= new RnrKitItem();
        kitCHWItem.setKitCode(RnrKitItem.APE_KIT);

        if (!TextUtils.isEmpty(kitsReceivedCHW)) {
            kitCHWItem.setKitsReceived(Integer.parseInt(kitsReceivedCHW));
        }
        if (!TextUtils.isEmpty(kitsOpenedCHW)) {
            kitCHWItem.setKitsOpened(Integer.parseInt(kitsOpenedCHW));
        }
        return newArrayList(kitHFItem, kitCHWItem);
    }
}
