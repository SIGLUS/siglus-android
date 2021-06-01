package org.openlmis.core.persistence.migrations;

import org.openlmis.core.persistence.Migration;

public class AddLastReportEndTimeToReportType extends Migration {
    @Override
    public void up() {
        execSQL("ALTER TABLE 'reports_type' ADD COLUMN lastReportEndTime VARCHAR");
    }
}
