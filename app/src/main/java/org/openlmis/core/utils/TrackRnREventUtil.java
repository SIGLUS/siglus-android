package org.openlmis.core.utils;

import org.openlmis.core.LMISApp;
import org.openlmis.core.googleAnalytics.TrackerCategories;
import org.openlmis.core.model.repository.MMIARepository;

public final class TrackRnREventUtil {
    private TrackRnREventUtil() {
    }

    public static void trackRnRListEvent(String action, String programCode) {
        if (MMIARepository.MMIA_PROGRAM_CODE.equals(programCode)) {
            LMISApp.getInstance().trackerEvent(TrackerCategories.MMIA.getString(), action);
        } else {
            LMISApp.getInstance().trackerEvent(TrackerCategories.VIA.getString(), action);
        }
    }
}
