package org.openlmis.core.utils;

import androidx.annotation.NonNull;

import com.viethoa.models.AlphabetItem;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

import java.util.ArrayList;
import java.util.List;

public final class InventoryUtils {

    private InventoryUtils() {
    }

    @NonNull
    public static List<AlphabetItem> getAlphabetItemsByInventories(List<InventoryViewModel> viewModels) {
        List<AlphabetItem> mAlphabetItems = new ArrayList<>();

        if (viewModels != null) {
            List<String> strAlphabets = new ArrayList<>();

            for (int i = 0; i < viewModels.size(); i++) {
                String name = viewModels.get(i).getProductName();

                if (StringUtils.isBlank(name)) {
                    continue;
                }

                String word = name.substring(0, 1).toUpperCase();
                if (!strAlphabets.contains(word)) {
                    strAlphabets.add(word);
                    mAlphabetItems.add(new AlphabetItem(i, word, false));
                }
            }
        }
        return mAlphabetItems;
    }
}