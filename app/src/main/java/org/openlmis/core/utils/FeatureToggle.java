package org.openlmis.core.utils;

import org.openlmis.core.LMISApp;

public class FeatureToggle {

    private FeatureToggle() { /* cannot be instantiated */ }

    public static boolean isOpen(int id) {
       return LMISApp.getContext().getResources().getBoolean(id);
    }
}
