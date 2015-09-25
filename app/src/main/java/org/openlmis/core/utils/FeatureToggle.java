package org.openlmis.core.utils;

import org.openlmis.core.LMISApp;

public class FeatureToggle {
    public static boolean toggle(int id) {
       return LMISApp.getContext().getResources().getBoolean(id);
    }
}
