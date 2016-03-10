package org.openlmis.core.utils;

import org.openlmis.core.LMISApp;
import org.openlmis.core.googleAnalytics.TrackerActions;
import org.openlmis.core.googleAnalytics.TrackerCategories;
import org.openlmis.core.model.repository.MMIARepository;

public final class TrackRnREventUtil {
    private TrackRnREventUtil() {
    }

    public static void trackRnRListEvent(TrackerActions action, String programCode) {
        if (MMIARepository.MMIA_PROGRAM_CODE.equals(programCode)) {
            LMISApp.getInstance().trackEvent(TrackerCategories.MMIA, action);
        } else {
            LMISApp.getInstance().trackEvent(TrackerCategories.VIA, action);
        }
    }
}
